package com.ship.agent;

import com.ship.entity.FaultDiagnosis;
import com.ship.entity.SensorData;
import com.ship.entity.ShipStatus;
import com.ship.service.ShipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class AgentCoordinator {

    @Autowired
    private SensorAgent sensorAgent;

    @Autowired
    private AnomalyDetectionAgent anomalyAgent;

    @Autowired
    private FuelEstimationAgent fuelAgent;

    @Autowired
    private ShipService shipService;

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public Map<String, Object> executeMultiAgentWorkflow(Long shipId, ShipStatus shipStatus) {
        Map<String, Object> results = new HashMap<>();
        Map<String, Object> environment = new HashMap<>();
        environment.put("shipId", shipId);
        environment.put("shipStatus", shipStatus);

        log.info("========== 多智能体系统启动 ==========");

        try {
            // 1. 传感器智能体采集数据
            CompletableFuture<List<SensorData>> sensorFuture = CompletableFuture.supplyAsync(() -> {
                Object perception = sensorAgent.perceive(environment);
                @SuppressWarnings("unchecked")
                List<SensorData> dataList = (List<SensorData>) sensorAgent.decide(perception);
                sensorAgent.execute(dataList);

                // 保存到数据库
                for (SensorData data : dataList) {
                    shipService.saveSensorData(data);
                }

                return dataList;
            }, executor);

            List<SensorData> sensorData = sensorFuture.get();
            environment.put("sensorData", sensorData);
            results.put("sensorData", sensorData);

            // 2. 异常检测智能体分析
            CompletableFuture<List<FaultDiagnosis>> anomalyFuture = CompletableFuture.supplyAsync(() -> {
                Object perception = anomalyAgent.perceive(environment);
                @SuppressWarnings("unchecked")
                List<FaultDiagnosis> faults = (List<FaultDiagnosis>) anomalyAgent.decide(perception);
                anomalyAgent.execute(faults);

                // 保存故障到数据库
                for (FaultDiagnosis fault : faults) {
                    shipService.saveFault(fault);
                }

                return faults;
            }, executor);

            // 3. 燃料估算智能体分析
            CompletableFuture<Map<String, Object>> fuelFuture = CompletableFuture.supplyAsync(() -> {
                Object perception = fuelAgent.perceive(environment);
                @SuppressWarnings("unchecked")
                Map<String, Object> fuelResult = (Map<String, Object>) fuelAgent.decide(perception);
                fuelAgent.execute(fuelResult);
                return fuelResult;
            }, executor);

            // 等待所有智能体完成
            results.put("anomalies", anomalyFuture.get());
            results.put("fuelEstimation", fuelFuture.get());

            log.info("========== 多智能体系统执行完成 ==========");

        } catch (Exception e) {
            log.error("多智能体执行失败", e);
            results.put("error", e.getMessage());
        }

        return results;
    }
}
