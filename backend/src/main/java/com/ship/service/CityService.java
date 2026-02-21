package com.ship.service;

import com.ship.entity.CityInfo;
import com.ship.mapper.CityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class CityService {

    @Autowired
    private CityMapper cityMapper;

    // 从消息中智能提取城市
    public CityInfo extractCityFromMessage(String message) {
        System.out.println("原始消息: " + message);

        // 1. 尝试匹配"XX天气"格式
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,})[的]?(?:天气|气温|温度|天气怎么样|气温如何|多少度)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String cityName = matcher.group(1);
            System.out.println("模式1提取到城市: " + cityName);
            CityInfo city = cityMapper.findCityByName(cityName);
            if (city != null) return city;
        }

        // 2. 匹配"在XX"格式
        pattern = Pattern.compile("[在去到来回往]\\s*([\\u4e00-\\u9fa5]{2,})");
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            String cityName = matcher.group(1);
            System.out.println("模式2提取到城市: " + cityName);
            CityInfo city = cityMapper.findCityByName(cityName);
            if (city != null) return city;
        }

        // 3. 直接匹配数据库中的城市
        List<CityInfo> allCities = cityMapper.getAllCities();
        for (CityInfo city : allCities) {
            if (message.contains(city.getCityName())) {
                System.out.println("直接匹配到城市: " + city.getCityName());
                return city;
            }
        }

        // 4. 如果包含"这里"、"当地"，返回默认（北京）
        if (message.contains("这里") || message.contains("当地") ||
                message.contains("所在") || message.contains("当前位置")) {
            System.out.println("使用当前位置，默认北京");
            return cityMapper.findCityByName("北京");
        }

        // 5. 默认返回北京
        System.out.println("未匹配到城市，使用默认北京");
        return cityMapper.findCityByName("北京");
    }

    // 根据城市名获取城市
    public CityInfo getCityByName(String name) {
        return cityMapper.findCityByName(name);
    }

    // 搜索城市
    public List<CityInfo> searchCities(String keyword) {
        return cityMapper.searchCities(keyword);
    }

    // 获取热门城市
    public List<CityInfo> getHotCities() {
        return cityMapper.getHotCities();
    }

    // 获取所有城市
    public List<CityInfo> getAllCities() {
        return cityMapper.getAllCities();
    }
}
