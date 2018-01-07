package thesis.data.preparation.hierarchy;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import thesis.data.entity.PlaceNode;


public class GeoNamesHierarchy {

	private static Map<String, List<String>> geonameFeatureClassMap = new HashMap();
	public static void getGeoNameHierarchy() {
		loadFeatureClassAndCode();
		createHierarchyFile();
		createHierarchyByFC(); //  hierarchy-representation-onlygeoname is created here..
	}
	
	private static void loadFeatureClassAndCode() {
		String fileNames[] = new String[] { "A-count.txt", "A-state.txt", "H.txt", "L.txt", "P.txt", "R.txt", "S.txt",
				"T.txt", "U.txt", "V.txt" };
		for (int i = 0; i < fileNames.length; i++) {
			String key = fileNames[i].split(Pattern.quote("."))[0];
			Path hierarchyFile = Paths.get(fileNames[i]);
			Stream<String> gridlines = null;
			try {
				gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (String line : (Iterable<String>) gridlines::iterator) {
				String lineArr[] = line.split("\t");
				if (geonameFeatureClassMap.containsKey(key)) {
					geonameFeatureClassMap.get(key).add(lineArr[0]);
				} else {
					List<String> codeList = new ArrayList();
					codeList.add(lineArr[0]);
					geonameFeatureClassMap.put(key, codeList);
				}
			}

		}
		System.out.println("Loaded the feature classes and corresponding codes: " + geonameFeatureClassMap.size());
	}
	
	
	private static void createHierarchyFile() {
		Path hierarchyFile = Paths.get("allCountries.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = 0;
		// searching for countries by A-count codes
		List<String> countryCodeList = geonameFeatureClassMap.get("A-count");
		Map<String, List<String>> countryMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if ("A".equals(lineArr[6]) && countryCodeList.contains(lineArr[7])) {
				if (countryMap.containsKey(lineArr[8])) {
					countryMap.get(lineArr[8]).add(lineArr[0]);
				} else {
					List<String> countryIdList = new ArrayList();
					countryIdList.add(lineArr[0]);
					countryMap.put(lineArr[8], countryIdList);
				}
			}
		}
		System.out.println("Loaded countries.." + countryMap.size());
		List<String> provinceCodeList = geonameFeatureClassMap.get("A-state");
		hierarchyFile = Paths.get("allCountries.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter presentWriter = null;
		try {
			presentWriter = new PrintWriter(new FileWriter("hierarchy-by-FC-ALLADM.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> coveredGeonameIds = new HashSet();
		Map<String, List<String>> provinceMap = new HashMap();
		Map<String, String> fcMap = new HashMap();
		fcMap.put("ADM1", "ADM1");
		// fcMap.put("ADM1H", "ADM1H");
		// fcMap.put("ADMD", "ADMD");
		// fcMap.put("ADMDH", "ADMDH");
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if ("A".equals(lineArr[6]) && fcMap.containsKey(lineArr[7]) && countryMap.containsKey(lineArr[8])) {
				write(countryMap.get(lineArr[8]), lineArr[0], fcMap.get(lineArr[7]), presentWriter); // parentid
				// list
				// ,
				// child
				// id,
				coveredGeonameIds.addAll(countryMap.get(lineArr[8]));
				coveredGeonameIds.add(lineArr[0]);
				String key = lineArr[8] + ":" + lineArr[10]; // cc:admin1Code
				if (provinceMap.containsKey(key)) {
					provinceMap.get(key).add(lineArr[0]);
				} else {
					List<String> provinceList = new ArrayList();
					provinceList.add(lineArr[0]);
					provinceMap.put(key, provinceList);
				}
			}
		}
		// System.out.println("ca:01 = " + provinceMap.get("CA:01").size());
		System.out.println("Loaded all provinces by ADM1:" + provinceMap.size());

		hierarchyFile = Paths.get("allCountries.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[7].equals("ADMD")) {
				String key = lineArr[8] + ":" + lineArr[10];
				if (!provinceMap.containsKey(key)) {
					List<String> provinceList = new ArrayList();
					provinceList.add(lineArr[0]);
					provinceMap.put(key, provinceList);
				}
			}
		}
		System.out.println("Loaded provinces by ADMD:" + provinceMap.size());
		if (provinceMap.containsKey("AF:00")) {
			System.out.println("AF:00 is present.");
		}

		List<String> smallPlaceCodeList = geonameFeatureClassMap.get("S");
		List<String> cityCodeList = geonameFeatureClassMap.get("P");
		List<String> lakeCodeList = geonameFeatureClassMap.get("H");
		List<String> parkCodeList = geonameFeatureClassMap.get("L");
		List<String> railCodeList = geonameFeatureClassMap.get("R");
		List<String> hillCodeList = geonameFeatureClassMap.get("T");
		List<String> seaPlaceCodeList = geonameFeatureClassMap.get("U");
		List<String> forestCodeList = geonameFeatureClassMap.get("V");

		hierarchyFile = Paths.get("allCountries.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, List<String>> adm2Map = new HashMap();
		fcMap.clear();
		fcMap.put("ADM2", "ADM2");
		// fcMap.put("ADM2H", "ADM2H");
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			String currKey = lineArr[8] + ":" + lineArr[10];

			// ADM2 // "A".equals(lineArr[6]) && "ADM1".equals(lineArr[7]) &&
			// countryMap.containsKey(lineArr[8])
			if ("A".equals(lineArr[6]) && provinceMap.containsKey(currKey) && fcMap.containsKey(lineArr[7])) {
				write(provinceMap.get(currKey), lineArr[0], fcMap.get(lineArr[7]), presentWriter); // parentid
				// list
				// ,
				String key = currKey + ":" + lineArr[11]; // child
				if (adm2Map.containsKey(key)) {
					adm2Map.get(key).add(lineArr[0]);
				} else {
					List<String> adm2List = new ArrayList();
					adm2List.add(lineArr[0]);
					adm2Map.put(key, adm2List);
				} // id,

				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
			// cities
			if ("P".equals(lineArr[6]) && provinceMap.containsKey(currKey) && cityCodeList.contains(lineArr[7])) {
				write(provinceMap.get(currKey), lineArr[0], lineArr[7], presentWriter); // parentid
																						// list
																						// ,
																						// child
																						// id,
				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
			// small places buildings
			if ("S".equals(lineArr[6]) && smallPlaceCodeList.contains(lineArr[7]) && provinceMap.containsKey(currKey)) {
				write(provinceMap.get(currKey), lineArr[0], lineArr[7], presentWriter); // parentid
																						// list
																						// ,
																						// child
																						// id,
				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
			// lake, stream
			if ("H".equals(lineArr[6]) && lakeCodeList.contains(lineArr[7]) && provinceMap.containsKey(currKey)) {
				write(provinceMap.get(currKey), lineArr[0], lineArr[7], presentWriter); // parentid
																						// list
																						// ,
																						// child
																						// id,
				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
			// park , areas
			if ("L".equals(lineArr[6]) && parkCodeList.contains(lineArr[7]) && provinceMap.containsKey(currKey)) {
				write(provinceMap.get(currKey), lineArr[0], lineArr[7], presentWriter); // parentid
																						// list
																						// ,
																						// child
																						// id,
				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
			// rail road
			if ("R".equals(lineArr[6]) && railCodeList.contains(lineArr[7]) && provinceMap.containsKey(currKey)) {
				write(provinceMap.get(currKey), lineArr[0], lineArr[7], presentWriter); // parentid
																						// list
																						// ,
																						// child
																						// id,
				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
			// mountain,hill,rock
			if ("T".equals(lineArr[6]) && hillCodeList.contains(lineArr[7]) && provinceMap.containsKey(currKey)) {
				write(provinceMap.get(currKey), lineArr[0], lineArr[7], presentWriter); // parentid
																						// list
																						// ,
																						// child
																						// id,
				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
			// undersea
			if ("U".equals(lineArr[6]) && seaPlaceCodeList.contains(lineArr[7]) && provinceMap.containsKey(currKey)) {
				write(provinceMap.get(currKey), lineArr[0], lineArr[7], presentWriter); // parentid
																						// list
																						// ,
																						// child
																						// id,
				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
			// forest,health
			if ("V".equals(lineArr[6]) && forestCodeList.contains(lineArr[7]) && provinceMap.containsKey(currKey)) {
				write(provinceMap.get(currKey), lineArr[0], lineArr[7], presentWriter); // parentid
																						// list
																						// ,
																						// child
																						// id,
				coveredGeonameIds.addAll(provinceMap.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
		}

		System.out.println("Done for ADM2 and other feature classes..");

		hierarchyFile = Paths.get("allCountries.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fcMap.clear();
		fcMap.put("ADM3", "ADM3");
		// fcMap.put("ADM3H", "ADM3H");

		Map<String, List<String>> adm3Map = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			String currKey = lineArr[8] + ":" + lineArr[10] + ":" + lineArr[11];
			if ("A".equals(lineArr[6]) && adm2Map.containsKey(currKey) && fcMap.containsKey(lineArr[7])) {
				write(adm2Map.get(currKey), lineArr[0], fcMap.get(lineArr[7]), presentWriter); // parentid
				// list
				// ,
				String key = currKey + ":" + lineArr[12]; // child
				if (adm3Map.containsKey(key)) {
					adm3Map.get(key).add(lineArr[0]);
				} else {
					List<String> adm3List = new ArrayList();
					adm3List.add(lineArr[0]);
					adm3Map.put(key, adm3List);
				} // id,

				coveredGeonameIds.addAll(adm2Map.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
		}
		System.out.println("Done for ADM3..");
		hierarchyFile = Paths.get("allCountries.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fcMap.clear();
		fcMap.put("ADM4", "ADM4");
		// fcMap.put("ADM4H","ADM4H");
		Map<String, List<String>> adm4Map = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			String currKey = lineArr[8] + ":" + lineArr[10] + ":" + lineArr[11] + ":" + lineArr[12];
			if ("A".equals(lineArr[6]) && adm3Map.containsKey(currKey) && fcMap.containsKey(lineArr[7])) {
				write(adm3Map.get(currKey), lineArr[0], fcMap.get(lineArr[7]), presentWriter); // parentid
				// list
				// ,
				String key = currKey + ":" + lineArr[13]; // child
				if (adm4Map.containsKey(key)) {
					adm4Map.get(key).add(lineArr[0]);
				} else {
					List<String> adm4List = new ArrayList();
					adm4List.add(lineArr[0]);
					adm4Map.put(key, adm4List);
				} // id,

				coveredGeonameIds.addAll(adm3Map.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
		}
		System.out.println("Done for ADM4 ...");
		hierarchyFile = Paths.get("allCountries.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fcMap.clear();
		fcMap.put("ADM5", "ADM5");
		// fcMap.put("ADM5H","ADM5H");
		// Map<String, List<String>> adm5Map = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			String currKey = lineArr[8] + ":" + lineArr[10] + ":" + lineArr[11] + ":" + lineArr[12] + ":" + lineArr[13];
			if ("A".equals(lineArr[6]) && adm4Map.containsKey(currKey) && fcMap.containsKey(lineArr[7])) {
				write(adm4Map.get(currKey), lineArr[0], fcMap.get(lineArr[7]), presentWriter); // parentid
				// list
				// ,
				// String key = currKey + ":" + lineArr[13]; // child
				// if (adm4Map.containsKey(key)) {
				// adm4Map.get(key).add(lineArr[0]);
				// } else {
				// List<String> adm4List = new ArrayList();
				// adm4List.add(lineArr[0]);
				// adm4Map.put(key, adm4List);
				// } // id,
				//

				coveredGeonameIds.addAll(adm4Map.get(currKey));
				coveredGeonameIds.add(lineArr[0]);
			}
		}

		System.out.println("Done for ADM5..");

		System.out.println("Total geonames id covered:" + coveredGeonameIds.size());
		presentWriter.close();

		hierarchyFile = Paths.get("allCountries.txt");
		// hierarchyFile = Paths.get("hierarchy.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("notcovered-by-FC-ALLADM.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (!coveredGeonameIds.contains(lineArr[0])) {
				writer.write(line + "\n");
				writer.flush();
			}
		}
		writer.close();
	}
	
	private static void write(List<String> parentidList, String childid, String featureCode,
			PrintWriter presentWriter) {

		for (int i = 0; i < parentidList.size(); i++) {
			presentWriter.write(parentidList.get(i) + "\t" + childid + "\t" + featureCode + "\n");
			presentWriter.flush();
		}
	}
	
	
	
	

	private static void createHierarchyByFC() {

		Path hierarchyFile = Paths.get("hierarchy-by-FC-ALLADM.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> hierarchyElementsMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			hierarchyElementsMap.put(lineArr[0], null);
			hierarchyElementsMap.put(lineArr[1], null);
		}
		System.out.println("Loaded the present hierarchy.." + hierarchyElementsMap.size());
		hierarchyFile = Paths.get("allCountries.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (hierarchyElementsMap.containsKey(lineArr[0])) {
				hierarchyElementsMap.put(lineArr[0], lineArr[4] + ":" + lineArr[5]);
				count++;
			}
		}
		System.out.println("Updated the map with its lat and long.." + count);
		Map<String, PlaceNode> hierarchyMap = new HashMap();

		hierarchyFile = Paths.get("hierarchy-by-FC-ALLADM.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int parentCount = 0;
//		Hierarchy obj = new Hierarchy();
		long lineNo = 0;
		// Map<String, PlaceNode> parentMap = new HashMap();
		long totalParentNodes = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			lineNo++;
			if (hierarchyElementsMap.containsKey(lineArr[0]) && hierarchyElementsMap.containsKey(lineArr[1])) {

				if (hierarchyMap.containsKey(lineArr[0])) {
					PlaceNode parentNode = hierarchyMap.get(lineArr[0]); // .add(lineArr[1]);

					String leftLowerLat;
					String leftLowerLong;
					String rightUpLat;
					String rightUpLong;
					String childcoordinate = hierarchyElementsMap.get(lineArr[1]);
					// if(childcoordinate == null ){
					// System.out.println(lineArr[0] + " " + lineArr[1]);
					// }
					// System.out.println(lineArr[1]);
					String childlat = childcoordinate.split(":")[0];
					String childlongitude = childcoordinate.split(":")[1];
					if (hierarchyMap.containsKey(lineArr[1])) {
						PlaceNode childNode = hierarchyMap.get(lineArr[1]);
						leftLowerLat = minimum(childNode.getLeftLowerLat(), childNode.getRightUpLat(),
								parentNode.getLeftLowerLat(), parentNode.getRightUpLat());
						leftLowerLong = minimum(childNode.getLeftLowerLong(), childNode.getRightUpLong(),
								parentNode.getLeftLowerLong(), parentNode.getRightUpLong());
						rightUpLat = maximum(childNode.getLeftLowerLat(), childNode.getRightUpLat(),
								parentNode.getLeftLowerLat(), parentNode.getRightUpLat());
						rightUpLong = maximum(childNode.getLeftLowerLong(), childNode.getRightUpLong(),
								parentNode.getLeftLowerLong(), parentNode.getRightUpLong());
						// System.out.println("rightUpLong1:" + rightUpLong);

					} else {
						leftLowerLat = minimum(childlat, childlat, parentNode.getLeftLowerLat(),
								parentNode.getRightUpLat());
						// System.out.println(lineArr[0] + " " +
						// parentNode.getRightUpLong());
						leftLowerLong = minimum(childlongitude, childlongitude, parentNode.getLeftLowerLong(),
								parentNode.getRightUpLong());
						rightUpLat = maximum(childlat, childlat, parentNode.getLeftLowerLat(),
								parentNode.getRightUpLat());
						rightUpLong = maximum(childlongitude, childlongitude, parentNode.getLeftLowerLong(),
								parentNode.getRightUpLong());
						// System.out.println("rightUpLong2:" + rightUpLong);
					}
					parentNode.setLeftLowerLat(leftLowerLat);
					parentNode.setLeftLowerLong(leftLowerLong);
					parentNode.setRightUpLat(rightUpLat);
					parentNode.setRightUpLong(rightUpLong);
					PlaceNode currentParentNode = parentNode;
					while (currentParentNode.getParentGeonameId() != null) {
						PlaceNode prevParentNode = hierarchyMap.get(currentParentNode.getParentGeonameId());
						leftLowerLat = minimum(currentParentNode.getLeftLowerLat(), currentParentNode.getRightUpLat(),
								prevParentNode.getLeftLowerLat(), prevParentNode.getRightUpLat());
						leftLowerLong = minimum(currentParentNode.getLeftLowerLong(),
								currentParentNode.getRightUpLong(), prevParentNode.getLeftLowerLong(),
								prevParentNode.getRightUpLong());
						rightUpLat = maximum(currentParentNode.getLeftLowerLat(), currentParentNode.getRightUpLat(),
								prevParentNode.getLeftLowerLat(), prevParentNode.getRightUpLat());
						rightUpLong = maximum(currentParentNode.getLeftLowerLong(), currentParentNode.getRightUpLong(),
								prevParentNode.getLeftLowerLong(), prevParentNode.getRightUpLong());

						prevParentNode.setLeftLowerLat(leftLowerLat);
						prevParentNode.setLeftLowerLong(leftLowerLong);
						prevParentNode.setRightUpLat(rightUpLat);
						prevParentNode.setRightUpLong(rightUpLong);

						currentParentNode = prevParentNode;
					}
					if (parentNode.getChildGeonameIdSet() != null) {
						parentNode.getChildGeonameIdSet().add(lineArr[1]);
					} else {
						Set<String> childIds = new HashSet();
						childIds.add(lineArr[1]);
						parentNode.setChildGeonameIdSet(childIds);
					}

					if (!hierarchyMap.containsKey(lineArr[1])) {
						PlaceNode childPlaceNode = new PlaceNode();
						childPlaceNode.setParentGeonameId(lineArr[0]);
						childPlaceNode.setLeftLowerLat(childlat);
						childPlaceNode.setRightUpLat(childlat);
						childPlaceNode.setLeftLowerLong(childlongitude);
						childPlaceNode.setRightUpLong(childlongitude);
						hierarchyMap.put(lineArr[1], childPlaceNode);
					} else {
						hierarchyMap.get(lineArr[1]).setParentGeonameId(lineArr[0]);
					}

					// hierarchyMap.
				} else {
					// adding parent
					// if (lineArr[0].equals("6255148")) {
					// System.out.println("Error.");
					// }
					PlaceNode placeNode = new PlaceNode();
					String coordinate = hierarchyElementsMap.get(lineArr[0]);
					String parentlat = coordinate.split(":")[0];
					String parentlongitude = coordinate.split(":")[1];
					String childcoordinate = hierarchyElementsMap.get(lineArr[1]);
					String childlat = childcoordinate.split(":")[0];
					String childlongitude = childcoordinate.split(":")[1];
					String leftLowerLat;
					String leftLowerLong;
					String rightUpLat;
					String rightUpLong;
					if (hierarchyMap.containsKey(lineArr[1])) {
						PlaceNode childNode = hierarchyMap.get(lineArr[1]);
						leftLowerLat = minimum(childNode.getLeftLowerLat(), childNode.getRightUpLat(), parentlat,
								parentlat);
						leftLowerLong = minimum(childNode.getLeftLowerLong(), childNode.getRightUpLong(),
								parentlongitude, parentlongitude);
						rightUpLat = maximum(childNode.getLeftLowerLat(), childNode.getRightUpLat(), parentlat,
								parentlat);
						rightUpLong = maximum(childNode.getLeftLowerLong(), childNode.getRightUpLong(), parentlongitude,
								parentlongitude);
						// System.out.println("rightUpLong3:" + rightUpLong);

					} else {
						leftLowerLat = minimum(childlat, childlat, parentlat, parentlat);
						leftLowerLong = minimum(childlongitude, childlongitude, parentlongitude, parentlongitude);
						rightUpLat = maximum(childlat, childlat, parentlat, parentlat);
						rightUpLong = maximum(childlongitude, childlongitude, parentlongitude, parentlongitude);
						// System.out.println("rightUpLong4:" + rightUpLong);
					}
					if (leftLowerLat == null || leftLowerLat == null || rightUpLat == null || rightUpLong == null) {
						System.out.println("Error");
					}
					placeNode.setLeftLowerLat(leftLowerLat);
					placeNode.setLeftLowerLong(leftLowerLong);
					placeNode.setRightUpLat(rightUpLat);
					placeNode.setRightUpLong(rightUpLong);
					Set<String> childIds = new HashSet();
					childIds.add(lineArr[1]);
					placeNode.setChildGeonameIdSet(childIds);
					parentCount++;
					hierarchyMap.put(lineArr[0], placeNode);
					totalParentNodes++;
					// parentMap.put(lineArr[0], placeNode);
					// adding child
					if (!hierarchyMap.containsKey(lineArr[1])) {
						PlaceNode childPlaceNode = new PlaceNode();
						childPlaceNode.setLeftLowerLat(childlat);
						childPlaceNode.setRightUpLat(childlat);
						childPlaceNode.setLeftLowerLong(childlongitude);
						childPlaceNode.setRightUpLong(childlongitude);
						childPlaceNode.setParentGeonameId(lineArr[0]);
						hierarchyMap.put(lineArr[1], childPlaceNode);
					} else {
						hierarchyMap.get(lineArr[1]).setParentGeonameId(lineArr[0]);
					}
				}
			}
			if (lineNo % 100000 == 0) {
				System.out.println("Processed:" + lineNo);
			}
		}
		System.out.println("Parent Node: " + parentCount);
		System.out.println("Done: " + hierarchyMap.size());
		
		
		Iterator it = hierarchyMap.entrySet().iterator();
		PrintWriter hieWriter = null;
		try {
			hieWriter = new PrintWriter(new FileWriter("hierarchy-representation-onlygeonames.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String key = (String) entry.getKey();
			PlaceNode value = (PlaceNode) entry.getValue();
			String toWrite = key + "\t" + value.getLeftLowerLat() + "\t" + value.getLeftLowerLong() + "\t"
					+ value.getRightUpLat() + "\t" + value.getRightUpLong() + "\t" + value.getParentGeonameId();
			Set<String> childSet = value.getChildGeonameIdSet();
			String childs = "";
			if (childSet != null) {
				Iterator iter = childSet.iterator();
				boolean first = true;
				while (iter.hasNext()) {
					if (first) {
						childs = (String) iter.next();
						first = false;
					} else {
						childs = childs + " " + (String) iter.next();
					}
				}
				toWrite = toWrite + "\t" + childs;
			}
			hieWriter.write(toWrite + "\n");
			hieWriter.flush();
		}
		hieWriter.close();
		
		/// We have used the hierarchy till here only - hierarchy-representation-onlygeonames.txt
	}
	
	

	public static String maximum(String childlat, String childlat2, String parentlat, String parentlat2) {
		double max = -1000;
		Double childlatDouble = Double.parseDouble(childlat);
		Double childlat2Double = Double.parseDouble(childlat2);
		Double parentlatDouble = Double.parseDouble(parentlat);
		Double parentlat2Double = Double.parseDouble(parentlat2);

		if (max < childlatDouble) {
			max = childlatDouble;
		}

		if (max < childlat2Double) {
			max = childlat2Double;
		}

		if (max < parentlatDouble) {
			max = parentlatDouble;
		}

		if (max < parentlat2Double) {
			max = parentlat2Double;
		}

		if (max == -1000.0) {
			System.out.println("Max is used..");
		}
		return String.valueOf(max);
	}

	public static String minimum(String childlongitude, String childlongitude2, String parentlongitude,
			String parentlongitude2) {
		double min = Double.MAX_VALUE;
		Double childLongDouble = Double.parseDouble(childlongitude);
		Double childLongDouble2 = Double.parseDouble(childlongitude2);
		Double parentLongitude = Double.parseDouble(parentlongitude);
		// System.out.println(parentlongitude2);
		Double parentLongitude2 = Double.parseDouble(parentlongitude2);

		if (min > childLongDouble) {
			min = childLongDouble;
		}
		if (min > childLongDouble2) {
			min = childLongDouble2;
		}
		if (min > parentLongitude) {
			min = parentLongitude;
		}
		if (min > parentLongitude2) {
			min = parentLongitude2;
		}

		return String.valueOf(min);
	}



}
