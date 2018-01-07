package thesis.data.preparation.mbr;

import java.util.ArrayList;
import java.util.List;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import thesis.evaluation.EvaluateHierarchy;

public class GroundTruthForMBRs {


	public static String getRealBoundingboxByDivision(String place, String lat, String longitude, String key) throws Exception {
		final List<GeocodingResult[]> resps = new ArrayList<GeocodingResult[]>();

		// AIzaSyD3-9MtFvrnu3KizTXOGxMbZ7qPL_OwFcE sankjinta
		// AIzaSyCgQLqV8bcmZlSIxJit9TJd1qRrSEBqAfs sanketku
		// AIzaSyBKKIv3Bj-MlAhDJF9w3O4AADjxJNov06k sanket.vit
		// AIzaSyAMsa8AfIWJTWA58OBlsO7oiTyJ8GAZm2Q sankalp
		// AIzaSyC90thGZF-i7cy9CvsoapBt61WFtl4dN78 sankalp2

		GeoApiContext context = new GeoApiContext().setApiKey(key);
		double placeLat = Double.parseDouble(lat);
		double placeLong = Double.parseDouble(longitude);
		LatLng obj = new LatLng(Double.parseDouble(lat), Double.parseDouble(longitude));

		GeocodingResult[] results = GeocodingApi.newRequest(context).latlng(obj).await();
		Thread.sleep(1000);
		double lowerLat = 0.0;
		double lowerLong = 0.0;
		double upperLat = 0.0;
		double upperLong = 0.0;
		boolean notFound = true;
		if (results.length > 0) {
			int bestI = -1;
			int count = Integer.MIN_VALUE;
			for (int i = 0; i < results.length; i++) {
				if (results[i].addressComponents != null && results[i].addressComponents.length > 0) {
					String longName = results[i].addressComponents[0].longName;
					String placeArr[] = place.split(" ");
					boolean found = false;

					if (results[i].geometry != null && results[i].geometry.location != null) {
						double currentLat = results[i].geometry.location.lat;
						double currentLong = results[i].geometry.location.lng;
						double distance = EvaluateHierarchy.getDistance(currentLat, currentLong, placeLat, placeLong);
						if (distance > 30) {
							continue;
						}
					}

					int localCount = -1;
					for (int l = 0; l < placeArr.length; l++) {
						// System.out.println(placeArr[l].toLowerCase());
						// System.out.println("longName.toLowerCase():" +
						// longName.toLowerCase());
						if (!placeArr[l].toLowerCase().isEmpty() && placeArr[l].toLowerCase().length() > 2
								&& longName.toLowerCase().contains(placeArr[l].toLowerCase())) {
							// found = true;
							localCount++;
							// break;
						}
					}

					if (count < localCount) {
						count = localCount;
						bestI = i;
					}

				}
			}
			if (bestI != -1) {
				int i = bestI;
				AddressComponentType type[] = results[i].addressComponents[0].types;
				for (int j = 0; j < type.length; j++) {
					// if (placeLevel.equalsIgnoreCase(type[j].name()))
					// {
					if (results[i] != null && results[i].geometry != null && results[i].geometry.bounds != null) {
						lowerLat = results[i].geometry.bounds.southwest.lat;
						lowerLong = results[i].geometry.bounds.southwest.lng;
						upperLat = results[i].geometry.bounds.northeast.lat;
						upperLong = results[i].geometry.bounds.northeast.lng;
						notFound = false;
					} else {
						if (results[i] != null && results[i].geometry != null && results[i].geometry.viewport != null) {
							lowerLat = results[i].geometry.viewport.southwest.lat;
							lowerLong = results[i].geometry.viewport.southwest.lng;
							upperLat = results[i].geometry.viewport.northeast.lat;
							upperLong = results[i].geometry.viewport.northeast.lng;
							notFound = false;
						}

					}

					// }
				}

			}
			// }

			// }

		}

		if (notFound) {
			return null;

		} else {
			String result = lowerLat + "\t" + lowerLong + "\t" + upperLat + "\t" + upperLong;
			// System.out.println("result:" + result);
			return result;
		}

	}
}