package com.lightbend.modelServer.model

trait DataToServe {
  def getType : String
  def getRecord : AnyVal
}
