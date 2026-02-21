package com.ship.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FaultDiagnosis {
    private Long id;
    private Long shipId;
    private String faultType;
    private String faultLevel;
    private String description;
    private String solution;
    private String detectedBy;
    private LocalDateTime detectedTime;
    private Integer isSolved;
}
