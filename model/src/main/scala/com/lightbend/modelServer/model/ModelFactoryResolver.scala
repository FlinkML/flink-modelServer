package com.lightbend.modelServer.model

// Generic definition of model factory resolver
trait ModelFactoryResolver {
  def getFactory(`type` : Int) : Option[ModelFactory]
}
