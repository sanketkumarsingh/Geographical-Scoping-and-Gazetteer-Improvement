package thesis.data.entity;

import java.util.Comparator;

public class PlaceByLong implements Comparator<Place>{
	@Override
	public int compare(Place o1, Place o2) {
		// TODO Auto-generated method stub
		return Double.compare(o1.lon, o2.lon);
	}
}