package dev.overwave.whereeat.core.geocoding;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MapService {

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    @PostConstruct
    private void init() {
//        try (GeoApiContext context = new GeoApiContext.Builder().apiKey(googleMapsApiKey).build()) {
//            GeocodingResult[] results = GeocodingApi.newRequest(context).region("ru").language("ru").address("спб улица маршала тухачевского 23").await();
//            System.out.println(Arrays.toString(results));
//        }
    }

    @PreDestroy
    private void shutdown() {

    }
}
