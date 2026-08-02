package br.com.joaofelipefaria.mapdata.api.service;

import java.util.List;
import java.util.Optional;

import br.com.joaofelipefaria.mapdata.api.dto.MapdataDTO;

public interface MapDataService {

    List<MapdataDTO> getAllMapData();

    Optional<MapdataDTO> getMapDataById(Integer id);

    void deleteById(Integer id);

    void deleteAllMapData();

    MapdataDTO create(MapdataDTO data);

    MapdataDTO update(MapdataDTO data);
}