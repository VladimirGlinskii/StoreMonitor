package ru.vglinskii.storemonitor.baseapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "sensor", fetch = FetchType.LAZY)
    @OrderBy("datetime DESC")
    private List<SensorValue> values = new ArrayList<>();
}