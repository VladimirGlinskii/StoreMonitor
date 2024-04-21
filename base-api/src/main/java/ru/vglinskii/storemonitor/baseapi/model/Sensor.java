package ru.vglinskii.storemonitor.baseapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "sensor")
public class Sensor extends BaseEntity {
    @Column(name = "location", nullable = false)
    private String location;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "inventory_number", nullable = false, length = 15)
    private String inventoryNumber;

    @Column(name = "factory_code", nullable = false, unique = true, length = 15)
    private String factoryCode;
}