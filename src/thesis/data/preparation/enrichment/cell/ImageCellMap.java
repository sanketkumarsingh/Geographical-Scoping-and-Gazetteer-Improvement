package thesis.data.preparation.enrichment.cell;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import thesis.data.entity.MBR;

public class ImageCellMap {

	public static void main(String[] args) {
		try {
		generateImageGridFile("test-yfcc-mediaeval.txt"); // This is run just
															// once and then
															// remove the
		// test instances and comment it.
		
			generateImageGridFile("train-yfcc-mediaeval.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	private static void generateImageGridFile(String fileName) throws IOException {
		// find all MBR with some geographic extent
		// Map<String, MBR> mbrIdMbrMap = new HashMap();
		List<MBR> gridList = new ArrayList();
		Path hierarchyFile = Paths.get("0.3Grid.txt"); // taking 1.0 degree grid
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

			MBR mbr = new MBR(); // consider each MBR as grid..
			mbr.setId(lineArr[0]);
			mbr.setSwLat(Double.parseDouble(lineArr[1]));
			mbr.setSwLong(Double.parseDouble(lineArr[2]));
			mbr.setNeLat(Double.parseDouble(lineArr[3]));
			mbr.setNeLong(Double.parseDouble(lineArr[4]));
			gridList.add(mbr);
		}

		System.out.println("Loaded all locations.." + gridList.size());

		Collections.sort(gridList); // sort by swLong ascending

		// Loading the Images and // forming : image \t mbrids
		hierarchyFile = Paths.get(fileName);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("image-gridids-test-mediaeval-raw-0.3.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// PrintWriter gtWriter = new PrintWriter(new
		// FileWriter("correct-test-yfcc-mediaeval.txt", true));

		int count = 0;
		int notFoundCount = 0;
		double degree = 0.3;
		int totalLong = 1200; // change it based on degree...
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split(":");
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

			StringBuffer mbrids = new StringBuffer();
			StringBuffer result = new StringBuffer();
			result.append(line).append(":");
			// result.append(lineArr[0]).append(":").append(lineArr[1]).append(":").append(lineArr[2]).append(":")
			// .append(lineArr[3]).append(":").append(lineArr[4]).append(":").append(lineArr[5]).append(":")
			// .append(lineArr[6]).append(":").append(lineArr[7]);

			// gtWriter.write(result.toString()+"\n");
			// gtWriter.flush();
			// append(str)
			// result.append(":");
//			imageLat = -0.8;
//			imageLong = -179.6;
			if (imageLong >= -180 + degree) {
				//
				// if (lineArr[1].equals("197667621")) {
				// System.out.println("break");
				//
				// }

				int index = getCandidateMbrIndex(gridList, imageLat, imageLong, 0, gridList.size() - 1);

				if (index == -1) {
					// notFoundCount++;
					// System.out.println("break;");
					continue;
				}

				boolean notFound = true;
				for (int i = index; i < gridList.size(); i++) {
					MBR mbr = gridList.get(i);

					if (isMbrContainImage(mbr, imageLat, imageLong)) {
						notFound = false;
						if (mbrids.toString().isEmpty()) {
							mbrids.append(mbr.getId());
						} else {
							mbrids.append(" ").append(mbr.getId());
						}

					}

					if (imageLong < mbr.getSwLong()) {
						break;
					}
				}

				// if (notFound) {
				// notFoundCount++;
				// System.out.println("Not found:" + lineArr[1]);
				// //continue;
				// }
			} else {
//				if (imageLat < 0) {
//					double val = Math.floor(imageLat);
//					if (imageLat == val) {
//
//						if (imageLat == -90.0) {
//							int id = totalLong * (90 + Integer.parseInt(String.valueOf(Math.abs(val))))
//									* ((int) (1 / degree));
//							//if()
//							mbrids.append(id);
//						} else {
//							int id = totalLong * (90 + Integer.parseInt(String.valueOf(Math.abs(val))))
//									* ((int) (1 / degree));
//							mbrids.append(id);
//							id = id + totalLong;
//							mbrids.append(" ").append(id);
//						}
//
//					} else {
//						int id = totalLong * (90 + (int) (Math.abs(val))) * ((int) (1 / degree));
//						mbrids.append(id);
//					}
//				} else {
//					double val = Math.floor(imageLat);
//					if (imageLat == val) {
//						if (imageLat == 90.0) {
//							int id = totalLong;
//							mbrids.append(id);
//						} else {
//							int id = totalLong * (90 - Integer.parseInt(String.valueOf(Math.abs(val))))  * ((int)(1/ degree));
//							mbrids.append(id);
//							id = id + totalLong;
//							mbrids.append(" ").append(id);
//						}
//					} else {
//						int id = totalLong * (90 - (int) Math.abs(val)) *  ((int)(1/ degree));
//						mbrids.append(id);
//					}
//				}
				
				double startLat = 90.0;
				int startId = totalLong;
				while(startLat!= -90.3){
					
					if(imageLat <startLat && imageLat> (startLat - degree) ){
						mbrids.append(startId);
						break;
					}
					
					if(imageLat == startLat){
						
						if(startLat == 90.0 || startLat == -90.0){
							mbrids.append(startId);
							break;
						}else{
							mbrids.append(startId);
							mbrids.append(" ").append((startId-totalLong));
							break;
						}
						
					}
					
					startLat = startLat - degree;
					startId = startId + totalLong;
				}
				
			}

			if (mbrids.toString().isEmpty()) {
				// notFoundCount++;
				System.out.println("Not found for image:" + lineArr[1] + ":" + imageLat + ":" + imageLong);
				continue;
			}

			result.append(mbrids.toString());
			writer.write(result.toString() + "\n");
			writer.flush();
			count++;
			if (count % 1000000 == 0) {

				System.out.println(("Processed" + count));
			}
		}
		writer.close();
		// gtWriter.close();
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
}
