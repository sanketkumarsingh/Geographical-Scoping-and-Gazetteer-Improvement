package thesis.data.entity;

public class Image implements Comparable{

	double lat;
	double longitude;
	String id;
	String tag;
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public int compareTo(Object o) {
		Image anotherObj = (Image) o;
		return Double.compare(longitude, anotherObj.getLongitude());
	}
	
	
	
}
