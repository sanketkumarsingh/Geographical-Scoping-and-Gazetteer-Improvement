package thesis.approach.heuristic;

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

/*
 * Change the filename for OSMNames and use filename specific to outlier detection used.
 * 
 */
public class FindOutlierAndCenterIntersection {

	public static void getOutlierAndCenterIntersectionMBR(String centerMbrFile, String outlierMbrFile, 
			String intersectionFileName) {

		// Loading coordinate by area method:
		Path hierarchyFile = Paths.get(centerMbrFile);
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> idBoundaryDetail = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			String areaBB = lineArr[8] + "\t" + lineArr[9] + "\t" + lineArr[10] + "\t" + lineArr[11];// +"\t"
			// + lineArr[12];
			idBoundaryDetail.put(lineArr[0], areaBB);
		}

		// iterating over bb of given method mbr and creating bb of intersection
		// of area and
		// geo for 611 instances.
		hierarchyFile = Paths.get(outlierMbrFile);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(intersectionFileName, true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (idBoundaryDetail.containsKey(lineArr[0]) && lineArr.length >= 12) {
				String areaBB = idBoundaryDetail.get(lineArr[0]);
				String areaCoord[] = areaBB.split("\t");
				String swLat = lineArr[8];
				String swLong = lineArr[9];
				String neLat = lineArr[10];
				String neLong = lineArr[11];
				if (Double.parseDouble(areaCoord[0]) > Double.parseDouble(swLat)) {
					swLat = areaCoord[0];
				}
				if (Double.parseDouble(areaCoord[1]) > Double.parseDouble(swLong)) {
					swLong = areaCoord[1];
				}
				if (Double.parseDouble(areaCoord[2]) < Double.parseDouble(neLat)) {
					neLat = areaCoord[2];
				}
				if (Double.parseDouble(areaCoord[3]) < Double.parseDouble(neLong)) {
					neLong = areaCoord[3];
				}

				if (Double.parseDouble(swLat) > Double.parseDouble(neLat)
						|| Double.parseDouble(swLong) > Double.parseDouble(neLong)) {
					swLat = lineArr[2];
					neLat = lineArr[2];
					swLong = lineArr[3];
					neLong = lineArr[3];
				}

				String toWrite = lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t"
						+ lineArr[4] + "\t" + lineArr[5] + "\t" + lineArr[6] + "\t" + lineArr[7] + "\t" + swLat + "\t"
						+ swLong + "\t" + neLat + "\t" + neLong;// + "\t"
																// +areaCoord[4];

				writer.write(toWrite + "\n");
				writer.flush();
			}
		}
		writer.close();

	
	}
	
}
