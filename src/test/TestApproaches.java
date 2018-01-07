package test;

import java.io.IOException;

import thesis.approach.geometric.CenterMBR;
import thesis.approach.heuristic.expansion.EnlargeMBR;
import thesis.approach.heuristic.expansion.ScaleMBR;
import thesis.approach.heuristic.outlier.Bagplot;
import thesis.approach.heuristic.outlier.Boxplot;
import thesis.approach.hierarchical.ChildrenMBRForGeoName;
import thesis.approach.probablistic.PomMBRForGeoName;
import thesis.approach.probablistic.PomMBRForOSMName;

public class TestApproaches {

	public static void testApproaches() {
		getChildrenMBR();
		getCenterMBR();
		getChildrenMbrWithoutOutlierUsingBoxplot();
		getChildrenMbrWithoutOutlierUsingBagplot();
		getHybridMBR();
		getProbabilisticMBR();
	}
	
	/**
	 *  The method requires allCountries.txt, hierarchy-representation-onlygeonames.txt file. The filename
	 *  are hard-coded in the code. These files can be found in the project home directory.
	 *  number of place: It will retrieve childrenMBR for specified number of places. 
	 *  key: Generate the Google Maps reverse geocoding api key and set it to key variable below.
	 */
	private static void getChildrenMBR(){
		int numberOfPlace = 140;
		String key = "Your key";
		ChildrenMBRForGeoName.getChildrenMBRForGeoNames(key, numberOfPlace);
	}
	
	/**
	 * This method generates the childrenMbr for the place. centerMBR method requires 
	 * childrenMBR file. 140-geo-childrenMbr.txt is provided as an example below.
	 */
	private static void getCenterMBR(){
		String childrenMbrFile = "140-geo-childrenMbr.txt";
		CenterMBR.getCenterMBR(childrenMbrFile);
	}
	
	
	/**
	 * This method generate childrenMbr after removing outliers using boxplot.
	 *  It requires childrenMbr file and the
	 * output filename.
	 */
	private static void getChildrenMbrWithoutOutlierUsingBoxplot(){
		Boxplot.generateHierarchyWithoutOutlierForOSMName("160-osm-childrenMbr.txt", "160-osm-boxplotMbr.txt");
		Boxplot.generateHierarchyWithoutOutlierForGeoName("140-geo-childrenMbr.txt", "140-geo-boxplotMbr.txt");

	}
	
	/**
	 * This method generates MBR after remvoing outlier using Bagplot
	 */
	private static void getChildrenMbrWithoutOutlierUsingBagplot(){
	     
//		To generate MBR using bagplot:
//			i) Generate file required for bagplot:
		String childrenMbr = "140-geo-childrenMbr.txt";
		String outputFolder =  "/Users/sanket/Documents/workspace/yfcc/child/140geo/";
		Bagplot.generateChildFileForAllIdsForOSMName(childrenMbr, outputFolder);
		 childrenMbr = "140-osm-childrenMbr.txt";
		 outputFolder =  "/Users/sanket/Documents/workspace/yfcc/child/160osm/";
		Bagplot.generateChildFileForAllIdsForGeoNames(childrenMbr, outputFolder);
		
//			ii) With files generated, run the R script in home directory. use outputFolder folder above
//		as input directory while running the script.
//		140bagplot.R
//		160bagplot.R
		
//			iii) With result of bagplot, run following to generated the MBR: 
		String bagplotInputFile = "140-bagplot-mbr-witharea-8000limit.txt";
		String outlierInputFile = "140-id-count-outlier-witharea-8000limit.txt";
		String bagplotOutputFolder = "/Users/sanket/Documents/workspace/yfcc/child/140geoout/";
		String childrenMbrFile = "140-geo-childrenMbr.txt";
		String outBagplotFile = "140-geo-bagplotMbr.txt" ;
		String outOutlierFile = "140-geo-outliers.txt";
		Bagplot.getMbrPostBagplotForGeo(bagplotInputFile, outlierInputFile, bagplotOutputFolder, childrenMbrFile, outBagplotFile, outOutlierFile);
		
		 bagplotInputFile = "160-bagplot-mbr-witharea-8000limit.txt";
		 outlierInputFile = "160-id-count-outlier-witharea-8000limit.txt";
		 bagplotOutputFolder = "/Users/sanket/Documents/workspace/yfcc/child/160osmout/";
		 childrenMbrFile = "160-osm-childrenMbr.txt";
		 outBagplotFile = "160-osm-bagplotMbr.txt" ;
		 outOutlierFile = "160-osm-outliers.txt";
		Bagplot.getMbrPostBagplotForOSM(bagplotInputFile, outlierInputFile, childrenMbrFile, bagplotOutputFolder, outBagplotFile, outOutlierFile);
	}

	
	/**
	 * This method generate hybrid MBRs based on enlargement and 
	 */
	private static void getHybridMBR(){
		String childrenMbrFile = "140-geo-childrenMbr.txt";
		String centerMbrFile = "140-geo-centerMbr.txt";
		String outlierMbrFile = "140-geo-boxplotMbr.txt" ;
		String intersectionFileName = "140-geo-center-boxplot-intersectionMbr.txt";
		String outFileName = "140-geo-boxplot-enlarge-hybridMbr.txt";
		EnlargeMBR.getEnlargeMBR(childrenMbrFile, centerMbrFile,outlierMbrFile,
				intersectionFileName, outFileName);
		outFileName = "140-geo-boxplot-scale-hybridMbr.txt";
		ScaleMBR.getScaleMBR(childrenMbrFile, centerMbrFile,outlierMbrFile,
				intersectionFileName, outFileName);
	}
	
	/**
	 * This method generate POM based MBR. Few of the required files are hardcode such as: 
	 * hierarchy files , sample files. These files are provided in project home directory.
	 */
	private static void getProbabilisticMBR(){
		String childrenMbrFile= "140-geo-childrenMbr.txt"; 
		String outputMbrFile = "140-geo-pomMbr.txt";
		try {
			PomMBRForGeoName.getPomMbrForGeoName( childrenMbrFile,  outputMbrFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		childrenMbrFile= "160-osm-childrenMbr.txt"; 
		outputMbrFile = "160-osm-pomMbr.txt";
		try {
			PomMBRForOSMName.getPomMbrForOSMName(childrenMbrFile, outputMbrFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
