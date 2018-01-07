package thesis.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import thesis.data.entity.AncestorDescendant;



public class DataLoader {


	public static Map<String, AncestorDescendant> loadAllGeoNameChild(String fileName){
		Path hierarchyFile = Paths.get("hierarchy-representation-onlygeonames.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> parentChildMap = new HashMap();
		int count = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			count++;
			if (lineArr.length > 6) {
				parentChildMap.put(lineArr[0], lineArr[6]);
			}
			if (count % 1000000 == 0) {
				System.out.println("Processed places:" + count);
			}
		}
//"50-us-states-geo-mbr.txt"
		hierarchyFile = Paths.get(fileName);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int itemWithChild = 0;
		Map<String, AncestorDescendant> idChildParentIdMap = new HashMap();
		
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			AncestorDescendant hierarchy = new AncestorDescendant();
			Set<String> childIdSet = new HashSet();
//			Set<String> parentIdSet = new HashSet();
//			hierarchy.setAscendants(parentIdSet);
			hierarchy.setDescendants(childIdSet);
			idChildParentIdMap.put(lineArr[0], hierarchy);
		}
		
		
		hierarchyFile = Paths.get("1000-sample-geoname.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			AncestorDescendant hierarchy = new AncestorDescendant();
			Set<String> childIdSet = new HashSet();
//			Set<String> parentIdSet = new HashSet();
//			hierarchy.setAscendants(parentIdSet);
			hierarchy.setDescendants(childIdSet);
			idChildParentIdMap.put(lineArr[0], hierarchy);
		}
		
		hierarchyFile = Paths.get(fileName);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (parentChildMap.containsKey(lineArr[0])) {
				String children[] = parentChildMap.get(lineArr[0]).split(" ");
				if (children.length == 0) {
					System.out.println("No child for:" + lineArr[0]);
					continue;
				}
				Set<String> allChildrenSet = new HashSet();
				getAllChildren(children, parentChildMap, allChildrenSet);
				if (allChildrenSet.size() != 0) {
					itemWithChild++;
				}
				idChildParentIdMap.get(lineArr[0]).setDescendants(allChildrenSet);
			}
		}
		
		System.out.println("Loaded children for places in testset.");
		
		hierarchyFile = Paths.get("1000-sample-geoname.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (parentChildMap.containsKey(lineArr[0])) {
				String children[] = parentChildMap.get(lineArr[0]).split(" ");
				if (children.length == 0) {
					System.out.println("No child for:" + lineArr[0]);
					continue;
				}
				Set<String> allChildrenSet = new HashSet();
				getAllChildren(children, parentChildMap, allChildrenSet);
				if (allChildrenSet.size() != 0) {
					itemWithChild++;
				}
				idChildParentIdMap.get(lineArr[0]).setDescendants(allChildrenSet);
			}
		}
		
		System.out.println("Loaded all children.." + itemWithChild);

		return idChildParentIdMap;
	}
	
	

	public static Map<String, AncestorDescendant> loadAllOSMNameChild(String fileName){
		Path hierarchyFile = Paths.get("osmname-hierarchy.txt");
		Stream<String> gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, String> parentChildMap = new HashMap();
		int count = 0;
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			count++;
			//if (lineArr[0]) {
			String children[] = lineArr[6].split(" ");
			if(children.length ==1 && children[0].equals(lineArr[0])){
				continue;
			}else{
				parentChildMap.put(lineArr[0], lineArr[6]);
			}
			//}
			if (count % 1000000 == 0) {
				System.out.println("Processed places:" + count);
			}
		}
//"50-us-states-geo-mbr.txt"
		hierarchyFile = Paths.get(fileName);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int itemWithChild = 0;
		Map<String, AncestorDescendant> idChildParentIdMap = new HashMap();
		
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			AncestorDescendant hierarchy = new AncestorDescendant();
			Set<String> childIdSet = new HashSet();
//			Set<String> parentIdSet = new HashSet();
//			hierarchy.setAscendants(parentIdSet);
			hierarchy.setDescendants(childIdSet);
			idChildParentIdMap.put(lineArr[0], hierarchy);
		}
		
		
		hierarchyFile = Paths.get("1000-sample-osmname.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			AncestorDescendant hierarchy = new AncestorDescendant();
			Set<String> childIdSet = new HashSet();
//			Set<String> parentIdSet = new HashSet();
//			hierarchy.setAscendants(parentIdSet);
			hierarchy.setDescendants(childIdSet);
			idChildParentIdMap.put(lineArr[0], hierarchy);
		}
		
		hierarchyFile = Paths.get(fileName);
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (parentChildMap.containsKey(lineArr[0])) {
				String children[] = parentChildMap.get(lineArr[0]).split(" ");
				if (children.length == 1 && children[0].equals(lineArr[0])) {
					System.out.println("No child for:" + lineArr[0]);
					continue;
				}
				Set<String> allChildrenSet = new HashSet();
				getAllChildrenForOSM(children, parentChildMap, allChildrenSet, lineArr[0]);
				if (allChildrenSet.size() != 0) {
					itemWithChild++;
				}
				idChildParentIdMap.get(lineArr[0]).setDescendants(allChildrenSet);
			}
		}
		
		System.out.println("Loaded children for places in testset.");
		
		hierarchyFile = Paths.get("1000-sample-osmname.txt");
		gridlines = null;
		try {
			gridlines = Files.lines(hierarchyFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String line : (Iterable<String>) gridlines::iterator) {
			String lineArr[] = line.split("\t");
			if (parentChildMap.containsKey(lineArr[0])) {
				String children[] = parentChildMap.get(lineArr[0]).split(" ");
				if (children.length == 1 && children[0].equals(lineArr[0])) {
					System.out.println("No child for:" + lineArr[0]);
					continue;
				}
				Set<String> allChildrenSet = new HashSet();
				getAllChildrenForOSM(children, parentChildMap, allChildrenSet, lineArr[0]);
				if (allChildrenSet.size() != 0) {
					itemWithChild++;
				}
				idChildParentIdMap.get(lineArr[0]).setDescendants(allChildrenSet);
			}
		}
		
		System.out.println("Loaded all children.." + itemWithChild);

		return idChildParentIdMap;
	}
	

	private static void getAllChildrenForOSM(String[] children, Map<String, String> childParentMap,
			Set<String> allChildrenSet, String parentid) {
		for (int i = 0; i < children.length; i++) {
			if(children[i].equals(parentid)){
				allChildrenSet.add(children[i]);
				continue;
			}
			allChildrenSet.add(children[i]);
			if (childParentMap.containsKey(children[i])) {
				String nextLevelChildren[] = childParentMap.get(children[i]).split(" ");
				getAllChildrenForOSM(nextLevelChildren, childParentMap, allChildrenSet, children[i]);
			}
		}
	}
	
	
	private static void getAllChildren(String[] children, Map<String, String> childParentMap,
			Set<String> allChildrenSet) {
		for (int i = 0; i < children.length; i++) {
			allChildrenSet.add(children[i]);
			if (childParentMap.containsKey(children[i])) {
				String nextLevelChildren[] = childParentMap.get(children[i]).split(" ");
				getAllChildren(nextLevelChildren, childParentMap, allChildrenSet);
			}
		}
	}
	
	
}
