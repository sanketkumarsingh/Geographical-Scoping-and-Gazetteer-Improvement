package thesis.data.entity;

public class Geoname {
	String area;
	String child[];
	String geoMbr; // Used in DetectOutlier, not in this class.

	public String getArea() {
		return area;
	}

	public String getGeoMbr() {
		return geoMbr;
	}

	public void setGeoMbr(String geoMbr) {
		this.geoMbr = geoMbr;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String[] getChild() {
		return child;
	}

	public void setChild(String[] child) {
		this.child = child;
	}

}
