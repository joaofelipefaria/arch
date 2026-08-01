package br.com.joaofelipefaria.mapdata.hex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.joaofelipefaria.mapdata.hex.entity.MapdataEntity;

@Repository
public interface MapDataRepository extends JpaRepository<MapdataEntity, Integer> {
}