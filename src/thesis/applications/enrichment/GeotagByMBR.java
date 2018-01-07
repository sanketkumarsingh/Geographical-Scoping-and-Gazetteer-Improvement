package thesis.applications.enrichment;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import thesis.data.entity.Image;
import thesis.data.entity.MBRInfo;
import thesis.evaluation.EvaluateHierarchy;

public class GeotagByMBR {
	private static Map<String, Integer> tagGlobalCountMap = null;
	// private static Map<String, MBR> mbrHierarchyMap = null;
	private static Map<String, MBRInfo> mbrInfoMap = null;
	private static final String NO_STRING_SPACE = "";
	private static double totalError = 0.0;
	private static double normalError = 0.0;

    public static void geotagByMbr() {
      
    	String popularMbr = getCandidateMbrsForTestImage();  // MBR Prediction
		calculateDistanceErrorForTestImages(popularMbr);  // coordinate estimation
	}
    
    private static void calculateDistanceErrorForTestImages(String popularMbrId) {

		Map<String, String> testImageIdPredMbrMap = new HashMap();

		Path hierarchyFile = Paths.get("test-mbr-prediction-user-normal-new-base.txt");

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, List<Image>> mbrIdImageListMap = new HashMap();

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split(":");
			String mbrs[] = lineArr[1].split(" ");
			testImageIdPredMbrMap.put(lineArr[0], line);

			for (int i = 0; i < mbrs.length; i++) {
				mbrIdImageListMap.put(mbrs[i], null);
			}
		}

		System.out.println("Loaded test images and prediction:" + testImageIdPredMbrMap.size());

		hierarchyFile = Paths.get("image-mbrids-train-mediaeval-allmap-new.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Image> mbrImages = new ArrayList();

		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split(":");
			String mbrs[] = lineArr[lineArr.length - 1].split(" ");

			Image image = new Image();
			image.setId(lineArr[1]);
			image.setLat(Double.parseDouble(lineArr[3]));
			image.setLongitude(Double.parseDouble(lineArr[4]));
			StringBuffer tag = new StringBuffer();
			tag.append(lineArr[5]).append(":").append(lineArr[6]).append(":").append(lineArr[7]).append(":")
					.append("NA");
			image.setTag(tag.toString());

			for (int i = 0; i < mbrs.length; i++) {

				if (mbrs[i].equals(popularMbrId)) {

					if (mbrIdImageListMap.containsKey(mbrs[i])) {
						if (mbrIdImageListMap.get(mbrs[i]) != null) {

							mbrIdImageListMap.get(mbrs[i]).add(image);
						} else {
							List<Image> imageList = new ArrayList();
							imageList.add(image);
							mbrIdImageListMap.put(mbrs[i], imageList);
						}
					} else {
						List<Image> imageList = new ArrayList();
						imageList.add(image);
						mbrIdImageListMap.put(mbrs[i], imageList);
					}
					mbrImages.add(image);

				} else {
					if (mbrIdImageListMap.containsKey(mbrs[i])) {
						if (mbrIdImageListMap.get(mbrs[i]) != null) {

							mbrIdImageListMap.get(mbrs[i]).add(image);
						} else {
							List<Image> imageList = new ArrayList();
							imageList.add(image);
							mbrIdImageListMap.put(mbrs[i], imageList);
						}
					}

				}

			}
		}

		System.out.println("Loaded predicted Mbrs and Images:" + mbrIdImageListMap.size());

		System.out.println("Calculating image in most popular mbr:" + popularMbrId);

		// 4211774 expected

		System.out.println("Total image in most popular mbr is:" + mbrImages.size());
		Image centreImage = getEqualDisImageThruWeiszfeld(mbrImages);
		double minDistance = Double.MAX_VALUE;
		Image equalDistImage = null;
		for (Image q : mbrImages) {
			double distancePQ = EvaluateHierarchy.getDistance(centreImage.getLat(), centreImage.getLongitude(),
					q.getLat(), q.getLongitude());
			if (minDistance > distancePQ) {
				distancePQ = minDistance;
				equalDistImage = q;
			}
		}

		System.out.println("Image id:" + equalDistImage.getId() + "\t" + equalDistImage.getLat() + "\t"
				+ equalDistImage.getLongitude());

		hierarchyFile = Paths.get("image-mbrids-test-mediaeval-allmap-new.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int count = 0;
		int thruPred = 0;
		int thrupop = 0;

		PrintWriter mbrwriter = null;
		try {
			mbrwriter = new PrintWriter(new FileWriter("test-mbr-result-user-normalization-new-base.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : (Iterable<String>) gridlines::iterator) {
			String imageArr[] = line.split(":");
			String testTag = NO_STRING_SPACE;
			count++;
			if (imageArr[7].isEmpty()) {
				if (imageArr[5].isEmpty()) {
					testTag = imageArr[6];
				} else if (imageArr[6].isEmpty()) {
					testTag = imageArr[5];
				} else {
					testTag = imageArr[5] + "," + imageArr[6];
				}

			} else {
				testTag = imageArr[7];
			}
			String trainingTag = NO_STRING_SPACE;
			String bestMbr = NO_STRING_SPACE;
			String imageId = NO_STRING_SPACE;
			double maxSim = 0.0;
			double imageLat = 0.0;
			double imageLong = 0.0;

			String testResult[] = testImageIdPredMbrMap.get(imageArr[1]).split(":");

			if (testResult[2].equals("N")) {
				String candidateMbrs[] = testResult[1].split(" ");
				for (int i = 0; i < candidateMbrs.length; i++) {
					List<Image> imagesInMbr = mbrIdImageListMap.get(candidateMbrs[i]);
					Iterator it = imagesInMbr.iterator();
					while (it.hasNext()) {
						Image image = (Image) it.next();
						String tagArr[] = image.getTag().split(":");
						if (tagArr.length < 2) {
							System.out.println("break");
						}
						if (tagArr[2].isEmpty()) {
							if (tagArr[0].isEmpty()) {
								trainingTag = tagArr[1];
							} else if (tagArr[1].isEmpty()) {
								trainingTag = tagArr[0];
							} else {
								trainingTag = tagArr[0] + "," + tagArr[1];
							}

						} else {
							trainingTag = tagArr[2];
						}

						// System.out.println(trainingTag + " \t " + testTag);

						double similarity = getJaccardSimilarity(trainingTag.split(","), testTag.split(","));
						if (maxSim < similarity) {
							maxSim = similarity;
							bestMbr = candidateMbrs[i];
							imageId = image.getId();
							imageLat = image.getLat();
							imageLong = image.getLongitude();
						}
					}
				}
				thruPred++;
			} else {
				thrupop++;
				bestMbr = popularMbrId;
				imageId = equalDistImage.getId();
				imageLat = equalDistImage.getLat();
				imageLong = equalDistImage.getLongitude();
			}

			double errorDistance = EvaluateHierarchy.getDistance(imageLat, imageLong, Double.parseDouble(imageArr[3]),
					Double.parseDouble(imageArr[4]));
			if (imageLat == Double.parseDouble(imageArr[3]) && imageLong == Double.parseDouble(imageArr[4])) {
				errorDistance = 0;
			}
			if (Double.isNaN(errorDistance)) {
				System.out.println("NaN:" + imageArr[1] + ":" + imageLat + ":" + imageLong + ":"
						+ Double.parseDouble(imageArr[3]) + ":" + Double.parseDouble(imageArr[4]));
			}
			StringBuffer result = new StringBuffer();
			result.append(imageArr[1]).append(":");
			result.append(bestMbr).append(":").append(imageId).append(":").append(imageLat).append(":")
					.append(imageLong).append(":").append(errorDistance);
			mbrwriter.write(result.toString() + "\n");
			mbrwriter.flush();
			totalError = totalError + errorDistance;
			if (testResult[2].equals("N")) {
				normalError = normalError + errorDistance;
			}

			if (count % 1000000 == 0) {
				System.out.println("Processed:" + count);
			}

		}
		double average = totalError / (double) count;
		double corrAverage = normalError / (double) thruPred;
		System.out.println("Average error distance:" + average + " over imagecount:" + count);
		System.out.println("Correct Average error distance:" + corrAverage + " over image count:" + thruPred);
		System.out.println("Total prediction thru popular image:" + thrupop);
		mbrwriter.close();
		// return result;
	}
    
    
    private static String getCandidateMbrsForTestImage() {

		// loaded train data..
		
		double alpha = 0.8;
		mbrInfoMap = generateMbrInfoFile();
		// loadMbrHierarchy(mbrInfoMap);
		Iterator it = mbrInfoMap.entrySet().iterator();
		tagGlobalCountMap = new HashMap();

		String mostPopularCellId = "";
		MBRInfo mostPopularCellInfo = null;

		
		int totalNumberOfMbrs = 0;

		// int numberOfMbrsWithLessThan51Image = 0;
		// int numberOfMbrsWithLessThan10Image = 0;
		int totalUserInTrain = 0;
		int maxTotalUser = 0;
		int totalMbrsOnWhichNormalizationIsDone = 0;
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String mbrid = (String) entry.getKey();
			MBRInfo value = (MBRInfo) entry.getValue();
			Map<String, Set<String>> tagUserMap = value.getTagUserIdMap();
			if (tagUserMap == null) {
				it.remove();
				continue;
			}
			totalNumberOfMbrs++;
			
			Iterator iter = tagUserMap.entrySet().iterator();
			StringBuffer tagCount = new StringBuffer();
			int i = 0;
			StringBuffer result = new StringBuffer();
			Set<String> allUserForMbr = new HashSet();
			while (iter.hasNext()) {
				Entry entry1 = (Entry) iter.next();
				String tag = (String) entry1.getKey();
				if (tag.isEmpty()) {
					continue;
				}
				Set<String> userSet = (Set<String>) entry1.getValue();

				if (i == 0) {
					tagCount.append(tag).append(":").append(userSet.size());
					i = 1;
				} else {
					tagCount.append(" ").append(tag).append(":").append(userSet.size());
				}

				if (tagGlobalCountMap.containsKey(tag)) {
					tagGlobalCountMap.put(tag, tagGlobalCountMap.get(tag) + userSet.size());
				} else {
					tagGlobalCountMap.put(tag, userSet.size());
				}

				// if()
				allUserForMbr.addAll(userSet);
			}

			int currentMbrTotalUser = allUserForMbr.size();
			if(currentMbrTotalUser > 2000){
				totalMbrsOnWhichNormalizationIsDone++;
			}
			value.setUserCount(currentMbrTotalUser);
			if (currentMbrTotalUser > maxTotalUser) {
				maxTotalUser = currentMbrTotalUser;
				mostPopularCellId = mbrid;
				mostPopularCellInfo = value;
			}

			totalUserInTrain = totalUserInTrain + currentMbrTotalUser;
		}
//		userwriter.close();
		System.out.println("Total number of mbrs mapped:" + totalNumberOfMbrs);
		System.out.println("totalMbrsOnWhichNormalizationIsDone:" + totalMbrsOnWhichNormalizationIsDone);
		System.out.println("Size of mbrInfoMap:" + mbrInfoMap.size());
		System.out.println("Total user in train:" + totalUserInTrain);
		System.out.println("Popular MBR:" + mostPopularCellId + " contain image size:"
				+ mostPopularCellInfo.getTotalImageAssigned() + "  total user:" + mostPopularCellInfo.getUserCount());
		Path hierarchyFile = Paths.get("image-mbrids-test-mediaeval-allmap-new.txt"); // taking

		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter mbrwriter = null;
		try {
			// mbrwriter = new PrintWriter(new
			// FileWriter("267572-mbr-error-distance.txt", true));
			mbrwriter = new PrintWriter(new FileWriter("test-mbr-prediction-user-normal-new-base.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int count = 0;
		int predCount = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split(":");
			double score = Double.NEGATIVE_INFINITY;
			Set<String> bestMbrSet = null;
			// double grAreaScore = 0.0;
			// Set<String> bestMbrSetGrArea = null;
			count++;
			Iterator iter = mbrInfoMap.entrySet().iterator();
			int first = 0;
			// int grFirst = 0;

			String tag = "";
			if (lineArr[7].isEmpty()) {
				if (lineArr[5].isEmpty()) {
					tag = lineArr[6];
				} else if (lineArr[6].isEmpty()) {
					tag = lineArr[5];
				} else {
					tag = lineArr[5] + "," + lineArr[6];
				}
			} else {
				tag = lineArr[7];
			}
			String tagArr[] = tag.split(",");

			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				String mbrId = (String) entry.getKey();

				MBRInfo mbrInfo = (MBRInfo) entry.getValue();
				Map<String, Set<String>> tagUserIdMap = mbrInfo.getTagUserIdMap();
				if (tagUserIdMap == null) {
					continue;
				}

				// if ((mbrInfo.getArea() > 6) || mbrInfo.getParent() == null) {
				double mbrScore = 0.0;
				for (int i = 0; i < tagArr.length; i++) {
					if (tagArr[i].isEmpty()) {
						continue;
					}
					
					int userCountForTagInMbr = 0;
					int userCountForTagGlobally = 0;
					if (tagUserIdMap.containsKey(tagArr[i])) {
						userCountForTagInMbr = tagUserIdMap.get(tagArr[i]).size();
					}
					if (tagGlobalCountMap.containsKey(tagArr[i])) {
						userCountForTagGlobally = tagGlobalCountMap.get(tagArr[i]);
					}
//					******************
//					if (userCountForTagGlobally != 0) {
//						double prob = ((double) (userCountForTagInMbr + 1))
//								/ ((double) (userCountForTagGlobally + totalNumberOfMbrs));
//						mbrScore = mbrScore + Math.log(prob);
//					}else{
//						double prob = (double) 1/ (double)totalNumberOfMbrs;
//						mbrScore = mbrScore + Math.log(prob);
//					}
//					*******************
					
					
					if (userCountForTagGlobally != 0) {
						double probM = (double) userCountForTagInMbr/ (double) mbrInfo.getUserCount();
						double probC = (double) userCountForTagGlobally / (double)totalUserInTrain ;
						double prob = (alpha * probM) + ((1-alpha) * probC);
						mbrScore = mbrScore + Math.log(prob);
					}else{
						double prob = (double) 1/ (double)totalNumberOfMbrs;
						mbrScore = mbrScore + Math.log(prob);
					}
				}

				// 58 because of totalUserInTrain (5433392) / (totalGrids ==
				// 52420)
//				if (mbrInfo.getUserCount() > 2000) {
//					double mbrProb = (double) mbrInfo.getUserCount() / (double) totalUserInTrain;
//					mbrScore = mbrScore / mbrProb;
//				}
				// normalizing the
				// score with area
				// of the MBR.

				// mbrScore = mbrScore
				if (mbrScore > score) {
					score = mbrScore;
					bestMbrSet = new HashSet();
					bestMbrSet.add(mbrId);
				}
				if (mbrScore == score) {
					if (first == 0) {
						bestMbrSet = new HashSet();
						bestMbrSet.add(mbrId);
						first = 1;
					} else {
						bestMbrSet.add(mbrId);
					}
				}

			}
			StringBuffer ans = new StringBuffer();
			ans.append(lineArr[1]);

			if (score == 0.0) {
				bestMbrSet.clear();
				bestMbrSet.add(mostPopularCellId);

				ans.append(":").append(mostPopularCellId);
				ans.append(":").append("Y");
				// predCount++;
			} else {

				if (bestMbrSet != null && bestMbrSet.size() != 0) {
					StringBuffer mbrs = new StringBuffer();

					Iterator mbrIter = bestMbrSet.iterator();
					boolean firsCandidate = true;
					while (mbrIter.hasNext()) {
						String candMbr = (String) mbrIter.next();
						if (firsCandidate) {
							mbrs.append(candMbr);
							firsCandidate = false;
						} else {
							mbrs.append(" ").append(candMbr);
						}
					}

					predCount++;
					ans.append(":").append(mbrs.toString());
					ans.append(":").append("N");
				} else {
					System.out.println("Can never occur:" + lineArr[1]);
				}
			}
			ans.append(":").append(lineArr[lineArr.length - 1]);
			mbrwriter.write(ans.toString() + "\n");
			mbrwriter.flush();
			if (count % 100000 == 0) {

				System.out.println("Processed:" + count);
				System.out.println("predCount:" + predCount);

			}
		}
		System.out.println("Done..Processed:" + count);
		System.out.println("predCount:" + predCount);
		mbrwriter.close();

		return mostPopularCellId;
	}


public static Map<String, MBRInfo> generateMbrInfoFile() {
	tagGlobalCountMap = new HashMap();
	Map<String, MBRInfo> mbrInfoMap = new HashMap();
	Path hierarchyFile = Paths.get("420-pom-mbr-forapp.txt");

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
		MBRInfo mbrInfo = new MBRInfo();
		double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
				Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));
		mbrInfo.setArea(area);
		mbrInfoMap.put(lineArr[0], mbrInfo);
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
		MBRInfo mbrInfo = new MBRInfo();
		double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[4]),
				Double.parseDouble(lineArr[5]), Double.parseDouble(lineArr[6]), Double.parseDouble(lineArr[7]));
		mbrInfo.setArea(area);

		mbrInfoMap.put(lineArr[0], mbrInfo);
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
		MBRInfo mbrInfo = new MBRInfo();
		double area = EvaluateHierarchy.getAreaByCartesian(Double.parseDouble(lineArr[1]),
				Double.parseDouble(lineArr[2]), Double.parseDouble(lineArr[3]), Double.parseDouble(lineArr[4]));
		mbrInfo.setArea(area);
		mbrInfoMap.put(lineArr[0], mbrInfo);
	}
	System.out.println("Loaded all locations.." + mbrInfoMap.size());


	hierarchyFile = Paths.get("image-mbrids-train-mediaeval-allmap-new.txt");
	gridlines = null;
	try {
		gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	int count = 0;

	for (String line : (Iterable<String>) gridlines::iterator) {
		String lineArr[] = line.split(":");
		String mbrIds[] = lineArr[lineArr.length - 1].split(" ");
		count++;
		for (int i = 0; i < mbrIds.length; i++) {
			if (mbrInfoMap.containsKey(mbrIds[i])) {
				Map<String, Set<String>> tagUserIdMap = getTagUserIdMap(lineArr[2], lineArr[5], lineArr[6],
						lineArr[7]);
				MBRInfo mbrInfo = mbrInfoMap.get(mbrIds[i]);
				mbrInfo.setMbrId(mbrIds[i]);
				mbrInfo.setTotalImageAssigned(mbrInfo.getTotalImageAssigned() + 1);
				

				if (mbrInfo.getTagUserIdMap() != null) {
					Map<String, Set<String>> prevTagUserIdMap = mbrInfo.getTagUserIdMap();

					Iterator it = tagUserIdMap.entrySet().iterator();
					while (it.hasNext()) {
						Entry entry = (Entry) it.next();
						String tag = (String) entry.getKey();
						Set<String> userSet = (Set<String>) entry.getValue();
						if (prevTagUserIdMap.containsKey(tag)) {
							prevTagUserIdMap.get(tag).addAll(userSet);
						} else {
							prevTagUserIdMap.put(tag, userSet);
						}
					}

					mbrInfo.setTagUserIdMap(prevTagUserIdMap);
				} else {
					mbrInfo.setTagUserIdMap(tagUserIdMap);
				}
			}
		}

		if (count % 100000 == 0) {
			System.out.println("Processed images:" + count);
		}
	}

	System.out.println("Loaded the Map.." + mbrInfoMap.size());

	// System.out.println("Size of removing the unmapped mbrs:" +
	// mbrInfoMap.size());
/*
	PrintWriter mbrwriter = null;
	try {
		mbrwriter = new PrintWriter(new FileWriter("image-mbrids-train-mediaeval-allmap-mbrinfo-user.txt", true));
	} catch (IOException e) {
		e.printStackTrace();
	}

	PrintWriter tagwriter = null;
	try {
		tagwriter = new PrintWriter(new FileWriter("image-mbrids-train-mediaeval-allmap-globaltagcount.txt", true));
	} catch (IOException e) {
		e.printStackTrace();
	}

	Iterator it = mbrInfoMap.entrySet().iterator();

	while (it.hasNext()) {
		Entry entry = (Entry) it.next();
		String mbrid = (String) entry.getKey();
		MBRInfo value = (MBRInfo) entry.getValue();
		Map<String, Set<String>> tagUserMap = value.getTagUserIdMap();
		if (tagUserMap == null) {
			// removing the other mbrids which were not mapped to any images
			// from the map..

			it.remove();
			continue;
		}

		Iterator iter = tagUserMap.entrySet().iterator();
		StringBuffer tagCount = new StringBuffer();
		int i = 0;
		StringBuffer result = new StringBuffer();
		Set<String> allUserForMbr = new HashSet();
		while (iter.hasNext()) {
			Entry entry1 = (Entry) iter.next();
			String tag = (String) entry1.getKey();
			Set<String> userSet = (Set<String>) entry1.getValue();

			if (i == 0) {
				tagCount.append(tag).append(":").append(userSet.size());
				i = 1;
			} else {
				tagCount.append(" ").append(tag).append(":").append(userSet.size());
			}

			if (tagGlobalCountMap.containsKey(tag)) {
				tagGlobalCountMap.put(tag, tagGlobalCountMap.get(tag) + userSet.size());
			} else {
				tagGlobalCountMap.put(tag, userSet.size());
			}
			allUserForMbr.addAll(userSet);
		}

		int currentMbrTotalUser = allUserForMbr.size();
		value.setUserCount(currentMbrTotalUser);

		result.append(mbrid).append("\t").append(value.getArea()).append("\t").append(value.getUserCount())
				.append("\t").append(tagCount.toString());
		mbrwriter.write(result.toString() + "\n");
		mbrwriter.flush();
	}

	System.out.println("Size after removing the unmapped mbrs:" + mbrInfoMap.size());

	mbrwriter.close(); // write two files..

	it = tagGlobalCountMap.entrySet().iterator();

	while (it.hasNext()) {
		Entry entry1 = (Entry) it.next();
		String tag = (String) entry1.getKey();
		count = (int) entry1.getValue();
		tagwriter.write(tag + "\t" + count + "\n");
		tagwriter.flush();
	}
	tagwriter.close();
*/
	return mbrInfoMap;

}


private static Map<String, Set<String>> getTagUserIdMap(String userId, String title, String desc, String userTag) {
	Map<String, Set<String>> tagUserIdMap = new HashMap(); // tag, set of
															// userid
	if (!userTag.isEmpty()) {
		String tags[] = userTag.split(",");
		for (int i = 0; i < tags.length; i++) {
			if (!tags[i].isEmpty()) {
				if (tagUserIdMap.containsKey(tags[i])) {
					tagUserIdMap.get(tags[i]).add(userId);
				} else {
					Set<String> userIdSet = new HashSet();
					userIdSet.add(userId);
					tagUserIdMap.put(tags[i], userIdSet);
				}
			}
		}
	} else {
		String tags[] = title.split(",");
		for (int i = 0; i < tags.length; i++) {
			if (!tags[i].isEmpty()) {
				if (tagUserIdMap.containsKey(tags[i])) {
					tagUserIdMap.get(tags[i]).add(userId);
				} else {
					Set<String> userIdSet = new HashSet();
					userIdSet.add(userId);
					tagUserIdMap.put(tags[i], userIdSet);
				}

			}
		}
		tags = desc.split(",");
		for (int i = 0; i < tags.length; i++) {
			if (!tags[i].isEmpty()) {
				if (tagUserIdMap.containsKey(tags[i])) {
					tagUserIdMap.get(tags[i]).add(userId);
				} else {
					Set<String> userIdSet = new HashSet();
					userIdSet.add(userId);
					tagUserIdMap.put(tags[i], userIdSet);
				}
			}
		}

	}
	return tagUserIdMap;
}


private static Image getEqualDisImageThruWeiszfeld(List<Image> mbrImages) {

	// Collections.sort(mbrImages ); // by longitude
	double totalLong = 0.0;
	double totalLat = 0.0;
	int n = mbrImages.size();
	for (int i = 0; i < n; i++) {
		totalLong = totalLong + mbrImages.get(i).getLongitude();
		totalLat = totalLat + mbrImages.get(i).getLat();
	}

	double avgLat = totalLat / (double) n;
	double avgLong = totalLong / (double) n;

	Image p = new Image();
	p.setLat(avgLat);
	p.setLongitude(avgLong);
	double epsilon = 100.0;
	int count = 0;
	while (true) {

		Image q = medianApprox(p, mbrImages);
		double distancePQ = EvaluateHierarchy.getDistance(p.getLat(), p.getLongitude(), q.getLat(),
				q.getLongitude());

		if (distancePQ < epsilon) {
			return q;
		}
		p = q;
		count++;
		if (count % 100 == 0) {
			System.out.println("distancePQ:" + distancePQ);
		}
	}

	// return p;
}

private static Image medianApprox(Image p, List<Image> mbrImages) {
	double x = 0.0;
	double y = 0.0;
	double W = 0.0;
	for (Image q : mbrImages) {
		double distancePQ = EvaluateHierarchy.getDistance(p.getLat(), p.getLongitude(), q.getLat(),
				q.getLongitude());
		if (distancePQ != 0.0) {
			double w = 1.0 / distancePQ;
			W = W + w;
			x = x + q.getLat() * w;
			y = y + q.getLongitude() * w;
		}
	}

	Image s = new Image();
	s.setLat(x / W);
	s.setLongitude(y / W);
	return s;
}


private static double getJaccardSimilarity(String[] trainTags, String[] testTags) {
	Set<String> unionSet = new HashSet();
	for (int i = 0; i < trainTags.length; i++) {
		unionSet.add(trainTags[i]);
	}
	int intersectionCount = 0;
	for (int i = 0; i < testTags.length; i++) {
		if (unionSet.contains(testTags[i])) {
			intersectionCount++;
		}
		unionSet.add(testTags[i]);
	}
	// unionSet.addAll(trainTags)
	return ((double) intersectionCount) / ((double) unionSet.size());
}

}