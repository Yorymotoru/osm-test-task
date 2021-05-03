package com.yorymotoru.geofromosm.controller;

import com.yorymotoru.geofromosm.model.GeoObject;
import com.yorymotoru.geofromosm.service.GeoObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeoObjectController {

    private final GeoObjectService geoObjectService;

    @Autowired
    public GeoObjectController(GeoObjectService geoObjectService) {
        this.geoObjectService = geoObjectService;
    }

    @GetMapping("/{type}/{name}")
    public ResponseEntity<GeoObject> find(@PathVariable String type, @PathVariable String name) {
        GeoObject geoObject = geoObjectService.find(type, name);
        return new ResponseEntity<>(geoObject, geoObject != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

}
