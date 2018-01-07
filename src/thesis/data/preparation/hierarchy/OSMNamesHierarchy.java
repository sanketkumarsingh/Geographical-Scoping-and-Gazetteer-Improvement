package thesis.data.preparation.hierarchy;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import thesis.data.entity.BoundingBox;
import thesis.data.entity.PlaceInformation;

/*
 * Download planet-latest.tsv from www.osmnames.org and put it in classpath.
 * 
 */

public class OSMNamesHierarchy {

	public static void getOSMNameHierarchy() {
		getUniquePlaceId();
		replaceDisplayNameWithId();
		createOSMHierarchy();
	}
	
	private static void getUniquePlaceId() {
		Path hierarchyFile = Paths.get("planet-latest.tsv");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("unique-planet-latest.tsv", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		long max = Long.MIN_VALUE;
		Map<String, String> idLineMap = new TreeMap();
		long count = 0;
		int j = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (j == 0) {
				j = 1;
				continue;
			}
			if (lineArr.length < 21 || lineArr.length > 23) {
				continue;
			}
			if (lineArr[0].contains("\"")) {
				continue;
			}
			if (idLineMap.containsKey(lineArr[0])) {
				writer.write(line + "\n");
				writer.flush();
			} else {
				max = max + 1;
				StringBuffer newLine = new StringBuffer();
				if (lineArr.length == 21) {
					newLine.append(lineArr[0]).append("\t").append(lineArr[1]).append("\t").append(lineArr[2])
							.append("\t").append(max).append("\t").append(lineArr[4]).append("\t").append(lineArr[5])
							.append("\t").append(lineArr[6]).append("\t").append(lineArr[7]).append("\t")
							.append(lineArr[8]).append("\t").append(lineArr[9]).append("\t").append(lineArr[10])
							.append("\t").append(lineArr[11]).append("\t").append(lineArr[12]).append("\t")
							.append(lineArr[13]).append("\t").append(lineArr[14]).append("\t").append(lineArr[15])
							.append("\t").append(lineArr[16]).append("\t").append(lineArr[17]).append("\t")
							.append(lineArr[18]).append("\t").append(lineArr[19]).append("\t").append(lineArr[20]);
				} else if (lineArr.length == 22) {
					newLine.append(lineArr[0]).append("\t").append(lineArr[1]).append("\t").append(lineArr[2])
							.append("\t").append(max).append("\t").append(lineArr[4]).append("\t").append(lineArr[5])
							.append("\t").append(lineArr[6]).append("\t").append(lineArr[7]).append("\t")
							.append(lineArr[8]).append("\t").append(lineArr[9]).append("\t").append(lineArr[10])
							.append("\t").append(lineArr[11]).append("\t").append(lineArr[12]).append("\t")
							.append(lineArr[13]).append("\t").append(lineArr[14]).append("\t").append(lineArr[15])
							.append("\t").append(lineArr[16]).append("\t").append(lineArr[17]).append("\t")
							.append(lineArr[18]).append("\t").append(lineArr[19]).append("\t").append(lineArr[20])
							.append("\t").append(lineArr[21]);
				} else if (lineArr.length == 23) {
					newLine.append(lineArr[0]).append("\t").append(lineArr[1]).append("\t").append(lineArr[2])
							.append("\t").append(max).append("\t").append(lineArr[4]).append("\t").append(lineArr[5])
							.append("\t").append(lineArr[6]).append("\t").append(lineArr[7]).append("\t")
							.append(lineArr[8]).append("\t").append(lineArr[9]).append("\t").append(lineArr[10])
							.append("\t").append(lineArr[11]).append("\t").append(lineArr[12]).append("\t")
							.append(lineArr[13]).append("\t").append(lineArr[14]).append("\t").append(lineArr[15])
							.append("\t").append(lineArr[16]).append("\t").append(lineArr[17]).append("\t")
							.append(lineArr[18]).append("\t").append(lineArr[19]).append("\t").append(lineArr[20])
							.append("\t").append(lineArr[21]).append("\t").append(lineArr[22]);
				}
				writer.write(newLine.toString() + "\n");
				writer.flush();
			}
			count++;
			if (count % 1000000 == 0) {
				System.out.println("Count:" + count);
			}
		}

		writer.close();

		Iterator it = idLineMap.entrySet().iterator();

		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String value = (String) entry.getValue();
			writer.write(value + "\n");
			writer.flush();
		}
		writer.close();
	}
	
	
	private static void replaceDisplayNameWithId() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("osmname-parsed.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path hierarchyFile = Paths.get("unique-planet-latest.tsv");
		// Path hierarchyFile = Paths.get("sample.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int j = 0;
		Map<String, String> nameIdMap = new HashMap();
		long count = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (j == 0) {
				j = 1;
				continue;
			}
			if (lineArr.length < 21 || lineArr.length > 23) {
				continue;
			}
			if (lineArr[0].contains("\"")) {
				continue;
			}
			nameIdMap.put(lineArr[16].trim(), lineArr[3].trim());
			count++;
			if (count % 1000000 == 0) {
				System.out.println("Processed:" + count);
			}
		}
		System.out.println("Total items: " + nameIdMap.size());

		hierarchyFile = Paths.get("unique-planet-latest.tsv");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		j = 0;
		long counter = 0;

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			counter++;
			if (counter % 100000 == 0) {
				System.out.println("Processed:" + counter);
			}
			if (j == 0) {
				j = 1;
				continue;
			}
			if (lineArr.length < 21 || lineArr.length > 23) {
				continue;
			}
			if (lineArr[0].contains("\"")) {
				continue;
			}
			String display[] = lineArr[16].trim().split(",");
			// String ids[] = new String[display.length];
			String placeName = lineArr[16].trim();
			String ids = "";
			boolean isWrong = false;
			for (int i = 0; i < display.length; i++) {
				placeName = placeName.trim();
				if (i == 0) {
					ids = nameIdMap.get(placeName);
					if (ids == null) {
						isWrong = true;
						break;
					}
				} else {
					String id = nameIdMap.get(placeName);
					if (id == null) {
						isWrong = true;
						break;
					}
					ids = ids + "," + id;
				}
				int index = placeName.indexOf(",");
				if (index == -1) {
					break;
				}
				placeName = placeName.substring(index + 1);
			}
			if (isWrong) {
				continue;
			}

			StringBuffer obj = new StringBuffer(lineArr[0]);
			obj.append("\t");
			obj.append(lineArr[3]);
			obj.append("\t");
			obj.append(lineArr[7]);
			obj.append("\t");
			obj.append(lineArr[6]);
			obj.append("\t");
			obj.append(ids);
			obj.append("\t");
			obj.append(lineArr[16]);
			obj.append("\t");
			obj.append(lineArr[17]);
			obj.append("\t");
			obj.append(lineArr[18]);
			obj.append("\t");
			obj.append(lineArr[19]);
			obj.append("\t");
			obj.append(lineArr[20]);

			// writer.write(lineArr[0] + "\t" + lineArr[3] + "\t" + lineArr[7] +
			// "\t" + lineArr[6] + "\t" + ids + "\t"
			// + lineArr[16] + "\t" + lineArr[17] + "\t" + lineArr[18] + "\t" +
			// lineArr[19] + "\t" + lineArr[20]
			// + "\n");
			writer.write(obj.toString() + "\n");
			writer.flush();
		}
		writer.close();
	}
	
	
	private static void createOSMHierarchy() {

		Map<String, PlaceInformation> allPlaceInfoMap = new HashMap();

		// int countPerIteration = 0;
		// int skippedInIteration = 0;
		// int total = 0;
		int count = 0;
		for (int i = 1; i <= 18; i++) {

			Path hierarchyFile = Paths.get("osmname-parsed.txt");
			Stream<String> gridlines = null;
			try {
				gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("i=" + i);
			int j = 0;

			for (String line : (Iterable<String>) gridlines::iterator) {
				String lineArr[] = line.split("\t");
				if (lineArr.length < 10) {
					// skippedInIteration++;
					continue;
				}
				String display[] = lineArr[4].split(","); // ids
				if (display.length == i) {
					// countPerIteration++;
					if (display.length == 1) {
						PlaceInformation pInfo = new PlaceInformation();
						pInfo.setParentId(null);
						pInfo.setName(lineArr[0]);
						pInfo.setLongitude(Double.parseDouble(lineArr[3]));
						pInfo.setLat(Double.parseDouble(lineArr[2]));
						Set<String> childSet = new HashSet();
						childSet.add(display[0]);
						pInfo.setChildList(childSet);
						BoundingBox bb = new BoundingBox();
						bb.setNeLat(Double.parseDouble(lineArr[2]));
						bb.setNeLong(Double.parseDouble(lineArr[3]));
						bb.setSwLat(Double.parseDouble(lineArr[2]));
						bb.setSwLong(Double.parseDouble(lineArr[3]));
						pInfo.setBb(bb);
						allPlaceInfoMap.put(display[0], pInfo);
					} else {

						String placeId = display[0];
						PlaceInformation pInfo = new PlaceInformation();
						pInfo.setParentId(display[1]);
						pInfo.setName(lineArr[0]);
						pInfo.setLongitude(Double.parseDouble(lineArr[3]));
						pInfo.setLat(Double.parseDouble(lineArr[2]));
						Set<String> childSet = new HashSet();
						childSet.add(placeId);
						pInfo.setChildList(childSet);
						BoundingBox bb = new BoundingBox();
						bb.setNeLat(Double.parseDouble(lineArr[2]));
						bb.setNeLong(Double.parseDouble(lineArr[3]));
						bb.setSwLat(Double.parseDouble(lineArr[2]));
						bb.setSwLong(Double.parseDouble(lineArr[3]));
						pInfo.setBb(bb);
						allPlaceInfoMap.put(display[0], pInfo);
						if (display[1] == null || display[1].isEmpty()) {
							System.out.println(lineArr[4]);
						}
						updateParentBB(bb, allPlaceInfoMap, display[1], lineArr[4]);
						PlaceInformation parentPInfo = allPlaceInfoMap.get(display[1]);
						parentPInfo.getChildList().add(placeId);
					}
				}
			}
			// total = total + skippedInIteration + countPerIteration;
			// System.out.println("Iteration:" + i + " " + "skippedInIteration:"
			// + skippedInIteration + " " + "countPerIteration:" +
			// countPerIteration) ;
			//
			// System.out.println("total:" + (skippedInIteration +
			// countPerIteration));
			// System.out.println("Total till iteration:" + total);
			// skippedInIteration = 0;
			// countPerIteration = 0;
		}
		// System.out.println("allPlaceInfoMap size:" + allPlaceInfoMap.size());
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("osmname-hierarchy.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("allPlaceInfoMap size:" + allPlaceInfoMap.size());
		count = 0;
		Iterator it = allPlaceInfoMap.entrySet().iterator();
		while (it.hasNext()) {
			count++;
			if (count % 1000000 == 0) {
				System.out.println("Processed:" + count);
			}
			Entry entry = (Entry) it.next();
			String id = (String) entry.getKey();
			PlaceInformation pInfo = (PlaceInformation) entry.getValue();
			Set<String> childSet = pInfo.getChildList();
			Iterator iter = childSet.iterator();
			int i = 0;
			StringBuffer childIdSet = null;
			while (iter.hasNext()) {
				String childId = (String) iter.next();

				if (i == 0) {
					childIdSet = new StringBuffer(childId);
					i = 1;
				} else {
					childIdSet.append(" ").append(childId);
				}

			}
			StringBuffer obj = new StringBuffer();
			obj.append(id).append("\t").append(pInfo.getBb().getSwLat()).append("\t").append(pInfo.getBb().getSwLong())
					.append("\t").append(pInfo.getBb().getNeLat()).append("\t").append(pInfo.getBb().getNeLong())
					.append("\t").append(pInfo.getParentId()).append("\t").append(childIdSet).append("\t")
					.append(pInfo.getName()).append("\t").append(pInfo.getLat()).append("\t")
					.append(pInfo.getLongitude());
			writer.write(obj.toString() + "\n");
			writer.flush();
		}

		writer.close();
	}
	
	private static void updateParentBB(BoundingBox childbb, Map<String, PlaceInformation> allPlaceInfoMap,
			String parentId, String info) {
		if (parentId == null || parentId.isEmpty()) {
			return;
		}
		if (!allPlaceInfoMap.containsKey(parentId)) {
			System.out.println("info:" + info + "parentId:" + parentId);
		}

		BoundingBox parentBB = allPlaceInfoMap.get(parentId).getBb();
		double updNeLat = Math.max(childbb.getNeLat(), parentBB.getNeLat());
		double updNeLong = Math.max(childbb.getNeLong(), parentBB.getNeLong());
		double updSwLat = Math.min(childbb.getSwLat(), parentBB.getSwLat());
		double updSwLong = Math.min(childbb.getSwLong(), parentBB.getSwLong());
		parentBB.setNeLat(updNeLat);
		parentBB.setNeLong(updNeLong);
		parentBB.setSwLat(updSwLat);
		parentBB.setSwLong(updSwLong);
		updateParentBB(allPlaceInfoMap.get(parentId).getBb(), allPlaceInfoMap,
				allPlaceInfoMap.get(parentId).getParentId(), info);
	}
}
