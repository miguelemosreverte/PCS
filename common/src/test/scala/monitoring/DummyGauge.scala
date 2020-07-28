package com.letgo.rules_engine.acceptance.infrastructure.monitoring

import com.letgo.rules_engine.infrastructure.monitoring.Gauge

class DummyGauge(name: String) extends Gauge {
  override def increment(): Unit = ()
  override def decrement(): Unit = ()

  override def add(num: Int): Unit      = ()
  override def subtract(num: Int): Unit = ()
}
