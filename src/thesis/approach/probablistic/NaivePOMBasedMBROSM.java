
package thesis.approach.probablistic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import thesis.data.entity.AncestorDescendant;
import thesis.data.entity.Place;
import thesis.data.entity.PlaceByLat;
import thesis.data.entity.PlaceByLong;
import thesis.evaluation.EvaluateHierarchy;
import thesis.util.DataLoader;

public class NaivePOMBasedMBROSM {
	static Map<String, AncestorDescendant> childrenMap = null;
	public static Map<String, String> centreCoordinateMap = null;
	static double std = 0.0;
	static double mean = 0.0;
	public static double errorProb = 0.0;

	
	public static void main(String[] args) throws IOException {
		long startTimeInMs = System.currentTimeMillis();
		childrenMap = DataLoader.loadAllOSMNameChild("160-osm-childrenmbr-sorted-less100.txt");
		getCentreForAllPlaceOsm();
		// // getCentreForAllPlaceInGeonames();
		System.out.println("Loaded all the centres");
		calculateErrorProb();
		System.out.println("Calculated Q value:" + errorProb);
		std = calculateStdeviation();
		System.out.println("Calculated std: " + std);
		// mean = 0.0;
		long startAlgTime = System.currentTimeMillis();
		generateFinalMbr();
		long endAlgTime = System.currentTimeMillis();
		System.out.println("Total time taken for algo:" + (endAlgTime - startAlgTime));
		long endTimeInMs = System.currentTimeMillis();
		System.out.println("Total time taken:" + (endTimeInMs - startTimeInMs));
	}

	private static void generateFinalMbr() {

		

		Path hierarchyFile = Paths.get("160-osm-childrenmbr-sorted-less100.txt");
		// Path hierarchyFile = Paths.get("1067-1.txt");

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> geoMbrMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			double trueArea = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
					Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));
			geoMbrMap.put(lineArr[0], line + "\t" + trueArea);
		}

		hierarchyFile = Paths.get("osmname-hierarchy.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

//		PrintWriter writer = null;
//		try {
//			writer = new PrintWriter(new FileWriter("160-pom-mbr-naive-30Dec.txt", true));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		PrintWriter highwriter = null;
		try {
			highwriter = new PrintWriter(new FileWriter("160-pom-mbr-naive-6Jan.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PrintWriter processedWriter = null;
		try {
			processedWriter = new PrintWriter(new FileWriter("160-id-placename-childcount-timetaken-osm-naive.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		PrintWriter skipWriter = null;
		try {
			skipWriter = new PrintWriter(new FileWriter("160-pom-mbr-naive-6Jan-exceptions.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// PrintWriter skipWriter1 = null;
		// try {
		// skipWriter1 = new PrintWriter(new
		// FileWriter("310-pom-toomanychilddropped.txt", true));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		System.out.println("Going to generate MBR..");
		int counter = 0;
		int gr10Count = 0;
		int processedCount = 0;

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			processedCount++;
			if (processedCount % 100000 == 0) {
				System.out.println("Processed:" + processedCount);
			}
			if (geoMbrMap.containsKey(lineArr[0])) {
				String geoMbr[] = geoMbrMap.get(lineArr[0]).split("\t");
				double geoSwLat = Double.parseDouble(geoMbr[8]);
				double geoSwLong = Double.parseDouble(geoMbr[9]);
				double geoNeLat = Double.parseDouble(geoMbr[10]);
				double geoNeLong = Double.parseDouble(geoMbr[11]);
				double trueArea = Double.parseDouble(geoMbr[geoMbr.length - 1]);
				double currArea = EvaluateHierarchy.getAreaByCartesian(geoSwLat, geoSwLong, geoNeLat, geoNeLong);
				String modelMbr = geoSwLat + "\t" + geoSwLong + "\t" + geoNeLat + "\t" + geoNeLong;
				double modelSwLat = geoSwLat;
				double modelSwLong = geoSwLong;
				double modelNeLat = geoNeLat;
				double modelNeLong = geoNeLong;
				String bestCentre = geoMbr[2] + "\t" + geoMbr[3];
				counter++;
				double maxProb = -1.0;
				Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
				if ((trueArea) < currArea) {
					long startTime = System.currentTimeMillis();
					System.out.println("Going to prepare MBR for :" + lineArr[0]);
					List<Place> childBasedOnLat = new ArrayList<>();
					List<Place> childBasedOnLong = new ArrayList<>();
//					[4283866191, 4283759184, 4283710099, 4283823911, 4283823912]
					if(lineArr[0].equals("4288368736")){
						System.out.println("break");
					}
					
					Map<String, Place> idPlaceMap = new HashMap<>();
					for (String child : childrenSet) {
						String centre = centreCoordinateMap.get(child);
						String coordinate[] = centre.split("\t");
						Place place = new Place();
						place.setId(child);
						place.setLat(Double.parseDouble(coordinate[0]));
						place.setLon(Double.parseDouble(coordinate[1]));
						idPlaceMap.put(coordinate[0] + ":" + coordinate[1], place);
					}

					Iterator it = idPlaceMap.entrySet().iterator();
					while (it.hasNext()) {
						Entry entry = (Entry) it.next();
						Place placeObj = (Place) entry.getValue();
						childBasedOnLat.add(placeObj);
						childBasedOnLong.add(placeObj);
					}

					PlaceByLat placeByLatObj = new PlaceByLat();
					PlaceByLong placeByLongObj = new PlaceByLong();
					Collections.sort(childBasedOnLat, placeByLatObj);
					Collections.sort(childBasedOnLong, placeByLongObj);

					System.out.println(" total child:" + childBasedOnLong.size() + "  true area:" + trueArea);
					int totalUniqueChild = childBasedOnLong.size();

					modelSwLat = geoSwLat;
					modelSwLong = geoSwLong;
					modelNeLat = geoNeLat;
					modelNeLong = geoNeLong;
					// forming n1
					List<Place> topDownSwapList = new ArrayList<>();
					// int ptsToUse = totalPoints + 1;
					for (int i = childBasedOnLat.size() - 1; i >= 0; i--) {
						// if (ptsToUse > 0) {
						Place currPlaceByLat = childBasedOnLat.get(i);
						topDownSwapList.add(currPlaceByLat);
						// ptsToUse--;
						// } else {
						// break;
						// }

					}
					Collections.sort(topDownSwapList, placeByLatObj);
					Collections.sort(topDownSwapList, Collections.reverseOrder());
					// forming 4
					// ptsToUse = totalPoints + 1;
					List<Place> downUpSwapList = new ArrayList();

					for (int i = 0; i < childBasedOnLat.size(); i++) {
						// if (ptsToUse > 0) {
						Place currPlaceByLat = childBasedOnLat.get(i);
						downUpSwapList.add(currPlaceByLat);
						// ptsToUse--;
						// } else {
						// break;
						// }
					}
					Collections.sort(downUpSwapList, placeByLatObj);
					// forming n2
					// ptsToUse = totalPoints + 1;
					List<Place> rightLeftSwapList = new ArrayList();

					for (int i = childBasedOnLong.size() - 1; i >= 0; i--) {
						// if (ptsToUse > 0) {
						Place currPlaceByLong = childBasedOnLong.get(i);
						rightLeftSwapList.add(currPlaceByLong);
						// ptsToUse--;
						// } else {
						// break;
						// }
					}
					Collections.sort(rightLeftSwapList, placeByLongObj);
					Collections.sort(rightLeftSwapList, Collections.reverseOrder());
					// forming n3
					// ptsToUse = totalPoints + 1;
					List<Place> leftRightSwapList = new ArrayList();

					for (int i = 0; i < childBasedOnLong.size(); i++) {
						// if (ptsToUse > 0) {
						Place currPlaceByLong = childBasedOnLong.get(i);
						leftRightSwapList.add(currPlaceByLong);
						// ptsToUse--;
						// } else {
						// break;
						// }
					}
					Collections.sort(leftRightSwapList, placeByLongObj);
					int minSpace = totalUniqueChild-1;
					boolean found = false;
					for (int i = 0; i <= minSpace; i++) { // topdown
						for (int j = 0; j <= (minSpace - i); j++) { // bottomUp
							for (int k = 0; k <= (minSpace - i - j); k++) { // leftRight
								for (int l = 0; l <= minSpace - i - j - k; l++) { //// rightLeft

									// int l = minSpace - i - j - k;
									Place topDownPt = null;
									Place bottomUpPt = null;
									Place leftRightPt = null;
									Place rightLeftPt = null;
									int ptToDrop = 0;

									// System.out.println("i=" + i + " j=" + j +
									// " k=" + k + " l=" + l);

									Map<String, String> droppedPtMap = new HashMap();

									int index = 0;
									while (ptToDrop != i) {
										topDownPt = topDownSwapList.get(topDownSwapList.size() - 1 - index);
										if (!droppedPtMap.containsKey(topDownPt.getLat() + ":" + topDownPt.getLon())) {
											ptToDrop++;
											droppedPtMap.put(topDownPt.getLat() + ":" + topDownPt.getLon(), null);
										}
										index++;
									}
									topDownPt = topDownSwapList.get(topDownSwapList.size() - 1 - index);

									index = 0;
									ptToDrop = 0;
									while (ptToDrop != j) {
										bottomUpPt = downUpSwapList.get(index);
										if (!droppedPtMap
												.containsKey(bottomUpPt.getLat() + ":" + bottomUpPt.getLon())) {
											ptToDrop++;
											droppedPtMap.put(bottomUpPt.getLat() + ":" + bottomUpPt.getLon(), null);
										}
										index++;
									}
									bottomUpPt = downUpSwapList.get(index);

									index = 0;
									ptToDrop = 0;
									while (ptToDrop != k) {
										leftRightPt = leftRightSwapList.get(index);
										if (!droppedPtMap
												.containsKey(leftRightPt.getLat() + ":" + leftRightPt.getLon())) {
											ptToDrop++;
											droppedPtMap.put(leftRightPt.getLat() + ":" + leftRightPt.getLon(), null);
										}
										index++;
									}
									leftRightPt = leftRightSwapList.get(index);

									index = 0;
									ptToDrop = 0;
									// if(l==46){
									// System.out.println("break");
									// }
									while (ptToDrop != l) {
										rightLeftPt = rightLeftSwapList.get(rightLeftSwapList.size() - 1 - index);
										if (!droppedPtMap
												.containsKey(rightLeftPt.getLat() + ":" + rightLeftPt.getLon())) {
											ptToDrop++;
											droppedPtMap.put(rightLeftPt.getLat() + ":" + rightLeftPt.getLon(), null);
										}
										index++;
										if(index == rightLeftSwapList.size()){
											System.out.println("Test");
										}
									}
									rightLeftPt = rightLeftSwapList.get(rightLeftSwapList.size() - 1 - index);

									index = 0;
									topDownPt = topDownSwapList.get(topDownSwapList.size() - 1 - index);
									while (droppedPtMap.containsKey(topDownPt.getLat() + ":" + topDownPt.getLon())) {
										index++;
										topDownPt = topDownSwapList.get(topDownSwapList.size() - 1 - index);
									}

									index = 0;
									bottomUpPt = downUpSwapList.get(index);
									while (droppedPtMap.containsKey(bottomUpPt.getLat() + ":" + bottomUpPt.getLon())) {
										index++;
										bottomUpPt = downUpSwapList.get(index);
									}

									index = 0;
									leftRightPt = leftRightSwapList.get(index);
									while (droppedPtMap
											.containsKey(leftRightPt.getLat() + ":" + leftRightPt.getLon())) {
										index++;
										leftRightPt = leftRightSwapList.get(index);
									}

									index = 0;
									rightLeftPt = rightLeftSwapList.get(rightLeftSwapList.size() - 1 - index);
									while (droppedPtMap
											.containsKey(rightLeftPt.getLat() + ":" + rightLeftPt.getLon())) {
										index++;
										rightLeftPt = rightLeftSwapList.get(rightLeftSwapList.size() - 1 - index);
									}

									modelSwLat = Math.min(topDownPt.getLat(), Math.min(bottomUpPt.getLat(),
											Math.min(leftRightPt.getLat(), rightLeftPt.getLat())));
									modelSwLong = Math.min(topDownPt.getLon(), Math.min(bottomUpPt.getLon(),
											Math.min(leftRightPt.getLon(), rightLeftPt.getLon())));
									modelNeLat = Math.max(topDownPt.getLat(), Math.max(bottomUpPt.getLat(),
											Math.max(leftRightPt.getLat(), rightLeftPt.getLat())));
									modelNeLong = Math.max(topDownPt.getLon(), Math.max(bottomUpPt.getLon(),
											Math.max(leftRightPt.getLon(), rightLeftPt.getLon())));
									double areaBySamplePts = EvaluateHierarchy.getAreaByCartesian(modelSwLat,
											modelSwLong, modelNeLat, modelNeLong);

									if (areaBySamplePts > trueArea) {
										continue;

									} else {
										found = true;
										double placeCentre[] = getCentreOfMbr(modelSwLat, modelSwLong, modelNeLat,
												modelNeLong);
										double distanceBtCurrAndGivenCentre = EvaluateHierarchy.getDistance(
												Double.parseDouble(geoMbr[2]), Double.parseDouble(geoMbr[3]),
												placeCentre[0], placeCentre[1]);
										double currCentreProb = calculateCentreProbability(std,
												distanceBtCurrAndGivenCentre);

										// calculate subsetProbability

										int numberOfPointsEnclosed = (totalUniqueChild) - (i + j + k + l);
										double enclosingProb = ((double) numberOfPointsEnclosed)
												/ ((double) (totalUniqueChild));
										double probProd = currCentreProb * enclosingProb;
										// find max and keep the mbr for
										// the
										// max
										// prob ...
										if (maxProb <= probProd) {
											maxProb = probProd;
											bestCentre = placeCentre[0] + "\t" + placeCentre[1];
											// bestWrongpts = notInMbr;
											modelMbr = modelSwLat + "\t" + modelSwLong + "\t" + modelNeLat + "\t"
													+ modelNeLong + "\t"
													+ numberOfPointsEnclosed + "\t" + (totalUniqueChild);
										
										// initialeclosedpt+ bestenclosedpt+totaluniquchild
										}

									}
								}
							}
						}

					}
					
					long endTime = System.currentTimeMillis();
					long ithTimeTake = endTime - startTime;
					System.out.println("Total Time taken: " + ithTimeTake);

					if (!found) {
						// System.out.println("area is always bigger..");
						modelSwLat = geoSwLat;
						modelSwLong = geoSwLong;
						modelNeLat = geoNeLat;
						modelNeLong = geoNeLong;
						modelMbr = modelSwLat + "\t" + modelSwLong + "\t" + modelNeLat + "\t" + modelNeLong;
						String toWrite = geoMbr[0] + "\t" + geoMbr[1] + "\t" + bestCentre + "\t" + geoMbr[4] + "\t"
								+ geoMbr[5] + "\t" + geoMbr[6] + "\t" + geoMbr[7] + "\t" + modelMbr + "\t" 
								+ totalUniqueChild + "\t" + totalUniqueChild + "\t" + totalUniqueChild + "\t"
								+ childrenSet.size();
						skipWriter.write(toWrite + "\n");
						skipWriter.flush();
						processedWriter.write(lineArr[0] + "\t" + lineArr[1] + "\t" + childrenSet.size()
						+ "\t" + ithTimeTake + "\n");
				processedWriter.flush();
					} else {

						String toWrite = geoMbr[0] + "\t" + geoMbr[1] + "\t" + bestCentre + "\t" + geoMbr[4] + "\t"
								+ geoMbr[5] + "\t" + geoMbr[6] + "\t" + geoMbr[7] + "\t" + modelMbr + "\t"
								+ childrenSet.size();
						highwriter.write(toWrite + "\n");
						highwriter.flush();
						processedWriter.write(lineArr[0] + "\t" + lineArr[1] + "\t" + childrenSet.size()
						+ "\t" + ithTimeTake + "\n");
				processedWriter.flush();
					}

				} else {
					modelMbr = modelSwLat + "\t" + modelSwLong + "\t" + modelNeLat + "\t" + modelNeLong;
					String toWrite = geoMbr[0] + "\t" + geoMbr[1] + "\t" + bestCentre + "\t" + geoMbr[4] + "\t"
							+ geoMbr[5] + "\t" + geoMbr[6] + "\t" + geoMbr[7] + "\t" + modelMbr + "\t"
							+ childrenSet.size() + "\t" + childrenSet.size() + "\t" + childrenSet.size();
					skipWriter.write(toWrite + "\n");
					skipWriter.flush();

				}
				// System.out.println("Done for :" + lineArr[0]);
			}
		}

		System.out.println("Less area count:" + gr10Count);
		processedWriter.close();
		highwriter.close();

	}

	private static double[] getCentreOfMbr(double swLat, double swLong, double neLat, double neLong) {
		double centreLat = (swLat + neLat) / (double) 2;
		double centreLong = (swLong + neLong) / (double) 2;
		double centreArr[] = new double[2];
		centreArr[0] = centreLat;
		centreArr[1] = centreLong;
		return centreArr;
	}

	private static void getCentreForAllPlaceOsm() {

		Path hierarchyFile = Paths.get("160-osm-childrenmbr-sorted-less100.txt");

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		centreCoordinateMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			centreCoordinateMap.put(lineArr[0], null); // id:area

			Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
			for (String child : childrenSet) {
				centreCoordinateMap.put(child, null);
			}
		}
		//
		// loading all childs..
		// Map<String, String> idCoordinateMap = new HashMap();
		hierarchyFile = Paths.get("1000-sample-osmname.txt");
//		hierarchyFile = Paths.get("1044-sample-mbr.txt");
		
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			centreCoordinateMap.put(lineArr[0], null); // id:area
			Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
			for (String child : childrenSet) {
				centreCoordinateMap.put(child, null);
			}
		}

		// hierarchyFile = Paths.get("osmname-hierarchy.txt");
		// gridlines = null;
		// try {
		// gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// for (String line : (Iterable<String>) gridlines::iterator) {
		// String lineArr[] = line.split("\t");
		// if (centreCoordinateMap.containsKey(lineArr[0])) {
		// String children[] = lineArr[6].split(" ");
		// for (int i = 0; i < children.length; i++) {
		// centreCoordinateMap.put(children[i], null);
		// }
		//
		// }
		// }
		// loading coordinate for each childs.
		hierarchyFile = Paths.get("osmname-hierarchy.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (centreCoordinateMap.containsKey(lineArr[0])) {
				centreCoordinateMap.put(lineArr[0], lineArr[8] + "\t" + lineArr[9]);
			}
		}

	}

	private static double calculateStdeviation() {
		Path hierarchyFile = Paths.get("1000-sample-osmname.txt");
//		Path hierarchyFile = Paths.get("1044-sample-mbr.txt");
		

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, String> testMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			testMap.put(lineArr[0], line);
		}

		hierarchyFile = Paths.get("osmname-hierarchy.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Double> distanceList = new ArrayList();
		double meanDistance = 0.0;
		int count = 0;
		double totalDistance = 0.0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (testMap.containsKey(lineArr[0])) {
				String centre[] = getCentreForMBR(lineArr[1], lineArr[2], lineArr[3], lineArr[4]).split("\t");
				double distanceBetweenCenters = EvaluateHierarchy.getDistance(Double.parseDouble(centre[0]),
						Double.parseDouble(centre[1]), Double.parseDouble(lineArr[8]), Double.parseDouble(lineArr[9]));
				if (Double.isNaN(distanceBetweenCenters)) {
					continue;
				}
				distanceList.add(distanceBetweenCenters);
				totalDistance = totalDistance + distanceBetweenCenters;
				count++;
			}
		}

		meanDistance = totalDistance / (double) count;
		double sum = 0.0;
		for (Double distance : distanceList) {
			sum = sum + Math.pow((distance - meanDistance), 2.0);
		}
		sum = sum / (double) (count - 1); // estimated standard deviation.
		mean = meanDistance;
		System.out.println("Mean Distance:" + meanDistance + " count:" + count);
		return Math.sqrt(sum);
	}

	private static double calculateCentreProbability(double std, double distanceBtCurrAndGivenCentre) {
		double exp = -(Math.pow((distanceBtCurrAndGivenCentre - mean), 2.0) / (2 * Math.pow(std, 2)));
		exp = Math.exp(exp);
		exp = exp / Math.sqrt(2 * Math.PI * Math.pow(std, 2));
		return exp;
	}

	private static String getCentreForMBR(String swLat, String swLong, String neLat, String neLong) {
		double centreLat = (Double.parseDouble(swLat) + Double.parseDouble(neLat)) / (double) 2;
		double centreLong = (Double.parseDouble(swLong) + Double.parseDouble(neLong)) / (double) 2;
		return centreLat + "\t" + centreLong;
	}

	public static BigInteger factorial(int num) {
		if (num == 0)
			return BigInteger.ONE;
		else
			return factorial(num - 1).multiply(BigInteger.valueOf(num));
	}

	public static BigInteger nCr(int n, int r) {
		return factorial(n).divide(factorial(n - r).multiply(factorial(r)));
	}

	private static double calculateSubSetProb(int n, int r) {

		// double deno = fact(n - r) * fact(r);
		// if(deno == 0){
		// System.out.println("break");
		// }
		// double prob = (fact(n) / deno);
		BigInteger prob = nCr(n, r);
		return prob.doubleValue() * Math.pow(errorProb, r) * Math.pow((1 - errorProb), n - r);

	}

	private static void calculateErrorProb() {
		// iterator through all items which are not in test set and find among
		// its children how many are not in
		// true mbr and calculate the prob. take average and set it to error
		// prob..
		Path hierarchyFile = Paths.get("1000-sample-osmname.txt");
//		Path hierarchyFile = Paths.get("1044-sample-mbr.txt");
		

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> sampleMbrMap = new HashMap();
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			sampleMbrMap.put(lineArr[0], line);
		}

		hierarchyFile = Paths.get("osmname-hierarchy.txt");

		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		double totalProb = 0;
		int totalItem = 0;
		int totalPlacesChecked = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (sampleMbrMap.containsKey(lineArr[0])) {
				totalItem++;
				String sampleTrueMbr[] = sampleMbrMap.get(lineArr[0]).split("\t");
				double sampleTrueMbrSwLat = Double.parseDouble(sampleTrueMbr[4]);
				double sampleTrueMbrSwLong = Double.parseDouble(sampleTrueMbr[5]);
				double sampleTrueMbrNeLat = Double.parseDouble(sampleTrueMbr[6]);
				double sampleTrueMbrNeLong = Double.parseDouble(sampleTrueMbr[7]);
				int count = 0;
				// String children[] = lineArr[6].split(" ");
				// for (int i = 0; i < children.length; i++) {
				// String coordinate[] =
				// centreCoordinateMap.get(children[i]).split("\t");
				// double placeLat = Double.parseDouble(coordinate[0]);
				// double placeLong = Double.parseDouble(coordinate[1]);
				// if (placeLat >= sampleTrueMbrSwLat && placeLat <=
				// sampleTrueMbrNeLat) {
				// if (placeLong >= sampleTrueMbrSwLong && placeLong <=
				// sampleTrueMbrNeLong) {
				// count++;
				// }
				// }
				// }

				Set<String> childrenSet = childrenMap.get(lineArr[0]).getDescendants();
				for (String child : childrenSet) {
					String coordinate[] = centreCoordinateMap.get(child).split("\t");
					double placeLat = Double.parseDouble(coordinate[0]);
					double placeLong = Double.parseDouble(coordinate[1]);
					if (placeLat >= sampleTrueMbrSwLat && placeLat <= sampleTrueMbrNeLat) {
						if (placeLong >= sampleTrueMbrSwLong && placeLong <= sampleTrueMbrNeLong) {
							count++;
						}
					}
				}

				int notInGoogleMbr = childrenSet.size() - count;
				totalProb = totalProb + ((double) notInGoogleMbr / (double) childrenSet.size());
				totalPlacesChecked = totalPlacesChecked + childrenSet.size();
			}

		}
		errorProb = totalProb / (double) totalItem;
		System.out.println("Q value:" + errorProb);
		System.out.println("totalItem:" + totalItem + " totalPlacesChecked:" + totalPlacesChecked);
	}

}
