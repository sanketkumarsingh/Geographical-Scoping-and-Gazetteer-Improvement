package thesis.data.entity;

import java.util.Comparator;

public class PlaceByLat implements Comparator<Place>{

	@Override
	public int compare(Place o1, Place o2) {
		// TODO Auto-generated method stub
		return Double.compare(o1.lat, o2.lat);
	}
}

