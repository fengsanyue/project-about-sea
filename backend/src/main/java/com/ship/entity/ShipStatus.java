package com.ship.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipStatus {
    private Long id;
    private Long shipId;
    private BigDecimal speed;
    private BigDecimal fuelConsumption;
    private BigDecimal engineTemp;
    private BigDecimal oilPressure;
    private BigDecimal remainingFuel;
    private BigDecimal predictedRange;
    private LocalDateTime updateTime;
}
