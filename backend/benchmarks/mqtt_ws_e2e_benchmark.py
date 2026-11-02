# -*- coding: utf-8 -*-
"""E2E benchmark: MQTT publish -> Spring bridge -> STOMP WebSocket receive.

Measures reproducible latency / throughput for resume metrics.
"""
from __future__ import annotations

import json
import math
import statistics
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

import paho.mqtt.client as mqtt
from websocket import create_connection

REPORT = Path(__file__).resolve().parent / "reports" / "mqtt_ws_e2e_report.json"
BROKER = "localhost"
MQTT_PORT = 1883
MQTT_USER = __import__("os").environ.get("MQTT_USERNAME", "mqtt_user")
MQTT_PASS = __import__("os").environ.get("MQTT_PASSWORD", "changeme")
WS_URL = "ws://localhost:8080/ws"  # Spring STOMP endpoint (no SockJS)
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


class StompClient:
    def __init__(self, url: str):
        self.url = url
        self.ws = None
        self.ready = threading.Event()
        self.lock = threading.Lock()
        self.inbox: dict[int, float] = {}
        self._stop = False
        self._thread = None

    def connect(self):
        self.ws = create_connection(self.url, timeout=10)
        self.ws.send("CONNECT\naccept-version:1.1,1.0\nhost:localhost\n\n\x00")
        # wait CONNECTED
        deadline = time.time() + 5
        while time.time() < deadline:
            frame = self.ws.recv()
            if isinstance(frame, bytes):
                frame = frame.decode("utf-8", errors="ignore")
            if frame.startswith("CONNECTED"):
                break
        self.ws.send(f"SUBSCRIBE\nid:sub-0\ndestination:{TOPIC_STOMP}\nack:auto\n\n\x00")
        self.ready.set()
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
            # body after blank line
            parts = frame.split("\n\n", 1)
            if len(parts) < 2:
                continue
            body = parts[1].rstrip("\x00").strip()
            try:
                data = json.loads(body)
                seq = data.get("seq", data.get("wd"))
                seq = int(seq)
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


def run_latency(n: int = 100) -> dict:
    stomp = StompClient(WS_URL)
    stomp.connect()
    time.sleep(0.5)
    pub = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"bench-pub-{time.time_ns()}")
    pub.username_pw_set(MQTT_USER, MQTT_PASS)
    pub.connect(BROKER, MQTT_PORT, 60)
    pub.loop_start()
    latencies = []
    misses = 0
    for seq in range(1, n + 1):
        # wd 用合法温度，seq 单独做端到端关联（避免被异常过滤误杀）
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


def run_throughput(seconds: float = 10.0, workers: int = 4) -> dict:
    stomp = StompClient(WS_URL)
    stomp.connect()
    time.sleep(0.3)
    stop_at = time.time() + seconds
    sent = {"n": 0}
    lock = threading.Lock()
    base = int(time.time()) % 100000

    def worker(wid: int):
        local = 0
        client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"bench-tps-{wid}-{time.time_ns()}")
        client.username_pw_set(MQTT_USER, MQTT_PASS)
        client.connect(BROKER, MQTT_PORT, 60)
        seq = base * 1000 + wid * 100000
        while time.time() < stop_at:
            seq += 1
            payload = json.dumps({"id": DEVICE_ID, "wd": 26.5, "seq": seq})
            client.publish(TOPIC_MQTT, payload, qos=0)
            local += 1
        client.disconnect()
        with lock:
            sent["n"] += local

    t0 = time.perf_counter()
    with ThreadPoolExecutor(max_workers=workers) as pool:
        futs = [pool.submit(worker, i) for i in range(workers)]
        for f in as_completed(futs):
            f.result()
    elapsed = time.perf_counter() - t0
    # give bridge a moment to flush
    time.sleep(1.0)
    with stomp.lock:
        received = len(stomp.inbox)
    stomp.close()
    return {
        "duration_s": round(elapsed, 3),
        "workers": workers,
        "mqtt_publish_count": sent["n"],
        "mqtt_publish_qps": round(sent["n"] / elapsed, 2),
        "stomp_unique_seqs_seen": received,
        "note": "throughput uses QoS0 publish; receive count is unique seqs observed on one STOMP subscriber",
    }


def run_anomaly_filter(n_bad: int = 50, n_good: int = 50) -> dict:
    """Publish out-of-range + valid temps; only valid should appear on STOMP."""
    import urllib.request

    try:
        req = urllib.request.Request("http://localhost:8080/api/metrics/sensor/reset", method="POST")
        urllib.request.urlopen(req, timeout=3).read()
    except Exception:
        pass

    stomp = StompClient(WS_URL)
    stomp.connect()
    time.sleep(0.3)
    pub = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"bench-filter-{time.time_ns()}")
    pub.username_pw_set(MQTT_USER, MQTT_PASS)
    pub.connect(BROKER, MQTT_PORT, 60)
    pub.loop_start()

    # invalid: wd=999 / -100；valid: 合法温度 + seq
    for i in range(n_bad):
        bad = 999 if i % 2 == 0 else -100
        pub.publish(TOPIC_MQTT, json.dumps({"id": DEVICE_ID, "wd": bad, "seq": 10000 + i}), qos=1)
    for seq in range(1, n_good + 1):
        pub.publish(TOPIC_MQTT, json.dumps({"id": DEVICE_ID, "wd": 24.0, "seq": seq}), qos=1)

    deadline = time.time() + 5
    while time.time() < deadline:
        with stomp.lock:
            if len(stomp.inbox) >= n_good:
                break
        time.sleep(0.05)

    with stomp.lock:
        received_seqs = sorted(stomp.inbox.keys())
    pub.loop_stop()
    pub.disconnect()
    stomp.close()

    metrics = {}
    try:
        with urllib.request.urlopen("http://localhost:8080/api/metrics/sensor", timeout=3) as resp:
            metrics = json.loads(resp.read().decode("utf-8"))
    except Exception as e:
        metrics = {"error": str(e)}

    invalid_seqs = {10000 + i for i in range(n_bad)}
    valid_seqs = set(range(1, n_good + 1))
    leaked = [s for s in received_seqs if s in invalid_seqs]
    got_valid = [s for s in received_seqs if s in valid_seqs]
    return {
        "published_invalid": n_bad,
        "published_valid": n_good,
        "stomp_received_valid": len(got_valid),
        "stomp_received_invalid_leaked": len(leaked),
        "filter_effectiveness": round((n_bad - len(leaked)) / n_bad, 4) if n_bad else 1.0,
        "valid_delivery_ratio": round(len(got_valid) / n_good, 4) if n_good else 0,
        "server_metrics": metrics,
    }


def http_json(url: str, method: str = "GET") -> dict:
    import urllib.request
    req = urllib.request.Request(url, method=method)
    with urllib.request.urlopen(req, timeout=5) as resp:
        return json.loads(resp.read().decode("utf-8"))


def run_throughput_server_side(seconds: float = 10.0, workers: int = 4) -> dict:
    """用服务端 wsPushed 计数吞吐，避免单订阅端读不过来导致低估。"""
    import urllib.request
    urllib.request.urlopen(urllib.request.Request("http://localhost:8080/api/metrics/sensor/reset", method="POST"), timeout=3).read()
    time.sleep(0.5)
    stop_at = time.time() + seconds
    sent = {"n": 0}
    lock = threading.Lock()

    def worker(wid: int):
        local = 0
        client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"bench-tps2-{wid}-{time.time_ns()}")
        client.username_pw_set(MQTT_USER, MQTT_PASS)
        client.connect(BROKER, MQTT_PORT, 60)
        seq = wid * 1000000
        while time.time() < stop_at:
            seq += 1
            client.publish(TOPIC_MQTT, json.dumps({"id": DEVICE_ID, "wd": 26.5, "seq": seq}), qos=0)
            local += 1
        client.disconnect()
        with lock:
            sent["n"] += local

    t0 = time.perf_counter()
    with ThreadPoolExecutor(max_workers=workers) as pool:
        for f in as_completed([pool.submit(worker, i) for i in range(workers)]):
            f.result()
    elapsed = time.perf_counter() - t0
    time.sleep(2.0)  # 等入站队列排空
    metrics = http_json("http://localhost:8080/api/metrics/sensor")
    pushed = int(metrics.get("wsPushed", 0))
    return {
        "duration_s": round(elapsed, 3),
        "workers": workers,
        "mqtt_publish_count": sent["n"],
        "mqtt_publish_qps": round(sent["n"] / elapsed, 2),
        "server_ws_pushed": pushed,
        "server_ws_push_qps": round(pushed / elapsed, 2),
        "delivery_ratio": round(pushed / sent["n"], 4) if sent["n"] else 0,
        "server_metrics": metrics,
    }


def main():
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    print("running latency...", flush=True)
    latency = run_latency(200)
    print(json.dumps(latency, ensure_ascii=False), flush=True)
    print("cooling down...", flush=True)
    time.sleep(3.0)
    print("running throughput (server-side)...", flush=True)
    tps = run_throughput_server_side(10.0, workers=4)
    print(json.dumps(tps, ensure_ascii=False), flush=True)
    print("cooling down...", flush=True)
    time.sleep(3.0)
    print("running anomaly filter...", flush=True)
    anomaly = run_anomaly_filter(50, 50)
    print(json.dumps(anomaly, ensure_ascii=False), flush=True)
    report = {
        "project": "智慧环境监控平台",
        "path_frontend": r"frontend/",
        "path_backend": r"backend/",
        "broker": "EMQX @ localhost:1883",
        "bridge": "MQTT(kg/wd) -> Spring ExecutorChannel -> STOMP(/topic/temperature/{id})",
        "optimizations": [
            "MQTT 入站改 ExecutorChannel，回调与业务解耦",
            "热路径先 WebSocket 推送再异步落库",
            "传感器异常值范围校验并统计拒绝率",
            "关闭 MyBatis SQL 控制台日志，扩大异步线程池与连接池",
            "修复 pub/sub 共用 clientId 互相踢线问题",
        ],
        "latency": latency,
        "throughput": tps,
        "anomaly_filter": anomaly,
        "device_topics": ["ID1OUT", "ID2OUT", "ID3OUT", "kg/wd", "kg/sd", "kg/gg"],
        "notes": [
            "本机可复现：MQTT 发布到 Spring Integration 桥接，再经 STOMP WebSocket 送达",
            "延迟为单条端到端（MQTT publish -> STOMP MESSAGE）",
        ],
    }
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {REPORT}", flush=True)


if __name__ == "__main__":
    main()
