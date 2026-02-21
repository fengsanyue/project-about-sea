package com.ship.mapper;

import com.ship.entity.CityInfo;
import com.ship.entity.CityAlias;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CityMapper {

    // 根据城市名或别名查询城市
    @Select("SELECT ci.* FROM city_info ci " +
            "LEFT JOIN city_alias ca ON ci.id = ca.city_id " +
            "WHERE ci.city_name = #{name} OR ca.alias_name = #{name} " +
            "LIMIT 1")
    CityInfo findCityByName(@Param("name") String name);

    // 模糊搜索城市
    @Select("SELECT * FROM city_info WHERE city_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR pinyin LIKE CONCAT('%', #{keyword}, '%') " +
            "LIMIT 10")
    List<CityInfo> searchCities(@Param("keyword") String keyword);

    // 获取所有城市
    @Select("SELECT * FROM city_info ORDER BY province, city_name")
    List<CityInfo> getAllCities();

    // 根据省份获取城市
    @Select("SELECT * FROM city_info WHERE province = #{province} ORDER BY city_name")
    List<CityInfo> getCitiesByProvince(@Param("province") String province);

    // 获取城市别名
    @Select("SELECT * FROM city_alias WHERE city_id = #{cityId}")
    List<CityAlias> getCityAliases(@Param("cityId") Integer cityId);

    // 获取热门城市（首都或著名城市）
    @Select("SELECT * FROM city_info WHERE is_capital = 1 OR is_famous = 1 ORDER BY province")
    List<CityInfo> getHotCities();
}