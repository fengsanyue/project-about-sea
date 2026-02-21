package com.ship.service;

import com.ship.entity.FaultDiagnosis;
import com.ship.entity.SensorData;
import com.ship.entity.ShipStatus;
import com.ship.mapper.ShipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShipService {

    @Autowired
    private ShipMapper shipMapper;

    public ShipStatus getShipInfo(Long shipId) {
        return shipMapper.getShipInfo(shipId);
    }

    public List<SensorData> getRecentSensorData(Long shipId) {
        return shipMapper.getRecentSensorData(shipId);
    }

    public List<FaultDiagnosis> getRecentFaults(Long shipId) {
        return shipMapper.getRecentFaults(shipId);
    }

    public void saveSensorData(SensorData sensorData) {
        shipMapper.insertSensorData(sensorData);
    }

    public void saveFault(FaultDiagnosis fault) {
        shipMapper.insertFault(fault);
    }
}