package thesis.applications.topologicalconstraints;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import thesis.data.entity.MBR;
import thesis.data.entity.MBRInfo;
import thesis.evaluation.EvaluateHierarchy;

public class SoftConstraintStats {

	public static void generateStatsForSoftConstraint(String constraintInputFile, String adminLevel, String outputFileName) {

		getConstraintStats(constraintInputFile, adminLevel, outputFileName);
	}

	private static void getConstraintStats(String constraintInputFile, String adminLevel, String outputFileName) {

		Map<String, MBRInfo> mbrInfoMap = new HashMap();
		Path hierarchyFile = Paths.get("420-pom-mbr-forapp.txt");

		Stream<String> gridlines = null;
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
			MBRInfo mbrInfo = new MBRInfo();
			double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));
			mbrInfo.setArea(area);
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[4]));
			mbr.setSwLong(Double.parseDouble(lineArr[5]));
			mbr.setNeLat(Double.parseDouble(lineArr[6]));
			mbr.setNeLong(Double.parseDouble(lineArr[7]));
			mbrInfo.setMbr(mbr);

			mbrInfoMap.put(lineArr[0], mbrInfo);
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
			MBRInfo mbrInfo = new MBRInfo();
			double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));
			mbrInfo.setArea(area);
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[4]));
			mbr.setSwLong(Double.parseDouble(lineArr[5]));
			mbr.setNeLat(Double.parseDouble(lineArr[6]));
			mbr.setNeLong(Double.parseDouble(lineArr[7]));
			mbrInfo.setMbr(mbr);

			mbrInfoMap.put(lineArr[0], mbrInfo);
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
			MBRInfo mbrInfo = new MBRInfo();
			double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[1]),
					Double.parseDouble(lineArr[2]), Double.parseDouble(lineArr[3]), Double.parseDouble(lineArr[4]));
			mbrInfo.setArea(area);

			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[1]));
			mbr.setSwLong(Double.parseDouble(lineArr[2]));
			mbr.setNeLat(Double.parseDouble(lineArr[3]));
			mbr.setNeLong(Double.parseDouble(lineArr[4]));
			mbrInfo.setMbr(mbr);
			mbrInfoMap.put(lineArr[0], mbrInfo);
		}
		System.out.println("Loaded all locations.." + mbrInfoMap.size());

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
			if (mbrInfoMap.containsKey(lineArr[0])) {
				mbrInfoMap.get(lineArr[0]).setLevel(lineArr[7]);
			}
		}

		hierarchyFile = Paths.get(constraintInputFile);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter mbrwriter = null;
		try {
			mbrwriter = new PrintWriter(
					new FileWriter(outputFileName, true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// double totalAvg = 0.0;
		int count = 0;
		double minOverlapArea = Double.POSITIVE_INFINITY;
		double maxOverLapArea = Double.NEGATIVE_INFINITY;
		// int totalOverallPlace = 0;
		// List<Double> placeStds = new ArrayList();
		List<Double> placeMeans = new ArrayList();
		int x = 0;
		// List<Integer> totalItemForPlace = new ArrayList();
		for (String line : (Iterable<String>) gridlines::iterator) {
			if (x < 2000) {
				x++;
				continue;
			}
			String lineArr[] = line.split("\t");

			// if(!placeWhichHaveOverlapMap.containsKey(lineArr[0])){
			// continue;
			// }
			double minOverlapAreaForPlace = Double.POSITIVE_INFINITY;
			double maxOverLapAreaForPlace = Double.NEGATIVE_INFINITY;
			Iterator it = mbrInfoMap.entrySet().iterator();
			double totalCurrPlaceOverLapArea = 0;
			int totalPlaceForOverLapAreaForCurrPlace = 0;
			double placeArea = getAreaByCartesian(Double.parseDouble(lineArr[4]), Double.parseDouble(lineArr[5]),
					Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));
			List<Double> overlapForPlace = new ArrayList();
			double maxArea = placeArea;

			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				String placeId = (String) entry.getKey();
				if (placeId.equals(lineArr[0])) {
					continue;
				}
				MBRInfo mbrInfo = (MBRInfo) entry.getValue();
				if (!mbrInfo.getLevel().equals(adminLevel)) {
					continue;
				}

				// Set<String> parentSet =
				// placeIdPCidMap.get(lineArr[0]).getAscendants();
				// if (parentSet.contains(placeId)) {
				// continue;
				// }
				//
				// Set<String> childSet =
				// placeIdPCidMap.get(lineArr[0]).getAscendants();
				// if (childSet.contains(placeId)) {
				// continue;
				// }

				if (isParent(mbrInfo, lineArr)) {
					continue;
				}

				if (isChild(mbrInfo, lineArr)) {
					continue;
				}

				double area = getIntersectionArea(mbrInfo, lineArr, placeArea);
				// if(iArea>area){
				// continue;
				// }
				// get intersection of mbrInfo and lineArr

				// find area of intersection region

				if (area > 0) {
					totalPlaceForOverLapAreaForCurrPlace++;
					if (maxArea < area) {
						maxArea = area;
					}
					if (lineArr[0].equals("3193160")) {
						System.out.println("break..");
					}

					totalCurrPlaceOverLapArea = totalCurrPlaceOverLapArea + area;
					minOverlapAreaForPlace = Math.min(minOverlapAreaForPlace, area);
					maxOverLapAreaForPlace = Math.max(maxOverLapAreaForPlace, area);
					overlapForPlace.add(area);
				}
			}

			if (totalPlaceForOverLapAreaForCurrPlace == 0) {
				continue;
			}

			double avgOverlapAreaForPlaceNormalizedByItsArea = 0.0;
			avgOverlapAreaForPlaceNormalizedByItsArea = totalCurrPlaceOverLapArea
					/ (double) totalPlaceForOverLapAreaForCurrPlace;
			avgOverlapAreaForPlaceNormalizedByItsArea = avgOverlapAreaForPlaceNormalizedByItsArea / placeArea;
			placeMeans.add(avgOverlapAreaForPlaceNormalizedByItsArea);

			count++;
			mbrwriter.write(lineArr[0] + "\t" + avgOverlapAreaForPlaceNormalizedByItsArea + "\t" + placeArea + "\t"
					+ totalPlaceForOverLapAreaForCurrPlace + "\t" + minOverlapAreaForPlace + "\t"
					+ maxOverLapAreaForPlace + "\t" + maxArea + "\n");
			mbrwriter.flush();
			minOverlapArea = Math.min(minOverlapArea, minOverlapAreaForPlace);
			maxOverLapArea = Math.max(maxOverLapArea, maxOverLapAreaForPlace);

			// find average area inter for the place..
			// print the place id, avg area , number of place over which avg is
			// taken..

			// sum the avg area for the places
			if (count == 2000) {
				break;
			}
			if (count % 100 == 0) {
				System.out.println("Processed:" + count);
			}
		}
		mbrwriter.close();

		double finalAvg = 0.0;
		for (int i = 0; i < placeMeans.size(); i++) {
			finalAvg = finalAvg + placeMeans.get(i);
		}

		finalAvg = finalAvg / (double) placeMeans.size();

		double numerator = 0.0;
		for (int i = 0; i < placeMeans.size(); i++) {
			// numerator = numerator + totalItemForPlace.get(i) *
			// Math.pow(placeStds.get(i), 2)
			// + totalItemForPlace.get(i) * Math.pow((placeMeans.get(i) -
			// finalAvg), 2);
			numerator = numerator + Math.pow((placeMeans.get(i) - finalAvg), 2);
		}

		double std = Math.sqrt((numerator / (double) (placeMeans.size() - 1)));

		System.out.println("finalAvg:" + finalAvg + " count:" + placeMeans.size());
		System.out.println("minOverlapArea: " + minOverlapArea);
		System.out.println("maxOverLapArea: " + maxOverLapArea);
		System.out.println("standard deviation: " + std);

		// take average and report the average area over 1000 places.

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

	private static boolean isChild(MBRInfo mbrInfo, String[] lineArr) {

		MBR childmbr = new MBR();
		childmbr.setSwLat(Double.parseDouble(lineArr[4]));
		childmbr.setSwLong(Double.parseDouble(lineArr[5]));
		childmbr.setNeLat(Double.parseDouble(lineArr[6]));
		childmbr.setNeLong(Double.parseDouble(lineArr[7]));
		return isParentMbrContains(childmbr, mbrInfo.getMbr());
	}

	private static boolean isParent(MBRInfo mbrInfo, String[] lineArr) {

		MBR childmbr = new MBR();
		childmbr.setSwLat(Double.parseDouble(lineArr[4]));
		childmbr.setSwLong(Double.parseDouble(lineArr[5]));
		childmbr.setNeLat(Double.parseDouble(lineArr[6]));
		childmbr.setNeLong(Double.parseDouble(lineArr[7]));
		return isParentMbrContains(mbrInfo.getMbr(), childmbr);
	}

	private static double getIntersectionArea(MBRInfo mbrInfo, String[] lineArr, double placeArea) {

		double neLat1 = mbrInfo.getMbr().getNeLat();
		double swLat1 = mbrInfo.getMbr().getSwLat();
		double neLat2 = Double.parseDouble(lineArr[6]);
		double swLat2 = Double.parseDouble(lineArr[4]);

		if (neLat1 > neLat2 && swLat1 > neLat2) {
			return 0;
		}
		if (neLat1 < swLat2 && swLat1 < swLat2) {
			return 0;
		}

		String iLowerLat = getMaxLat(String.valueOf(mbrInfo.getMbr().getSwLat()), String.valueOf(lineArr[4]));
		String iLowerLong = getMaxLong(String.valueOf(mbrInfo.getMbr().getSwLong()), String.valueOf(lineArr[5]));
		String iUpLat = getMinLat(String.valueOf(mbrInfo.getMbr().getNeLat()), String.valueOf(lineArr[6]));
		String iUpLong = getMinLong(String.valueOf(mbrInfo.getMbr().getNeLong()), String.valueOf(lineArr[7]));
		double iArea = 0.0;
		if (((Double.parseDouble(iLowerLat) > Double.parseDouble(iUpLat)
				|| Double.parseDouble(iLowerLong) > Double.parseDouble(iUpLong)))) {
			if (Double.parseDouble(iLowerLong) > 175 && Double.parseDouble(iLowerLong) < 180) {
				if (Double.parseDouble(iUpLong) > -179.999999 && Double.parseDouble(iUpLong) < -120) {
					iArea = getAreaByCartesian(Double.parseDouble(iLowerLat), Double.parseDouble(iLowerLong),
							Double.parseDouble(iUpLat), Double.parseDouble(iUpLong));

					// System.out.println(lineArr[0]);
				} else {
					// System.out.println(lineArr[0]);
					iArea = 0.0;
				}
			} else {
				// System.out.println(lineArr[0]);
				iArea = 0.0;
			}
		} else {
			iArea = getAreaByCartesian(Double.parseDouble(iLowerLat), Double.parseDouble(iLowerLong),
					Double.parseDouble(iUpLat), Double.parseDouble(iUpLong));

		}

		if (iArea > placeArea) {
			return 0;
		}
		return iArea;
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

	public static String getMinLat(String lat1, String lat2) {

		double latitude1 = Double.parseDouble(lat1);
		double latitude2 = Double.parseDouble(lat2);
		if (latitude1 < latitude2) {
			return lat1;
		} else {
			return lat2;
		}
		// double modifiedLat1 = latitude1;
		// double modifiedLat2= latitude2;
		// if(latitude1 < 0.0){
		// modifiedLat1 = 180 + (180 - Math.abs(latitude1));
		// }
		// if(latitude2 < 0.0){
		// modifiedLat2 = 180 + (180 - Math.abs(latitude2));
		// }
		// if (modifiedLat1 > modifiedLat2) {
		// return lat2;
		// } else {
		// return lat1;
		// }
	}

	public static String getMaxLat(String lat1, String lat2) {
		double latitude1 = Double.parseDouble(lat1);
		double latitude2 = Double.parseDouble(lat2);
		if (latitude1 > latitude2) {
			return lat1;
		} else {
			return lat2;
		}
		// double modifiedLong1 = longitude1;
		// double modifiedLong2= longitude2;
		// if(longitude1 < 0.0){
		// modifiedLong1 = 180 + (180 - Math.abs(longitude1));
		// }
		// if(longitude2 < 0.0){
		// modifiedLong2 = 180 + (180 - Math.abs(longitude2));
		// }
		// if (modifiedLong1 > modifiedLong2) {
		// return long1;
		// } else {
		// return long2;
		// }
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
