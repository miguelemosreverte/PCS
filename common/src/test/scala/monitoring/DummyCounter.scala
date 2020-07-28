package com.letgo.rules_engine.acceptance.infrastructure.monitoring

import com.letgo.rules_engine.infrastructure.monitoring.Counter

class DummyCounter(name: String) extends Counter {
  override def increment(): Unit   = ()
  override def add(num: Int): Unit = ()
}
