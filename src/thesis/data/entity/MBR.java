package thesis.data.entity;

public class MBR implements Comparable {

	double neLat;
	double neLong;
	double swLat;
	double swLong;
	String id;
	//double area;

	public double getNeLat() {
		return neLat;
	}

	public void setNeLat(double neLat) {
		this.neLat = neLat;
	}

//	public double getArea() {
//		return area;
//	}
//
//	public void setArea(double area) {
//		this.area = area;
//	}

	public double getNeLong() {
		return neLong;
	}

	public void setNeLong(double neLong) {
		this.neLong = neLong;
	}

	public double getSwLat() {
		return swLat;
	}

	public void setSwLat(double swLat) {
		this.swLat = swLat;
	}

	public double getSwLong() {
		return swLong;
	}

	public void setSwLong(double swLong) {
		this.swLong = swLong;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(Object o) {
		MBR anotherObj = (MBR) o;
		if (this.getSwLong() > anotherObj.getSwLong()) {
			return 1;
		} else if (this.getSwLong() < anotherObj.getSwLong()) {
			return -1;
		}
		//return 0;
		return Double.valueOf(this.getSwLat()).compareTo(anotherObj.getSwLat());
	}

}