package br.com.joaofelipefaria.mapdata.hex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity
@Table(name = "metadata")
public class MetadataEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metadata_id_seq")
    @SequenceGenerator(name = "metadata_id_seq", sequenceName = "metadata_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idmapdata", nullable = false)
    private MapdataEntity mapdata;

    @Column(nullable = false)
    private String value1;

    @Column(nullable = false)
    private String value2;

}
