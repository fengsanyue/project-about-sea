package com.ship.controller;

import com.ship.entity.CityInfo;
import com.ship.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class DatabaseTestController {

    @Autowired
    private CityService cityService;

    @GetMapping("/city/{name}")
    public Map<String, Object> testCity(@PathVariable String name) {
        Map<String, Object> result = new HashMap<>();

        try {
            CityInfo city = cityService.getCityByName(name);
            if (city != null) {
                result.put("code", 200);
                result.put("data", city);
                result.put("message", "找到城市: " + city.getCityName());
            } else {
                result.put("code", 404);
                result.put("message", "未找到城市: " + name);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "错误: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}