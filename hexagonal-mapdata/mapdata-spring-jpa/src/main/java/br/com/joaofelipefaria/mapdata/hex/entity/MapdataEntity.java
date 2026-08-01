package br.com.joaofelipefaria.mapdata.hex.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity
@Table(name = "mapdata")
public class MapdataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mapdata_id_seq")
    @SequenceGenerator(name = "mapdata_id_seq", sequenceName = "mapdata_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false)
    private String value;

    @Transient
    @OneToMany(mappedBy = "mapdata", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetadataEntity> metadata;

}
