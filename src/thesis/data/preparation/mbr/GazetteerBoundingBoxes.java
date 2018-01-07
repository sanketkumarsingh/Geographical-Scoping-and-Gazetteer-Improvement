package thesis.data.preparation.mbr;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.geonames.BoundingBox;
import org.geonames.Toponym;
import org.geonames.WebService;

public class GazetteerBoundingBoxes {


	public static void main(String[] args) throws Exception {

		String username = "username" ;// add your username here
		generateBoundingBoxFromGeoNames(username);
		generateBoundingBoxFromOSMNames();
	}

	private static void generateBoundingBoxFromGeoNames(String username) throws NumberFormatException, IOException, Exception {

		WebService.setUserName(username); 

		Path hierarchyFile = Paths.get("540-geo-childrenmbr.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter hieWriter = null;
		try {
			hieWriter = new PrintWriter(new FileWriter("540-geo-geobb-mbr.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			Toponym toponym = WebService.get(Integer.parseInt(lineArr[0]), null, null);
			BoundingBox bb = toponym.getBoundingBox();
			if (bb == null) {
				System.out.println(lineArr[0]);
				continue;
			}
			String toWrite = lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t" + lineArr[4]
					+ "\t" + lineArr[5] + "\t" + lineArr[6] + "\t" + lineArr[7] + "\t" + bb.getSouth() + "\t"
					+ bb.getWest() + "\t" + bb.getNorth() + "\t" + bb.getEast();
			hieWriter.write(toWrite + "\n");
			hieWriter.flush();

		}
		hieWriter.close();

	}

	private static void generateBoundingBoxFromOSMNames() {
		Path hierarchyFile = Paths.get("1500-osm-childrenmbr.txt"); // taking

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {

			e.printStackTrace();
		}
		Map<String, String> iddetailmap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			iddetailmap.put(lineArr[2] + ":" + lineArr[3], line);
		}

		hierarchyFile = Paths.get("planet-latest.tsv"); // taking

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {

			e.printStackTrace();
		}

		PrintWriter mbrWriter = null;
		try {
			mbrWriter = new PrintWriter(new FileWriter("1500-osmname-osmbb-mbr.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		long count = 0;
		int found = 0;
		Map<String, String> result = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			count++;
			if (lineArr.length > 20) {
				String key = lineArr[7] + ":" + lineArr[6];

				if (iddetailmap.containsKey(key)) {
					String mbr[] = iddetailmap.get(key).split("\t");
					String toWrite = mbr[0] + "\t" + mbr[1] + "\t" + mbr[2] + "\t" + mbr[3] + "\t" + mbr[4] + "\t"
							+ mbr[5] + "\t" + mbr[6] + "\t" + mbr[7];
					toWrite = toWrite + "\t" + lineArr[18] + "\t" + lineArr[17] + "\t" + lineArr[20] + "\t"
							+ lineArr[19] + "\t" + lineArr[3] + "\t" + lineArr[0];

					if (lineArr[0].equals(mbr[1])) {
						// mbrWriter.write(toWrite + "\n");
						// mbrWriter.flush();
						result.put(lineArr[0] + ":" + key, toWrite);
						found++;

					} else {
						System.out.println("Wrong for:" + lineArr[0] + ":" + mbr[1]);
					}
				}

			}
			if (count % 1000000 == 0) {
				System.out.println("Processed:" + count);
			}
			if (result.size() == 1500) {
				System.out.println("done.");
				break;
			}
		}
		// mbrWriter.close();

		Iterator it = result.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String value = (String) entry.getValue();
			mbrWriter.write(value + "\n");
			mbrWriter.flush();
		}
		mbrWriter.close();
	}



}
