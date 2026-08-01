package brr.com.joaofelipefaria.mapdata.hex.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.joaofelipefaria.mapdata.api.dto.MapdataDTO;
import br.com.joaofelipefaria.mapdata.api.service.MapDataService;
import br.com.joaofelipefaria.mapdata.hex.entity.MapdataEntity;
import br.com.joaofelipefaria.mapdata.hex.repository.MapDataRepository;

@Service
public class MapdataServiceImpl implements MapDataService{

    @Autowired
    private MapDataRepository mapDataRepository;
    
	@Override
	public List<MapdataDTO> getAllMapData() {
        return mapDataRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
	}

	@Override
	public Optional<MapdataDTO> getMapDataById(Integer id) {
        return mapDataRepository.findById(id)
                .map(this::convertToDTO);
	}

	@Override
	public void deleteById(Integer id) {
        mapDataRepository.deleteById(id);
	}

	@Override
	public void deleteAllMapData() {
        mapDataRepository.deleteAll();
	}

	@Override
	public MapdataDTO create(MapdataDTO data) {
        MapdataEntity mapdataDb = convertToEntity(data);
        return convertToDTO(mapDataRepository.save(mapdataDb));
	}

	@Override
	public MapdataDTO update(MapdataDTO data) {
        MapdataEntity mapdataDb = convertToEntity(data);
        return convertToDTO(mapDataRepository.save(mapdataDb));
	}
	
	// --- Conversion Methods ---
    private MapdataDTO convertToDTO(MapdataEntity mapdata) {
        MapdataDTO dto = new MapdataDTO(mapdata.getId(), mapdata.getValue());
        return dto;
    }

    private MapdataEntity convertToEntity(MapdataDTO dto) {
        MapdataEntity entity = new MapdataEntity();
        entity.setId(dto.id());
        entity.setValue(dto.value());
        return entity;
    }

}
