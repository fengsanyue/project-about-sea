package com.ship.agent;

import com.ship.entity.CityInfo;
import com.ship.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Component
public class ChatAgent extends BaseAgent {

    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEEPSEEK_API_KEY = "sk-23fe51edb0c3499886978996069e73dc";

    // 高德API Key（用于地理编码，获取坐标）
    private static final String AMAP_API_KEY = "72d908ad7293a0f360286741515a7952";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 保存对话历史
    private final List<Map<String, String>> conversationHistory = new ArrayList<>();

    @Autowired
    private CityService cityService;

    public ChatAgent() {
        super("对话智能体");
        // 初始化系统提示
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个智能助手DeepSeek，可以回答各种问题。当用户询问天气时，你会自动获取实时天气数据。对于其他问题，你会用你的知识回答。用友好、专业的语气，用中文回答。");
        conversationHistory.add(systemMsg);
    }

    @Override
    public Object perceive(Map<String, Object> environment) {
        String userMessage = (String) environment.get("userMessage");
        logAction("接收消息", userMessage, "");

        Map<String, Object> context = new HashMap<>();
        context.put("userMessage", userMessage);
        context.put("time", LocalDateTime.now());

        return context;
    }

    @Override
    public Object decide(Object perception) {
        Map<String, Object> context = (Map<String, Object>) perception;
        String userMessage = (String) context.get("userMessage");

        String response;

        // 判断是否是天气查询
        String[] weatherKeywords = {"天气", "气温", "温度", "下雨", "下雪", "晴", "阴", "雨", "雪", "雾", "霾", "台风", "冷不冷", "热不热", "多少度"};
        boolean isWeatherQuery = false;
        for (String keyword : weatherKeywords) {
            if (userMessage.contains(keyword)) {
                isWeatherQuery = true;
                break;
            }
        }

        if (isWeatherQuery) {
            System.out.println("检测到天气查询，调用OpenWeatherMap");
            response = getWeatherFromOpenWeather(userMessage);
        } else {
            System.out.println("调用DeepSeek API");
            response = callDeepSeekAPI(userMessage);
        }

        Map<String, Object> decision = new HashMap<>();
        decision.put("response", response);
        decision.put("context", context);

        return decision;
    }

    @Override
    public void execute(Object decision) {
        Map<String, Object> result = (Map<String, Object>) decision;
        String response = (String) result.get("response");
        logAction("回复", response, "");
    }

    // ==================== DeepSeek API调用 ====================
    private String callDeepSeekAPI(String userMessage) {
        try {
            System.out.println("========== 开始调用DeepSeek API ==========");
            System.out.println("用户问题: " + userMessage);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            conversationHistory.add(userMsg);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + DEEPSEEK_API_KEY);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", conversationHistory);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                setConnectTimeout(15000);
                setReadTimeout(30000);
            }});

            long startTime = System.currentTimeMillis();
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    DEEPSEEK_API_URL,
                    request,
                    Map.class
            );
            long endTime = System.currentTimeMillis();

            System.out.println("API响应时间: " + (endTime - startTime) + "ms");

            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");

                    Map<String, String> assistantMsg = new HashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", content);
                    conversationHistory.add(assistantMsg);

                    if (conversationHistory.size() > 20) {
                        List<Map<String, String>> newHistory = new ArrayList<>();
                        newHistory.add(conversationHistory.get(0));
                        newHistory.addAll(conversationHistory.subList(
                                conversationHistory.size() - 19,
                                conversationHistory.size()
                        ));
                        conversationHistory.clear();
                        conversationHistory.addAll(newHistory);
                    }

                    System.out.println("AI回复: " + content);
                    return content;
                }
            }

            return "抱歉，无法获取有效回复。";

        } catch (Exception e) {
            System.out.println("DeepSeek API调用失败: " + e.getMessage());
            return "AI服务暂时不可用，请稍后重试。";
        }
    }

    private String getWeatherFromOpenWeather(String userMessage) {
        try {
            // 1. 从数据库提取城市名
            String cityName = null;
            List<CityInfo> allCities = cityService.getAllCities();

            for (CityInfo city : allCities) {
                if (userMessage.contains(city.getCityName())) {
                    cityName = city.getCityName();
                    System.out.println("数据库匹配到城市: " + cityName);
                    break;
                }
            }

            if (cityName == null) {
                cityName = "北京";
            }

            // 2. 调用 Open-Meteo API
            return getWeatherFromOpenMeteo(cityName);

        } catch (Exception e) {
            e.printStackTrace();
            return getMockWeather("北京");
        }
    }

    // ==================== Open-Meteo API（修复版）====================
    private String getWeatherFromOpenMeteo(String cityName) {
        try {
            // 1. 先用高德地理编码获取坐标
            String geoUrl = "https://restapi.amap.com/v3/geocode/geo?address=" +
                    URLEncoder.encode(cityName, "UTF-8") +
                    "&key=" + AMAP_API_KEY;

            System.out.println("地理编码请求: " + geoUrl);

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> geoResponse = restTemplate.getForObject(geoUrl, Map.class);

            System.out.println("地理编码响应: " + geoResponse);

            String location = null;
            String formattedAddress = null;

            if (geoResponse != null && "1".equals(geoResponse.get("status"))) {
                List<Map<String, Object>> geocodes = (List<Map<String, Object>>) geoResponse.get("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    Map<String, Object> first = geocodes.get(0);
                    location = (String) first.get("location");
                    formattedAddress = (String) first.get("formatted_address");
                    System.out.println("获取到坐标: " + location + " 对应地址: " + formattedAddress);
                }
            }

            // 如果没有获取到坐标，返回模拟数据
            if (location == null) {
                System.out.println("未获取到坐标，使用模拟数据");
                return getMockWeather(cityName);
            }

            String[] latLon = location.split(",");
            String lon = latLon[0];
            String lat = latLon[1];

            // 2. 调用 Open-Meteo API
            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                    "&longitude=" + lon +
                    "&current_weather=true" +
                    "&timezone=Asia/Shanghai" +
                    "&temperature_unit=celsius" +
                    "&windspeed_unit=kmh";

            System.out.println("天气请求: " + weatherUrl);

            Map<String, Object> weatherResponse = restTemplate.getForObject(weatherUrl, Map.class);

            System.out.println("天气响应: " + weatherResponse);

            if (weatherResponse != null && weatherResponse.containsKey("current_weather")) {
                Map<String, Object> current = (Map<String, Object>) weatherResponse.get("current_weather");

                double temp = (double) current.get("temperature");
                double windspeed = (double) current.get("windspeed");
                int weathercode = (int) current.get("weathercode");
                String time = (String) current.get("time");

                String weatherText = getWeatherDescription(weathercode);

                return String.format("%s当前天气：%s，温度%.1f℃，风速%.1fkm/h（更新时间：%s）",
                        cityName, weatherText, temp, windspeed, time);
            }

            return getMockWeather(cityName);

        } catch (Exception e) {
            System.out.println("Open-Meteo API调用失败:");
            e.printStackTrace();
            return getMockWeather(cityName);
        }
    }

    // 天气代码转文字
    private String getWeatherDescription(int code) {
        if (code == 0) return "晴朗";
        if (code == 1 || code == 2 || code == 3) return "多云";
        if (code >= 45 && code <= 49) return "雾";
        if (code >= 51 && code <= 67) return "雨";
        if (code >= 71 && code <= 77) return "雪";
        if (code >= 80 && code <= 99) return "暴雨";
        return "未知";
    }

    // ==================== 模拟天气数据 ====================
    private String getMockWeather(String cityName) {
        String[] weathers = {"晴", "多云", "阴", "小雨", "中雨", "大雨"};
        String[] winds = {"东北", "东南", "西南", "西北", "北", "南"};
        Random random = new Random();

        String weather = weathers[random.nextInt(weathers.length)];
        String wind = winds[random.nextInt(winds.length)];
        int temp = 5 + random.nextInt(20);

        return String.format("%s当前天气：%s，温度%d℃，%s风%d级（模拟数据）",
                cityName, weather, temp, wind, 2 + random.nextInt(4));
    }

    // ==================== 工具方法 ====================

    private String getWindDirection(int deg) {
        String[] directions = {"北", "东北", "东", "东南", "南", "西南", "西", "西北", "北"};
        int index = (int) Math.round(((double) deg % 360) / 45);
        return directions[index];
    }

    // 以下方法保留但可能用不到
    private String getStringSafe(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "未知";
    }

    private String formatReportTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return "";
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime time = LocalDateTime.parse(timeStr, inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM月dd日 HH:mm");
            return "\n\n数据更新时间：" + time.format(outputFormatter);
        } catch (Exception e) {
            return "";
        }
    }
}