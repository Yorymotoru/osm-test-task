package com.yorymotoru.geofromosm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GeoFromOsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeoFromOsmApplication.class, args);
    }

}
