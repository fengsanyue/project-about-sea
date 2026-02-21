package com.ship.agent;

import com.ship.entity.SensorData;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class SensorAgent extends BaseAgent {

    private Random random = new Random();

    public SensorAgent() {
        super("传感器智能体");
    }

    @Override
    public Object perceive(Map<String, Object> environment) {
        Long shipId = (Long) environment.get("shipId");
        logAction("感知", shipId, "开始采集传感器数据");

        List<SensorData> dataList = new ArrayList<>();

        // 模拟采集各种传感器数据
        dataList.add(createSensorData(shipId, "发动机", "温度", 70.0, 90.0, "℃", 75.0));
        dataList.add(createSensorData(shipId, "发动机", "排烟温度", 300.0, 400.0, "℃", 360.0));
        dataList.add(createSensorData(shipId, "发动机", "滑油压力", 0.2, 0.4, "MPa", 0.3));
        dataList.add(createSensorData(shipId, "发动机", "电压", 380.0, 420.0, "V", 400.0));
        dataList.add(createSensorData(shipId, "发动机", "频率", 49.5, 50.5, "Hz", 50.0));
        dataList.add(createSensorData(shipId, "航行", "航速", 8.0, 15.0, "km/h", 11.0));
        dataList.add(createSensorData(shipId, "航行", "油耗", 60.0, 80.0, "L/h", 75.0));

        return dataList;
    }

    @Override
    public Object decide(Object perception) {
        List<SensorData> dataList = (List<SensorData>) perception;

        // 检测异常
        for (SensorData data : dataList) {
            BigDecimal value = data.getParameterValue();
            BigDecimal min = data.getStandardMin();
            BigDecimal max = data.getStandardMax();

            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                data.setIsAbnormal(1);
                logAction("异常检测", data.getParameterName(), "超出正常范围");
            } else {
                data.setIsAbnormal(0);
            }
        }

        return dataList;
    }

    @Override
    public void execute(Object decision) {
        List<SensorData> dataList = (List<SensorData>) decision;
        logAction("执行", "数据采集完成", "共采集" + dataList.size() + "条数据");
    }

    private SensorData createSensorData(Long shipId, String type, String name,
                                        double min, double max, String unit, double currentValue) {
        SensorData data = new SensorData();
        data.setShipId(shipId);
        data.setSensorType(type);
        data.setParameterName(name);
        data.setUnit(unit);
        data.setStandardMin(BigDecimal.valueOf(min));
        data.setStandardMax(BigDecimal.valueOf(max));
        data.setParameterValue(BigDecimal.valueOf(currentValue));
        data.setCollectTime(LocalDateTime.now());
        data.setIsAbnormal(0);
        return data;
    }
}