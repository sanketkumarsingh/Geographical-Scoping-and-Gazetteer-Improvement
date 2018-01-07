package thesis.data.preparation.topologicalconstraints;

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

public class SoftConstraintData {

	
	public static void generateConstraintFileBasedOnAdminLevel() {
		Map<String, String> mbrInfoMap = new HashMap();
		Path hierarchyFile = Paths.get("420-pom-mbr-forapp.txt");
		// Path hierarchyFile = Paths.get("rem-geo-mbr.txt");

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

			// if (lineArr[1].equals(lineArr[3]) ||
			// lineArr[2].equals(lineArr[4])) {
			// continue;
			// }

			mbrInfoMap.put(lineArr[0], lineArr[4] + "\t" + lineArr[5] + "\t" + lineArr[6] + "\t" + lineArr[7]);
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
			mbrInfoMap.put(lineArr[0], lineArr[4] + "\t" + lineArr[5] + "\t" + lineArr[6] + "\t" + lineArr[7]);
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

			mbrInfoMap.put(lineArr[0], lineArr[1] + "\t" + lineArr[2] + "\t" + lineArr[3] + "\t" + lineArr[4]);
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

		PrintWriter adm1writer = null;
		try {
			adm1writer = new PrintWriter(new FileWriter("constraint-ADM1-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter adm2writer = null;
		try {
			adm2writer = new PrintWriter(new FileWriter("constraint-ADM2-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter adm3writer = null;
		try {
			adm3writer = new PrintWriter(new FileWriter("constraint-ADM3-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter adm4writer = null;
		try {
			adm4writer = new PrintWriter(new FileWriter("constraint-ADM4-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (mbrInfoMap.containsKey(lineArr[0])) {
				String value = mbrInfoMap.get(lineArr[0]);
				if (lineArr[7].equals("ADM1")) {
					adm1writer.write(lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[4] + "\t" + lineArr[5] + "\t"
							+ value + "\t" + lineArr[7] + "\n");
					adm1writer.flush();
				} else if (lineArr[7].equals("ADM2")) {
					adm2writer.write(lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[4] + "\t" + lineArr[5] + "\t"
							+ value + "\t" + lineArr[7] + "\n");
					adm2writer.flush();
				} else if (lineArr[7].equals("ADM3")) {
					adm3writer.write(lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[4] + "\t" + lineArr[5] + "\t"
							+ value + "\t" + lineArr[7] + "\n");
					adm3writer.flush();
				} else {
					adm4writer.write(lineArr[0] + "\t" + lineArr[1] + "\t" + lineArr[4] + "\t" + lineArr[5] + "\t"
							+ value + "\t" + lineArr[7] + "\n");
					adm4writer.flush();
				}
			}
		}
		adm1writer.close();
		adm2writer.close();
		adm3writer.close();
		adm4writer.close();

	}
}
