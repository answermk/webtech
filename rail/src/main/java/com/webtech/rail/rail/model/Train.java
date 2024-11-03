package com.webtech.rail.rail.model;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trains")
public class Train {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long trainId;
        @NotNull
        private String name;
        @NotNull
        private String type;
        private Integer capacity;
        @NotNull
        private String status;
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    private LocalDateTime createdDate;

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }


    public void setCreatedDate(LocalDateTime createdAt) {
        this.createdDate= createdAt;
    }
    private LocalDateTime updatedDate;

    public LocalDateTime getUpdateDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedAt) {
        this.updatedDate = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }



    // private Boolean active;

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long TrainId) {
        this.trainId = TrainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Train{" +
                "trainId=" + trainId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
// Getters and setters
}

