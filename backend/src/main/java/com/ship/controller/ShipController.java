package com.ship.controller;

import com.ship.agent.AgentCoordinator;
import com.ship.agent.ChatAgent;
import com.ship.entity.FaultDiagnosis;
import com.ship.entity.SensorData;
import com.ship.entity.ShipStatus;
import com.ship.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ship")
@CrossOrigin(origins = "*")
public class ShipController {

    @Autowired
    private AgentCoordinator agentCoordinator;

    @Autowired
    private ChatAgent chatAgent;

    @Autowired
    private ShipService shipService;

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "后端服务运行正常");
        return response;
    }

    @GetMapping("/status/{shipId}")
    public Map<String, Object> getShipStatus(@PathVariable Long shipId) {
        // 修改这里：getLatestStatus 改为 getShipInfo
        ShipStatus status = shipService.getShipInfo(shipId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", status);
        result.put("message", "success");

        return result;
    }

    @PostMapping("/analyze/{shipId}")
    public Map<String, Object> analyzeShip(@PathVariable Long shipId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 修改这里：getLatestStatus 改为 getShipInfo
            ShipStatus status = shipService.getShipInfo(shipId);
            System.out.println("获取到船舶状态: " + status);

            Map<String, Object> analysisResult = agentCoordinator.executeMultiAgentWorkflow(shipId, status);
            System.out.println("分析结果: " + analysisResult);

            result.put("code", 200);
            result.put("data", analysisResult);
            result.put("message", "多智能体分析完成");
        } catch (Exception e) {
            System.err.println("分析异常: " + e.getMessage());
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "分析失败: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        Long shipId = Long.valueOf(request.get("shipId").toString());

        Map<String, Object> environment = new HashMap<>();
        environment.put("userMessage", message);
        environment.put("shipId", shipId);

        Object perception = chatAgent.perceive(environment);
        Object decision = chatAgent.decide(perception);
        chatAgent.execute(decision);

        String response = (String) ((Map<?, ?>) decision).get("response");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", response);
        result.put("message", "success");

        return result;
    }

    @GetMapping("/sensor-data/{shipId}")
    public Map<String, Object> getSensorData(@PathVariable Long shipId) {
        List<SensorData> data = shipService.getRecentSensorData(shipId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        result.put("message", "success");

        return result;
    }

    @GetMapping("/faults/{shipId}")
    public Map<String, Object> getFaults(@PathVariable Long shipId) {
        List<FaultDiagnosis> faults = shipService.getRecentFaults(shipId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", faults);
        result.put("message", "success");

        return result;
    }
}