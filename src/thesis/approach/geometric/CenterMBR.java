package thesis.approach.geometric;

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

import thesis.evaluation.EvaluateHierarchy;


/*
 * Run EvaluateHierarchy.evaluateByArea for 140-geo-childrenmbr.txt to get the file
 * accuracy-by-area-140-geo-childrenmbr.txt
 */

public class CenterMBR {

	public static void getCenterMBR(String childrenMbrFile) {

		// Path hierarchyFile = Paths.get("16120-child-area.txt");

		Path hierarchyFile = Paths.get(childrenMbrFile); // taking
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
			double area  = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]),
					Double.parseDouble(lineArr[6]));
			placeAreaMap.put(lineArr[0], String.valueOf(area)); // id:area
		}


		hierarchyFile = Paths.get(childrenMbrFile);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("geo-centermbr.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (placeAreaMap.containsKey(lineArr[0])) {
				String coordinateByArea = getCoverageCoordinates(lineArr[2] + ":" + lineArr[3],
						placeAreaMap.get(lineArr[0]), 0.0);
				
				if(coordinateByArea.isEmpty()){
					System.out.println(lineArr[0]);
					continue;
				}
				String place = lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t"
						+ lineArr[4] + "\t" + lineArr[5] + "\t" + lineArr[6] + "\t" + lineArr[7] + "\t"
						+ coordinateByArea; // id-place-real coord-predicted
											// coord
				writer.write(place + "\n");

				writer.flush();
			} else {
				System.out.println(lineArr[0] + "not found.");
			}
		}
		writer.close();

	
	}
	
	private static String getCoverageCoordinates(String idCoordinate, String areaInKm , double spreadness) {
		String strArr[] = idCoordinate.split(":");
		String id = strArr[0];
		Double lat = Double.parseDouble(strArr[0]);
		Double longitude = Double.parseDouble(strArr[1]);
		Double lenOfSquare = 0.0;
		if(spreadness == 0){
			 lenOfSquare = Math.sqrt(Double.parseDouble(areaInKm) );
		}else{
			 lenOfSquare = Math.sqrt(Double.parseDouble(areaInKm) / spreadness );
		}
		//Double lenOfSquare = Math.sqrt(Double.parseDouble(areaInKm) / spreadness );
		Double degreeToChangeForlat = (lenOfSquare / (2* 111));  // distance between two latitude is approx 111km everywhere.
		Double swLat = lat - degreeToChangeForlat;
		Double neLat = lat + degreeToChangeForlat;
		double swlatRadians = (swLat * Math.PI) / 180.0;
		double swlongDistanceAtGivenLat = 111 * Math.cos(swlatRadians); // distance between two longitude at given latitude.
		double swdegreeToChangeForLong =0;
		if(spreadness == 0){
			swdegreeToChangeForLong = ((lenOfSquare )/ (2*swlongDistanceAtGivenLat));
		}else{
			swdegreeToChangeForLong = ((lenOfSquare * spreadness)/ (2*swlongDistanceAtGivenLat));
		}
		double nelatRadians = (neLat * Math.PI) / 180.0;
		double nelongDistanceAtGivenLat = 111 * Math.cos(nelatRadians);
		double nedegreeToChangeForLong =0;
		if(spreadness == 0){
			nedegreeToChangeForLong = ((lenOfSquare )/ (2*nelongDistanceAtGivenLat));
		}else{
			nedegreeToChangeForLong = ((lenOfSquare * spreadness)/ (2*nelongDistanceAtGivenLat));
		}
		
		double neLong = longitude + nedegreeToChangeForLong ;
	    double swLong = longitude - swdegreeToChangeForLong;
		
		boolean wrong = false;
		if (swLat < -90 || swLat > 90) {
			wrong = true;
			System.out.println("Wrong swLat..");
		}
		
		if (neLat < -90 || neLat > 90) {
			wrong = true;
			System.out.println("Wrong neLat..");
		}
		
		if (neLong < -180 || neLong > 180) {
			
			if(neLong<-180){
				
				neLong = neLong+180;
				neLong = neLong + 180;
			} else if(neLong>180){
				neLong = neLong -180;
				neLong = neLong - 180;
			}
			
			wrong = true;
			System.out.println("Wrong neLong..");
		}
		
		
		if (swLong < -180 || swLong > 180) {
			wrong = true;
			System.out.println("Wrong swLong..");
			

			if(swLong<-180){
				
				swLong = swLong+180;
				swLong = swLong + 180;
			} else if(swLong>180){
				swLong = swLong -180;
				swLong = swLong -180;
			}
		}
//		if (wrong) {
//			return "";
//		}
		return swLat + "\t" + swLong + "\t" + neLat +  "\t" + neLong;
	}

	
}
