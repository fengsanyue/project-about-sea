package com.ship.agent;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class BaseAgent {

    protected String agentId;
    protected String agentType;
    protected Map<String, Object> memory = new ConcurrentHashMap<>();

    public BaseAgent(String agentType) {
        this.agentId = agentType + "_" + System.currentTimeMillis();
        this.agentType = agentType;
    }

    // 感知环境
    public abstract Object perceive(Map<String, Object> environment);

    // 决策
    public abstract Object decide(Object perception);

    // 执行动作
    public abstract void execute(Object decision);

    // 记录日志
    protected void logAction(String action, Object input, Object output) {
        log.info("智能体 [{}] - {}: 输入={}, 输出={}", agentId, action, input, output);
    }
}