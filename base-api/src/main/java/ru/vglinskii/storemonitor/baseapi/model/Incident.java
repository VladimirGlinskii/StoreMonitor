package ru.vglinskii.storemonitor.baseapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.vglinskii.storemonitor.common.enums.IncidentType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "incident")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private IncidentType eventType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "datetime", nullable = false)
    private Instant datetime;

    @Column(name = "called_police", nullable = false)
    private boolean calledPolice;

    @Column(name = "called_ambulance", nullable = false)
    private boolean calledAmbulance;

    @Column(name = "called_fire_department", nullable = false)
    private boolean calledFireDepartment;

    @Column(name = "called_gas_service", nullable = false)
    private boolean calledGasService;
}