package com.ship.mapper;

import com.ship.entity.ShipStatus;
import com.ship.entity.SensorData;
import com.ship.entity.FaultDiagnosis;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ShipMapper {

    // 从 ship_info 表获取船舶信息
    @Select("SELECT * FROM ship_info WHERE id = #{shipId}")
    ShipStatus getShipInfo(@Param("shipId") Long shipId);

    // 从 sensor_data 表获取最近的传感器数据
    @Select("SELECT * FROM sensor_data WHERE ship_id = #{shipId} ORDER BY collect_time DESC LIMIT 20")
    List<SensorData> getRecentSensorData(@Param("shipId") Long shipId);

    // 从 fault_record 表获取最近的故障记录
    @Select("SELECT * FROM fault_record WHERE ship_id = #{shipId} ORDER BY fault_time DESC LIMIT 10")
    List<FaultDiagnosis> getRecentFaults(@Param("shipId") Long shipId);

    // 插入传感器数据
    @Insert("INSERT INTO sensor_data(ship_id, sensor_type, parameter_name, parameter_value, unit, " +
            "standard_min, standard_max, collect_time, is_abnormal) VALUES(#{shipId}, #{sensorType}, " +
            "#{parameterName}, #{parameterValue}, #{unit}, #{standardMin}, #{standardMax}, #{collectTime}, #{isAbnormal})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSensorData(SensorData sensorData);

    // 插入故障记录
    @Insert("INSERT INTO fault_record(ship_id, fault_type, fault_level, description, solution, " +
            "detected_by, detected_time, is_solved) VALUES(#{shipId}, #{faultType}, #{faultLevel}, " +
            "#{description}, #{solution}, #{detectedBy}, #{detectedTime}, #{isSolved})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFault(FaultDiagnosis fault);
}