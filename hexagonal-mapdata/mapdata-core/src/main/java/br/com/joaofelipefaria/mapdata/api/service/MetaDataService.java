package br.com.joaofelipefaria.mapdata.api.service;

import java.util.List;
import java.util.Optional;

import br.com.joaofelipefaria.mapdata.api.dto.MetadataDTO;

public interface MetaDataService {

    public Optional<MetadataDTO> getMetadataById(Integer id);

    public List<MetadataDTO> getMetadataByMapdataId(Integer mapdataId);

    public MetadataDTO createMetadata(Integer mapdataId, MetadataDTO metadataDTO);

    public MetadataDTO updateMetadata(Integer mapdataId, Integer metadataId, MetadataDTO metadataDTO);

    public void deleteMetadataById(Integer id);

    public void deleteMetadataByMapdataId(Integer id);
}