package br.com.joaofelipefaria.mapdata.hex.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.joaofelipefaria.mapdata.api.dto.MapdataDTO;
import br.com.joaofelipefaria.mapdata.api.service.MapDataService;
import br.com.joaofelipefaria.mapdata.hex.entity.MapdataEntity;
import br.com.joaofelipefaria.mapdata.hex.repository.MapDataRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapdataServiceImpl implements MapDataService {

    private final MapDataRepository mapDataRepository;

    @Override
    public List<MapdataDTO> getAllMapData() {
        return mapDataRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
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
        MapdataEntity entity = convertToEntity(data);
        return convertToDTO(mapDataRepository.save(entity));
    }

    @Override
    public MapdataDTO update(MapdataDTO data) {
        MapdataEntity entity = convertToEntity(data);
        return convertToDTO(mapDataRepository.save(entity));
    }

    private MapdataDTO convertToDTO(MapdataEntity entity) {
        return new MapdataDTO(entity.getId(), entity.getValue());
    }

    private MapdataEntity convertToEntity(MapdataDTO dto) {
        MapdataEntity entity = new MapdataEntity();
        entity.setId(dto.id());
        entity.setValue(dto.value());
        return entity;
    }
}
