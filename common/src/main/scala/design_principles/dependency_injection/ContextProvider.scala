package design_principles.dependency_injection

trait ContextProvider[ContextProviderRequirements, Context] {
  def getContext(requirements: ContextProviderRequirements): Context
}

trait ContextProvider2[ContextProviderRequirements, Context, Out, Out2] {
  def getContext(requirements: ContextProviderRequirements)(a: Context => Out): Out2
}
