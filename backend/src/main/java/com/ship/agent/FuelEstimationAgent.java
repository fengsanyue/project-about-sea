package com.ship.agent;

import com.ship.entity.ShipStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
public class FuelEstimationAgent extends BaseAgent {

    public FuelEstimationAgent() {
        super("燃料估算智能体");
    }

    @Override
    public Object perceive(Map<String, Object> environment) {
        ShipStatus status = (ShipStatus) environment.get("shipStatus");
        logAction("感知", "船舶ID:" + status.getShipId(), "当前油耗:" + status.getFuelConsumption());
        return status;
    }

    @Override
    public Object decide(Object perception) {
        ShipStatus status = (ShipStatus) perception;

        // 估算剩余航程
        BigDecimal remainingFuel = status.getRemainingFuel() != null ? status.getRemainingFuel() : BigDecimal.valueOf(5000);
        BigDecimal fuelConsumption = status.getFuelConsumption() != null ? status.getFuelConsumption() : BigDecimal.valueOf(75);
        BigDecimal speed = status.getSpeed() != null ? status.getSpeed() : BigDecimal.valueOf(11);

        // 计算预测航程 (剩余燃料 / 每小时油耗 * 航速)
        BigDecimal range = BigDecimal.ZERO;
        if (fuelConsumption.compareTo(BigDecimal.ZERO) > 0) {
            range = remainingFuel.divide(fuelConsumption, 2, RoundingMode.HALF_UP)
                    .multiply(speed)
                    .setScale(0, RoundingMode.HALF_UP);
        }

        // 计算最优航速
        BigDecimal optimalSpeed = calculateOptimalSpeed(speed, fuelConsumption);

        // 生成建议
        String advice = generateAdvice(speed, optimalSpeed, remainingFuel);

        Map<String, Object> result = new HashMap<>();
        result.put("predictedRange", range + " km");
        result.put("optimalSpeed", optimalSpeed + " km/h");
        result.put("advice", advice);

        return result;
    }

    @Override
    public void execute(Object decision) {
        Map<String, Object> result = (Map<String, Object>) decision;
        logAction("燃料估算完成",
                "预测航程:" + result.get("predictedRange"),
                "建议:" + result.get("advice"));
    }

    private BigDecimal calculateOptimalSpeed(BigDecimal currentSpeed, BigDecimal fuelConsumption) {
        // 简单的最优航速计算：如果油耗高，建议降速；如果油耗低，可以适当提速
        double fuelRate = fuelConsumption.doubleValue();
        double speed = currentSpeed.doubleValue();

        double optimal;
        if (fuelRate > 80) {
            optimal = speed * 0.9; // 降速10%
        } else if (fuelRate < 60) {
            optimal = speed * 1.1; // 提速10%
        } else {
            optimal = speed; // 保持当前
        }

        // 限制在合理范围内 (8-15 km/h)
        optimal = Math.max(8, Math.min(15, optimal));

        return BigDecimal.valueOf(optimal).setScale(1, RoundingMode.HALF_UP);
    }

    private String generateAdvice(BigDecimal currentSpeed, BigDecimal optimalSpeed, BigDecimal remainingFuel) {
        StringBuilder advice = new StringBuilder();

        if (currentSpeed.compareTo(optimalSpeed) > 0) {
            advice.append("建议降低航速至").append(optimalSpeed).append("km/h，可节省燃油");
        } else if (currentSpeed.compareTo(optimalSpeed) < 0) {
            advice.append("可适当提高航速至").append(optimalSpeed).append("km/h，提高运输效率");
        } else {
            advice.append("当前航速经济性良好");
        }

        if (remainingFuel.doubleValue() < 1000) {
            advice.append("，燃料储备不足，建议规划加油");
        }

        return advice.toString();
    }
}