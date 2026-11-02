# 智慧环境监控平台 (Smart Environment Monitor)

竞赛演示向物联网监控：设备 **MQTT** 上报 → Spring Boot 校验/桥接 → **WebSocket (STOMP)** 推送 → Vue + ECharts 实时展示。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 3、Spring Integration MQTT、MyBatis-Plus、MySQL |
| 消息 | EMQX、MQTT → STOMP WebSocket |
| 前端 | Vue 3、ECharts、Vite |
| 压测 | Python（`backend/benchmarks`） |

## 仓库结构

```
backend/     Spring Boot MQTT 桥接与业务 API
frontend/    Vue 实时大屏 / 设备页
```

## 快速开始

### 1. 依赖

- JDK 17+、Maven 3.9+
- MySQL 8（库名建议 `mqtt`）
- EMQX（默认 `localhost:1883`）
- Node 18+（前端）

### 2. 配置（勿提交真实密码）

后端默认读取环境变量；也可参考 `backend/src/main/resources/application-example.yaml`。

```bash
# Windows PowerShell 示例
$env:MYSQL_PASSWORD="your_mysql_password"
$env:MQTT_USERNAME="your_emqx_user"
$env:MQTT_PASSWORD="your_emqx_password"
```

```bash
cd backend
mvn -DskipTests spring-boot:run
```

```bash
cd frontend
pnpm install   # 或 npm install
pnpm dev
```

前端默认请求 `http://127.0.0.1:5000` 时可按本地网关改 `src/utils/request.js`；WebSocket 端点为后端 `ws://localhost:8080/ws`。

## 核心设计

1. **热路径**：MQTT 入站使用 `ExecutorChannel`；校验通过后先推 STOMP，再异步落库。
2. **异常过滤**：温度/湿度/光感/电流电压超范围直接丢弃，并暴露 `/api/metrics/sensor`。
3. **ClientId**：发布与订阅使用不同 MQTT clientId，避免互相踢线。

## 本机可复现指标（示例）

在本机 EMQX + Spring 联调下，使用 `backend/benchmarks/mqtt_ws_clean_metrics.py`：

- MQTT → STOMP 端到端：约 **1.3 ms** 均延，P95 ≈ **1.7 ms**（200 条，丢失 0）
- 匀速 **800 / 1500 msg/s**：服务端推送送达率 **100%**
- 异常温度过滤：越界拦截 **100%**，合法送达 **100%**

> 数字依赖本机硬件与 Broker 配置；请以你跑出的 `benchmarks/reports/` 为准。

## 压测

```bash
pip install paho-mqtt websocket-client
# 先启动 EMQX + 后端，再执行：
python backend/benchmarks/mqtt_ws_clean_metrics.py
```

## License

个人作品集项目，仅供学习交流。
