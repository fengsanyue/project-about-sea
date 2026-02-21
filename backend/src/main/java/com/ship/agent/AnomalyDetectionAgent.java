package com.ship.agent;

import com.ship.entity.FaultDiagnosis;
import com.ship.entity.SensorData;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Component
public class AnomalyDetectionAgent extends BaseAgent {

    public AnomalyDetectionAgent() {
        super("异常检测智能体");
    }

    @Override
    public Object perceive(Map<String, Object> environment) {
        List<SensorData> sensorDataList = (List<SensorData>) environment.get("sensorData");
        logAction("感知", "接收到" + sensorDataList.size() + "条传感器数据", "");
        return sensorDataList;
    }

    @Override
    public Object decide(Object perception) {
        List<SensorData> dataList = (List<SensorData>) perception;
        List<FaultDiagnosis> faults = new ArrayList<>();

        for (SensorData data : dataList) {
            if (data.getIsAbnormal() == 1) {
                FaultDiagnosis fault = createFaultFromSensorData(data);
                faults.add(fault);
            }
        }

        return faults;
    }

    @Override
    public void execute(Object decision) {
        List<FaultDiagnosis> faults = (List<FaultDiagnosis>) decision;
        if (!faults.isEmpty()) {
            logAction("故障报警", "发现" + faults.size() + "个异常", faults.get(0).getDescription());
        } else {
            logAction("检测完成", "未发现异常", "");
        }
    }

    private FaultDiagnosis createFaultFromSensorData(SensorData data) {
        FaultDiagnosis fault = new FaultDiagnosis();
        fault.setShipId(data.getShipId());
        fault.setFaultType(data.getParameterName() + "异常");

        // 判断故障等级
        BigDecimal value = data.getParameterValue();
        BigDecimal min = data.getStandardMin();
        BigDecimal max = data.getStandardMax();

        String level = "medium";
        if (value.compareTo(min) < 0) {
            double diff = min.subtract(value).doubleValue();
            level = diff > 0.1 ? "high" : "medium";
        } else if (value.compareTo(max) > 0) {
            double diff = value.subtract(max).doubleValue();
            level = diff > 10 ? "high" : "medium";
        }

        fault.setFaultLevel(level);
        fault.setDescription(String.format("%s当前值%.2f%s(正常范围:%.2f-%.2f%s)",
                data.getParameterName(), value, data.getUnit(), min, max, data.getUnit()));
        fault.setSolution("请检查相关设备，必要时联系维修人员");
        fault.setDetectedBy(this.agentType);
        fault.setDetectedTime(LocalDateTime.now());
        fault.setIsSolved(0);

        return fault;
    }
}