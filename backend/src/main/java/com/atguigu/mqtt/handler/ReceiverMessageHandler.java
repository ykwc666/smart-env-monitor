package com.atguigu.mqtt.handler;

import com.atguigu.mqtt.service.SensorValueValidator;
import com.atguigu.mqtt.service.TbLampService;
import com.atguigu.mqtt.service.TbLampStatusService;
import com.atguigu.mqtt.service.impl.SensorDataAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ReceiverMessageHandler implements MessageHandler {

    private static final Set<String> DEVICE_TOPICS = Set.of("ID1OUT", "ID2OUT", "ID3OUT");
    private static final Pattern KV_PATTERN = Pattern.compile("(\\w+)\\s*[:=]?\\s*([-+]?\\d*\\.?\\d+)");

    @Autowired
    private TbLampService tbLampService;

    @Autowired
    private TbLampStatusService tbLampStatusService;

    @Autowired
    private SensorDataAsyncService sensorDataAsyncService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SensorValueValidator validator;

    private Map<String, String> parseKeyValueString(String input) {
        Map<String, String> map = new HashMap<>();
        Matcher matcher = KV_PATTERN.matcher(input);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String payload = message.getPayload().toString();
        MessageHeaders headers = message.getHeaders();
        Object topicHeader = headers.get("mqtt_receivedTopic");
        if (topicHeader == null) {
            return;
        }
        String topicName = topicHeader.toString();

        if ("atguigu/iot/lamp/line".equals(topicName)) {
            tbLampService.updateLampOnlineStatus(payload);
            return;
        }
        if ("atguigu/iot/lamp/device/status".equals(topicName)) {
            tbLampStatusService.saveDeviceStatus(payload);
            return;
        }
        if ("kg/wd".equals(topicName)) {
            handleKgWd(payload);
            return;
        }
        if ("camera/frames".equals(topicName)) {
            handleCamera(payload);
            return;
        }
        if (DEVICE_TOPICS.contains(topicName)) {
            handleDeviceOut(topicName, payload);
        }
    }

    /** 温度主题：先 WebSocket 推送，再异步落库，避免堵 MQTT 入站 */
    private void handleKgWd(String payload) {
        try {
            JSONObject jsonObject = new JSONObject(payload);
            double temperature = jsonObject.getDouble("wd");
            int id = jsonObject.getInt("id");
            if (!validator.isValidTemp(temperature)) {
                log.debug("reject invalid temperature: id={}, wd={}", id, temperature);
                return;
            }
            JSONObject json = new JSONObject();
            json.put("wd", temperature);
            // 可选序号：压测/链路追踪用，不参与温度校验
            if (jsonObject.has("seq")) {
                json.put("seq", jsonObject.getInt("seq"));
            }
            messagingTemplate.convertAndSend("/topic/temperature/" + id, json.toString());
            validator.markWsPushed();
            sensorDataAsyncService.saveWdAsync(id, temperature);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCamera(String payload) {
        try {
            messagingTemplate.convertAndSend("/topic/camera", payload);
        } catch (Exception e) {
            log.error("forward camera frame failed", e);
        }
    }

    /** ID1/2/3OUT 统一处理：校验 → 推送 → 异步落库 */
    private void handleDeviceOut(String topicName, String payload) {
        try {
            Map<String, String> dataMap = parseKeyValueString(payload.trim());
            int id = Integer.parseInt(dataMap.getOrDefault("id", "-1"));
            if (id < 0) {
                log.debug("reject device payload without id on {}", topicName);
                return;
            }

            double wd = Double.parseDouble(dataMap.getOrDefault("wd", "-1"));
            double sd = Double.parseDouble(dataMap.getOrDefault("sd", "-1"));
            double gg = Double.parseDouble(dataMap.getOrDefault("gg", "-1"));
            double dy = Double.parseDouble(dataMap.getOrDefault("dy", "-1"));
            double dl = Double.parseDouble(dataMap.getOrDefault("dl", "-1"));

            if (wd != -1 || sd != -1) {
                boolean tempOk = wd == -1 || validator.isValidTemp(wd);
                boolean humiOk = sd == -1 || validator.isValidHumi(sd);
                if (tempOk && humiOk) {
                    JSONObject json = new JSONObject();
                    json.put("temp", wd);
                    json.put("humi", sd);
                    messagingTemplate.convertAndSend("/topic/environment/" + id, json.toString());
                    sensorDataAsyncService.saveWsdAsync(id, wd, sd);
                }
            }

            if (gg != -1 && validator.isValidLight(gg)) {
                JSONObject json = new JSONObject();
                json.put("gg", gg);
                messagingTemplate.convertAndSend("/topic/light/" + id, json.toString());
                sensorDataAsyncService.saveGgAsync(id, gg);
            }

            if ((dl != -1 || dy != -1)
                    && (dl == -1 || validator.isValidCurrent(dl))
                    && (dy == -1 || validator.isValidVoltage(dy))) {
                JSONObject json = new JSONObject();
                json.put("current", dl);
                json.put("voltage", dy);
                messagingTemplate.convertAndSend("/topic/electricity/" + id, json.toString());
                sensorDataAsyncService.saveElectricityAsync(id, dl, dy);
            }
        } catch (Exception e) {
            throw new RuntimeException("parse MQTT payload failed on " + topicName + ": " + payload, e);
        }
    }
}
