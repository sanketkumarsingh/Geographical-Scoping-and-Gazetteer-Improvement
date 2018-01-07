package thesis.approach.heuristic.outlier;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import thesis.data.entity.AncestorDescendant;
import thesis.evaluation.EvaluateHierarchy;
import thesis.util.DataLoader;


public class Bagplot {

//	public static void prepareFileForBagplotForOSMName(String childrenMbrFile, String outputFolder){
//		
//		generateChildFileForAllIdsForGeoNames( childrenMbrFile,  outputFolder);
//		generateChildFileForAllIdsForOSMName( childrenMbrFile,  outputFolder);
//	}
//	
//	
	
	// bagplot done using R. available in bagplot-R folder.
	
	public static void getMbrPostBagplotForOSM(String bagplotInputFile, String outlierInputFile,
			String childrenMbrFile, String bagplotOutputFolder,
			String outBagplotFile,String
			outOutlierFile){
		
		
//		generateBagPlotMbrForOSM("/Users/sanket/Documents/workspace/yfcc/child/160osmout/");
		generateBagPlotMbrForOSM(bagplotInputFile,  outlierInputFile,bagplotOutputFolder, 
				childrenMbrFile, outBagplotFile, outOutlierFile);
	}
	
	public static void getMbrPostBagplotForGeo(String bagplotInputFile, String outlierInputFile,
			String bagplotOutputFolder,String childrenMbrFile, 
			String outBagplotFile,String
			outOutlierFile){
		
//		generateBagPlotMbrForGeo("/Users/sanket/Documents/workspace/yfcc/child/140geoout/");
		generateBagPlotMbrForGeo(bagplotInputFile,  outlierInputFile,bagplotOutputFolder, 
				childrenMbrFile, outBagplotFile, outOutlierFile);
	}
	
	
	private static void generateBagPlotMbrForOSM(String bagplotInputFile, String outlierInputFile,
			String childrenMbrFile, String bagplotOutputFolder,
			String outBagplotFile,String
			outOutlierFile) {

		Map<String, String> placeIdChildListMap = new HashMap();
		Path hierarchyFile = Paths.get(childrenMbrFile);
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			placeIdChildListMap.put(lineArr[0], line);
		}

		// loading all childs..
		Map<String, String> centreCoordinateMap = new HashMap();
		Map<String, AncestorDescendant> childrenMap  = DataLoader.loadAllOSMNameChild(childrenMbrFile);

		hierarchyFile = Paths.get(childrenMbrFile);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");		
			centreCoordinateMap.put(lineArr[0], null);
			Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
			for (String child: childrenSet) {
				centreCoordinateMap.put(child, null);
			}
		}

		// loading coordinate for each childs.
		hierarchyFile = Paths.get("osmname-hierarchy.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (centreCoordinateMap.containsKey(lineArr[0])) {
				centreCoordinateMap.put(lineArr[8] + ":" + lineArr[9], lineArr[0]);
			}
		}

		System.out.println("Loaded the data.. Going to read outlier and prepare MBR..");
		PrintWriter mbrWriter = null;
		try {

			mbrWriter = new PrintWriter(new FileWriter(outBagplotFile, true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter outlierWriter = null;
		try {
			outlierWriter = new PrintWriter(new FileWriter(outOutlierFile, true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String path = bagplotOutputFolder;
//		hierarchyFile = Paths.get(path + "160-id-count-outlier-witharea-8000limit.txt");
		hierarchyFile = Paths.get(path + outlierInputFile);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[1].equals("0")) {
				outlierWriter.write(line + "\n");
				outlierWriter.flush();
				continue;
			}
			String toWrite = lineArr[0] + "\t" + lineArr[1];
			String childId = centreCoordinateMap.get(lineArr[2]);
			for (int i = 3; i < lineArr.length; i++) {
				childId = childId + " " + centreCoordinateMap.get(lineArr[i]);
			}
			toWrite = toWrite + "\t" + childId;
			outlierWriter.write(toWrite + "\n");
			outlierWriter.flush();
		}
		outlierWriter.close();

//		hierarchyFile = Paths.get(path + "160-bagplot-mbr-witharea-8000limit.txt");
		hierarchyFile = Paths.get(path + bagplotInputFile);
		
		
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String newGeoMbrLine[] = line.split("\t");
			if (placeIdChildListMap.containsKey(newGeoMbrLine[0])) {
				String oldGeoMbrLine[] = placeIdChildListMap.get(newGeoMbrLine[0]).split("\t");
				String newGeoLine = oldGeoMbrLine[0] + "\t" + oldGeoMbrLine[1] + "\t" + oldGeoMbrLine[2] + "\t"
						+ oldGeoMbrLine[3] + "\t" + oldGeoMbrLine[4] + "\t" + oldGeoMbrLine[5] + "\t" + oldGeoMbrLine[6]
						+ "\t" + oldGeoMbrLine[7] + "\t" + newGeoMbrLine[1] + "\t" + newGeoMbrLine[2] + "\t"
						+ newGeoMbrLine[3] + "\t" + newGeoMbrLine[4] + "\t" + newGeoMbrLine[6] + "\t"
						+ newGeoMbrLine[5];
				mbrWriter.write(newGeoLine + "\n");
				mbrWriter.flush();
			} else {
				System.out.println(newGeoMbrLine[0]);
			}
		}
		mbrWriter.close();
	}

	private static void generateBagPlotMbrForGeo(String bagplotInputFile, String outlierInputFile,
			String bagplotOutputFolder,String childrenMbrFile, 
			String outBagplotFile,String
			outOutlierFile){

		Map<String, String> placeIdChildListMap = new HashMap();
		Path hierarchyFile = Paths.get(childrenMbrFile);
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			placeIdChildListMap.put(lineArr[0], line);
		}

		//
		hierarchyFile = Paths.get("allCountries.txt"); // to get the
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> centreCoordinateMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			centreCoordinateMap.put(lineArr[4] + ":" + lineArr[5], lineArr[0]);
		}

		System.out.println("Loaded the data.. Going to read outlier and prepare MBR..");
		PrintWriter mbrWriter = null;
		try {
			mbrWriter = new PrintWriter(new FileWriter(outBagplotFile, true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter outlierWriter = null;
		try {
			outlierWriter = new PrintWriter(new FileWriter(outOutlierFile, true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String path = bagplotOutputFolder ;
//		hierarchyFile = Paths.get(path + "140-id-count-outlier-witharea-8000limit.txt");
		hierarchyFile = Paths.get(path + outlierInputFile);
		
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[1].equals("0")) {
				outlierWriter.write(line + "\n");
				outlierWriter.flush();
				continue;
			}
			String toWrite = lineArr[0] + "\t" + lineArr[1];
			String childId = centreCoordinateMap.get(lineArr[2]);
			for (int i = 3; i < lineArr.length; i++) {
				childId = childId + " " + centreCoordinateMap.get(lineArr[i]);
			}
			toWrite = toWrite + "\t" + childId;
			outlierWriter.write(toWrite + "\n");
			outlierWriter.flush();
		}
		outlierWriter.close();
		
		hierarchyFile = Paths.get(path + bagplotInputFile);
//		hierarchyFile = Paths.get(path + "140-bagplot-mbr-witharea-8000limit.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String newGeoMbrLine[] = line.split("\t");
			if (placeIdChildListMap.containsKey(newGeoMbrLine[0])) {
				String oldGeoMbrLine[] = placeIdChildListMap.get(newGeoMbrLine[0]).split("\t");
				String newGeoLine = oldGeoMbrLine[0] + "\t" + oldGeoMbrLine[1] + "\t" + oldGeoMbrLine[2] + "\t"
						+ oldGeoMbrLine[3] + "\t" + oldGeoMbrLine[4] + "\t" + oldGeoMbrLine[5] + "\t" + oldGeoMbrLine[6]
						+ "\t" + oldGeoMbrLine[7] + "\t" + newGeoMbrLine[1] + "\t" + newGeoMbrLine[2] + "\t"
						+ newGeoMbrLine[3] + "\t" + newGeoMbrLine[4] + "\t" + newGeoMbrLine[6] + "\t"
						+ newGeoMbrLine[5];
				mbrWriter.write(newGeoLine + "\n");
				mbrWriter.flush();
			} else {
				System.out.println(newGeoMbrLine[0]);
			}
		}
		mbrWriter.close();
	}

	public  static void generateChildFileForAllIdsForGeoNames(String childrenMbrFile, String outputFolder) {


		Map<String, String> idGeonameMap = new HashMap();
		Path hierarchyFile = Paths.get(childrenMbrFile);
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			idGeonameMap.put(lineArr[0], null);
		}
		System.out.println("Loaded the geo mbr file..");

		Map<String, String> centreCoordinateMap = new HashMap();
		Map<String, AncestorDescendant> childrenMap  = DataLoader.loadAllGeoNameChild(childrenMbrFile);

		hierarchyFile = Paths.get(childrenMbrFile);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");		
			centreCoordinateMap.put(lineArr[0], null);
			Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
			for (String child: childrenSet) {
				centreCoordinateMap.put(child, null);
			}
		}
		

		hierarchyFile = Paths.get("allCountries.txt"); // to get the
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (centreCoordinateMap.containsKey(lineArr[0])) {
				centreCoordinateMap.put(lineArr[0], lineArr[4] + "\t" + lineArr[5]);
			}
		}

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
			if (centreCoordinateMap.containsKey(lineArr[0])) {
				String givenCenter = centreCoordinateMap.get(lineArr[0]);
				centreCoordinateMap.put(lineArr[0],
						givenCenter + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t" + lineArr[4]);
			}
		}

		System.out.println("Loaded the coordinate of centre of places..");
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
			if (lineArr.length > 6) {
				if (idGeonameMap.containsKey(lineArr[0])) {

					PrintWriter outlierWriter = null;
					try {
						// optWriter = new PrintWriter(new
						// FileWriter("531-opt-geo-minshift-mbr.txt", true));
//						outputFolder
						outlierWriter = new PrintWriter(new FileWriter(
								outputFolder + lineArr[0] + ".txt", true));
						
//						outlierWriter = new PrintWriter(new FileWriter(
//								"/Users/sanket/Documents/workspace/yfcc/child/140geo/" + lineArr[0] + ".txt", true));
					} catch (IOException e) {
						e.printStackTrace();
					}

					Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
					for (String child: childrenSet) {
						String coordinate[] = centreCoordinateMap.get(child).split("\t");
						outlierWriter.write(coordinate[0] + "\t" + coordinate[1] + "\n");
						outlierWriter.write(coordinate[2] + "\t" + coordinate[3] + "\n");
						outlierWriter.write(coordinate[4] + "\t" + coordinate[5] + "\n");
						outlierWriter.flush();
					}
					String coordinate[] = centreCoordinateMap.get(lineArr[0]).split("\t");
					outlierWriter.write(coordinate[0] + "\t" + coordinate[1] + "\n");
					outlierWriter.write(coordinate[2] + "\t" + coordinate[3] + "\n");
					outlierWriter.write(coordinate[4] + "\t" + coordinate[5] + "\n");
					outlierWriter.flush();
					outlierWriter.close();
				}
			}
		}

	}

	public static void generateChildFileForAllIdsForOSMName(String childrenMbr, String outputFolder) {

		Path hierarchyFile = Paths.get(childrenMbr); // taking
		// place
		// id
		// and
		// google map area from this.

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> osmIdMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			double trueArea = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));
			
			osmIdMap.put(lineArr[0], String.valueOf(trueArea)); // id:area
		}

		// loading all childs..
		Map<String, String> centreCoordinateMap = new HashMap();
		Map<String, AncestorDescendant> childrenMap  = DataLoader.loadAllOSMNameChild(childrenMbr);

		hierarchyFile = Paths.get(childrenMbr);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");		
			centreCoordinateMap.put(lineArr[0], null);
			Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
			for (String child: childrenSet) {
				centreCoordinateMap.put(child, null);
			}
		}
		
		// loading coordinate for each childs.
		hierarchyFile = Paths.get("osmname-hierarchy.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (centreCoordinateMap.containsKey(lineArr[0])) {
				centreCoordinateMap.put(lineArr[0], lineArr[8] + "\t" + lineArr[9] + "\t" + lineArr[1] + "\t"
						+ lineArr[2] + "\t" + lineArr[3] + "\t" + lineArr[4]);
			}
		}

		System.out.println("Loaded the coordinate of centre of places..");
		hierarchyFile = Paths.get("osmname-hierarchy.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr.length > 6) {
				if (osmIdMap.containsKey(lineArr[0])) {

					PrintWriter outlierWriter = null;
					try {
						// optWriter = new PrintWriter(new
						// FileWriter("531-opt-geo-minshift-mbr.txt", true));
						
						
						outlierWriter = new PrintWriter(new FileWriter(
								outputFolder + lineArr[0] + ".txt", true));
						
//						outlierWriter = new PrintWriter(new FileWriter(
//								"/Users/sanket/Documents/workspace/yfcc/child/160osm/" + lineArr[0] + ".txt", true));
					} catch (IOException e) {
						e.printStackTrace();
					}

					Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
					for (String child: childrenSet) {
						String coordinate[] = centreCoordinateMap.get(child).split("\t");
						outlierWriter.write(coordinate[0] + "\t" + coordinate[1] + "\n");
						outlierWriter.write(coordinate[2] + "\t" + coordinate[3] + "\n");
						outlierWriter.write(coordinate[4] + "\t" + coordinate[5] + "\n");
						outlierWriter.flush();
					}
					outlierWriter.close();
				}
			}
		}

	}
	
}
