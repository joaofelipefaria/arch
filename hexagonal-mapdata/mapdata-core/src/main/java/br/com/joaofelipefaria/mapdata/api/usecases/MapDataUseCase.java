package br.com.joaofelipefaria.mapdata.api.usecases;

import java.util.List;

import br.com.joaofelipefaria.mapdata.api.dto.MetadataDTO;

public interface MapDataUseCase {

    public List<MetadataDTO> getMetadata(Integer id);

    public MetadataDTO getMetadataById(Integer idmetadata); 
    
    public MetadataDTO createMetadata(Integer mapdataId, MetadataDTO metadataDTO);

    public MetadataDTO updateMetadata(Integer mapdataId, Integer metadataId, MetadataDTO metadataDTO);

    public void deleteMetadata(Integer mapdataId, Integer metadataId);

    public void deleteAllMetadata(Integer mapdataId);
}