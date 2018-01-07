package thesis.approach.heuristic.expansion;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import thesis.approach.heuristic.FindOutlierAndCenterIntersection;
import thesis.evaluation.EvaluateHierarchy;


public class EnlargeMBR {

	public static void getEnlargeMBR(String childrenMbrFile, String centerMbrFile, String outlierMbrFile, 
			String intersectionFileName, String outFileName ){
		
		FindOutlierAndCenterIntersection.getOutlierAndCenterIntersectionMBR( centerMbrFile,
				 outlierMbrFile, 
				 intersectionFileName);
		generateEnlargeMBR(childrenMbrFile, intersectionFileName, outFileName);
		
	}
	
	private static void generateEnlargeMBR(String childrenMbrFile, String intersectionFileName, 
			String outFileName){


		Path hierarchyFile = Paths.get(childrenMbrFile);
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
			double area  = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]),
					Double.parseDouble(lineArr[7]));
			placeAreaMap.put(lineArr[0], String.valueOf(area));
		}
		
		hierarchyFile = Paths.get(childrenMbrFile);
		gridlines = null;
		try { 
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> placeChildrenMBRMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			placeChildrenMBRMap.put(lineArr[0], line); // id:area
		}
		

		hierarchyFile = Paths.get(intersectionFileName);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(outFileName, true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			// double realswLat = Double.parseDouble(lineArr[4]);
			// double realswLong = Double.parseDouble(lineArr[5]);
			// double realneLat = Double.parseDouble(lineArr[6]);
			// double realneLong = Double.parseDouble(lineArr[7]);
			double predictedswLat = Double.parseDouble(lineArr[8]);
			double predictedswLong = Double.parseDouble(lineArr[9]);
			double predictedneLat = Double.parseDouble(lineArr[10]);
			double predictedneLong = Double.parseDouble(lineArr[11]);
			if (predictedswLat == predictedneLat) {
				if (predictedswLong == predictedneLong) {
					System.out.println("no intersection area found. writing mbr based on childrenMBR.");
					writer.write(placeChildrenMBRMap.get(lineArr[0]) + "\n");
					writer.flush();
					continue;
				}
			}
			// double realArea =
			// EvaluateHierarchy.getAreaByCartesian(realswLat,realswLong,realneLat,realneLong);
			// double predictedArea =
			// EvaluateHierarchy.getAreaByCartesian(predictedswLat,predictedswLong,predictedneLat,predictedneLong);

			double width = EvaluateHierarchy.getDistance(predictedswLat, predictedswLong, predictedneLat,
					predictedswLong);
			double length = EvaluateHierarchy.getDistance(predictedswLat, predictedswLong, predictedswLat,
					predictedneLong);
			// String iLowerLat = EvaluateHierarchy.getMax(lineArr[4],
			// lineArr[8]);
			// String iLowerLong = EvaluateHierarchy.getMax(lineArr[5],
			// lineArr[9]);
			// String iUpLat = EvaluateHierarchy.getMin(lineArr[6],lineArr[10]);
			// String iUpLong =
			// EvaluateHierarchy.getMin(lineArr[7],lineArr[11]);
			// double iArea = 0.0;
			// if ((Double.parseDouble(iLowerLat) > Double.parseDouble(iUpLat)
			// || Double.parseDouble(iLowerLong) > Double.parseDouble(iUpLong)))
			// {
			// iArea = 0.0;
			// }else{
			// iArea =
			// EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(iLowerLat),Double.parseDouble(iLowerLong),
			// Double.parseDouble(iUpLat),Double.parseDouble(iUpLong));
			// }
			// ax^2+bx+c
			double givenArea = Double.parseDouble(placeAreaMap.get(lineArr[0]));
			double a = 4;
			double b = 2 * (width + length);
			double c = (length * width) - givenArea;

			double temp1 = Math.sqrt(b * b - (4 * a * c));

			double root1 = (-b + temp1) / (2 * a);
			double root2 = (-b - temp1) / (2 * a);

			// double predswlatRadians = (predictedswLat * Math.PI) / 180.0;
			// double longswDistanceAtGivenLat = 111 *
			// Math.cos(predswlatRadians);
			// predictedswLong = predictedswLong - (changeInLength /
			// longswDistanceAtGivenLat);
			//
			// double prednelatRadians = (predictedneLat * Math.PI) / 180.0;
			// double longneDistanceAtGivenLat = 111 *
			// Math.cos(prednelatRadians);
			// predictedneLong = predictedneLong + (changeInLength /
			// longneDistanceAtGivenLat);

			if (root1 < 0 && root2 < 0) {
				// eps is root1
				double eps = Math.abs(root1);
				if (root1 < root2) {
					eps = Math.abs(root2);
				}
				predictedswLat = predictedswLat + (eps / 111);
				predictedneLat = predictedneLat - (eps / 111);
				double predswlatRadians = (predictedswLat * Math.PI) / 180.0;
				double longswDistanceAtGivenLat = 111 * Math.cos(predswlatRadians);
				predictedswLong = predictedswLong + (eps / longswDistanceAtGivenLat);

				double prednelatRadians = (predictedneLat * Math.PI) / 180.0;
				double longneDistanceAtGivenLat = 111 * Math.cos(prednelatRadians);
				predictedneLong = predictedneLong - (eps / longneDistanceAtGivenLat);

				String place = lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t"
						+ lineArr[4] + "\t" + lineArr[5] + "\t" + lineArr[6] + "\t" + lineArr[7] + "\t" + predictedswLat
						+ "\t" + predictedswLong + "\t" + predictedneLat + "\t" + predictedneLong;
				writer.write(place + "\t" + width + "\t" + length + "\t" + root1 + "\t" + root2 + "\n");
			} else {
				double eps = root1;
				if (root1 > 0 && root2 < 0) {
					predictedswLat = predictedswLat - (eps / 111);
					predictedneLat = predictedneLat + (eps / 111);

					double predswlatRadians = (predictedswLat * Math.PI) / 180.0;
					double longswDistanceAtGivenLat = 111 * Math.cos(predswlatRadians);
					predictedswLong = predictedswLong - (eps / longswDistanceAtGivenLat);

					double prednelatRadians = (predictedneLat * Math.PI) / 180.0;
					double longneDistanceAtGivenLat = 111 * Math.cos(prednelatRadians);
					predictedneLong = predictedneLong + (eps / longneDistanceAtGivenLat);
				}
				if (root1 < 0 && root2 > 0) {
					eps = root2;
					predictedswLat = predictedswLat - (eps / 111);
					predictedneLat = predictedneLat + (eps / 111);
					double predswlatRadians = (predictedswLat * Math.PI) / 180.0;
					double longswDistanceAtGivenLat = 111 * Math.cos(predswlatRadians);
					predictedswLong = predictedswLong - (eps / longswDistanceAtGivenLat);
					double prednelatRadians = (predictedneLat * Math.PI) / 180.0;
					double longneDistanceAtGivenLat = 111 * Math.cos(prednelatRadians);
					predictedneLong = predictedneLong + (eps / longneDistanceAtGivenLat);
				}
				if (root1 > 0 && root2 > 0) {
					if (root1 > root2) {
						eps = root1;
					} else {
						eps = root2;
					}
					predictedswLat = predictedswLat - (eps / 111);
					predictedneLat = predictedneLat + (eps / 111);
					double predswlatRadians = (predictedswLat * Math.PI) / 180.0;
					double longswDistanceAtGivenLat = 111 * Math.cos(predswlatRadians);
					predictedswLong = predictedswLong - (eps / longswDistanceAtGivenLat);
					double prednelatRadians = (predictedneLat * Math.PI) / 180.0;
					double longneDistanceAtGivenLat = 111 * Math.cos(prednelatRadians);
					predictedneLong = predictedneLong + (eps / longneDistanceAtGivenLat);
				}

				String place = lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t"
						+ lineArr[4] + "\t" + lineArr[5] + "\t" + lineArr[6] + "\t" + lineArr[7] + "\t" + predictedswLat
						+ "\t" + predictedswLong + "\t" + predictedneLat + "\t" + predictedneLong;
				writer.write(place + "\t" + width + "\t" + length + "\t" + root1 + "\t" + root2 + "\n");
			}

			writer.flush();
		}
		writer.close();

	
		
	}
	
}
