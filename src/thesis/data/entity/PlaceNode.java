package thesis.data.entity;

import java.util.Set;

public class PlaceNode {

	Set<String> childGeonameIdSet;
	String leftLowerLat;
	String leftLowerLong;
	String parentGeonameId;

	public String getParentGeonameId() {
		return parentGeonameId;
	}

	public void setParentGeonameId(String parentGeonameId) {
		this.parentGeonameId = parentGeonameId;
	}

	public Set<String> getChildGeonameIdSet() {
		return childGeonameIdSet;
	}

	public void setChildGeonameIdSet(Set<String> childGeonameIdSet) {
		this.childGeonameIdSet = childGeonameIdSet;
	}

	public String getLeftLowerLat() {
		return leftLowerLat;
	}

	public void setLeftLowerLat(String leftLowerLat) {
		this.leftLowerLat = leftLowerLat;
	}

	public String getLeftLowerLong() {
		return leftLowerLong;
	}

	public void setLeftLowerLong(String leftLowerLong) {
		this.leftLowerLong = leftLowerLong;
	}

	public String getRightUpLat() {
		return rightUpLat;
	}

	public void setRightUpLat(String rightUpLat) {
		this.rightUpLat = rightUpLat;
	}

	public String getRightUpLong() {
		return rightUpLong;
	}

	public void setRightUpLong(String rightUpLong) {
		this.rightUpLong = rightUpLong;
	}

	String rightUpLat;
	String rightUpLong;
	// List<String> childGeonameIdList;
}