package life_cycle

case class AppLifecycleActorState(
    isAppShuttingDown: Boolean = false
) {
  def shutdown(): AppLifecycleActorState = copy(isAppShuttingDown = true)
  def isReady: Boolean = !isAppShuttingDown
}
