package com.yorymotoru.geofromosm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoObject {

    private Long id;
    private String name;
    private List<Double> center;
    private List<List<Double>> coordinates;

}
