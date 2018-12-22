package com.lightbend.modelServer.model

// Container for the data to serve
trait DataToServe {
  def getType : String
  def getRecord : AnyVal
}
