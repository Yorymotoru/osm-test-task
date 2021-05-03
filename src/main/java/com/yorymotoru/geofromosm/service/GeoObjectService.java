package com.yorymotoru.geofromosm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yorymotoru.geofromosm.model.GeoObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

@Service
public class GeoObjectService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(GeoObjectService.class);

    @Autowired
    public GeoObjectService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Cacheable(cacheNames = "geo object")
    public GeoObject find(String type, String name) {
        GeoObject geoObject = null;
        String osm = "https://nominatim.openstreetmap.org/search?#type#=#search#&country=russia&format=json&polygon_geojson=1";
        type = type.toLowerCase();
        final String[] types = {"state", "region", "republic", "district", "county", "область", "республика", "край", "округ"};
        String sType = Arrays.asList(types).contains(type) ? "state" : "q";

        ResponseEntity<String> response = restTemplate.getForEntity(
                osm.replace("#type#", sType).replace("#search#", name), String.class);

        try {
            if (!objectMapper.readTree(response.getBody()).has(0)) {
                throw new NullPointerException();
            }

            JsonNode root = objectMapper.readTree(response.getBody()).path(0);
            JsonNode geojson = root.path("geojson");
            JsonNode cords = geojson.path("coordinates").path(0);

            if (geojson.path("type").asText().equals("MultiPolygon")) {
                cords = cords.path(0);
            }
            List<List<Double>> coordinates = objectMapper.readValue(cords.toString(), new TypeReference<>() {});

            List<Double> center = extractCenter(coordinates);

            geoObject = new GeoObject(
                    root.path("place_id").asLong(),
                    root.path("display_name").asText(),
                    center,
                    coordinates
            );
        } catch (JsonProcessingException e) {
            log.warn("Couldn't parse the json that came in on the request: [type = " + type + ", name = " + name + "]");
        } catch (NullPointerException e) {
            log.info("An empty response came: [type = " + type + ", name = " + name + "]");
        }

        return geoObject;
    }

    // Поиск географического центра, как центра тяжести площади поверхности с очертанием местности
    private List<Double> extractCenter(List<List<Double>> cords) {
        double S = 0.0;
        for (int i = 0; i < cords.size() - 1; i++) {
            S += cords.get(i).get(0) * cords.get(i + 1).get(1);
            S -= cords.get(i + 1).get(0) * cords.get(i).get(1);
        }
        S += cords.get(cords.size() - 1).get(0) * cords.get(0).get(1);
        S -= cords.get(0).get(0) * cords.get(cords.size() - 1).get(1);
        S /= 2;

        double x = 0, y = 0;
        for (int i = 0; i < cords.size() - 1; i++) {
            double xx, yy, ss;
            xx = (cords.get(i).get(0) + cords.get(i + 1).get(0)) / 3;
            yy = (cords.get(i).get(1) + cords.get(i + 1).get(1)) / 3;

            ss = cords.get(i).get(0) * cords.get(i + 1).get(1);
            ss -= cords.get(i + 1).get(0) * cords.get(i).get(1);
            ss /= 2;

            x += xx * ss;
            y += yy * ss;
        }

        S = abs(S);
        x = x / S;
        y = y / S;

        List<Double> center = new ArrayList<>();
        center.add(x);
        center.add(y);

        return center;
    }

}
