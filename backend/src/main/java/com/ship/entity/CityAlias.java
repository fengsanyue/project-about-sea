package com.ship.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CityAlias {
    private Integer id;
    private Integer cityId;
    private String aliasName;
    private String aliasType;
}