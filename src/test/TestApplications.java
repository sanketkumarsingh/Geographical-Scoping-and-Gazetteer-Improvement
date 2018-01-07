package test;

import thesis.applications.enrichment.GeotagByMBR;
import thesis.applications.refinement.GeoNameRefinement;
import thesis.applications.topologicalconstraints.SoftConstraintStats;

public class TestApplications {

	
	public static void testApplications(){
		refineGeoNameHierarchy();
		enrichGeoNameWithYFCC();
		generateDataForSoftConstraint();
	}

	/**
	 * This method map 500000 photos from flickr to places in GeoNames based on the MBRs.
	 * MBRs are generated and in files: 420-pom-mbr-forapp.txt, 78639-center-mbr.txt, 14215-childrenmbr.txt
	 * The names of these files are hardcoded. 
	 * The train and test data consist of one file each with MBRid mapped to images id. File names are :
	 * image-mbrids-train-mediaeval-allmap-new.txt , image-mbrids-test-mediaeval-allmap-new.txt
	 * 
	 * MBR prediction file generated :   test-mbr-prediction-user-normal-new-base.txt
	 * Coordinate Prediction File generated :  test-mbr-result-user-normalization-new-base.txt
	 * Method also output the average distance error
	 */
	private static void enrichGeoNameWithYFCC(){
		GeotagByMBR.geotagByMbr();
	}
	
	
	
	/**
	 * This method generates the soft constraint stats by finding normalized 
	 * overlap area between the places at the same level. 
	 */
	private static void generateDataForSoftConstraint(){
		String constraintInputFile = "constraint-ADM1-new.txt"; 
		String adminLevel = "ADM1"; // ADM2, ADM3 are other values
		String outputFileName = "constraint-ADM1-areanormal-intersectionwithADM1-new.txt";
		SoftConstraintStats.generateStatsForSoftConstraint(constraintInputFile, adminLevel, outputFileName);
	}
	
	
	/**
	 *  Refining is three step process. All filenames are hardcoded. 
	 *  It require hierarchy obtained from spatial hierarchy expressed in a gazetteer.
	 */
	private static void refineGeoNameHierarchy(){
		
		GeoNameRefinement.step1();
		// sort the file using unix command and then uncomment and run step3: 
//		sort -k1,1n hierarchy-refine-phase1-new.txt > sorted-hierarchy-refine-phase1-new.txt
//		GeoNameRefinement.step3();
		
	}

	
}
