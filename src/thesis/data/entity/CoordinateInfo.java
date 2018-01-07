package thesis.data.entity;

public class CoordinateInfo implements Comparable {
	double coordinate; // either lat or long
	String id;

	public double getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(double coordinate) {
		this.coordinate = coordinate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(Object o) {
		CoordinateInfo obj = (CoordinateInfo) o;
		return Double.compare(this.coordinate, obj.coordinate);
	}

}
