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

public class ScaleMBR {

	public static void getScaleMBR(String childrenMbrFile, String centerMbrFile, String outlierMbrFile,
			String intersectionFileName, String outFileName) {

		FindOutlierAndCenterIntersection.getOutlierAndCenterIntersectionMBR(centerMbrFile, outlierMbrFile,
				intersectionFileName);
		generateScaleMBR(childrenMbrFile, intersectionFileName, outFileName);

	}

	private static void generateScaleMBR(String childrenMbrFile, String intersectionFileName, String outFileName) {

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
			double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
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
			double predictedswLat = Double.parseDouble(lineArr[8]);
			double predictedswLong = Double.parseDouble(lineArr[9]);
			double predictedneLat = Double.parseDouble(lineArr[10]);
			double predictedneLong = Double.parseDouble(lineArr[11]);
			if (predictedswLat == predictedneLat) {
				if (predictedswLong == predictedneLong) {
					System.out.println("no intersection area found. writing mbr based on childrenmbr.");
					writer.write(placeChildrenMBRMap.get(lineArr[0]) + "\n");
					writer.flush();
					continue;
				}
			}

			double predictedArea = EvaluateHierarchy.getAreaByCartesian(predictedswLat, predictedswLong, predictedneLat,
					predictedneLong);
			double width = EvaluateHierarchy.getDistance(predictedswLat, predictedswLong, predictedneLat,
					predictedswLong);
			double length = EvaluateHierarchy.getDistance(predictedswLat, predictedswLong, predictedswLat,
					predictedneLong);

			double scaleFactor = Math.sqrt(Double.parseDouble(placeAreaMap.get(lineArr[0])) / predictedArea);

			if (scaleFactor < 1) {
				double changeInLength = (length - (scaleFactor * length)) / 2;
				double changeInWidth = (width - (scaleFactor * width)) / 2;
				predictedswLat = predictedswLat + (changeInWidth / 111);

				double predswlatRadians = (predictedswLat * Math.PI) / 180.0;
				double longswDistanceAtGivenLat = 111 * Math.cos(predswlatRadians);
				predictedswLong = predictedswLong + (changeInLength / longswDistanceAtGivenLat);

				predictedneLat = predictedneLat - (changeInWidth / 111);

				double prednelatRadians = (predictedneLat * Math.PI) / 180.0;
				double longneDistanceAtGivenLat = 111 * Math.cos(prednelatRadians);
				predictedneLong = predictedneLong - (changeInLength / longneDistanceAtGivenLat);
			} else {
				double changeInLength = ((scaleFactor * length) - length) / 2;
				double changeInWidth = ((scaleFactor * width) - width) / 2;
				predictedswLat = predictedswLat - (changeInWidth / 111);
				predictedneLat = predictedneLat + (changeInWidth / 111);

				double predswlatRadians = (predictedswLat * Math.PI) / 180.0;
				double longswDistanceAtGivenLat = 111 * Math.cos(predswlatRadians);
				predictedswLong = predictedswLong - (changeInLength / longswDistanceAtGivenLat);

				double prednelatRadians = (predictedneLat * Math.PI) / 180.0;
				double longneDistanceAtGivenLat = 111 * Math.cos(prednelatRadians);
				predictedneLong = predictedneLong + (changeInLength / longneDistanceAtGivenLat);
			}
			String place = lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t" + lineArr[4]
					+ "\t" + lineArr[5] + "\t" + lineArr[6] + "\t" + lineArr[7];
			writer.write(place + "\t" + predictedswLat + "\t" + predictedswLong + "\t" + predictedneLat + "\t"
					+ predictedneLong + "\t" + predictedArea + "\t" + scaleFactor + "\n");
			writer.flush();
		}
		writer.close();
	}

}
