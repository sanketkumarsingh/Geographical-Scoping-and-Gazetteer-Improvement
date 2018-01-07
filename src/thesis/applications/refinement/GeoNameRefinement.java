package thesis.applications.refinement;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Stream;

import thesis.data.entity.MBR;
import thesis.data.entity.MBRInfo;
import thesis.evaluation.EvaluateHierarchy;

public class GeoNameRefinement {
	public static Map<String, String> centreCoordinateMap = null;
	static Map<String, MBR> mbrInfoMap = null;

	/*
	 * 1. Form phase 1 by running, loadMbr and retstructureHierarchy 
	 * 2. For notInRefined file, by running, findCompletelyIndependentPlaces with phase1/ 
	 * 3. Sorted phase 1 file and adjust by running - generatephase2 
	 * 4. use phase 2 and notInRefine and run assignWrongAndCompletelyIndependentPlace to get final file.
	 * 5. use final file to find the result.
	 */

	public static void main(String[] args) throws IOException {
		// step1
		loadMbr();
		retstructureHierarchy();
////		
//		
//		// step2
	    findCompletelyIndependentPlaces();
//		
//		//step 3
//		// sorted the phase 1 file by first column and then compile it again..
		 generatephase2();
		 
////		 step4
//		// assign notInRefined to the new hierarchy.
//		// // requires Load MBR
		  loadMbr();
		  assignWrongAndCompletelyIndependentPlace();
		 
//		step5 
		loadMbr();
		 getCentreForAllPlaceInGeonames();
		 calculateMoveAndDeleteAcc();

	}
	
	
	public static void step1(){
		loadMbr();
		retstructureHierarchy();
		 findCompletelyIndependentPlaces();
	}
	
	public static void step3(){
		loadMbr();
		assignWrongAndCompletelyIndependentPlace();
		getCentreForAllPlaceInGeonames();
	    try {
			calculateMoveAndDeleteAcc();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private static void generatephase2() {

		Path hierarchyFile = Paths.get("sorted-hierarchy-refine-phase1-new.txt"); // to
		// get
		// the
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("hierarchy-refine-phase2-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int count = 0;
		Map<String, String> childParentMap = new HashMap();
		int first = 1;
		String prevParent = "";
		String children = "";
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (first == 1) {
				prevParent = lineArr[0];
				first = 2;
				children = line;
				children = children.trim();
			} else {

				if (prevParent.equals(lineArr[0])) {
					if (lineArr.length == 3) {
						if (children.split("\t").length == 3) {
							children = children + " " + lineArr[2].trim();
						} else {
							children = children + "\t" + lineArr[2].trim();
						}

					}
				} else {
					writer.write(children + "\n");
					writer.flush();

					prevParent = lineArr[0];
					children = line;
					children = children.trim();
				}

			}

			count++;
			if (count % 100000 == 0) {
				System.out.println("Processed:" + count);
			}
		}
		writer.write(children + "\n");
		writer.flush();
		writer.close();

	}

	private static void assignWrongAndCompletelyIndependentPlace() {
		Path hierarchyFile = Paths.get("notin-refined-hierarchy-new.txt"); // to
		// get
		// the
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> childParentMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			childParentMap.put(lineArr[0], lineArr[1]);
		}

		System.out.println("Total wrong and independent place:" + childParentMap.size());

		hierarchyFile = Paths.get("hierarchy-representation-onlygeonames.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (childParentMap.containsKey(lineArr[0])) {
				String parent = childParentMap.get(lineArr[0]);
				// parent = parent.concat("\t");
				// parent = parent.concat(lineArr[4]);
				// parent = parent.concat("\t");
				// parent = parent.concat(lineArr[5]);
				StringBuffer parentStr = new StringBuffer(parent);
				parentStr.append("\t").append(lineArr[1]).append("\t").append(lineArr[2]).append("\t").
				append(lineArr[3]).append("\t").append(lineArr[4]);
				childParentMap.put(lineArr[0], parentStr.toString());
			}
		}

		System.out.println("Loaded coordinates.." + childParentMap.size());

		List<MBR> mbrList = new ArrayList();
		hierarchyFile = Paths.get("420-pom-mbr-forapp.txt"); // taking
		// place
		// id
		// and
		// google map area from this.

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[4].equals(lineArr[6]) || lineArr[5].equals(lineArr[7])) {
				continue;
			}
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[4]));
			mbr.setSwLong(Double.parseDouble(lineArr[5]));
			mbr.setNeLat(Double.parseDouble(lineArr[6]));
			mbr.setNeLong(Double.parseDouble(lineArr[7]));
			// double area =
			// EvaluateHierarchy.getAreaByCartesian(mbr.getSwLat(),mbr.getSwLong(),
			// mbr.getNeLat(), mbr.getNeLong());
			// mbr.setArea(area);
			mbrList.add(mbr);
		}

		hierarchyFile = Paths.get("78639-center-mbr.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[4].equals(lineArr[6]) || lineArr[5].equals(lineArr[7])) {
				continue;
			}
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[4]));
			;
			mbr.setSwLong(Double.parseDouble(lineArr[5]));
			mbr.setNeLat(Double.parseDouble(lineArr[6]));
			mbr.setNeLong(Double.parseDouble(lineArr[7]));
			// double area =
			// EvaluateHierarchy.getAreaByCartesian(mbr.getSwLat(),mbr.getSwLong(),
			// mbr.getNeLat(), mbr.getNeLong());
			// mbr.setArea(area);
			mbrList.add(mbr);
		}

		hierarchyFile = Paths.get("rem-childrenmbr.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");

			if (lineArr[1].equals(lineArr[3]) || lineArr[2].equals(lineArr[4])) {
				continue;
			}
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[1]));
			mbr.setSwLong(Double.parseDouble(lineArr[2]));
			mbr.setNeLat(Double.parseDouble(lineArr[3]));
			mbr.setNeLong(Double.parseDouble(lineArr[4]));
			// double area =
			// EvaluateHierarchy.getAreaByCartesian(mbr.getSwLat(),mbr.getSwLong(),
			// mbr.getNeLat(), mbr.getNeLong());
			// mbr.setArea(area);
			mbrList.add(mbr);
		}
		// System.out.println("Loaded all locations.." + mbrList.size());

		Collections.sort(mbrList);
		System.out.println("Total places with MBR:" + mbrList.size());

		Iterator it = childParentMap.entrySet().iterator();

		Map<String, List<String>> parentChildResultMap = new HashMap();
		int count = 0;

		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String idDetail = (String) entry.getValue();
			String id = (String) entry.getKey();
			String idDetailArr[] = idDetail.split("\t");
			// if (idDetailArr[0].equals("1337216")) {
			// System.out.println("break");
			// }
			
			MBR mbrPlace = new MBR();
			mbrPlace.setId(id);
			mbrPlace.setSwLat(Double.parseDouble(idDetailArr[1]));
			mbrPlace.setSwLong(Double.parseDouble(idDetailArr[2]));
			mbrPlace.setNeLat(Double.parseDouble(idDetailArr[3]));
			mbrPlace.setNeLong(Double.parseDouble(idDetailArr[4]));
			
			double placeLat = Double.parseDouble(idDetailArr[1]);
			double placeLong = Double.parseDouble(idDetailArr[2]);
			int numParent = 0;
			String parent = null;
			Iterator iter = mbrList.iterator();
			while (iter.hasNext()) {
				MBR mbr = (MBR) iter.next();

				if (placeLong < mbr.getSwLong()) {
					break;
				}
				
				if(isParentMbrContains(mbr, mbrPlace)){
					parent = mbr.getId();
					numParent++;
				}

//				if (placeLat >= mbr.getSwLat() && placeLat <= mbr.getNeLat()) {
//					if (placeLong >= mbr.getSwLong() && placeLong <= mbr.getNeLong()) {
//						parent = mbr.getId();
//						numParent++;
//					}
//				}

			}

			if (numParent == 1) {
//				if (parent == null) {
//					System.out.println("For " + id + " parent is null");
//				}
				if (parentChildResultMap.containsKey(parent)) {
					parentChildResultMap.get(parent).add(id);
				} else {
					List<String> children = new ArrayList();
					children.add(id);
					parentChildResultMap.put(parent, children);
				}
//				List<String> children1 = new ArrayList();
//				StringBuffer strfb = new StringBuffer();
//				strfb.append(id).append("\t").append(idDetailArr[0]);
//				parentChildResultMap.put(strfb.toString(), children1);
				
				if(!parent.equals(idDetailArr[0])){
					System.out.println("Found a new parent:" + id);
				}
				
			} else {
//				if (idDetailArr[0] == null) {
//					System.out.println("For " + id + " idDetailArr[0] is null");
//				}
				if (parentChildResultMap.containsKey(idDetailArr[0])) {
					parentChildResultMap.get(idDetailArr[0]).add(id);
				} else {
					List<String> children = new ArrayList();
					children.add(id);
					parentChildResultMap.put(idDetailArr[0], children);
				}
//				List<String> children1 = new ArrayList();
//				StringBuffer strfb = new StringBuffer();
//				strfb.append(id).append("\t").append(idDetailArr[0]);
//				parentChildResultMap.put(strfb.toString(), children1);
			}

			count++;
			if (count % 10000 == 0) {
				System.out.println("Processed parent allocation:" + count);
			}
		}

		System.out.println("count:" + count);
		System.out.println("parentChildResultMap:" + parentChildResultMap.size());

		// if (parentChildResultMap.containsKey("1337216")) {
		// System.out.println(parentChildResultMap.get("1337216").size());
		// }

		hierarchyFile = Paths.get("hierarchy-refine-phase2-new.txt");

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("hierarchy-refine-final-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		count = 0;
		int totalParentFoundfromphase1 = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (parentChildResultMap.containsKey(lineArr[0])) {
				line = line.trim();
				if (line.split("\t").length == 3) {
					StringBuffer lineStr = new StringBuffer(line);
					List<String> children = parentChildResultMap.get(lineArr[0]);
					if (children == null) {
						System.out.println("children null in map for parent:" + lineArr[0]);
					}
					for (String child : children) {
						lineStr.append(" ").append(child);
					}
					writer.write(lineStr.toString() + "\n");
					writer.flush();
				} else {
					StringBuffer lineStr = new StringBuffer();
					List<String> children = parentChildResultMap.get(lineArr[0]);
					if (children == null) {
						System.out.println("children null in map for parent:" + lineArr[0]);
					}
					int k = 0;
					for (String child : children) {
						if (k == 0) {
							lineStr.append(child);
							k = 1;
						} else {
							lineStr.append(" ").append(child);
						}

					}
					writer.write(line + "\t" + lineStr.toString() + "\n");
					writer.flush();

				}
				totalParentFoundfromphase1++;
				parentChildResultMap.put(lineArr[0], null);
			} else {
				writer.write(line + "\n");
				writer.flush();
			}
			count++;
			if (count % 1000000 == 0) {
				System.out.println("Processed final:" + count);
			}
		}

		System.out.println("totalParentFoundfromphase1.." + totalParentFoundfromphase1);

		int totalNotInPhase1 = 0;
		it = parentChildResultMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String parent = (String) entry.getKey();
			List<String> children = (List<String>) entry.getValue();
			if (children == null) {
				continue;
			}
			totalNotInPhase1++;
			StringBuffer lineStr = new StringBuffer();

			int k = 0;
			for (String child : children) {
				if (k == 0) {
					lineStr.append(child);
					k = 1;
				} else {
					lineStr.append(" ").append(child);
				}
			}
			if(lineStr.toString().isEmpty()){
				writer.write(parent + "\t" + parent + "\n");
			}else{
				writer.write(parent + "\t" + parent + "\t" + lineStr.toString() + "\n");
			}
			
			writer.flush();
		}
		System.out.println("totalNotInPhase1:" + totalNotInPhase1);
		
		
		
		
		writer.close();

	}

	private static void findCompletelyIndependentPlaces() {
		Path hierarchyFile = Paths.get("hierarchy-refine-phase1-new.txt"); // to
		// get
		// the
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Set<String> idInRefinedHierarchy = new HashSet();

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			idInRefinedHierarchy.add(lineArr[0]);
			if (lineArr.length == 3) {
				String children[] = lineArr[2].split(" ");
				for (int i = 0; i < children.length; i++) {
					idInRefinedHierarchy.add(children[i]);
				}
			}

		}

		System.out.println("Loaded all ids in refined hierarchy:" + idInRefinedHierarchy.size());

		hierarchyFile = Paths.get("hierarchy-representation-onlygeonames.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int countExpected = 0;
		int countReal = 0;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("notin-refined-hierarchy-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Set<String> notInHierarchySet = new HashSet();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr.length == 6 && lineArr[5].equals("null")) {
				countExpected++;
			}
			if (!idInRefinedHierarchy.contains(lineArr[0])) {
				writer.write(lineArr[0] + "\t" + lineArr[5] + "\n");
				notInHierarchySet.add(lineArr[0]);
				writer.flush();
				countReal++;
			}
		}
		writer.close();
		System.out.println("Not in hierarchy expected:" + countExpected);
		System.out.println("Not in hierarchy real:" + countReal);

		hierarchyFile = Paths.get("wrong-child-phase1-new.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (!notInHierarchySet.contains(lineArr[0])) {
				System.out.println(line);
			}
		}
	}

	private static void retstructureHierarchy() {
		// Path hierarchyFile =
		// Paths.get("hierarchy-representation-onlygeonames.txt"); // to
		Path hierarchyFile = Paths.get("hierarchy-representation-onlygeonames.txt"); // to
		// get
		// the
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int totalWrongLocation = 0;
		// Set<StringBuffer> globalWrongList = new HashSet();

		PrintWriter mbrwriter = null;
		try {
			mbrwriter = new PrintWriter(new FileWriter("hierarchy-refine-phase1-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter globalWrongwriter = null;
		try {
			globalWrongwriter = new PrintWriter(new FileWriter("wrong-child-phase1-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter independentPresentwriter = null;
		try {
			independentPresentwriter = new PrintWriter(new FileWriter("independently-present-phase1-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		} // we have to first find all the places in this file which are not
			// child or parent in refined hierarchy
			// and then move them as we move the found wrong child..

		int totalProcessed = 0;

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			
			totalProcessed++;
			if (lineArr.length > 6) {
				if(!mbrInfoMap.containsKey(lineArr[0])){
					System.out.println(lineArr[0]);
				}
				MBR parentMbr = mbrInfoMap.get(lineArr[0]);
				String children[] = lineArr[6].split(" ");
				// int notInParent = 0;
				List<String> validChild = new ArrayList();
				for (int i = 0; i < children.length; i++) {
					MBR childMbr = mbrInfoMap.get(children[i]);
					if(childMbr == null){
						System.out.println(lineArr[0] + ":"  + children[i]);
					}
					if (!isParentMbrContains(parentMbr, childMbr)) {
						totalWrongLocation++;
						globalWrongwriter.write(children[i] + "\t" + lineArr[0] + "\n");
						globalWrongwriter.flush();

					} else {
						validChild.add(children[i]);
					}
				}
				// totalWrongLocation = totalWrongLocation + notInParent;
				int totalValidChild = validChild.size();
				
				if(totalValidChild ==0){
					mbrwriter.write(lineArr[0] + "\t" + lineArr[5] + "\n");
					mbrwriter.flush();
					continue;
				}
				
				if (totalValidChild > 10000) {
					System.out.println("For id:" + lineArr[0] + " total valid child is:" + totalValidChild);
				}
				// int parentChildArr[][] = new
				// int[totalValidChild][totalValidChild];

				Map<Integer, List<String>> levelLocationIdsMap = new TreeMap();
				List<String> mainParentLayer = validChild;
				
				for (int i = 0; i < totalValidChild; i++) {

					int currSum = 0;
					for (int j = 0; j < totalValidChild; j++) {
						if (i == j) {
							// parentChildArr[i][j] = 0;
							continue;
						}

						if (iIsContainedByj(validChild.get(i), validChild.get(j))) {
							currSum = currSum + 1;
						}
						// } else {
						// parentChildArr[i][j] = 0;
						// }
						// currSum = currSum + parentChildArr[i][j];
					}
					// if("9174359".equals(validChild.get(i))){
					// System.out.println("break..");
					// }
					// if("3182340".equals(validChild.get(i))){
					// System.out.println("break..");
					// }
					// if("6955699".equals(validChild.get(i))){
					// System.out.println("break..");
					// }
					// if("3183072".equals(validChild.get(i))){
					// System.out.println("break..");
					// }
					if (levelLocationIdsMap.containsKey(currSum)) {
						levelLocationIdsMap.get(currSum).add(validChild.get(i));
					} else {
						List<String> levelChildren = new ArrayList();
						levelChildren.add(validChild.get(i));
						levelLocationIdsMap.put(currSum, levelChildren);
					}

				}

				if (levelLocationIdsMap.size() != 0) {

					Iterator it = levelLocationIdsMap.entrySet().iterator();
					boolean first = true;
					List<String> parentLayer = null;
					
					while (it.hasNext()) {
						Entry entry = (Entry) it.next();
						int level = (Integer) entry.getKey();
						// System.out.println("Current level:" + level);
						List<String> currLayerList = (List<String>) entry.getValue();
						if (first) {
							parentLayer = currLayerList;
							mainParentLayer = currLayerList;
							first = false;

						} else {
							Map<String, StringBuffer> parentChildForPrevCurrLayer = new HashMap();

							int k = 0;
							for (String currChild : currLayerList) {
								if (currChild.equals("3716981")) {
									System.out.println("BREAK");
								}
								boolean firstParent = true;
								boolean moreThanOneParent = false;
								String parentForCurrChild = null;
								for (String parent : parentLayer) {
									if (k == 0) {
										parentChildForPrevCurrLayer.put(parent, null);

									}
									if (iIsContainedByj(currChild, parent)) {
										if (firstParent) {
											parentForCurrChild = parent;
											firstParent = false;
										} else {
											moreThanOneParent = true;
											break;
										}
									}
								}
								k = 1;

								if (!moreThanOneParent && parentForCurrChild != null) {
									if (parentChildForPrevCurrLayer.get(parentForCurrChild) != null) {
										StringBuffer currChildList = parentChildForPrevCurrLayer
												.get(parentForCurrChild);
										currChildList.append(" ").append(currChild);
									} else {
										StringBuffer child = new StringBuffer(currChild);
										parentChildForPrevCurrLayer.put(parentForCurrChild, child);
									}

								}

								if (!moreThanOneParent && parentForCurrChild == null) {
									mainParentLayer.add(currChild);
								}

								if (moreThanOneParent) {
									mainParentLayer.add(currChild);
								}

							}

							Iterator iter = parentChildForPrevCurrLayer.entrySet().iterator();
							while (iter.hasNext()) {
								Entry entry1 = (Entry) iter.next();
								String parent = (String) entry1.getKey();
								StringBuffer childs = (StringBuffer) entry1.getValue();
								if (childs == null) {
									mbrwriter.write(parent + "\t" + lineArr[0] + "\n");
								} else {
									mbrwriter.write(parent + "\t" + lineArr[0] + "\t" + childs.toString() + "\n");
								}

								mbrwriter.flush();
							}

							parentLayer = currLayerList;

						}

					}

					// writing the leaf node
					if (parentLayer != null) {
						Iterator iter = parentLayer.iterator();
						while (iter.hasNext()) {
							String parent = (String) iter.next();
							mbrwriter.write(parent + "\t" + lineArr[0] + "\n");
							mbrwriter.flush();
						}
					}

					

				} 
				// writing main parent layer..
				if (mainParentLayer != null) {
					 boolean first = true;
					StringBuffer childs = new StringBuffer();
					for (String parent : mainParentLayer) {
						if (first) {
							childs.append(parent);
							first = false;
						} else {
							childs.append(" ").append(parent);
						}
					}
					if(childs.toString().isEmpty()){
						mbrwriter.write(lineArr[0] + "\t" + lineArr[5] + "\n");
						mbrwriter.flush();
					}else{
					mbrwriter.write(lineArr[0] + "\t" + lineArr[5] + "\t" + childs.toString() + "\n");
					mbrwriter.flush();
					}
				}
				
				
			} else {

				// write the line as it is..
				independentPresentwriter.write(lineArr[0] + "\n");
				independentPresentwriter.flush();
			}

			if (totalProcessed % 100000 == 0) {
				System.out.println("Processed: " + totalProcessed);
			}
		}

		System.out.println("Total Wrong locations:" + totalWrongLocation);
		// System.out.println("Total moves:" + );
		globalWrongwriter.close();
		mbrwriter.close();
		independentPresentwriter.close();
	}

	private static void loadMbr() {
		mbrInfoMap = new HashMap();
		Path hierarchyFile = Paths.get("420-pom-mbr-forapp.txt");

		// double minMbrArea = 1200.0;
		int totalLessMbr = 0;

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[4].equals(lineArr[6]) && lineArr[5].equals(lineArr[7])) {
				continue;
			}
			// MBRInfo mbrInfo = new MBRInfo();
			double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));
			if (area < 6) {
				totalLessMbr++;
			}
			// mbrInfo.setArea(area);
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[4]));
			mbr.setSwLong(Double.parseDouble(lineArr[5]));
			mbr.setNeLat(Double.parseDouble(lineArr[6]));
			mbr.setNeLong(Double.parseDouble(lineArr[7]));
			// mbrInfo.setMbr(mbr);

			mbrInfoMap.put(lineArr[0], mbr);
		}
		System.out.println("Loaded 420 locations.." + mbrInfoMap.size());
		hierarchyFile = Paths.get("78639-center-mbr.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[4].equals(lineArr[6]) && lineArr[5].equals(lineArr[7])) {
				continue;
			}
			MBRInfo mbrInfo = new MBRInfo();
			double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));

			if (area < 6) {
				totalLessMbr++;
			}
			// mbrInfo.setArea(area);
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[4]));
			mbr.setSwLong(Double.parseDouble(lineArr[5]));
			mbr.setNeLat(Double.parseDouble(lineArr[6]));
			mbr.setNeLong(Double.parseDouble(lineArr[7]));
			// mbrInfo.setMbr(mbr);

			mbrInfoMap.put(lineArr[0], mbr);
		}
		System.out.println("Loaded 78639 + 420 locations.." + mbrInfoMap.size());
//		hierarchyFile = Paths.get("14215-childrenmbr.txt");
		hierarchyFile = Paths.get("rem-childrenmbr.txt");
		
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");

//			if (lineArr[1].equals(lineArr[3]) && lineArr[2].equals(lineArr[4])) {
//				continue;
//			}
			// MBRInfo mbrInfo = new MBRInfo();
			double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[1]),
					Double.parseDouble(lineArr[2]), Double.parseDouble(lineArr[3]), Double.parseDouble(lineArr[4]));
			if (area < 6) {
				totalLessMbr++;
			}
			// mbrInfo.setArea(area);

			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[1]));
			mbr.setSwLong(Double.parseDouble(lineArr[2]));
			mbr.setNeLat(Double.parseDouble(lineArr[3]));
			mbr.setNeLong(Double.parseDouble(lineArr[4]));
			// mbrInfo.setMbr(mbr);
			mbrInfoMap.put(lineArr[0], mbr);
		}

		System.out.println("Total area less than 6:" + totalLessMbr);
		System.out.println("Loaded all locations.." + mbrInfoMap.size());
	}

	
	
	
	private static void calculateMoveAndDeleteAcc() throws IOException {
		Map<String, String> idPrevAndCurrParentMap = new HashMap();
		Path hierarchyFile = Paths.get("hierarchy-representation-onlygeonames.txt"); // to
																						// get
																						// the
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int twoParent = 0;

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr.length > 6) {
				String children[] = lineArr[6].split(" ");
				for (int i = 0; i < children.length; i++) {
					// if(children[i].equals("8260673")){
					// System.out.println(lineArr[0]);
					// }
					if (idPrevAndCurrParentMap.containsKey(children[i])) {
						// System.out.println(children[i] + " have more than one
						// parents: " + lineArr[0]);
						twoParent++;
					} else {

						idPrevAndCurrParentMap.put(children[i], lineArr[0]);
					}
				}
			}
		}
		
//		System.out.println("Parent for old hierarchy:" + idPrevAndCurrParentMap.get("7295552"));
		
		System.out.println("Number of places with two or more parents:" + twoParent);

		hierarchyFile = Paths.get("hierarchy-representation-onlygeonames.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[5].equals("null")) {
				if (!idPrevAndCurrParentMap.containsKey(lineArr[0])) {
					idPrevAndCurrParentMap.put(lineArr[0], "####");
				}
			}
		}
		
//		System.out.println("Parent for old hierarchy:" + idPrevAndCurrParentMap.get("7295552"));

		System.out.println("Loaded ids from previous hierarchy:" + idPrevAndCurrParentMap.size());

		// hierarchyFile =
		// Paths.get("hierarchy-representation-onlygeonames.txt");
		//
		// gridlines = null;
		// try {
		// gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// for (String line : (Iterable<String>) gridlines::iterator) {
		// String lineArr[] = line.split("\t");
		//
		// if (!idPrevAndCurrParentMap.containsKey(lineArr[0])) {
		// System.out.println(lineArr[0]);
		// }
		//
		// }
		// "hierarchy-refine-final.txt", true
		hierarchyFile = Paths.get("hierarchy-refine-final-new.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int found = 0;

		int numberOfPlacesWithTwoParent = 0;

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr.length >= 3) {
				// String parent = idPrevAndCurrParentMap.get(lineArr[0]);
				// idPrevAndCurrParentMap.put(lineArr[0], parent + "\t" +
				// "####");
				// found++;
				StringBuffer allChild = new StringBuffer(lineArr[2]);
				String allChildStr =  allChild.toString();
				
				if(lineArr.length >3){
					allChild = new StringBuffer();
					for(int i=3;i<lineArr.length;i++){			
							allChild.append(" ").append(lineArr[i]);
					}
					allChildStr = allChild.toString();
					allChildStr = allChildStr.trim();
				}
				
				String children[] = allChildStr.split(" ");
				if(lineArr[0].equals("3333169")){
					System.out.println("break");
				}
				for (int i = 0; i < children.length; i++) {
					if (idPrevAndCurrParentMap.containsKey(children[i])) {
						String parent = idPrevAndCurrParentMap.get(children[i]);
						if (parent.split("\t").length < 2) {
							idPrevAndCurrParentMap.put(children[i], parent + "\t" + lineArr[0]);
						} else {
							String parentDetail[] = parent.split("\t");
							if (parentDetail[0].equals(parentDetail[1])) {
								continue;
							} else {
								if (parentDetail[0].equals(lineArr[0])) {
									idPrevAndCurrParentMap.put(children[i], parentDetail[0] + "\t" + lineArr[0]);
								} else {
									continue;
								}
							}
							numberOfPlacesWithTwoParent++;
						}

						found++;
					}
				}
			}
		}
		
		System.out.println("number of places with two parent in new hierarchy:" +
				numberOfPlacesWithTwoParent);
		System.out.println("modified with children:" + found);

		//
		hierarchyFile = Paths.get("hierarchy-refine-final-new.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			// if (lineArr.length > 1) {
			if (idPrevAndCurrParentMap.containsKey(lineArr[0])) {
				String parent = idPrevAndCurrParentMap.get(lineArr[0]);
				if (parent.split("\t").length < 2) {
					idPrevAndCurrParentMap.put(lineArr[0], parent + "\t" + "####");
					found++;
				}
			} else {
				System.out.println("id not present:" + lineArr[0]);
			}
			// }
		}

		System.out.println("all id modified:" + found);

		int moveCount = 0;
		int remCount = 0;

		int validMoveCount = 0;
		int validRemCount = 0;
		int count = 0;
		System.out.println("Loaded the parent child for each id:" + idPrevAndCurrParentMap.size());

		Iterator it = idPrevAndCurrParentMap.entrySet().iterator();
		String moveFrom = " is moved from ";
		String to = " to  ";
		String nextLine = "\n";
		int noParentInBothHierarchy = 0;
		PrintWriter writer = new PrintWriter(new FileWriter("hierarchy-movement-oneparent-test-new.txt", true));
		String valid = " valid ";
		String invalid = " invalid ";
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String id = (String) entry.getKey();
			String prevCurrParent = (String) entry.getValue();
			String prevCurrArr[] = prevCurrParent.split("\t");
			 if(prevCurrArr.length<2){
			 System.out.println("Id:" + id + " parents:" + prevCurrParent) ;
			 }
			//
			if (prevCurrArr[0].equals("####") && prevCurrArr[1].equals("####")) {
				System.out.println("Id:" + id + " parents:" + prevCurrParent);
				noParentInBothHierarchy++;
				continue;
			}
			StringBuffer result = new StringBuffer();
			if (id.equals("3716981")) {
				System.out.println("break..");
			}
			// 3716981
			result.append(id).append(moveFrom).append(prevCurrArr[0]).append(to).append(prevCurrArr[1]);

			if (prevCurrArr[1].equals("####")) {
				if (!childInParent(id, prevCurrArr[0])) {
					validRemCount++;
					result.append(valid + " WPP"); // wrong previous parent
				} else {
					result.append(invalid + " WPP");
				}
				remCount++;

			} else {

				if (!prevCurrArr[0].equals("####") && prevCurrArr[0].equals(prevCurrArr[1])) {
					result.append(" NC"); // no change
					writer.write(result.append(nextLine).toString());
					writer.flush();
					count++;
					if (count % 100000 == 0) {
						System.out.println("Processed:" + count);
					}
					continue;
				}

				if (prevCurrArr[0].equals("####")) {
					if (childInParent(id, prevCurrArr[1])) {
						validMoveCount++;
						result.append(valid + " NP"); // new parent
					} else {
						result.append(invalid + " NP");
					}
					moveCount++;
				} else {

					boolean founditem = false;

					if (childInParent(id, prevCurrArr[0]) && childInParent(id, prevCurrArr[1])
							&& childInParent(prevCurrArr[1], prevCurrArr[0])) {
						validMoveCount++;
						founditem = true;
						result.append(valid + " DP"); // deeper parent
					}

					if (!childInParent(id, prevCurrArr[0]) && childInParent(id, prevCurrArr[1])) {
						validMoveCount++;
						founditem = true;
						result.append(valid + " CP"); // change parent
					}

					if (!founditem) {
						result.append(invalid + " CP");
					}

					moveCount++;
				}

			}

			writer.write(result.append(nextLine).toString());
			writer.flush();
			count++;
			if (count % 100000 == 0) {
				System.out.println("Processed:" + count);
			}
		}
		writer.close();
		System.out.println("noParentInBothHierarchy:" + noParentInBothHierarchy);
		double validMoveAcc = (double) validMoveCount / (double) moveCount;
		double validRemAcc = (double) validRemCount / (double) remCount;
		double totalAcc = (double) (validMoveCount + validRemCount) / (double) (moveCount + remCount);

		System.out.println("validMoveAcc:" + validMoveAcc + " valid move count:" + validMoveCount + " total move count:"
				+ moveCount);
		System.out.println(
				"validRemAcc:" + validRemAcc + " valid rem count:" + validRemCount + " total rem count:" + remCount);

		System.out.println("totalAcc:" + totalAcc + " valid total count:" + (validMoveCount + validRemCount)
				+ " total count:" + (moveCount + remCount));

	}

	private static boolean childInParent(String child, String parentId) {

		MBR childmbr = mbrInfoMap.get(child);
		MBR parentMbr = mbrInfoMap.get(parentId);

		if (childmbr == null && parentMbr == null) {
			String childDetail[] = centreCoordinateMap.get(child).split("\t");
			String parentDetail[] = centreCoordinateMap.get(parentId).split("\t");
			if (childDetail[0].equals(parentDetail[0]) && childDetail[1].equals(parentDetail[1])) {
				return true;
			} else {
				return false;
			}
		}

		if (parentMbr != null) {
			if (childmbr == null) {
				String childDetail[] = centreCoordinateMap.get(child).split("\t");
				childmbr = new MBR();
				childmbr.setNeLat(Double.parseDouble(childDetail[0]));
				childmbr.setSwLat(Double.parseDouble(childDetail[0]));
				childmbr.setSwLong(Double.parseDouble(childDetail[1]));
				childmbr.setNeLong(Double.parseDouble(childDetail[1]));
			}
			return isParentMbrContains(parentMbr, childmbr);

		} else {
			return false;
		}
	}

	private static void getCentreForAllPlaceInGeonames() {
		Path hierarchyFile = Paths.get("allCountries.txt"); // to get the
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		centreCoordinateMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			centreCoordinateMap.put(lineArr[0], lineArr[4] + "\t" + lineArr[5] + "\t" + lineArr[1]);
		}

		System.out.println("Loaded allCounties..");
	}

	


	private static boolean isParentMbrContains(MBR parentMbr, MBR childMbr) {
		if (childMbr.getNeLat() >= parentMbr.getSwLat() && childMbr.getNeLat() <= parentMbr.getNeLat()) {
			if (childMbr.getSwLat() >= parentMbr.getSwLat() && childMbr.getSwLat() <= parentMbr.getNeLat()) {
				if (childMbr.getNeLong() >= parentMbr.getSwLong() && childMbr.getNeLong() <= parentMbr.getNeLong()) {
					if (childMbr.getSwLong() >= parentMbr.getSwLong()
							&& childMbr.getSwLong() <= parentMbr.getNeLong()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean iIsContainedByj(String i, String j) {
		MBR activeMbr = mbrInfoMap.get(j);
		MBR mbr = mbrInfoMap.get(i);
		if (mbr.getNeLat() >= activeMbr.getSwLat() && mbr.getNeLat() <= activeMbr.getNeLat()) {
			if (mbr.getSwLat() >= activeMbr.getSwLat() && mbr.getSwLat() <= activeMbr.getNeLat()) {
				if (mbr.getNeLong() >= activeMbr.getSwLong() && mbr.getNeLong() <= activeMbr.getNeLong()) {
					if (mbr.getSwLong() >= activeMbr.getSwLong() && mbr.getSwLong() <= activeMbr.getNeLong()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
