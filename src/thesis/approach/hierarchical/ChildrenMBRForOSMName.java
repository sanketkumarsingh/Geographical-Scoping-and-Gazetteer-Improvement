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

public class ChildrenMBRForOSMName {

	/*
	 * Replace your key 
	 * 
	 * 160-osm-childrenmbr.txt, 1500-osm-childrenmbr.txt was created for limited number of places.
	 * 
	 */
	
	public static void main(String[] args) {
		
		String key = "Your API Key for reverse geocoding api.";
		getChildrenMBRForOSMNames(key);
		
	}
	
	public static void getChildrenMBRForOSMNames(String key){

		
//		String key = "Your API Key for reverse geocoding api.";
	

		Path hierarchyFile = Paths.get("osmname-hierarchy.txt");
		// Path hierarchyFile = Paths.get("sample.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("osmnames-children-mbr.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			for (String line : (Iterable<String>) gridlines::iterator) {
				String lineArr[] = line.split("\t");

				String googleMbr = GroundTruthForMBRs.getRealBoundingboxByDivision(lineArr[7], lineArr[8],
						lineArr[9], key);

				if (googleMbr != null && !googleMbr.isEmpty()) {

					String toWrite = lineArr[0] + "\t" + lineArr[7] + "\t" + lineArr[8] + "\t" + lineArr[9]
							+ "\t" + googleMbr + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t"
							+ lineArr[4];
					writer.write(toWrite + "\n");
					writer.flush();
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		writer.close();
	
	}



}
