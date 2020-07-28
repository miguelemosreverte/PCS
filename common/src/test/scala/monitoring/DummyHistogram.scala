package com.letgo.rules_engine.acceptance.infrastructure.monitoring

import com.letgo.rules_engine.infrastructure.monitoring.Histogram

class DummyHistogram(name: String) extends Histogram {
  override def record(value: Long): Unit = ()
}
