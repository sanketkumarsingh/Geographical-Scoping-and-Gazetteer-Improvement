package thesis.approach.hierarchical;

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

import thesis.data.preparation.mbr.GroundTruthForMBRs;

/*
 * if one want to find the childrenMBR for few places in GeoNames, allCountries.txt file 
 * on line number 36, can be replaced with file containing data in the format :
 * place id \t place name \t \t \t latitude \t longitude
 * 
 * Replace key in line number 30
 * 
 * 140-geo-childrenmbr.txt, 540-geo-childrenmbr.txt, 50-geo-childrenmbr.txt was created using this.
 * 
 */

public class ChildrenMBRForGeoName {

	
	public static void getChildrenMBRForGeoNames(String key, int numberOfPlaces){

		Path hierarchyFile = Paths.get("allCountries.txt");
		// Path hierarchyFile = Paths.get("sample.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = 0;
		Map<String, String> geonameIdLineMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			geonameIdLineMap.put(lineArr[0], lineArr[1] + "\t" + lineArr[4] + "\t" + lineArr[5]);
		}

		hierarchyFile = Paths.get("hierarchy-representation-onlygeonames.txt");
		// Path hierarchyFile = Paths.get("sample.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("geonames-children-mbr.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int countPlace = 0;
		try {
			for (String line : (Iterable<String>) gridlines::iterator) {
				
				if(countPlace< numberOfPlaces ){
				String lineArr[] = line.split("\t");
				if(lineArr[1].equals(lineArr[3]) || lineArr[2].equals(lineArr[4])){
					continue;
				}
				String placeDetail[] = geonameIdLineMap.get(lineArr[0]).split("\t");
				String googleMbr = GroundTruthForMBRs.getRealBoundingboxByDivision(placeDetail[0], placeDetail[1],
						placeDetail[2], key);

				
				
				if (googleMbr != null && !googleMbr.isEmpty()) {

					String toWrite = lineArr[0] + "\t" + placeDetail[1] + "\t" + placeDetail[2] + "\t" + placeDetail[3]
							+ "\t" + googleMbr + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t"
							+ lineArr[4];
					writer.write(toWrite + "\n");
					writer.flush();
					countPlace++;
				}
				
				}	
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.close();
	
	}

}
