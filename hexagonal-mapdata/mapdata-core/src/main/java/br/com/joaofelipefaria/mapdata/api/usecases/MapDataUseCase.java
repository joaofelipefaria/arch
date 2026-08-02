package br.com.joaofelipefaria.mapdata.api.usecases;

import java.util.List;

import br.com.joaofelipefaria.mapdata.api.dto.MetadataDTO;

public interface MapDataUseCase {

    List<MetadataDTO> getMetadata(Integer id);

    MetadataDTO getMetadataById(Integer metadataId);

    MetadataDTO createMetadata(Integer mapdataId, MetadataDTO metadataDTO);

    MetadataDTO updateMetadata(Integer mapdataId, Integer metadataId, MetadataDTO metadataDTO);

    void deleteMetadata(Integer mapdataId, Integer metadataId);

    void deleteAllMetadata(Integer mapdataId);
}