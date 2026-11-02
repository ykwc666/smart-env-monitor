# -*- coding: utf-8 -*-
"""Clean metrics for resume: paced throughput + isolated anomaly filter."""
from __future__ import annotations

import json
import math
import statistics
import threading
import time
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

import paho.mqtt.client as mqtt
from websocket import create_connection

REPORT = Path(__file__).resolve().parent / "reports" / "mqtt_ws_e2e_report.json"
BROKER = "localhost"
MQTT_PORT = 1883
MQTT_USER = __import__("os").environ.get("MQTT_USERNAME", "mqtt_user")
MQTT_PASS = __import__("os").environ.get("MQTT_PASSWORD", "changeme")
WS_URL = "ws://localhost:8080/ws"
DEVICE_ID = 1
TOPIC_MQTT = "kg/wd"
TOPIC_STOMP = f"/topic/temperature/{DEVICE_ID}"


def percentile(sorted_vals: list[float], p: float) -> float:
    if not sorted_vals:
        return 0.0
    k = (len(sorted_vals) - 1) * p
    f = math.floor(k)
    c = math.ceil(k)
    if f == c:
        return sorted_vals[int(k)]
    return sorted_vals[f] * (c - k) + sorted_vals[c] * (k - f)


def http_json(url: str, method: str = "GET") -> dict:
    req = urllib.request.Request(url, method=method)
    with urllib.request.urlopen(req, timeout=5) as resp:
        return json.loads(resp.read().decode("utf-8"))


def reset_metrics():
    http_json("http://localhost:8080/api/metrics/sensor/reset", "POST")


class StompClient:
    def __init__(self, url: str):
        self.url = url
        self.ws = None
        self.lock = threading.Lock()
        self.inbox: dict[int, float] = {}
        self._stop = False
        self._thread = None

    def connect(self):
        self.ws = create_connection(self.url, timeout=10)
        self.ws.send("CONNECT\naccept-version:1.1,1.0\nhost:localhost\n\n\x00")
        deadline = time.time() + 5
        while time.time() < deadline:
            frame = self.ws.recv()
            if isinstance(frame, bytes):
                frame = frame.decode("utf-8", errors="ignore")
            if frame.startswith("CONNECTED"):
                break
        self.ws.send(f"SUBSCRIBE\nid:sub-0\ndestination:{TOPIC_STOMP}\nack:auto\n\n\x00")
        self._thread = threading.Thread(target=self._reader, daemon=True)
        self._thread.start()

    def _reader(self):
        while not self._stop:
            try:
                self.ws.settimeout(1.0)
                frame = self.ws.recv()
            except Exception:
                continue
            if isinstance(frame, bytes):
                frame = frame.decode("utf-8", errors="ignore")
            if not frame.startswith("MESSAGE"):
                continue
            parts = frame.split("\n\n", 1)
            if len(parts) < 2:
                continue
            body = parts[1].rstrip("\x00").strip()
            try:
                data = json.loads(body)
                seq = int(data.get("seq", data.get("wd")))
            except Exception:
                continue
            with self.lock:
                self.inbox[seq] = time.perf_counter()

    def wait_seq(self, seq: int, timeout: float = 3.0) -> float | None:
        deadline = time.time() + timeout
        while time.time() < deadline:
            with self.lock:
                if seq in self.inbox:
                    return self.inbox.pop(seq)
            time.sleep(0.001)
        return None

    def close(self):
        self._stop = True
        try:
            if self.ws:
                self.ws.close()
        except Exception:
            pass


def run_latency(n: int = 200) -> dict:
    stomp = StompClient(WS_URL)
    stomp.connect()
    time.sleep(0.4)
    pub = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"clean-lat-{time.time_ns()}")
    pub.username_pw_set(MQTT_USER, MQTT_PASS)
    pub.connect(BROKER, MQTT_PORT, 60)
    pub.loop_start()
    latencies = []
    misses = 0
    for seq in range(1, n + 1):
        payload = json.dumps({"id": DEVICE_ID, "wd": 25.0 + (seq % 10) * 0.1, "seq": seq})
        t0 = time.perf_counter()
        pub.publish(TOPIC_MQTT, payload, qos=1)
        t1 = stomp.wait_seq(seq, timeout=3.0)
        if t1 is None:
            misses += 1
            continue
        latencies.append((t1 - t0) * 1000)
        time.sleep(0.01)
    pub.loop_stop()
    pub.disconnect()
    stomp.close()
    latencies.sort()
    return {
        "samples": len(latencies),
        "misses": misses,
        "avg_ms": round(statistics.mean(latencies), 3) if latencies else None,
        "p50_ms": round(percentile(latencies, 0.50), 3) if latencies else None,
        "p95_ms": round(percentile(latencies, 0.95), 3) if latencies else None,
        "p99_ms": round(percentile(latencies, 0.99), 3) if latencies else None,
        "min_ms": round(min(latencies), 3) if latencies else None,
        "max_ms": round(max(latencies), 3) if latencies else None,
    }


def run_paced_throughput(target_qps: int = 800, seconds: float = 10.0) -> dict:
    """按目标速率匀速发布，验证稳定端到端送达。"""
    reset_metrics()
    time.sleep(0.5)
    interval = 1.0 / target_qps
    stop_at = time.time() + seconds
    sent = 0
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"clean-tps-{time.time_ns()}")
    client.username_pw_set(MQTT_USER, MQTT_PASS)
    client.connect(BROKER, MQTT_PORT, 60)
    client.loop_start()
    seq = 0
    t0 = time.perf_counter()
    next_t = t0
    while time.time() < stop_at:
        now = time.perf_counter()
        if now < next_t:
            time.sleep(min(0.001, next_t - now))
            continue
        seq += 1
        client.publish(TOPIC_MQTT, json.dumps({"id": DEVICE_ID, "wd": 26.5, "seq": seq}), qos=0)
        sent += 1
        next_t += interval
    elapsed = time.perf_counter() - t0
    client.loop_stop()
    client.disconnect()
    time.sleep(2.0)
    metrics = http_json("http://localhost:8080/api/metrics/sensor")
    pushed = int(metrics.get("wsPushed", 0))
    return {
        "target_qps": target_qps,
        "duration_s": round(elapsed, 3),
        "mqtt_publish_count": sent,
        "mqtt_publish_qps": round(sent / elapsed, 2),
        "server_ws_pushed": pushed,
        "server_ws_push_qps": round(pushed / max(elapsed, 1e-9), 2),
        "delivery_ratio": round(pushed / sent, 4) if sent else 0,
        "server_metrics": metrics,
    }


def run_anomaly_filter(n_bad: int = 100, n_good: int = 100) -> dict:
    reset_metrics()
    time.sleep(1.0)
    stomp = StompClient(WS_URL)
    stomp.connect()
    time.sleep(0.5)
    pub = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"clean-filter-{time.time_ns()}")
    pub.username_pw_set(MQTT_USER, MQTT_PASS)
    pub.connect(BROKER, MQTT_PORT, 60)
    pub.loop_start()

    for i in range(n_bad):
        bad = 999.0 if i % 2 == 0 else -100.0
        pub.publish(TOPIC_MQTT, json.dumps({"id": DEVICE_ID, "wd": bad, "seq": 20000 + i}), qos=1)
        time.sleep(0.01)
    for seq in range(1, n_good + 1):
        pub.publish(TOPIC_MQTT, json.dumps({"id": DEVICE_ID, "wd": 24.0, "seq": seq}), qos=1)
        time.sleep(0.01)

    deadline = time.time() + 8
    while time.time() < deadline:
        with stomp.lock:
            if len([s for s in stomp.inbox if 1 <= s <= n_good]) >= n_good:
                break
        time.sleep(0.05)

    with stomp.lock:
        received = set(stomp.inbox.keys())
    pub.loop_stop()
    pub.disconnect()
    stomp.close()
    time.sleep(0.5)
    metrics = http_json("http://localhost:8080/api/metrics/sensor")

    invalid_seqs = {20000 + i for i in range(n_bad)}
    valid_seqs = set(range(1, n_good + 1))
    leaked = received & invalid_seqs
    got_valid = received & valid_seqs
    return {
        "published_invalid": n_bad,
        "published_valid": n_good,
        "stomp_received_valid": len(got_valid),
        "stomp_received_invalid_leaked": len(leaked),
        "filter_effectiveness": round((n_bad - len(leaked)) / n_bad, 4) if n_bad else 1.0,
        "valid_delivery_ratio": round(len(got_valid) / n_good, 4) if n_good else 0,
        "server_metrics": metrics,
    }


def main():
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    print("== latency ==", flush=True)
    latency = run_latency(200)
    print(json.dumps(latency, ensure_ascii=False), flush=True)
    time.sleep(2)

    print("== paced throughput 800qps ==", flush=True)
    tps800 = run_paced_throughput(800, 10.0)
    print(json.dumps(tps800, ensure_ascii=False), flush=True)
    time.sleep(2)

    print("== paced throughput 1500qps ==", flush=True)
    tps1500 = run_paced_throughput(1500, 10.0)
    print(json.dumps(tps1500, ensure_ascii=False), flush=True)
    time.sleep(3)

    print("== anomaly filter ==", flush=True)
    anomaly = run_anomaly_filter(100, 100)
    print(json.dumps(anomaly, ensure_ascii=False), flush=True)

    report = {
        "project": "智慧环境监控平台",
        "bridge": "MQTT(kg/wd) -> Spring ExecutorChannel + 异步落库 -> STOMP(/topic/temperature/{id})",
        "optimizations": [
            "MQTT 入站 ExecutorChannel",
            "先 WebSocket 推送再异步落库",
            "传感器异常值范围校验",
            "关闭 SQL 控制台日志 / 扩大线程池与连接池",
            "修复 pub/sub clientId 冲突",
        ],
        "latency": latency,
        "throughput_stable_800qps": tps800,
        "throughput_stable_1500qps": tps1500,
        "anomaly_filter": anomaly,
    }
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {REPORT}", flush=True)


if __name__ == "__main__":
    main()
