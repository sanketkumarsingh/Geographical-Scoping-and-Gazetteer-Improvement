package thesis.data.entity;

import java.util.Set;


public class PlaceInformation {

	Set<String> childList;
	double lat;
	double longitude;
	BoundingBox bb;
	String name;
	String parentId;

	public Set<String> getChildList() {
		return childList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setChildList(Set<String> childList) {
		this.childList = childList;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public BoundingBox getBb() {
		return bb;
	}

	public void setBb(BoundingBox bb) {
		this.bb = bb;
	}

}
