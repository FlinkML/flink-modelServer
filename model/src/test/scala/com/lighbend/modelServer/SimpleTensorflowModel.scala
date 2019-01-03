package com.lighbend.modelServer

import com.lightbend.modelServer.ModelToServe
import com.lightbend.modelServer.model.{Model, ModelFactory}
import com.lightbend.modelServer.model.tensorflow.TensorFlowModel
import org.tensorflow.{Graph, Session}

class SimpleTensorflowModel (inputStream: Array[Byte]) extends TensorFlowModel(inputStream){

  override def score(input: AnyVal): AnyVal = null.asInstanceOf[AnyVal]

  // Getters for validatio
  def getSession: Session = session

  def getGrapth: Graph = graph
}

object SimpleTensorflowModel extends  ModelFactory {

  override def create(input: ModelToServe): Option[Model] = {
    try {
      Some(new SimpleTensorflowModel(input.model))
    }catch{
      case t: Throwable => None
    }
  }

  override def restore(bytes: Array[Byte]): Model = new SimpleTensorflowModel(bytes)

}
