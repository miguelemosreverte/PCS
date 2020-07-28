package com.letgo.rules_engine.acceptance.infrastructure.monitoring

import com.letgo.rules_engine.infrastructure.monitoring.{Counter, Gauge, Histogram, Monitoring}

class DummyMonitoring extends Monitoring {
  override def counter(name: String, context: Map[String, String] = Map.empty): Counter = new DummyCounter(name)

  override def histogram(name: String): Histogram = new DummyHistogram(name)

  override def gauge(name: String): Gauge = new DummyGauge(name)
}
