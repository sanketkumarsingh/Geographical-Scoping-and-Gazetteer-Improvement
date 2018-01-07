package thesis.approach.heuristic.outlier;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


import thesis.data.entity.AncestorDescendant;
import thesis.data.entity.CoordinateInfo;
import thesis.data.entity.Geoname;
import thesis.evaluation.EvaluateHierarchy;
import thesis.util.DataLoader;


public class Boxplot {

	public  static void generateHierarchyWithoutOutlierForOSMName(String childrenMbrFile, String outputfileName) {

		Map<String, Geoname> idGeonameMap = new HashMap();

		Path hierarchyFile = Paths.get(childrenMbrFile);
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			Geoname obj = new Geoname();
			double area  = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]),
					Double.parseDouble(lineArr[7]));
			obj.setArea(String.valueOf(area));
			idGeonameMap.put(lineArr[0], obj);
		}

		System.out.println("Loaded the area.." + idGeonameMap.size());

		hierarchyFile = Paths.get(childrenMbrFile);
		// hierarchyFile = Paths.get("531-place-geo-mbr.txt");// to get the
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (idGeonameMap.containsKey(lineArr[0])) {
				Geoname obj = idGeonameMap.get(lineArr[0]);
				obj.setGeoMbr(line);
			}
		}
		System.out.println("Loaded the geo mbr file..");

		// loading all childs..
		Map<String, String> centreCoordinateMap = new HashMap();

		Map<String, AncestorDescendant> childrenMap = DataLoader.loadAllOSMNameChild(childrenMbrFile);

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
			Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
			for (String child : childrenSet) {
				centreCoordinateMap.put(child, null);
			}
			centreCoordinateMap.put(lineArr[0], null);
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
		int count = 0;
		PrintWriter mbrWriter = null;
		try {
			mbrWriter = new PrintWriter(new FileWriter("160-osm-boxplot-mbr.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		int countDone = 0;

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr.length <= 6) {
				count++;
				continue;
			}
			
			
			if (idGeonameMap.containsKey(lineArr[0])) {
				Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
				childrenSet.add(lineArr[0]);
				String childArr[] = new String[childrenSet.size()];
				int index = 0;
				for (String child : childrenSet) {
					childArr[index] = child;
					index++;
				}
				
				if(childArr.length>10000){
					System.out.println("More children for:"+lineArr[0] );
				}
				
				if (childrenSet.size() > 1) {
					double givenArea = Double.parseDouble(idGeonameMap.get(lineArr[0]).getArea());
					System.out.println("performing boxplot for :" + lineArr[0] + " givenArea:" + givenArea + " child count"
							 + childArr.length);
					double calculateArea = 0.0;
					
					double alpha = 1.0;
					int childCount = 0;
					String instanceUpdateGeoMbr = "";

					String updateGeoMbr = "";
					int iteration = 0;
					boolean outlierFound = false;
					String stats = getOutlierStatsInfo(alpha, childArr, centreCoordinateMap);
					String currStats[]= stats.split(" ");
					double q1Lat = Double.parseDouble(currStats[2]);
					double q3Lat = Double.parseDouble(currStats[3]);
					double q1Long =  Double.parseDouble(currStats[8]);
					double q3Long = Double.parseDouble(currStats[9]);
					double interQuartileRangeLat = Double.parseDouble(currStats[12]);
					double interQuartileRangeLong = Double.parseDouble(currStats[13]);
					while (true) {
						updateGeoMbr = "";
						iteration++;

						double outlierPoint1Lat = q1Lat - (alpha * interQuartileRangeLat);
						double outlierPoint2Lat = q3Lat + (alpha * interQuartileRangeLat);
						double outlierPoint1Long = q1Long - (alpha * interQuartileRangeLong);
						double outlierPoint3Long = q3Long + (alpha * interQuartileRangeLong);
						
						StringBuffer statOutput = new StringBuffer(currStats[0]);
						statOutput.append(" ").append(currStats[1])
						.append(" ").append(currStats[2])
						.append(" ").append(currStats[3])
						.append(" ").append(outlierPoint1Lat)
						.append(" ").append(outlierPoint2Lat)
						.append(" ").append(currStats[6]).append(" ").append(currStats[7])
						.append(" ").append(currStats[8])
						.append(" ").append(currStats[9])
						.append(" ").append(outlierPoint1Long)
						.append(" ").append(outlierPoint3Long);
						
						stats = statOutput.toString();

						// remove outlier, prepare childlist and find new
						// geoname
						// mbr
						// count for how many instances outlier is detected and
						// which places are removed from each
						// to verify it manually.
						String statisticsInfo[] = stats.split(" ");

						boolean first = true;
						int outlierPerIdCount = 0;
						outlierFound = false;
						boolean reqChildFirst = true;

						//
						String placeCoord[] = centreCoordinateMap.get(lineArr[0]).split("\t");
						double swLat = Double.parseDouble(placeCoord[0]);
						double swLong = Double.parseDouble(placeCoord[1]);
						double neLat = Double.parseDouble(placeCoord[0]);
						double neLong = Double.parseDouble(placeCoord[1]);
						childCount = 0;
						for (int i = 0; i < childArr.length; i++) {
							String coordinate[] = centreCoordinateMap.get(childArr[i]).split("\t");
						//	for (int j = 0; j < 6; j = j + 2) {

								double placeLat = Double.parseDouble(coordinate[0]);
								double placeLong = Double.parseDouble(coordinate[1]);
								double outLierPt1Lat = Double.parseDouble(statisticsInfo[4]);
								double outLierPt2Lat = Double.parseDouble(statisticsInfo[5]);
								double outLierPt1Long = Double.parseDouble(statisticsInfo[10]);
								double outLierPt2Long = Double.parseDouble(statisticsInfo[11]);
								if (placeLat < outLierPt1Lat || placeLat > outLierPt2Lat) {

									if (first) {
										// countOutlierDetected++;
										outlierPerIdCount++;
										//outlierIds = childArr[i];
										first = false;
									} else {
										// countOutlierDetected++;
										outlierPerIdCount++;
										//outlierIds = outlierIds + " " + childArr[i];
									}
									outlierFound = true;
								} else if (placeLong < outLierPt1Long || placeLong > outLierPt2Long) {

									if (first) {
										// countOutlierDetected++;
										outlierPerIdCount++;
										//outlierIds = childArr[i];
										first = false;
									} else {
										// countOutlierDetected++;
										outlierPerIdCount++;
										//outlierIds = outlierIds + " " + childArr[i];
									}
									outlierFound = true;
								} else {

									// maintain swlat , swLong, neLat, neLong
									childCount++;
									if (swLat > placeLat) {
										swLat = placeLat;
									}
									if (swLong > placeLong) {
										swLong = placeLong;
									}
									if (neLat < placeLat) {
										neLat = placeLat;
									}
									if (neLong < placeLong) {
										neLong = placeLong;
									}

									if (reqChildFirst) {
										//childListForPlace = childArr[i];
										reqChildFirst = false;
									} else {
										//childListForPlace = childListForPlace + " " + childArr[i];
									}
								}

						//	}
						}

						calculateArea = EvaluateHierarchy.getAreaByCartesian(swLat, swLong, neLat, neLong);

						if (outlierFound) {
							if (calculateArea > givenArea) {
								break;
							}
						} else {
							if (calculateArea > givenArea) {
								break;
							}
							updateGeoMbr = swLat + "\t" + swLong + "\t" + neLat + "\t" + neLong;
							instanceUpdateGeoMbr = updateGeoMbr + "\t" + alpha + "\t" + childCount;
							break;
						}

						updateGeoMbr = swLat + "\t" + swLong + "\t" + neLat + "\t" + neLong;
						instanceUpdateGeoMbr = updateGeoMbr + "\t" + alpha + "\t" + childCount;
						alpha = alpha + 1.0;
					}

					String oldGeoMbrLine[] = idGeonameMap.get(lineArr[0]).getGeoMbr().split("\t");
					String newGeoLine = oldGeoMbrLine[0] + "\t" + oldGeoMbrLine[1] + "\t" + oldGeoMbrLine[2] + "\t"
							+ oldGeoMbrLine[3] + "\t" + oldGeoMbrLine[4] + "\t" + oldGeoMbrLine[5] + "\t"
							+ oldGeoMbrLine[6] + "\t" + oldGeoMbrLine[7] + "\t" + instanceUpdateGeoMbr;

					if (instanceUpdateGeoMbr.isEmpty()) {
						newGeoLine = idGeonameMap.get(lineArr[0]).getGeoMbr() + "\t" + "0.01" + "\t" + childArr.length;
					}

					mbrWriter.write(newGeoLine + "\n");
					mbrWriter.flush();

					countDone++;
					if (countDone % 50 == 0) {
						System.out.println("Place done:" + countDone);
					}
				} else {
					String oldGeoMbrLine = idGeonameMap.get(lineArr[0]).getGeoMbr();

					mbrWriter.write(oldGeoMbrLine + "\n");
					mbrWriter.flush();

				}
			}
			count++;
			if (count % 1000000 == 0) {
				System.out.println("Processed: " + count);
			}
		}
		mbrWriter.close();

	}
	
	public static void generateHierarchyWithoutOutlierForGeoName(String childrenMbrFile, String outputfileName) {
		Map<String, Geoname> idGeonameMap = new HashMap();

		Path hierarchyFile = Paths.get(childrenMbrFile);
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			Geoname obj = new Geoname();
			double area  = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]),
					Double.parseDouble(lineArr[7]));
			obj.setArea(String.valueOf(area));
			idGeonameMap.put(lineArr[0], obj);
		}

		System.out.println("Loaded the area..");

		hierarchyFile = Paths.get(childrenMbrFile);
		// hierarchyFile = Paths.get("531-place-geo-mbr.txt");// to get the
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (idGeonameMap.containsKey(lineArr[0])) {
				Geoname obj = idGeonameMap.get(lineArr[0]);
				obj.setGeoMbr(line);
			}
		}
		System.out.println("Loaded the geo mbr file..");

		Map<String, String> centreCoordinateMap = new HashMap();
		//
		Map<String, AncestorDescendant> childrenMap = DataLoader.loadAllGeoNameChild(childrenMbrFile);

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
			Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
			for (String child : childrenSet) {
				centreCoordinateMap.put(child, null);
			}
			centreCoordinateMap.put(lineArr[0], null);
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
		int count = 0;

		PrintWriter mbrWriter = null;
		try {
			mbrWriter = new PrintWriter(new FileWriter(outputfileName));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// int countOutlierDetected = 0;
		int countDone = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr.length <= 6) {
				count++;
				continue;
			}

			// String childArr[] = lineArr[6].split(" ");
			if (idGeonameMap.containsKey(lineArr[0])) {
				Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
				childrenSet.add(lineArr[0]);
				String childArr[] = new String[childrenSet.size()];
				int index = 0;
				for (String child : childrenSet) {
					childArr[index] = child;
					index++;
				}
				
				if(childArr.length>10000){
					System.out.println("More children for:"+lineArr[0] );
				}
				
				if(lineArr[0].equals("2696332")){
					System.out.println("break.");
				}

				if (childrenSet.size() > 1) {
					double givenArea = Double.parseDouble(idGeonameMap.get(lineArr[0]).getArea());
					System.out.println("performing boxplot for :" + lineArr[0] + " givenArea:" + givenArea + " child count"
							 + childArr.length);
					double calculateArea = 0.0;
					
					double alpha = 1.0;
					int childCount = 0;

					String instanceUpdateGeoMbr = "";

					String updateGeoMbr = "";

					int iteration = 0;
					boolean outlierFound = false;
					String stats = getOutlierStatsInfo(alpha, childArr, centreCoordinateMap);
					String currStats[]= stats.split(" ");
					double q1Lat = Double.parseDouble(currStats[2]);
					double q3Lat = Double.parseDouble(currStats[3]);
					double q1Long =  Double.parseDouble(currStats[8]);
					double q3Long = Double.parseDouble(currStats[9]);
					double interQuartileRangeLat = Double.parseDouble(currStats[12]);
					double interQuartileRangeLong = Double.parseDouble(currStats[13]);
					while (true) {
						updateGeoMbr = "";

						iteration++;
						
//						 
						double outlierPoint1Lat = q1Lat - (alpha * interQuartileRangeLat);
						double outlierPoint2Lat = q3Lat + (alpha * interQuartileRangeLat);
						double outlierPoint1Long = q1Long - (alpha * interQuartileRangeLong);
						double outlierPoint3Long = q3Long + (alpha * interQuartileRangeLong);
						
						StringBuffer statOutput = new StringBuffer(currStats[0]);
						statOutput.append(" ").append(currStats[1])
						.append(" ").append(currStats[2])
						.append(" ").append(currStats[3])
						.append(" ").append(outlierPoint1Lat)
						.append(" ").append(outlierPoint2Lat)
						.append(" ").append(currStats[6]).append(" ").append(currStats[7])
						.append(" ").append(currStats[8])
						.append(" ").append(currStats[9])
						.append(" ").append(outlierPoint1Long)
						.append(" ").append(outlierPoint3Long);
						
						stats = statOutput.toString();

						// remove outlier, prepare childlist and find new
						// geoname
						// mbr
						// count for how many instances outlier is detected and
						// which places are removed from each
						// to verify it manually.
						String statisticsInfo[] = stats.split(" ");

						boolean first = true;
						int outlierPerIdCount = 0;
						outlierFound = false;
						boolean reqChildFirst = true;

						//
						String placeCoord[] = centreCoordinateMap.get(lineArr[0]).split("\t");
						double swLat = Double.parseDouble(placeCoord[0]);
						double swLong = Double.parseDouble(placeCoord[1]);
						double neLat = Double.parseDouble(placeCoord[0]);
						double neLong = Double.parseDouble(placeCoord[1]);
						childCount = 0;
						for (int i = 0; i < childArr.length; i++) {
							String coordinate[] = centreCoordinateMap.get(childArr[i]).split("\t");
						//	for (int j = 0; j < 6; j = j + 2) {

								double placeLat = Double.parseDouble(coordinate[0]);
								double placeLong = Double.parseDouble(coordinate[1]);
								double outLierPt1Lat = Double.parseDouble(statisticsInfo[4]);
								double outLierPt2Lat = Double.parseDouble(statisticsInfo[5]);
								double outLierPt1Long = Double.parseDouble(statisticsInfo[10]);
								double outLierPt2Long = Double.parseDouble(statisticsInfo[11]);
								if (placeLat < outLierPt1Lat || placeLat > outLierPt2Lat) {

									if (first) {
										// countOutlierDetected++;
										outlierPerIdCount++;
										//outlierIds = childArr[i];
										first = false;
									} else {
										// countOutlierDetected++;
										outlierPerIdCount++;
										//outlierIds = outlierIds + " " + childArr[i];
									}
									outlierFound = true;
								} else if (placeLong < outLierPt1Long || placeLong > outLierPt2Long) {

									if (first) {
										// countOutlierDetected++;
										outlierPerIdCount++;
										//outlierIds = childArr[i];
										first = false;
									} else {
										// countOutlierDetected++;
										outlierPerIdCount++;
										//outlierIds = outlierIds + " " + childArr[i];
									}
									outlierFound = true;
								} else {

									// maintain swlat , swLong, neLat, neLong
									childCount++;
									if (swLat > placeLat) {
										swLat = placeLat;
									}
									if (swLong > placeLong) {
										swLong = placeLong;
									}
									if (neLat < placeLat) {
										neLat = placeLat;
									}
									if (neLong < placeLong) {
										neLong = placeLong;
									}

									if (reqChildFirst) {
										//childListForPlace = childArr[i];
										reqChildFirst = false;
									} else {
										//childListForPlace = childListForPlace + " " + childArr[i];
									}
								}

						//	}
						}

						calculateArea = EvaluateHierarchy.getAreaByCartesian(swLat, swLong, neLat, neLong);

						if (outlierFound) {
							if (calculateArea > givenArea) {
								break;
							}
						} else {
							if (calculateArea > givenArea) {
								break;
							}
							updateGeoMbr = swLat + "\t" + swLong + "\t" + neLat + "\t" + neLong;
							instanceUpdateGeoMbr = updateGeoMbr + "\t" + alpha + "\t" + childCount;
							break;
						}

						updateGeoMbr = swLat + "\t" + swLong + "\t" + neLat + "\t" + neLong;

						instanceUpdateGeoMbr = updateGeoMbr + "\t" + alpha + "\t" + childCount;
						alpha = alpha + 1.0;
					}

					String oldGeoMbrLine[] = idGeonameMap.get(lineArr[0]).getGeoMbr().split("\t");
					String newGeoLine = oldGeoMbrLine[0] + "\t" + oldGeoMbrLine[1] + "\t" + oldGeoMbrLine[2] + "\t"
							+ oldGeoMbrLine[3] + "\t" + oldGeoMbrLine[4] + "\t" + oldGeoMbrLine[5] + "\t"
							+ oldGeoMbrLine[6] + "\t" + oldGeoMbrLine[7] + "\t" + instanceUpdateGeoMbr;

					if (instanceUpdateGeoMbr.isEmpty()) {

						newGeoLine = idGeonameMap.get(lineArr[0]).getGeoMbr() + "\t" + "0.01" + "\t" + childArr.length;
					}

					mbrWriter.write(newGeoLine + "\n");
					mbrWriter.flush();

					countDone++;
					if (countDone % 20 == 0) {
						System.out.println("Place done:" + countDone);
					}
				} else {

					String oldGeoMbrLine = idGeonameMap.get(lineArr[0]).getGeoMbr();

					mbrWriter.write(oldGeoMbrLine + "\n");
					mbrWriter.flush();

				}
			}
			count++;
			if (count % 1000000 == 0) {
				System.out.println("Processed: " + count);
			}
		}
		mbrWriter.close();
	}
	
	private static String getOutlierStatsInfo(double alpha, String[] childArr,
			Map<String, String> centreCoordinateMap) {

		double medianLat = 0;
		double medianLong = 0;
		double stdLat = 0;
		double stdLong = 0;
		double q1Lat = 0;
		double q3Lat = 0;
		double q1Long = 0;
		double q3Long = 0;
		List<CoordinateInfo> latList = new ArrayList();
		List<CoordinateInfo> longList = new ArrayList();
		for (int i = 0; i < childArr.length; i++) {
			String coordinateArr[] = centreCoordinateMap.get(childArr[i]).split("\t");

			CoordinateInfo latObj1 = new CoordinateInfo();

			latObj1.setCoordinate(Double.parseDouble(coordinateArr[0]));

			latList.add(latObj1);

			CoordinateInfo longObj1 = new CoordinateInfo();

			longObj1.setCoordinate(Double.parseDouble(coordinateArr[1]));

			longList.add(longObj1);

		}
		Collections.sort(latList);
		Collections.sort(longList);
		if (latList.size() % 2 == 0) {
			int index = (latList.size() + 1) / 2;
			CoordinateInfo latObj1 = latList.get(index);
			CoordinateInfo latObj2 = latList.get(index - 1);
			medianLat = (latObj1.getCoordinate() + latObj2.getCoordinate()) / 2;
			CoordinateInfo longObj1 = longList.get(index);
			CoordinateInfo longObj2 = longList.get(index - 1);
			medianLong = (longObj1.getCoordinate() + longObj2.getCoordinate()) / 2;

			
			int firstHalfIndex = (latList.size()) /2;
			
			
			 // 4, 5, 6, 7
			if(firstHalfIndex%2 == 0){
				int secIndexq1 = firstHalfIndex/2;
				int firstIndexq1 = secIndexq1-1;
				q1Lat = (latList.get(firstIndexq1).getCoordinate() + latList.get(secIndexq1).getCoordinate())
						/ 2;
				q1Long = (longList.get(firstIndexq1).getCoordinate()
						+ longList.get(secIndexq1).getCoordinate()) / 2;
				
				int secIndexq3 = (firstHalfIndex + latList.size())/2;
				int firstIndexq3 = secIndexq3-1;
				
				q3Lat = (latList.get(secIndexq3).getCoordinate()
						+ latList.get(firstIndexq3).getCoordinate()) / 2;
				
				q3Long = (longList.get(secIndexq3).getCoordinate()
						+ longList.get(firstIndexq3).getCoordinate()) / 2;
			}else{
				
				int firstIndexq1 = firstHalfIndex/2;
				q1Lat = latList.get(firstIndexq1).getCoordinate();
				q1Long = longList.get(firstIndexq1).getCoordinate();
				
				int firstIndexq3 = (firstHalfIndex + latList.size())/2;
				
				q3Lat = latList.get(firstIndexq3).getCoordinate();
				q3Long = longList.get(firstIndexq3).getCoordinate();
				
			}
		} else {
			int index = (latList.size()) / 2;
			medianLat = latList.get(index).getCoordinate();
			medianLong = longList.get(index).getCoordinate();

			if(index%2 == 0){
				int secIndexq1 = index/2;
				int firstIndexq1 = secIndexq1-1;
				q1Lat = (latList.get(firstIndexq1).getCoordinate() + latList.get(secIndexq1).getCoordinate())
						/ 2;
				q1Long = (longList.get(firstIndexq1).getCoordinate()
						+ longList.get(secIndexq1).getCoordinate()) / 2;
				
				int secIndexq3 = (index + latList.size())/2;
				int firstIndexq3 = secIndexq3+1;
				
				q3Lat = (latList.get(secIndexq3).getCoordinate()
						+ latList.get(firstIndexq3).getCoordinate()) / 2;
				
				q3Long = (longList.get(secIndexq3).getCoordinate()
						+ longList.get(firstIndexq3).getCoordinate()) / 2;
				
			}else{
				
				int firstIndexq1 = index/2;
				q1Lat = latList.get(firstIndexq1).getCoordinate();
				q1Long = longList.get(firstIndexq1).getCoordinate();
				
				int firstIndexq3 = (index + latList.size())/2;
				
				q3Lat = latList.get(firstIndexq3).getCoordinate();
				q3Long = longList.get(firstIndexq3).getCoordinate();
			}

		}

		double interQuartileRangeLat = q3Lat - q1Lat;
		double interQuartileRangeLong = q3Long - q1Long;
		double outlierPoint1Lat = q1Lat - (alpha * interQuartileRangeLat);
		double outlierPoint2Lat = q3Lat + (alpha * interQuartileRangeLat);
		double outlierPoint1Long = q1Long - (alpha * interQuartileRangeLong);
		double outlierPoint3Long = q3Long + (alpha * interQuartileRangeLong);
		// medianLat, stdLat, q1Lat, q3Lat, outlierPoint1Lat,
		// outlierPoint2Lat,
		// medianLong, stdLong, q1Long, q3Long, outlierPoint1Long,
		// outlierPoint2Long

		String output = medianLat + " " + stdLat + " " + q1Lat + " " + q3Lat + " " + outlierPoint1Lat + " "
				+ outlierPoint2Lat + " " + medianLong + " " + stdLong + " " + q1Long + " " + q3Long + " "
				+ outlierPoint1Long + " " + outlierPoint3Long + " " + interQuartileRangeLat + " " + 
				interQuartileRangeLong;

		return output;
	}
}
