package br.com.joaofelipefaria.mapdata.api.service;

import java.util.List;
import java.util.Optional;

import br.com.joaofelipefaria.mapdata.api.dto.MapdataDTO;

public interface MapDataService {

    public List<MapdataDTO> getAllMapData();

    public Optional<MapdataDTO> getMapDataById(Integer id);

    public void deleteById(Integer id);

    public void deleteAllMapData();

    public MapdataDTO create(MapdataDTO data);

    public MapdataDTO update(MapdataDTO data);
}