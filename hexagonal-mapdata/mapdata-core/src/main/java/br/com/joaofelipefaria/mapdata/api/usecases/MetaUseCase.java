package br.com.joaofelipefaria.mapdata.api.usecases;

import java.util.List;

import br.com.joaofelipefaria.mapdata.api.dto.MapdataDTO;

public interface MetaUseCase {

    public List<MapdataDTO> getAllMapData();

    public MapdataDTO getMapDataById( Integer id);

    public MapdataDTO createMapData(MapdataDTO data);

    public MapdataDTO updateMapData(Integer id, MapdataDTO data);

    public void deleteMapDataById(Integer id);

    public void deleteAllMapData();
}