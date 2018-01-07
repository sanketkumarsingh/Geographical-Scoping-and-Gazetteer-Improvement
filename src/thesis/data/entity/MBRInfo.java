package thesis.data.entity;

import java.util.Map;
import java.util.Set;

public class MBRInfo {

	Map<String, Set<String>> tagUserIdMap;
	double area;
	//Set<Image> assignedImageSet;
	String mbrId ;
	int totalImageAssigned;
	String parent;
	String level; // used in ConstraintStats
	int userCount ; 
	
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getParent() {
		return parent;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	MBR mbr;
//	public Set<Image> getAssignedImageSet() {
//		return assignedImageSet;
//	}
//	public void setAssignedImageSet(Set<Image> assignedImageSet) {
//		this.assignedImageSet = assignedImageSet;
//	}
	public Map<String, Set<String>> getTagUserIdMap() {
		return tagUserIdMap;
	}
	
	public void setTagUserIdMap(Map<String, Set<String>> tagUserIdMap) {
		this.tagUserIdMap = tagUserIdMap;
	}
	public double getArea() {
		return area;
	}
	public MBR getMbr() {
		return mbr;
	}
	public void setMbr(MBR mbr) {
		this.mbr = mbr;
	}
	public void setArea(double area) {
		this.area = area;
	}
	public int getTotalImageAssigned() {
		return totalImageAssigned;
	}
	public void setTotalImageAssigned(int totalImageAssigned) {
		this.totalImageAssigned = totalImageAssigned;
	}
	public String getMbrId() {
		return mbrId;
	}
	public void setMbrId(String mbrId) {
		this.mbrId = mbrId;
	}
	
	
}
