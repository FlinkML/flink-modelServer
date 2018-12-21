package com.lightbend.modelServer.model


trait ModelFactoryResolverTrait {
  def getFactory(`type` : Int) : Option[ModelFactory]
}
