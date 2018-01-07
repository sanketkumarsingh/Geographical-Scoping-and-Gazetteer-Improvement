package thesis.data.entity;

import java.util.Comparator;

public class Place implements Comparable{
	double lat;

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	double lon;
	String id;

	double distance;

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
//
//	@Override
//	public boolean equals(Object obj) {
//		Place anotherPlace = (Place)obj;
//		if(this.lat == anotherPlace.lat && this.lon == anotherPlace.lon){
//			return true;
//		}
//		return false;
//	}
//	
//	
//	public int hashCode() {
//		int result = 17;
//		result = 37*result + (int)lat;
//		result = 37*result + (int)lon;
//		
//		return result;
//		}
//	
	@Override
	public int compareTo(Object o) {
		Place obj = (Place) o;
		// if (this.distance > obj.distance) {
		// return 1;
		// } else if (this.distance < obj.distance) {
		// return -1;
		// } else {
		// return 0;
		// }
		
		return Double.compare(this.distance, obj.distance);
	}
}


