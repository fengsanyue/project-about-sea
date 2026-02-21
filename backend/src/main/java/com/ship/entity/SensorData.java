package com.ship.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SensorData {
    private Long id;
    private Long shipId;
    private String sensorType;
    private String parameterName;
    private BigDecimal parameterValue;
    private String unit;
    private BigDecimal standardMin;
    private BigDecimal standardMax;
    private LocalDateTime collectTime;
    private Integer isAbnormal;
}
