package thesis.data.preparation.enrichment.mbr;

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

import thesis.data.entity.MBR;

public class ImageMBRMap {

	public static void main(String[] args) {
		generateImageMbrFile("test-yfcc-mediaeval.txt"); // This is run just
															// once and then
															// remove the
		// test instances and comment it.
		generateImageMbrFile("train-yfcc-mediaeval.txt");

	}

	private static void generateImageMbrFile(String fileName) {
		// find all MBR with some geographic extent
		// Map<String, MBR> mbrIdMbrMap = new HashMap();
		List<MBR> mbrList = new ArrayList();
		Path hierarchyFile = Paths.get("420-pom-mbr-forapp.txt"); // taking
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

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (lineArr[4].equals(lineArr[6]) || lineArr[5].equals(lineArr[7])) {
				continue;
			}
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[4]));
			mbr.setSwLong(Double.parseDouble(lineArr[5]));
			mbr.setNeLat(Double.parseDouble(lineArr[6]));
			mbr.setNeLong(Double.parseDouble(lineArr[7]));
			// double area =
			// EvaluateHierarchy.getAreaByCartesian(mbr.getSwLat(),mbr.getSwLong(),
			// mbr.getNeLat(), mbr.getNeLong());
			// mbr.setArea(area);
			mbrList.add(mbr);
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
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[4]));
			;
			mbr.setSwLong(Double.parseDouble(lineArr[5]));
			mbr.setNeLat(Double.parseDouble(lineArr[6]));
			mbr.setNeLong(Double.parseDouble(lineArr[7]));
			// double area =
			// EvaluateHierarchy.getAreaByCartesian(mbr.getSwLat(),mbr.getSwLong(),
			// mbr.getNeLat(), mbr.getNeLong());
			// mbr.setArea(area);
			mbrList.add(mbr);
		}

		hierarchyFile = Paths.get("14215-childrenmbr.txt");
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
			MBR mbr = new MBR();
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[1]));
			mbr.setSwLong(Double.parseDouble(lineArr[2]));
			mbr.setNeLat(Double.parseDouble(lineArr[3]));
			mbr.setNeLong(Double.parseDouble(lineArr[4]));
			// double area =
			// EvaluateHierarchy.getAreaByCartesian(mbr.getSwLat(),mbr.getSwLong(),
			// mbr.getNeLat(), mbr.getNeLong());
			// mbr.setArea(area);
			mbrList.add(mbr);
		}
		System.out.println("Loaded all locations.." + mbrList.size());

		Collections.sort(mbrList); // sort by swLong ascending

		// Loading the Images and // forming : image \t mbrids
		hierarchyFile = Paths.get(fileName);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> totalMapMapped = new HashMap();

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("image-mbrids-test-mediaeval-allmap-new.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		int count = 0;

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split(":");

			// if(lineArr[1].equals("5717949576")){
			// System.out.println("break");
			// }
			// long startTime = System.currentTimeMillis();
			if (lineArr.length == 8) {
				if (lineArr[7].isEmpty() && (lineArr[6].isEmpty() && lineArr[5].isEmpty())) {
					continue;
				}
			}
			if (lineArr.length == 7) {

				if (lineArr[6].isEmpty() && lineArr[5].isEmpty()) {
					continue;
				}
			}

			if (lineArr.length == 6) {

				if (lineArr[5].isEmpty()) {
					continue;
				}
			}

			double imageLat = Double.parseDouble(lineArr[3]);
			double imageLong = Double.parseDouble(lineArr[4]);
			//
			// if(lineArr[1].equals("3304610774")){
			// System.out.println("break");
			// }
			//
			// if(lineArr[1].equals("5780747158")){
			// System.out.println("break");
			// }
			// int index = 0;
			// int index = getCandidateMbrIndex(mbrList, imageLat, imageLong, 0,
			// mbrList.size() - 1);
			//
			// if (index == -1) {
			// continue;
			// }
			StringBuffer mbrids = new StringBuffer();
			StringBuffer result = new StringBuffer();
			result.append(line).append(":");
			boolean notFound = true;
			for (int i = 0; i < mbrList.size(); i++) {
				MBR mbr = mbrList.get(i);
				if (imageLong < mbr.getSwLong()) {
					break;
				}
				if (isMbrContainImage(mbr, imageLat, imageLong)) {
					notFound = false;
					if (mbrids.toString().isEmpty()) {
						totalMapMapped.put(mbr.getId(), null);
						mbrids.append(mbr.getId());
					} else {
						totalMapMapped.put(mbr.getId(), null);
						mbrids.append(" ").append(mbr.getId());
					}

				}
				if (imageLong < mbr.getSwLong()) {
					break;
				}
			}

			if (notFound) {
				continue;
			}

			result.append(mbrids.toString());
			writer.write(result.toString() + "\n");
			writer.flush();

			// long endTime = System.currentTimeMillis();
			// System.out.println(endTime - startTime);
			count++;
			if (count % 100000 == 0) {

				System.out.println(("Processed" + count));
			}
		}

		System.out.println("Total MBRs mapped:" + totalMapMapped);
		writer.close();
	}

	private static boolean isMbrContainImage(MBR mbr, double imageLat, double imageLong) {
		if (imageLat >= mbr.getSwLat() && imageLat <= mbr.getNeLat()) {
			if (imageLong >= mbr.getSwLong() && imageLong <= mbr.getNeLong()) {
				return true;
			}
		}
		return false;
	}

	private static int getCandidateMbrIndex(List<MBR> mbrList, double imageLat, double imageLong, int low, int high) {
		// TODO Auto-generated method stub
		while (low < high) {
			int middle = (low + high) / 2;
			MBR mbr = mbrList.get(middle);
			if (mbr.getSwLong() == imageLong) {
				// double prevMbr = mbr.getSwLong();
				while (middle > 0 && imageLong >= mbr.getSwLong() && imageLong <= mbr.getNeLong()) {
					// prevMbr = mbr.getSwLong();
					middle = middle - 1;
					mbr = mbrList.get(middle);
				}
				return middle + 1;
			}
			int middleMinusOne = middle - 1;
			int middlePlusOne = middle + 1;
			if (middlePlusOne < mbrList.size()) {
				if (imageLong <= mbrList.get(middlePlusOne).getSwLong()
						&& imageLong >= mbrList.get(middle).getSwLong()) {
					// double prevMbr = mbr.getSwLong();
					while (middle > 0 && imageLong >= mbr.getSwLong() && imageLong <= mbr.getNeLong()) {
						// prevMbr = mbr.getSwLong();
						middle = middle - 1;
						mbr = mbrList.get(middle);

					}
					return middle + 1;
				}
			}

			if (middleMinusOne >= 0) {
				if (imageLong <= mbrList.get(middle).getSwLong()
						&& imageLong >= mbrList.get(middleMinusOne).getSwLong()) {
					mbr = mbrList.get(middleMinusOne);
					// double prevMbr = mbr.getSwLong();
					while (middleMinusOne > 0 && imageLong >= mbr.getSwLong() && imageLong <= mbr.getNeLong()) {
						// prevMbr = mbr.getSwLong();
						middleMinusOne = middleMinusOne - 1;
						mbr = mbrList.get(middleMinusOne);

					}
					return middleMinusOne + 1;
					// return middleMinusOne;
				}
			}

			if (imageLong > mbrList.get(middlePlusOne).getSwLong()) {
				low = middlePlusOne;
			}

			if (imageLong < mbrList.get(middleMinusOne).getSwLong()) {
				high = middleMinusOne;
			}

		}

		return -1;
	}

	private static void generateTestAsGrid() {
		Path hierarchyFile = Paths.get("test-gridids-result-allmap-0.3-user-normal-100-smoothing-grid.txt");

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> gridMap = new HashMap();

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split(":");
			gridMap.put(lineArr[0], lineArr[lineArr.length - 1]);
		}

		hierarchyFile = Paths.get("test-mbr-result-user-normalization-100-smoothing-newmbr.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		// PrintWriter mbrwriter = null;
		// try {
		// mbrwriter = new PrintWriter(new
		// FileWriter("image-mbrids-test-mediaeval-allmap-new5L.txt", true));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		int total = 0;
		double mbrdistance = 0.0;
		double griddistance = 0.0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split(":");
			if (gridMap.containsKey(lineArr[0])) {
				total++;
				mbrdistance = mbrdistance + Double.parseDouble(lineArr[lineArr.length - 1]);
				griddistance = griddistance + Double.parseDouble(gridMap.get(lineArr[0]));
			}
		}

		double avgAdeMbr = mbrdistance / (double) total;
		double avgAdegrid = griddistance / (double) total;
		System.out.println("avgAdeMbr:" + avgAdeMbr);
		System.out.println("avgAdegrid:" + avgAdegrid);

		// mbrwriter.close();
		//
		// mbrwriter = null;
		// try {
		// mbrwriter = new PrintWriter(new
		// FileWriter("test-mbr-prediction-user-normal-200-new5L.txt", true));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// hierarchyFile =
		// Paths.get("test-mbr-prediction-user-normal-200-new.txt");
		//
		// gridlines = null;
		// try {
		// gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// for (String line : (Iterable<String>) gridlines::iterator) {
		// String lineArr[] = line.split(":");
		// if(mbrsMap.containsKey(lineArr[0])){
		// mbrwriter.write(line+"\n");
		// mbrwriter.flush();
		// }
		// }
		// mbrwriter.close();
		//
	}

	private static int countMbrCoverageInTrain() {
		Path hierarchyFile = Paths.get("image-gridids-train-mediaeval-allmap-0.3.txt");

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> mbrsMap = new HashMap();

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split(":");
			String mbrs[] = lineArr[lineArr.length - 1].split(" ");
			for (int i = 0; i < mbrs.length; i++) {
				mbrsMap.put(mbrs[i], null);
			}
		}

		return mbrsMap.size();
	}
}
