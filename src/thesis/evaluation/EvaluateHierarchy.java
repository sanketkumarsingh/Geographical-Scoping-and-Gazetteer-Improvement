package thesis.evaluation;

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
import java.util.stream.Stream;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicMask;

public class EvaluateHierarchy {

	public static void main(String[] args) throws Exception {
	
		 verifyCentre();
		 evaluateByArea(0.0);  // AOA for both Geo and OSM
     	 evaluateByPoints(0.0);  // POA for Geo
	     evaluateByPointsForOsm(0.0); // POA for OSM
	}
	
	// 849/1000 - osmname
	// 605/1000 - geo
	private static void verifyCentre() {
		Path hierarchyFile = Paths.get("1000-sample-osmname.txt"); // taking

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {

			e.printStackTrace();
		}

		int count = 0;
		int sameCentreCount = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			if (count > 1000) {
				break;
			}
			String lineArr[] = line.split("\t");
			count++;
			double geoCentreLat = Double.parseDouble(lineArr[2]);
			double geoCentreLong = Double.parseDouble(lineArr[3]);
			double googleSwLat = Double.parseDouble(lineArr[4]);
			double googleSwLong = Double.parseDouble(lineArr[5]);
			double googleNeLat = Double.parseDouble(lineArr[6]);
			double googleNeLong = Double.parseDouble(lineArr[7]);
			double googleCentreLat = (googleSwLat + googleNeLat) / (double) 2;
			double googleCentreLong = (googleSwLong + googleNeLong) / (double) 2;
			double distance = getDistance(geoCentreLat, geoCentreLong, googleCentreLat, googleCentreLong);
			if (distance < 10) {
				sameCentreCount++;
			}
			// if(Math.abs(geoCentreLat - googleCentreLat) < 0.1){
			// if(Math.abs(geoCentreLong - googleCentreLong) < 0.1){
			// sameCentreCount++;
			// }
			// }
		}

		System.out.println("Same centre count:" + sameCentreCount);
	}

	private static void evaluateByPointsForOsm(double d) {

		Path hierarchyFile = Paths.get("160-pom-mbr-osm-endpoints.txt"); // taking
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
		Map<String, String> placeAreaMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr.length >= 12) {
				placeAreaMap.put(lineArr[0], line);
			} // id:area
		}

		gridlines = null;

		// loading all childs..
		Map<String, String> centreCoordinateMap = new HashMap();
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
			if (placeAreaMap.containsKey(lineArr[0])) {
				String children[] = lineArr[6].split(" ");
				for (int i = 0; i < children.length; i++) {
					centreCoordinateMap.put(children[i], null);
				}

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
				centreCoordinateMap.put(lineArr[0], lineArr[8] + "\t" + lineArr[9]);
			}
		}

		hierarchyFile = Paths.get("osmname-hierarchy.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int numberOfElements = 0;

		PrintWriter accWriter = null;
		try {
			accWriter = new PrintWriter(
					new FileWriter("accuracy-by-child-160-pom-mbr-osm-endpoints.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		double outlierAcc = 0.0;
		double mbrAcc = 0.0;
		double mbrfn = 0.0;
		double mbrfp = 0.0;
		int oaCount = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeAreaMap.containsKey(lineArr[0])) {
				String mbr[] = placeAreaMap.get(lineArr[0]).split("\t");
				double googleMbrSwLat = Double.parseDouble(mbr[4]);
				double googleMbrSwLong = Double.parseDouble(mbr[5]);
				double googleMbrNeLat = Double.parseDouble(mbr[6]);
				double googleMbrNeLong = Double.parseDouble(mbr[7]);

				double mbrSwLat = Double.parseDouble(mbr[8]);
				double mbrSwLong = Double.parseDouble(mbr[9]);
				double mbrNeLat = Double.parseDouble(mbr[10]);
				double mbrNeLong = Double.parseDouble(mbr[11]);
				numberOfElements++;
				int inGoogle = 0;
				int inMbr = 0;

				int inGoogleAndMbr = 0;
				int notInGoogleAndMbr = 0;
				int inGoogleNotInMbr = 0;
				int inMbrNotInGoogle = 0;
				boolean isPresentInBoth = false;
				boolean isAbsentInBoth = true;
				boolean isPresentInGoogle = false;
				boolean isPresentInMbr = false;
				String children[] = lineArr[6].split(" ");
				for (int i = 0; i < children.length; i++) {
					String coordinate[] = centreCoordinateMap.get(children[i]).split("\t");
					double placeLat = Double.parseDouble(coordinate[0]);
					double placeLong = Double.parseDouble(coordinate[1]);
					if (placeLat >= googleMbrSwLat && placeLat <= googleMbrNeLat) {
						if (placeLong >= googleMbrSwLong && placeLong <= googleMbrNeLong) {
							inGoogle++;
							isPresentInBoth = true;
							isAbsentInBoth = false;
							isPresentInGoogle = true;
						}
					}

					if (placeLat >= mbrSwLat && placeLat <= mbrNeLat) {
						if (placeLong >= mbrSwLong && placeLong <= mbrNeLong) {
							inMbr++;
							isAbsentInBoth = false;
							isPresentInMbr = true;
							if (isPresentInBoth) {
								inGoogleAndMbr++;
							}
						}
					}

					if (isAbsentInBoth) {
						notInGoogleAndMbr++;

					}
					if (isPresentInGoogle && !isPresentInMbr) {
						inGoogleNotInMbr++;
					}

					if (isPresentInMbr && !isPresentInGoogle) {
						inMbrNotInGoogle++;
					}

					isPresentInBoth = false;
					isAbsentInBoth = true;
					isPresentInGoogle = false;
					isPresentInMbr = false;
				}

				int notInGoogle = children.length - inGoogle;
				int notInMbr = children.length - inMbr;
				double ithoutlierAcc = 0.0;
				if (notInGoogle != 0) {
					oaCount++;
					ithoutlierAcc = (double) (notInGoogleAndMbr) / (double) (notInGoogle);
					// ithoutlierAcc = 1.0;
				} else {
					// ithoutlierAcc =
					// (double)(notInGoogleAndMbr)/(double)(notInGoogle);
				}

				double ithmbrAcc = (double) inGoogleAndMbr
						/ ((double) inGoogle + (double) inMbr - (double) inGoogleAndMbr);

				double ithFalseNeg = (double) inGoogleNotInMbr / (double) inGoogle;
				double ithFalsePos = (double) inMbrNotInGoogle / (double) inMbr;
				double ithGeoDataAcc = (double) inGoogle / (double) children.length;

				if (inGoogle == 0.0) {
					ithFalseNeg = 1.0;
				}
				if (inMbr == 0.0) {
					ithFalsePos = 1.0;
				}
				if (inGoogle == 0.0 && inMbr == 0.0) {
					ithmbrAcc = 0.0;
				}

				accWriter.write(lineArr[0] + "\t" + mbr[1] + "\t" + children.length + "\t" + inGoogle + "\t" + inMbr
						+ "\t" + +ithmbrAcc + "\t" + ithFalseNeg + "\t" + ithFalsePos + "\t" + ithoutlierAcc + "\t"
						+ ithGeoDataAcc + "\n");
				accWriter.flush();

				outlierAcc = outlierAcc + ithoutlierAcc;
				mbrAcc = mbrAcc + ithmbrAcc;
				mbrfp = mbrfp + ithFalsePos;
				mbrfn = mbrfn + ithFalseNeg;
			}
		}

		System.out.println("oaCount:" + oaCount);
		outlierAcc = outlierAcc / (double) oaCount;
		// System.out.println("outlierAcc:" + outlierAcc);
		mbrAcc = mbrAcc / (double) numberOfElements;
		mbrfp = mbrfp / (double) numberOfElements;
		mbrfn = mbrfn / (double) numberOfElements;

		accWriter.close();

		System.out.println("Accuracy by Points:" + mbrAcc);
		System.out.println("False positive:" + mbrfp);
		System.out.println("False negative:" + mbrfn);
		System.out.println("Outlier Accuracy:" + outlierAcc);

	}

	

	public static void evaluateByPoints(double factor) {

		Path hierarchyFile = Paths.get("allCountries.txt");

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> centreCoordinateMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			centreCoordinateMap.put(lineArr[0], lineArr[4] + "\t" + lineArr[5]);
		}
		// System.out.println(centreCoordinateMap.size());

		Map<String, String> idMbrMap = new HashMap();
		// 531-base-mbr-count.txt
		hierarchyFile = Paths.get("140-mbr-pom-geo-endpoints.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			idMbrMap.put(lineArr[0], line);
		}
		
		hierarchyFile = Paths.get("hierarchy-representation-onlygeonames.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int numberOfElements = 0;

		PrintWriter accWriter = null;
		try {
			accWriter = new PrintWriter(
					new FileWriter("accuracy-by-child-140-mbr-pom-geo-endpoints.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		double outlierAcc = 0.0;
		double mbrAcc = 0.0;
		double mbrfn = 0.0;
		double mbrfp = 0.0;
		int oaCount = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (idMbrMap.containsKey(lineArr[0])) {
				String mbr[] = idMbrMap.get(lineArr[0]).split("\t");
				double googleMbrSwLat = Double.parseDouble(mbr[4]);
				double googleMbrSwLong = Double.parseDouble(mbr[5]);
				double googleMbrNeLat = Double.parseDouble(mbr[6]);
				double googleMbrNeLong = Double.parseDouble(mbr[7]);


				double mbrSwLat = Double.parseDouble(mbr[8]);
				double mbrSwLong = Double.parseDouble(mbr[9]);
				double mbrNeLat = Double.parseDouble(mbr[10]);
				double mbrNeLong = Double.parseDouble(mbr[11]);
				numberOfElements++;
				int inGoogle = 0;
				int inMbr = 0;

				int inGoogleAndMbr = 0;
				int notInGoogleAndMbr = 0;
				int inGoogleNotInMbr = 0;
				int inMbrNotInGoogle = 0;
				boolean isPresentInBoth = false;
				boolean isAbsentInBoth = true;
				boolean isPresentInGoogle = false;
				boolean isPresentInMbr = false;
				String children[] = lineArr[6].split(" ");
				for (int i = 0; i < children.length; i++) {
					String coordinate[] = centreCoordinateMap.get(children[i]).split("\t");
					double placeLat = Double.parseDouble(coordinate[0]);
					double placeLong = Double.parseDouble(coordinate[1]);
					if (placeLat >= googleMbrSwLat && placeLat <= googleMbrNeLat) {
						if (placeLong >= googleMbrSwLong && placeLong <= googleMbrNeLong) {
							inGoogle++;
							isPresentInBoth = true;
							isAbsentInBoth = false;
							isPresentInGoogle = true;
						}
					}

					if (placeLat >= mbrSwLat && placeLat <= mbrNeLat) {
						if (placeLong >= mbrSwLong && placeLong <= mbrNeLong) {
							inMbr++;
							isAbsentInBoth = false;
							isPresentInMbr = true;
							if (isPresentInBoth) {
								inGoogleAndMbr++;
							}
						}
					}

					if (isAbsentInBoth) {
						notInGoogleAndMbr++;

					}
					if (isPresentInGoogle && !isPresentInMbr) {
						inGoogleNotInMbr++;
					}

					if (isPresentInMbr && !isPresentInGoogle) {
						inMbrNotInGoogle++;
					}

					isPresentInBoth = false;
					isAbsentInBoth = true;
					isPresentInGoogle = false;
					isPresentInMbr = false;
				}

				int notInGoogle = children.length - inGoogle;
				int notInMbr = children.length - inMbr;
				double ithoutlierAcc = 0.0;
				if (notInGoogle != 0) {
					oaCount++;
					ithoutlierAcc = (double) (notInGoogleAndMbr) / (double) (notInGoogle);
					// ithoutlierAcc = 1.0;
				} else {
					// ithoutlierAcc =
					// (double)(notInGoogleAndMbr)/(double)(notInGoogle);
				}

				double ithmbrAcc = (double) inGoogleAndMbr
						/ ((double) inGoogle + (double) inMbr - (double) inGoogleAndMbr);

				double ithFalseNeg = (double) inGoogleNotInMbr / (double) inGoogle;
				double ithFalsePos = (double) inMbrNotInGoogle / (double) inMbr;
				double ithGeoDataAcc = (double) inGoogle / (double) children.length;

				if (inGoogle == 0.0) {
					ithFalseNeg = 1.0;
				}
				if (inMbr == 0.0) {
					ithFalsePos = 1.0;
				}
				if (inGoogle == 0.0 && inMbr == 0.0) {
					ithmbrAcc = 0.0;
				}

				accWriter.write(lineArr[0] + "\t" + mbr[1] + "\t" + children.length + "\t" + inGoogle + "\t" + inMbr
						+ "\t" + +ithmbrAcc + "\t" + ithFalseNeg + "\t" + ithFalsePos + "\t" + ithoutlierAcc + "\t"
						+ ithGeoDataAcc + "\n");
				accWriter.flush();

				outlierAcc = outlierAcc + ithoutlierAcc;
				mbrAcc = mbrAcc + ithmbrAcc;
				mbrfp = mbrfp + ithFalsePos;
				mbrfn = mbrfn + ithFalseNeg;
			}
		}

		System.out.println("oaCount:" + oaCount);
		outlierAcc = outlierAcc / (double) oaCount;
		// System.out.println("outlierAcc:" + outlierAcc);
		mbrAcc = mbrAcc / (double) numberOfElements;
		mbrfp = mbrfp / (double) numberOfElements;
		mbrfn = mbrfn / (double) numberOfElements;

		accWriter.close();

		System.out.println("Accuracy by Points:" + mbrAcc);
		System.out.println("False positive:" + mbrfp);
		System.out.println("False negative:" + mbrfn);
		System.out.println("Outlier Accuracy:" + outlierAcc);
	}

	public static void evaluateByArea(double factor) {

		Path hierarchyFile = Paths.get("100-mbr-pom-geo-old.txt");  //0.188844885703533

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int count = 0;
		double overlapAcc = 0;
		double falseNegative = 0;
		double falsePositive = 0;
		List<PlaceArea> area = new ArrayList();
		Map<String, List<String>> areaDetailMap = new HashMap();
		PrintWriter hieWriter = null;
		try {
			hieWriter = new PrintWriter(new FileWriter("accuracy-by-area-100-mbr-pom-geo-old.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//
		PrintWriter mbrWriter = null;
		try {
			mbrWriter = new PrintWriter(new FileWriter("leftout-mbr.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//
		int fcount = 0;

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			// if(!place_91_1067_Map.containsKey(lineArr[0])){
			// continue;
			// }
			if (lineArr.length < 12) {
				fcount++;
				System.out.println(line);
				continue;
				// System.out.println("break");
			}
			double realswLat = Double.parseDouble(lineArr[4]);
			double realswLong = Double.parseDouble(lineArr[5]);
			double realneLat = Double.parseDouble(lineArr[6]);
			double realneLong = Double.parseDouble(lineArr[7]);
			double divideBy = 0.0;

			realswLat = realswLat - (divideBy / (double) 2);
			realswLong = realswLong - (divideBy / (double) 2);
			;
			realneLat = realneLat + (divideBy / (double) 2);
			realneLong = realneLong + (divideBy / (double) 2);

			double predictedswLat = Double.parseDouble(lineArr[8]);
			double predictedswLong = Double.parseDouble(lineArr[9]);
			double predictedneLat = Double.parseDouble(lineArr[10]);
			double predictedneLong = Double.parseDouble(lineArr[11]);

			boolean altered = false;
			if (predictedswLong < -120 && predictedneLong > 120) {
				altered = true;
				double temp = predictedswLong;
				predictedswLong = predictedneLong;
				predictedneLong = temp;
				System.out.println("longitudes switched.. please verify.." + lineArr[0]);
			}
			double realArea = getAreaByCartesian(realswLat, realswLong, realneLat, realneLong);

			if (predictedneLat == predictedswLat) {
				if (predictedneLong == predictedswLong) {
					System.out.println(lineArr[0]);

					String output = realArea + "\t" + "0.0" + "\t" + "1.0" + "\t" + "0.0" + "\t" + lineArr[1] + "\t"
							+ lineArr[0] + "\t" + lineArr[lineArr.length - 1];
					overlapAcc = overlapAcc + 0.0;
					falseNegative = falseNegative + 1.0;
					falsePositive = falsePositive + 0.0;
					if (!areaDetailMap.containsKey(realArea)) {
						List<String> outputList = new ArrayList();
						outputList.add(output);
						areaDetailMap.put(lineArr[0], outputList);
					} else {
						areaDetailMap.get(lineArr[0]).add(output);
					}
					PlaceArea obj = new PlaceArea();
					obj.setArea(realArea);
					obj.setPlaceId(lineArr[0]);
					area.add(obj);
					count++;
					continue;
				}
			}

			// double realArea = getAreaBySphere(realswLat, realswLong,
			// realneLat, realneLong);
			double predictedArea = getAreaByCartesian(predictedswLat, predictedswLong, predictedneLat, predictedneLong);
			// double predictedArea = getAreaBySphere(predictedswLat,
			// predictedswLong, predictedneLat, predictedneLong);
			if (predictedArea == 0.0) {
				System.out.println("Stop.");
			}
			String iLowerLat = getMaxLat(String.valueOf(realswLat), String.valueOf(predictedswLat));
			String iLowerLong = getMaxLong(String.valueOf(realswLong), String.valueOf(predictedswLong));
			String iUpLat = getMinLat(String.valueOf(realneLat), String.valueOf(predictedneLat));
			String iUpLong = getMinLong(String.valueOf(realneLong), String.valueOf(predictedneLong));

			double iArea = 0.0;

			if (((Double.parseDouble(iLowerLat) > Double.parseDouble(iUpLat)
					|| Double.parseDouble(iLowerLong) > Double.parseDouble(iUpLong)))) {
				if (Double.parseDouble(iLowerLong) > 175 && Double.parseDouble(iLowerLong) < 180) {
					if (Double.parseDouble(iUpLong) > -179.999999 && Double.parseDouble(iUpLong) < -120) {
						iArea = getAreaByCartesian(Double.parseDouble(iLowerLat), Double.parseDouble(iLowerLong),
								Double.parseDouble(iUpLat), Double.parseDouble(iUpLong));

						System.out.println(lineArr[0]);
					} else {
						System.out.println(lineArr[0]);
						iArea = 0.0;
					}
				} else {
					System.out.println(lineArr[0]);
					iArea = 0.0;
				}
			} else {
				iArea = getAreaByCartesian(Double.parseDouble(iLowerLat), Double.parseDouble(iLowerLong),
						Double.parseDouble(iUpLat), Double.parseDouble(iUpLong));

			}

			// if(iArea ==0.0){
			// System.out.println(lineArr[0]);
			// continue;
			// }
			count++;

			mbrWriter.write(line + "\n");
			mbrWriter.flush();

			double ithOverlapAccuracy = (iArea / (realArea + predictedArea - iArea));
			if ((realArea + predictedArea - iArea) == 0) {
				System.out.println("break.");
			}

			overlapAcc = overlapAcc + ithOverlapAccuracy;
			double ithFalseNegative = ((realArea - iArea) / realArea);
			falseNegative = falseNegative + ithFalseNegative;
			double ithFalsePositive = ((predictedArea - iArea) / predictedArea);
			if (predictedArea == 0.0) {
				System.out.println("Error:" + lineArr[1]);
			}
			falsePositive = falsePositive + ithFalsePositive;

			String output = realArea + "\t" + ithOverlapAccuracy + "\t" + ithFalseNegative + "\t" + ithFalsePositive
					+ "\t" + lineArr[1] + "\t" + lineArr[0] + "\t" + lineArr[lineArr.length - 1];
			// "\t"+ lineArr[12] + "\t" + lineArr[13] +
			if (!areaDetailMap.containsKey(realArea)) {
				List<String> outputList = new ArrayList();
				outputList.add(output);
				areaDetailMap.put(lineArr[0], outputList);
			} else {
				areaDetailMap.get(lineArr[0]).add(output);
			}
			PlaceArea obj = new PlaceArea();
			obj.setArea(realArea);
			obj.setPlaceId(lineArr[0]);
			area.add(obj);
		}
		System.out.println("Total size of map:" + areaDetailMap.size());
		System.out.println("area list size:" + area.size());
		Collections.sort(area);
		for (int i = 0; i < area.size(); i++) {
			List<String> placestats = areaDetailMap.get(((PlaceArea) area.get(i)).getPlaceId());
			for (int j = 0; j < placestats.size(); j++) {
				hieWriter.write(placestats.get(j) + "\n");
				hieWriter.flush();
			}
		}
		hieWriter.close();
		mbrWriter.close();
		System.out.println("Total elements over which metric is calculated is:" + count);
		overlapAcc = overlapAcc / (double) count;
		falseNegative = falseNegative / (double) count;
		falsePositive = falsePositive / (double) count;
		System.out.println("Overlap accuracy:" + overlapAcc);
		System.out.println("False positive:" + falsePositive);
		System.out.println("False Negative:" + falseNegative);

		System.out.println("Count:" + count);
		System.out.println("fCount:" + fcount);
		// mbrWriter.close();
	}



	// http://www.pmel.noaa.gov/maillists/tmap/ferret_users/fu_2004/msg00023.html
	// https://badc.nerc.ac.uk/help/coordinates/cell-surf-area.html
	public static double getAreaBySphere(double lat1, double lon1, double lat2, double lon2) {
		double r = 6371.0;
		double h = 0;
		double lat1Radians = Math.toRadians(lat1);
		double lon1Radians = Math.toRadians(lon1);
		double lat2Radians = Math.toRadians(lat2);
		double lon2Radians = Math.toRadians(lon2);
		double longitudeDiff = 0;
		if (lon2Radians > lon1Radians) {
			longitudeDiff = lon2Radians - lon1Radians;
		} else {
			longitudeDiff = lon1Radians - lon2Radians;
		}
		double sinVal = 0;
		if (lat1Radians > lat2Radians) {
			sinVal = Math.sin(lat1Radians) - Math.sin(lat2Radians);
		} else {
			sinVal = Math.sin(lat2Radians) - Math.sin(lat1Radians);
		}

		double surfaceArea = (Math.PI * r * r * longitudeDiff * sinVal) / 180;
		return surfaceArea;
	}

	public static double getDistance(double lat1, double lon1, double lat2, double lon2) {

		// Haversine distance
		double r = 6371.0; // approx. radius of earth in km
		double lat1Radians = (lat1 * Math.PI) / 180.0;
		double lon1Radians = (lon1 * Math.PI) / 180.0;
		double lat2Radians = (lat2 * Math.PI) / 180.0;
		double lon2Radians = (lon2 * Math.PI) / 180.0;
		double d = r * Math.acos((Math.cos(lat1Radians) * Math.cos(lat2Radians) * Math.cos(lon2Radians - lon1Radians)
				+ (Math.sin(lat1Radians) * Math.sin(lat2Radians))));
		return d; // in km

	}

	public static double getAreaByCartesian(double lat1, double lon1, double lat2, double lon2) {
		// lat1-lon1 is the upper-left corner, lat2-lon2 is the lower-right
		if (lat1 == lat2 || lon1 == lon2) {
			return getDistance(lat1, lon1, lat2, lon2);
		}
		double width = getDistance(lat1, lon1, lat2, lon1);
		double length = getDistance(lat1, lon1, lat1, lon2);
		return length * width;
	}

	private static double getArea(String lowerLat, String lowerLong, String upLat, String upLong) {

		GeodesicData g = Geodesic.WGS84.Inverse(Double.parseDouble(lowerLat), Double.parseDouble(lowerLong),
				Double.parseDouble(lowerLat), Double.parseDouble(upLong), GeodesicMask.DISTANCE);
		double length = g.s12;
		g = Geodesic.WGS84.Inverse(Double.parseDouble(lowerLat), Double.parseDouble(upLong), Double.parseDouble(upLat),
				Double.parseDouble(upLong), GeodesicMask.DISTANCE);
		double breadth = g.s12;

		return (length * breadth);
	}

	public static String getMinLat(String lat1, String lat2) {

		double latitude1 = Double.parseDouble(lat1);
		double latitude2 = Double.parseDouble(lat2);
		if (latitude1 < latitude2) {
			return lat1;
		} else {
			return lat2;
		}

	}

	public static String getMaxLat(String lat1, String lat2) {
		double latitude1 = Double.parseDouble(lat1);
		double latitude2 = Double.parseDouble(lat2);
		if (latitude1 > latitude2) {
			return lat1;
		} else {
			return lat2;
		}

	}

	private static String getMinLong(String long1, String long2) {
		double longitude1 = Double.parseDouble(long1);
		double longitude2 = Double.parseDouble(long2);
		if ((longitude1 > 0 && longitude2 < 0) || (longitude1 < 0 && longitude2 > 0)) {
			double modifiedLong1 = longitude1;
			double modifiedLong2 = longitude2;
			if (longitude1 < 0.0) {
				if (longitude1 > -179.9999999 && longitude1 < -150) {
					modifiedLong1 = 180 + (180 - Math.abs(longitude1));
				}
			}
			if (longitude2 < 0.0) {
				if (longitude2 > -179.9999999 && longitude2 < -150) {
					modifiedLong2 = 180 + (180 - Math.abs(longitude2));
				}
			}
			if (modifiedLong1 < modifiedLong2) {
				return long1;
			} else {
				return long2;
			}
		} else {
			if (longitude1 < longitude2) {
				return long1;
			} else {
				return long2;
			}
		}
	}

	private static String getMaxLong(String long1, String long2) {
		double longitude1 = Double.parseDouble(long1);
		double longitude2 = Double.parseDouble(long2);
		if ((longitude1 > 0 && longitude2 < 0) || (longitude1 < 0 && longitude2 > 0)) {
			double modifiedLong1 = longitude1;
			double modifiedLong2 = longitude2;
			if (longitude1 < 0.0) {
				if (longitude1 > -179.9999999 && longitude1 < -150) {
					modifiedLong1 = 180 + (180 - Math.abs(longitude1));
				}
			}
			if (longitude2 < 0.0) {
				if (longitude2 > -179.9999999 && longitude2 < -150) {
					modifiedLong2 = 180 + (180 - Math.abs(longitude2));
				}
			}
			if (modifiedLong1 > modifiedLong2) {
				return long1;
			} else {
				return long2;
			}
		} else {
			if (longitude1 > longitude2) {
				return long1;
			} else {
				return long2;
			}
		}
	}


}

class PlaceArea implements Comparable<Object> {
	String placeId;
	double area;
	long no_of_child;

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public long getNo_of_child() {
		return no_of_child;
	}

	public void setNo_of_child(long no_of_child) {
		this.no_of_child = no_of_child;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	@Override
	public int compareTo(Object o) {
		PlaceArea obj = (PlaceArea) o;
		return Double.compare(obj.getArea(), this.getArea());
	}

}
