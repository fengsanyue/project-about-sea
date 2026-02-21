package com.ship.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CityInfo {
    private Integer id;
    private String cityName;
    private String province;
    private String country;
    private String pinyin;
    private String amapCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer isCapital;
    private Integer isFamous;
    private LocalDateTime createdTime;
}