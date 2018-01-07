package thesis.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import thesis.data.entity.AncestorDescendant;
import thesis.evaluation.EvaluateHierarchy;

public class GraphData {

	public static void main(String[] args) {
		 getBestPlaceInfo();
		 getAllGraphsData();
		 generatePlotForScaleAndInc();
		 getStatsOnTestSet();
	}

	
	private static void getOutLierGraphData(){
		Path hierarchyFile = Paths.get("accuracy-50-bagplot-2000.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int bagCount = 0;
		int boxCount = 0;
		Map<String, Double> bagplotAccMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			bagplotAccMap.put(lineArr[5],Double.parseDouble(lineArr[1]) );
		}
		

		hierarchyFile = Paths.get("accuracy-50-boxplot.txt");
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sameCount = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			double boxAcc = Double.parseDouble(lineArr[1]);
			if(boxAcc > bagplotAccMap.get(lineArr[5])){
				boxCount++;
			}else if(boxAcc < bagplotAccMap.get(lineArr[5])){
				bagCount++;
			} else{
				sameCount++;
				//bagCount++;
			}
		}
		
		System.out.println("boxCount:" + boxCount);
		System.out.println("bagCount:" + bagCount);
		System.out.println("sameCount:" + sameCount);
	}

	private static void getStatsOnTestSet() {
		int total = 160;
		Path hierarchyFile = Paths.get("accuracy-by-area-540-geo-childrenmbr.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double totalArea = 0.0;
		
		Map<String, String> placeIdAreaChildCountMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			placeIdAreaChildCountMap.put(lineArr[5], lineArr[0]);
			totalArea = totalArea + Double.parseDouble(lineArr[0]);
		}
		
		int totalChild = 0;
		Map<String, AncestorDescendant> childMap = DataLoader.loadAllGeoNameChild("540-geo-childrenmbr.txt");
		hierarchyFile = Paths.get("540-geo-childrenmbr.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			int childCount = childMap.get(lineArr[0]).getDescendants().size();
			placeIdAreaChildCountMap.put(lineArr[0],
					placeIdAreaChildCountMap.get(lineArr[0]) + "\t" + childCount);
			totalChild = totalChild + childCount;
		}

//		hierarchyFile = Paths.get("140-geo-childrenmbr.txt");
//		try {
//			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		int totalChild = 0;
//		for (String line : (Iterable<String>) gridlines::iterator) {
//			String lineArr[] = line.split("\t");
//			if (placeIdAreaChildCountMap.containsKey(lineArr[0])) {
//				int childCount = lineArr[6].split(" ").length;
//				placeIdAreaChildCountMap.put(lineArr[0],
//						placeIdAreaChildCountMap.get(lineArr[0]) + "\t" + lineArr[6].split(" ").length);
//				totalChild = totalChild + childCount;
//			}
//		}

		double avgArea = totalArea / (double) total;
		double avgChild = (double) totalChild / (double) total;

		int totalLess100 = 0;
		int totalChildForLessEq100 = 0;
		double totalAreaForLessEq100 = 0.0;

		int totalChildForMore100 = 0;
		double totalAreaForMore100 = 0.0;
		Iterator it = placeIdAreaChildCountMap.entrySet().iterator();

		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String id = (String) entry.getKey();
			String areaChildCount[] = ((String) entry.getValue()).split("\t");
			double area = Double.parseDouble(areaChildCount[0]);
			int childCount = Integer.parseInt(areaChildCount[1]);
//			if(area <= 100){
//				totalLess100++;
//				totalChildForLessEq100 = totalChildForLessEq100 + childCount;
//				totalAreaForLessEq100 = totalAreaForLessEq100 + area;
//			}else{
//				totalChildForMore100 = totalChildForMore100  + childCount;
//				totalAreaForMore100 = totalAreaForMore100 + area;
//			}
			if(childCount <= 25){
				totalLess100++;
				totalChildForLessEq100 = totalChildForLessEq100 + childCount;
				totalAreaForLessEq100 = totalAreaForLessEq100 + area;
			}else{
				totalChildForMore100 = totalChildForMore100  + childCount;
				totalAreaForMore100 = totalAreaForMore100 + area;
			}
		}
		
		double avgChildForLess100 = (double)totalChildForLessEq100/(double)totalLess100;
		double avgAreaForLess100 = (double)totalAreaForLessEq100/(double)totalLess100;
		
		double avgChildForMore100 = (double)totalChildForMore100/(double)(total - totalLess100);
		double avgareaForMore100 = (double)totalAreaForMore100/(double)(total - totalLess100);
		
		
		System.out.println("avgArea:" + avgArea);
		System.out.println("avgChild:" + avgChild);
		System.out.println("totalLess100:" + totalLess100);
		System.out.println("avgChildForLess100:" + avgChildForLess100);
		System.out.println("avgAreaForLess100:" + avgAreaForLess100);
		
		System.out.println("*****************");
		System.out.println("totalMore100:" + (total-totalLess100));
		System.out.println("avgChildForMore100:" + avgChildForMore100);
		System.out.println("avgareaForMore100:" + avgareaForMore100);
		
	}

	private static void getAllGraphsData() { 

		int range = 35;

		Path hierarchyFile = Paths.get("160-osmname-result-new.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("area range -  mean diff all method acc..");
		List<PlaceGraphInfo> areaList = new ArrayList();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			double roundOffArea = Math.round(Double.parseDouble(lineArr[3]) * 100.0) / 100.0; // AREA
			PlaceGraphInfo obj = new PlaceGraphInfo();
			obj.setArea(roundOffArea);
			obj.setArogeodiff(Double.parseDouble(lineArr[9]));
			obj.setPomgeodiff(Double.parseDouble(lineArr[8]));
			obj.setCentergeodiff(Double.parseDouble(lineArr[6]));
			obj.setHybridgeodiff(Double.parseDouble(lineArr[7]));
			obj.setMindiffacc(Double.parseDouble(lineArr[10]));
			areaList.add(obj);
		}
		Collections.sort(areaList);

		// mean diff acc vs area range
		// PlaceGraphInfoCCSort
		// String toWrite = id + "\t" + place.getBestMethod() + "\t" +
		// place.getBestAccuracy() + "\t" + place.getArea()
		// + "\t" + place.getChildCount() + "\t" + place.getBaseAcc() + "\t" +
		// place.getCentergeodiff() + "\t"
		// + place.getHybridgeodiff() + "\t" + place.getPomgeodiff() + "\t" +
		// place.getArogeodiff() + "\t"
		// + place.getMindiffacc();

		PrintWriter hieWriter = null;
		try {
			hieWriter = new PrintWriter(new FileWriter("160-areaRange-meanDiffAcc-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int totalCount = 0;
		double meanPomDiffAcc = 0.0;
		double meanAroDiffAcc = 0.0;
		double meanCenterDiffAcc = 0.0;
		double meanHyridDiffAcc = 0.0;
		double meanFromMinDiffAcc = 0.0;
		String areaRange = "";

		for (PlaceGraphInfo obj : areaList) {
			meanPomDiffAcc = meanPomDiffAcc + obj.getPomgeodiff();
			meanAroDiffAcc = meanAroDiffAcc + obj.getArogeodiff();
			meanCenterDiffAcc = meanCenterDiffAcc + obj.getCentergeodiff();
			meanHyridDiffAcc = meanHyridDiffAcc + obj.getHybridgeodiff();
			meanFromMinDiffAcc = meanFromMinDiffAcc + obj.getMindiffacc();
			totalCount++;
			if (totalCount == 1) {
				areaRange = obj.getArea() + " : ";
			}
			if (totalCount == range) {
				areaRange = areaRange + obj.getArea();
				meanPomDiffAcc = meanPomDiffAcc / (double) totalCount;
				meanAroDiffAcc = meanAroDiffAcc / (double) totalCount;
				meanCenterDiffAcc = meanCenterDiffAcc / (double) totalCount;
				meanHyridDiffAcc = meanHyridDiffAcc / (double) totalCount;
				meanFromMinDiffAcc = meanFromMinDiffAcc / (double) totalCount;

				hieWriter.write(areaRange + "\t" + meanPomDiffAcc + "\t" + meanHyridDiffAcc + "\t" + meanCenterDiffAcc
						+ "\t" + meanAroDiffAcc + "\t" + meanFromMinDiffAcc + "\n");
				hieWriter.flush();
				totalCount = 0;
				meanPomDiffAcc = 0.0;
				meanAroDiffAcc = 0.0;
				meanCenterDiffAcc = 0.0;
				meanHyridDiffAcc = 0.0;
				meanFromMinDiffAcc = 0.0;

				areaRange = "";

			}
		}

		if (totalCount != 0) {
			areaRange = areaRange + areaList.get(areaList.size() - 1).getArea();
			meanPomDiffAcc = meanPomDiffAcc / (double) totalCount;
			meanAroDiffAcc = meanAroDiffAcc / (double) totalCount;
			meanCenterDiffAcc = meanCenterDiffAcc / (double) totalCount;
			meanHyridDiffAcc = meanHyridDiffAcc / (double) totalCount;
			meanFromMinDiffAcc = meanFromMinDiffAcc / (double) totalCount;
			hieWriter.write(areaRange + "\t" + meanPomDiffAcc + "\t" + meanHyridDiffAcc + "\t" + meanCenterDiffAcc
					+ "\t" + meanAroDiffAcc + "\t" + meanFromMinDiffAcc + "\n");
			hieWriter.flush();
		}

		hieWriter.close();

		// mean diff acc vs child count range
		System.out.println("Child count -  mean diff all method acc..");
		hierarchyFile = Paths.get("160-osmname-result-new.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<PlaceGraphInfo> childCountList = new ArrayList();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			double roundOffArea = Math.round(Double.parseDouble(lineArr[3]) * 100.0) / 100.0; // AREA
			PlaceGraphInfo obj = new PlaceGraphInfo();
			// obj.setArea(roundOffArea);
			obj.setChildCount(Integer.parseInt(lineArr[4]));
			obj.setArogeodiff(Double.parseDouble(lineArr[9]));
			obj.setPomgeodiff(Double.parseDouble(lineArr[8]));
			obj.setCentergeodiff(Double.parseDouble(lineArr[6]));
			obj.setHybridgeodiff(Double.parseDouble(lineArr[7]));
			obj.setMindiffacc(Double.parseDouble(lineArr[10]));
			childCountList.add(obj);
		}
		PlaceGraphInfoCCSort sortobj = new PlaceGraphInfoCCSort();
		Collections.sort(childCountList, sortobj);

		// mean diff acc vs area range
		// PlaceGraphInfoCCSort
		// String toWrite = id + "\t" + place.getBestMethod() + "\t" +
		// place.getBestAccuracy() + "\t" + place.getArea()
		// + "\t" + place.getChildCount() + "\t" + place.getBaseAcc() + "\t" +
		// place.getCentergeodiff() + "\t"
		// + place.getHybridgeodiff() + "\t" + place.getPomgeodiff() + "\t" +
		// place.getArogeodiff() + "\t"
		// + place.getMindiffacc();

		hieWriter = null;
		try {
			hieWriter = new PrintWriter(new FileWriter("160-childCount-meanDiffAcc-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		totalCount = 0;
		meanPomDiffAcc = 0.0;
		meanAroDiffAcc = 0.0;
		meanCenterDiffAcc = 0.0;
		meanHyridDiffAcc = 0.0;
		meanFromMinDiffAcc = 0.0;
		String childCountRange = "";

		for (PlaceGraphInfo obj : childCountList) {
			meanPomDiffAcc = meanPomDiffAcc + obj.getPomgeodiff();
			meanAroDiffAcc = meanAroDiffAcc + obj.getArogeodiff();
			meanCenterDiffAcc = meanCenterDiffAcc + obj.getCentergeodiff();
			meanHyridDiffAcc = meanHyridDiffAcc + obj.getHybridgeodiff();
			meanFromMinDiffAcc = meanFromMinDiffAcc + obj.getMindiffacc();
			totalCount++;
			if (totalCount == 1) {
				childCountRange = obj.getChildCount() + " : ";
			}
			if (totalCount == range) {
				childCountRange = childCountRange + obj.getChildCount();
				meanPomDiffAcc = meanPomDiffAcc / (double) totalCount;
				meanAroDiffAcc = meanAroDiffAcc / (double) totalCount;
				meanCenterDiffAcc = meanCenterDiffAcc / (double) totalCount;
				meanHyridDiffAcc = meanHyridDiffAcc / (double) totalCount;
				meanFromMinDiffAcc = meanFromMinDiffAcc / (double) totalCount;

				hieWriter.write(childCountRange + "\t" + meanPomDiffAcc + "\t" + meanHyridDiffAcc + "\t"
						+ meanCenterDiffAcc + "\t" + meanAroDiffAcc + "\t" + meanFromMinDiffAcc + "\n");
				hieWriter.flush();
				totalCount = 0;
				meanPomDiffAcc = 0.0;
				meanAroDiffAcc = 0.0;
				meanCenterDiffAcc = 0.0;
				meanHyridDiffAcc = 0.0;
				meanFromMinDiffAcc = 0.0;

				childCountRange = "";

			}
		}

		if (totalCount != 0) {
			childCountRange = childCountRange + childCountList.get(childCountList.size() - 1).getChildCount();
			meanPomDiffAcc = meanPomDiffAcc / (double) totalCount;
			meanAroDiffAcc = meanAroDiffAcc / (double) totalCount;
			meanCenterDiffAcc = meanCenterDiffAcc / (double) totalCount;
			meanHyridDiffAcc = meanHyridDiffAcc / (double) totalCount;
			meanFromMinDiffAcc = meanFromMinDiffAcc / (double) totalCount;
			hieWriter.write(childCountRange + "\t" + meanPomDiffAcc + "\t" + meanHyridDiffAcc + "\t" + meanCenterDiffAcc
					+ "\t" + meanAroDiffAcc + "\t" + meanFromMinDiffAcc + "\n");
			hieWriter.flush();
		}

		hieWriter.close();

		// item count vs area

		// PlaceGraphInfoCCSort
		// String toWrite = id + "\t" + place.getBestMethod() + "\t" +
		// place.getBestAccuracy() + "\t" + place.getArea()
		// + "\t" + place.getChildCount() + "\t" + place.getBaseAcc() + "\t" +
		// place.getCentergeodiff() + "\t"
		// + place.getHybridgeodiff() + "\t" + place.getPomgeodiff() + "\t" +
		// place.getArogeodiff() + "\t"
		// + place.getMindiffacc();

		hierarchyFile = Paths.get("160-osmname-result-new.txt");
		System.out.println("area range - item count all method acc..");
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<PlaceGraphInfo> areaListForItem = new ArrayList();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			PlaceGraphInfo obj = new PlaceGraphInfo();
			obj.setBestMethod(lineArr[1]);
			double roundOffArea = Math.round(Double.parseDouble(lineArr[3]) * 100.0) / 100.0;
			// spread.setSpreadness(roundOff);
			obj.setArea(roundOffArea);
			areaListForItem.add(obj);

		}

		Collections.sort(areaListForItem);

		hieWriter = null;
		try {
			hieWriter = new PrintWriter(new FileWriter("160-areaRange-itemCount-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		totalCount = 0;
		int geoCount = 0;
		int centreCount = 0;
		int hybridCount = 0;
		int modelCount = 0;
		int aroCount = 0;
		areaRange = "";
		String counts = "";
		for (PlaceGraphInfo obj : areaListForItem) {

			if (obj.getBestMethod().equals("Hybrid")) {
				hybridCount++;
			}
			if (obj.getBestMethod().equals("Geo")) {
				geoCount++;
			}
			if (obj.getBestMethod().equals("Center")) {
				centreCount++;
			}
			if (obj.getBestMethod().equals("POM")) {
				modelCount++;
			}
			if (obj.getBestMethod().equals("ARO")) {
				aroCount++;
			}
			totalCount++;
			if (totalCount == 1) {
				areaRange = obj.getArea() + " : ";
			}
			// System.out.println(obj.getBestMethodName() + " " +
			// obj.getPlaceName() + " " + obj.getPlaceId() + " "
			// + obj.getSpreadNess());
			if (totalCount == range) {
				areaRange = areaRange + obj.getArea();
				counts = modelCount + " " + hybridCount + " " + centreCount + " " + aroCount + " " + geoCount;
				// counts = geoCount + " " + centreCount + " " + hybridCount
				// + " " + aroCount;
				hieWriter.write(areaRange + "\t" + counts + "\n");
				hieWriter.flush();
				totalCount = 0;
				geoCount = 0;
				centreCount = 0;
				hybridCount = 0;
				modelCount = 0;
				aroCount = 0;
				areaRange = "";
				counts = "";
			}
		}

		if (totalCount != 0) {
			areaRange = areaRange + areaListForItem.get(areaListForItem.size() - 1).getArea();
			counts = modelCount + " " + hybridCount + " " + centreCount + " " + aroCount + " " + geoCount;
			// counts = geoCount + " " + centreCount + " " + hybridCount
			// + " " + aroCount;
			hieWriter.write(areaRange + "\t" + counts + "\n");
			hieWriter.flush();
		}

		hieWriter.close();

		// item count vs child count
		// PlaceGraphInfoCCSort
		// String toWrite = id + "\t" + place.getBestMethod() + "\t" +
		// place.getBestAccuracy() + "\t" + place.getArea()
		// + "\t" + place.getChildCount() + "\t" + place.getBaseAcc() + "\t" +
		// place.getCentergeodiff() + "\t"
		// + place.getHybridgeodiff() + "\t" + place.getPomgeodiff() + "\t" +
		// place.getArogeodiff() + "\t"
		// + place.getMindiffacc();
		System.out.println("child count range -  item count all method acc..");
		hierarchyFile = Paths.get("160-osmname-result-new.txt");

		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<PlaceGraphInfo> childCountListForItem = new ArrayList();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			PlaceGraphInfo obj = new PlaceGraphInfo();
			obj.setBestMethod(lineArr[1]);
			// double roundOffArea = Math.round(Double.parseDouble(lineArr[3]) *
			// 100.0) / 100.0;
			// spread.setSpreadness(roundOff);
			obj.setChildCount(Integer.parseInt(lineArr[4]));
			childCountListForItem.add(obj);

		}

		Collections.sort(childCountListForItem, sortobj);

		hieWriter = null;
		try {
			hieWriter = new PrintWriter(new FileWriter("160-childCountRange-itemCount-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		totalCount = 0;
		geoCount = 0;
		centreCount = 0;
		hybridCount = 0;
		modelCount = 0;
		aroCount = 0;
		childCountRange = "";
		counts = "";
		for (PlaceGraphInfo obj : childCountListForItem) {

			if (obj.getBestMethod().equals("Hybrid")) {
				hybridCount++;
			}
			if (obj.getBestMethod().equals("Geo")) {
				geoCount++;
			}
			if (obj.getBestMethod().equals("Center")) {
				centreCount++;
			}
			if (obj.getBestMethod().equals("POM")) {
				modelCount++;
			}
			if (obj.getBestMethod().equals("ARO")) {
				aroCount++;
			}
			totalCount++;
			if (totalCount == 1) {
				childCountRange = obj.getChildCount() + " : ";
			}
			// System.out.println(obj.getBestMethodName() + " " +
			// obj.getPlaceName() + " " + obj.getPlaceId() + " "
			// + obj.getSpreadNess());
			if (totalCount == range) {
				childCountRange = childCountRange + obj.getChildCount();
				counts = modelCount + " " + hybridCount + " " + centreCount + " " + aroCount + " " + geoCount;
				// counts = geoCount + " " + centreCount + " " + hybridCount
				// + " " + aroCount;
				hieWriter.write(childCountRange + "\t" + counts + "\n");
				hieWriter.flush();
				totalCount = 0;
				geoCount = 0;
				centreCount = 0;
				hybridCount = 0;
				modelCount = 0;
				aroCount = 0;
				childCountRange = "";
				counts = "";
			}
		}

		if (totalCount != 0) {
			childCountRange = childCountRange
					+ childCountListForItem.get(childCountListForItem.size() - 1).getChildCount();
			counts = modelCount + " " + hybridCount + " " + centreCount + " " + aroCount + " " + geoCount;
			// counts = geoCount + " " + centreCount + " " + hybridCount
			// + " " + aroCount;
			hieWriter.write(childCountRange + "\t" + counts + "\n");
			hieWriter.flush();
		}

		hieWriter.close();

	}

	public static void getBestPlaceInfo() {
		// PlaceGraphInfoMap = new HashMap();
		Map<String, PlaceGraphInfo> placeBestMap = new HashMap();
		// Path hierarchyFile =
		// Paths.get("accuracy-by-area-1550-osmname-pom-mbr.txt");
		// Path hierarchyFile =
		// Paths.get("accuracy-by-area-1550-osmname-geo-mbr.txt");
		Path hierarchyFile = Paths.get("accuracy-by-area-540-geo-childrenmbr.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			PlaceGraphInfo place = new PlaceGraphInfo();
			place.setBestAccuracy(Double.parseDouble(lineArr[1]));
			place.setArea(Double.parseDouble(lineArr[0]));
			// place.setChildCount(Integer.parseInt(lineArr[lineArr.length-1]));
			place.setBestMethod("Geo");
			place.setBaseAcc(Double.parseDouble(lineArr[1]));
			// if (place.getArea() >= 10) {
			// placeBestMap.put(lineArr[5], place);
			// }

			placeBestMap.put(lineArr[5], place);
		}

//		 hierarchyFile =
//		 Paths.get("accuracy-by-area-140-geo-pom-mbr.txt");
//		 gridlines = null;
//		 try {
//		 gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
//		 } catch (IOException e) {
//		 // TODO Auto-generated catch block
//		 e.printStackTrace();
//		 }
//		 
//		 for (String line : (Iterable<String>) gridlines::iterator) {
//		 String lineArr[] = line.split("\t");
//		 if (placeBestMap.containsKey(lineArr[5])) {
//		 PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
//		 if (place.getBestAccuracy() < Double.parseDouble(lineArr[1])) {
//		 place.setBestAccuracy(Double.parseDouble(lineArr[1]));
//		 place.setBestMethod("POM");
//		
//		 }
//		 place.setPomgeodiff(Double.parseDouble(lineArr[1]) -
//		 place.getBaseAcc());
//		 place.setMindiffacc(Double.parseDouble(lineArr[1]));
//		 }
//		 }

		// hierarchyFile =
		// Paths.get("accuracy-by-area-1550-osmname-center-mbr.txt");
		hierarchyFile = Paths.get("accuracy-by-area-540-geo-center-mbr.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeBestMap.containsKey(lineArr[5])) {
				PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
				if (place.getBestAccuracy() < Double.parseDouble(lineArr[1])) {
					place.setBestAccuracy(Double.parseDouble(lineArr[1]));
					place.setBestMethod("Center");

				}
				place.setCentergeodiff(Double.parseDouble(lineArr[1]) - place.getBaseAcc());
				if (place.getMindiffacc() > Double.parseDouble(lineArr[1])) {
					place.setMindiffacc(Double.parseDouble(lineArr[1]));
				}

			}
		}

		// hierarchyFile =
		// Paths.get("accuracy-by-area-1550-osmname-boxplot-scale-mbr.txt");
		hierarchyFile = Paths.get("accuracy-by-area-540-geo-bagplot-scale-mbr.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeBestMap.containsKey(lineArr[5])) {
				PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
				if (place.getBestAccuracy() < Double.parseDouble(lineArr[1])) {
					place.setBestAccuracy(Double.parseDouble(lineArr[1]));
					place.setBestMethod("Hybrid");
				}
				place.setHybridgeodiff(Double.parseDouble(lineArr[1]) - place.getBaseAcc());
				if (place.getMindiffacc() > Double.parseDouble(lineArr[1])) {
					place.setMindiffacc(Double.parseDouble(lineArr[1]));
				}

			}
		}

		hierarchyFile = Paths.get("accuracy-by-area-540-geo-bagplot-enlarge-mbr.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeBestMap.containsKey(lineArr[5])) {
				PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
				if (place.getBestAccuracy() < Double.parseDouble(lineArr[1])) {
					place.setBestAccuracy(Double.parseDouble(lineArr[1]));
					place.setBestMethod("Hybrid");
				}
				place.setHybridgeodiff(Double.parseDouble(lineArr[1]) - place.getBaseAcc());
				if (place.getMindiffacc() > Double.parseDouble(lineArr[1])) {
					place.setMindiffacc(Double.parseDouble(lineArr[1]));
				}

			}
		}
		System.out.println("size of osm:" + placeBestMap.size());

		hierarchyFile = Paths.get("accuracy-by-area-540-geo-boxplot-scale-mbr.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeBestMap.containsKey(lineArr[5])) {
				PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
				if (place.getBestAccuracy() < Double.parseDouble(lineArr[1])) {
					place.setBestAccuracy(Double.parseDouble(lineArr[1]));
					place.setBestMethod("Hybrid");
				}
				place.setHybridgeodiff(Double.parseDouble(lineArr[1]) - place.getBaseAcc());
				if (place.getMindiffacc() > Double.parseDouble(lineArr[1])) {
					place.setMindiffacc(Double.parseDouble(lineArr[1]));
				}

			}
		}
		System.out.println("size of osm:" + placeBestMap.size());

		hierarchyFile = Paths.get("accuracy-by-area-540-geo-boxplot-enlarge-mbr.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeBestMap.containsKey(lineArr[5])) {
				PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
				if (place.getBestAccuracy() < Double.parseDouble(lineArr[1])) {
					place.setBestAccuracy(Double.parseDouble(lineArr[1]));
					place.setBestMethod("Hybrid");
				}
				place.setHybridgeodiff(Double.parseDouble(lineArr[1]) - place.getBaseAcc());
				if (place.getMindiffacc() > Double.parseDouble(lineArr[1])) {
					place.setMindiffacc(Double.parseDouble(lineArr[1]));
				}

			}
		}
		System.out.println("size of osm:" + placeBestMap.size());

		hierarchyFile = Paths.get("accuracy-by-area-540-geo-bagplot-mbr.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeBestMap.containsKey(lineArr[5])) {
				PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
				if (place.getBestAccuracy() < Double.parseDouble(lineArr[1])) {
					place.setBestAccuracy(Double.parseDouble(lineArr[1]));
					place.setBestMethod("ARO");
				}
				place.setArogeodiff(Double.parseDouble(lineArr[1]) - place.getBaseAcc());
				if (place.getMindiffacc() > Double.parseDouble(lineArr[1])) {
					place.setMindiffacc(Double.parseDouble(lineArr[1]));
				}
			}
		}
		System.out.println("size of osm:" + placeBestMap.size());

		hierarchyFile = Paths.get("accuracy-by-area-540-geo-boxplot-mbr.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeBestMap.containsKey(lineArr[5])) {
				PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
				if (place.getBestAccuracy() < Double.parseDouble(lineArr[1])) {
					place.setBestAccuracy(Double.parseDouble(lineArr[1]));
					place.setBestMethod("ARO");
				}
				place.setArogeodiff(Double.parseDouble(lineArr[1]) - place.getBaseAcc());
				if (place.getMindiffacc() > Double.parseDouble(lineArr[1])) {
					place.setMindiffacc(Double.parseDouble(lineArr[1]));
				}

			}
		}
		System.out.println("size of osm:" + placeBestMap.size());

		// set difference in acc

		hierarchyFile = Paths.get("accuracy-by-area-540-geo-childrenmbr.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeBestMap.containsKey(lineArr[5])) {
				PlaceGraphInfo place = placeBestMap.get(lineArr[5]);
				place.setMindiffacc(place.getMindiffacc() - place.getBaseAcc());
			}
		}
		System.out.println("size of osm:" + placeBestMap.size());

		// spreadness

		// loading all childs for each place in map
		// Map<String, String> idCoordinateMap = new HashMap();
		// hierarchyFile =
		// Paths.get("hierarchy-representation-onlygeonames.txt");
		
		Map<String, AncestorDescendant> childMap = DataLoader.loadAllGeoNameChild("540-geo-childrenmbr.txt");
		hierarchyFile = Paths.get("540-geo-childrenmbr.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
//			if (placeBestMap.containsKey(lineArr[0])) {
//				String children[] = lineArr[6].split(" ");
//				PlaceGraphInfo place = placeBestMap.get(lineArr[0]);
//				place.setChildCount(children.length);
//			}
			if(line.trim().isEmpty()){
				continue;
			}
			PlaceGraphInfo place = placeBestMap.get(lineArr[0]);
//			if(childMap.containsKey(lineArr[0])){
//				System.out.println(lineArr[0] + " not present.");
//			}
			place.setChildCount(childMap.get(lineArr[0]).getDescendants().size());
		}
		
		
		
		
		// // loading coordinate for each childs.
		// hierarchyFile = Paths.get("osmname-hierarchy.txt");
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
		// if (idCoordinateMap.containsKey(lineArr[0])) {
		// idCoordinateMap.put(lineArr[0], lineArr[8] + "\t" + lineArr[9]);
		// }
		// }
		// System.out.println("Counting child..");

		PrintWriter hieWriter = null;
		try {
			hieWriter = new PrintWriter(new FileWriter("540-geoname-result-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Iterator it = placeBestMap.entrySet().iterator();

		while (it.hasNext()) {

			Entry entry = (Entry) it.next();
			String id = (String) entry.getKey();
			PlaceGraphInfo place = (PlaceGraphInfo) entry.getValue();
			String toWrite = id + "\t" + place.getBestMethod() + "\t" + place.getBestAccuracy() + "\t" + place.getArea()
					+ "\t" + place.getChildCount() + "\t" + place.getBaseAcc() + "\t" + place.getCentergeodiff() + "\t"
					+ place.getHybridgeodiff() + "\t" + place.getPomgeodiff() + "\t" + place.getArogeodiff() + "\t"
					+ place.getMindiffacc();

			hieWriter.write(toWrite + "\n");
			hieWriter.flush();

		}

		hieWriter.close();
		System.out.println("Created 540-geoname-result-new.txt..");

		// hierarchyFile = Paths.get("osmname-hierarchy.txt");
		//
		// gridlines = null;
		//
		// try {
		// gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// for (String line : (Iterable<String>) gridlines::iterator) {
		// String lineArr[] = line.split("\t");
		// if (placeBestMap.containsKey(lineArr[0])) {
		// String mbr[] = idCoordinateMap.get(lineArr[0]).split("\t");
		// Double centreLat = Double.parseDouble(mbr[0]);
		// Double centreLong = Double.parseDouble(mbr[1]);
		// String children[] = lineArr[6].split(" ");
		// // if (children.length < 50) {
		// // continue;
		// // }
		// double sumSameLong = 0.0;
		// double sumSameLat = 0.0;
		//
		// for (int i = 0; i < children.length; i++) {
		// String ptCoordinate[] = idCoordinateMap.get(children[i]).split("\t");
		// double ptLat = Double.parseDouble(ptCoordinate[0]);
		// double ptLong = Double.parseDouble(ptCoordinate[1]);
		// double projectionLatForSameLat = centreLat;
		// double projectLong = ptLong;
		// double projectionLongForSameLong = centreLong;
		// double projectionLat = ptLat;
		// sumSameLong = sumSameLong
		// + DecideStrategy.getDistance(ptLat, ptLong, projectionLatForSameLat,
		// projectLong);
		// sumSameLat = sumSameLat
		// + DecideStrategy.getDistance(ptLat, ptLong, projectionLat,
		// projectionLongForSameLong);
		//
		// }
		//
		// double spreadness = sumSameLong / sumSameLat;
		// if (sumSameLat == 0.0) {
		// spreadness = Double.NEGATIVE_INFINITY; // NEED to take ratio
		// // ..
		// }
		// if (sumSameLong == 0.0) {
		// spreadness = Double.POSITIVE_INFINITY; // Need to take
		// // ratio..
		// }
		// if (sumSameLong == 0.0 && sumSameLat == 0.0) {
		// spreadness = 1.0;
		// }
		//
		// PlaceGraphInfo place = placeBestMap.get(lineArr[0]);
		// place.setSpreadness(spreadness);
		// }
		// }

		// point similarity
		// area similarity.

		// hierarchyFile =
		// Paths.get("1550-mbr-boxplot-centre-intersection.txt");
		// gridlines = null;
		// try {
		// gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// Map<String, String> placeInterMap = new HashMap();
		// for (String line : (Iterable<String>) gridlines::iterator) {
		// String lineArr[] = line.split("\t");
		// placeInterMap.put(lineArr[0], line);
		// }
		//
		// hierarchyFile = Paths.get("osmname-hierarchy.txt");
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
		// if (placeBestMap.containsKey(lineArr[0])) {
		// String intersectionMbr[] = placeInterMap.get(lineArr[0]).split("\t");
		// String children[] = lineArr[6].split(" ");
		//
		// double totalArea = placeBestMap.get(lineArr[0]).getArea();
		//
		// double swLat = Double.parseDouble(intersectionMbr[8]);
		// double swLong = Double.parseDouble(intersectionMbr[9]);
		// double neLat = Double.parseDouble(intersectionMbr[10]);
		// double neLong = Double.parseDouble(intersectionMbr[11]);
		//
		// double googleswLat = Double.parseDouble(intersectionMbr[4]);
		// double googleswLong = Double.parseDouble(intersectionMbr[5]);
		// double googleneLat = Double.parseDouble(intersectionMbr[6]);
		// double googleneLong = Double.parseDouble(intersectionMbr[7]);
		//
		// double intersectionArea = DecideStrategy.getAreaByCartesian(swLat,
		// swLong, neLat, neLong);
		// double areaRatio = intersectionArea / totalArea;
		// if (totalArea == 0.0) {
		// areaRatio = 0;
		// }
		// int totalChildInInterRegion = 0;
		// int totalInGoogle = 0;
		// for (int i = 0; i < children.length; i++) {
		// String coordinates[] = idCoordinateMap.get(children[i]).split("\t");
		// double ptLat = Double.parseDouble(coordinates[0]);
		// double ptLong = Double.parseDouble(coordinates[1]);
		//
		// if (ptLat >= googleswLat && ptLat <= googleneLat) {
		// if (ptLong >= googleswLong && ptLong <= googleneLong) {
		// totalInGoogle++;
		// if (ptLat >= swLat && ptLat <= neLat) {
		// if (ptLong >= swLong && ptLong <= neLong) {
		// totalChildInInterRegion++;
		// }
		// }
		// }
		// }
		//
		// }
		//
		// double childRatio = (double) totalChildInInterRegion / (double)
		// totalInGoogle;
		// if (totalInGoogle == 0.0) {
		// childRatio = 0;
		// }
		// PlaceGraphInfo place = placeBestMap.get(lineArr[0]);
		// place.setAreaSim(areaRatio);
		// place.setPointSim(childRatio);
		// }
		// }

	}

	

	// String toWrite = id + "\t" + place.getBestMethod() + "\t" +
	// place.getAccuracy() + "\t" + place.getArea()
	// + "\t" + place.getChildCount() + "\t" + place.getDiffFromGeo() + "\t" +
	// place.getPointSim() + "\t"
	// + place.getAreaSim() + "\t" + place.getSpreadness();


	private static void generatePlotForScaleAndInc() {
		Path hierarchyFile = Paths.get("accuracy-by-area-540-geo-boxplot-scale-mbr.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> placeInterMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			placeInterMap.put(lineArr[5], lineArr[1]);
		}

		hierarchyFile = Paths.get("accuracy-by-area-540-geo-boxplot-enlarge-mbr.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			placeInterMap.put(lineArr[5], placeInterMap.get(lineArr[5]) + "\t" + lineArr[1]);
		}

		hierarchyFile = Paths.get("allCountries.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int adm1Count = 0;
		int adm2Count = 0;
		int adm3Count = 0;
		int adm4Count = 0;
		int adm1ScaleCount = 0;
		int adm2ScaleCount = 0;
		int adm3ScaleCount = 0;
		int adm4ScaleCount = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeInterMap.containsKey(lineArr[0])) {
				String scaleEnlargeAcc[] = placeInterMap.get(lineArr[0]).split("\t");
				double scaleAcc = Double.parseDouble(scaleEnlargeAcc[0]);
				double incAcc = Double.parseDouble(scaleEnlargeAcc[1]);
				if (lineArr[7].equals("ADM1")) {
					adm1Count++;
					if (scaleAcc > incAcc) {
						adm1ScaleCount++;
					}
				}
				if (lineArr[7].equals("ADM2")) {
					adm2Count++;
					if (scaleAcc > incAcc) {
						adm2ScaleCount++;
					}
				}
				if (lineArr[7].equals("ADM3")) {
					adm3Count++;
					if (scaleAcc > incAcc) {
						adm3ScaleCount++;
					}
				}
				if (lineArr[7].equals("ADM4")) {
					adm4Count++;
					if (scaleAcc > incAcc) {
						adm4ScaleCount++;
					}
				}

				// ADM1 213 100 113
				// ADM2 225 156 69
				// ADM3 91 74 17
				// ADM4 2 0 2

			}
		}

		System.out.println("ADM1" + "\t" + adm1Count + "\t" + (adm1Count - adm1ScaleCount) + "\t" + adm1ScaleCount);
		System.out.println("ADM2" + "\t" + adm2Count + "\t" + (adm2Count - adm2ScaleCount) + "\t" + adm2ScaleCount);
		System.out.println("ADM3" + "\t" + adm3Count + "\t" + (adm3Count - adm3ScaleCount) + "\t" + adm3ScaleCount);
		System.out.println("ADM4" + "\t" + adm4Count + "\t" + (adm4Count - adm4ScaleCount) + "\t" + adm4ScaleCount);

	}

}

class PlaceGraphInfo implements Comparable {
	double bestAccuracy;
	String bestMethod;
	int childCount;
	double area;
	double pomgeodiff;
	double centergeodiff;
	double arogeodiff;
	double hybridgeodiff;
	double mindiffacc; // min acc all 4 methods - geo acc)
	double baseAcc;

	public double getBaseAcc() {
		return baseAcc;
	}

	public void setBaseAcc(double baseAcc) {
		this.baseAcc = baseAcc;
	}

	public double getBestAccuracy() {
		return bestAccuracy;
	}

	public void setBestAccuracy(double bestAccuracy) {
		this.bestAccuracy = bestAccuracy;
	}

	public double getPomgeodiff() {
		return pomgeodiff;
	}

	public void setPomgeodiff(double pomgeodiff) {
		this.pomgeodiff = pomgeodiff;
	}

	public double getCentergeodiff() {
		return centergeodiff;
	}

	public void setCentergeodiff(double centergeodiff) {
		this.centergeodiff = centergeodiff;
	}

	public double getArogeodiff() {
		return arogeodiff;
	}

	public void setArogeodiff(double arogeodiff) {
		this.arogeodiff = arogeodiff;
	}

	public double getHybridgeodiff() {
		return hybridgeodiff;
	}

	public void setHybridgeodiff(double hybridgeodiff) {
		this.hybridgeodiff = hybridgeodiff;
	}

	public double getMindiffacc() {
		return mindiffacc;
	}

	public void setMindiffacc(double mindiffacc) {
		this.mindiffacc = mindiffacc;
	}

	public String getBestMethod() {
		return bestMethod;
	}

	public void setBestMethod(String bestMethod) {
		this.bestMethod = bestMethod;
	}

	public int getChildCount() {
		return childCount;
	}

	public void setChildCount(int childCount) {
		this.childCount = childCount;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	@Override
	public int compareTo(Object o) {
		PlaceGraphInfo anotherObj = (PlaceGraphInfo) o;
		if (this.getArea() > anotherObj.getArea()) {
			return 1;
		} else if (this.getArea() < anotherObj.getArea()) {
			return -1;
		}
		return 0;
	}

}

class PlaceGraphInfoCCSort implements Comparator<PlaceGraphInfo> {

	@Override
	public int compare(PlaceGraphInfo o1, PlaceGraphInfo o2) {
		if (o1.getChildCount() > o2.getChildCount()) {
			return 1;
		} else if (o1.getChildCount() < o2.getChildCount()) {
			return -1;
		}
		return 0;
	}

}