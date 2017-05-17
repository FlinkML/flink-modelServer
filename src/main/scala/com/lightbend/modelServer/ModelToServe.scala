package com.lightbend.modelServer

import scala.util.Try
import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.modelServer.model.{Model, PMMLModel}

/**
  * Created by boris on 5/8/17.
  */
object ModelToServe {
  def fromByteArray(message: Array[Byte]): Try[ModelToServe] = Try{
    val m = ModelDescriptor.parseFrom(message)
    m.messageContent.isData match {
      case true => new ModelToServe(m.name, m.description, m.modeltype, m.getData.toByteArray, m.dataType)
      case _ => throw new Exception("Location based is not yet supported")
    }
  }
}

case class ModelToServe(name: String, description: String,
                        modelType: ModelDescriptor.ModelType,
                        model : Array[Byte], dataType : String) {}

case class ModelToServeStats(name: String, description: String,
                             modelType: ModelDescriptor.ModelType, since : Long, var usage : Long){
  def this(m : ModelToServe) = this(m.name, m.description, m.modelType, System.currentTimeMillis(), 0L)
  def incrementUsage() : ModelToServeStats = {
    usage = usage + 1
    this
  }
}