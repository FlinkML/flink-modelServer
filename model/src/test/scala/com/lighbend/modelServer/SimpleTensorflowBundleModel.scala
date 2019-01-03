package com.lighbend.modelServer


import com.lightbend.modelServer.ModelToServe
import com.lightbend.modelServer.model.{Model, ModelFactory}
import com.lightbend.modelServer.model.tensorflow.{Signature, TensorFlowBundleModel}
import org.tensorflow.{Graph, Session}

class SimpleTensorflowBundleModel(inputStream: Array[Byte]) extends TensorFlowBundleModel(inputStream) {

  override def score(input: AnyVal): AnyVal = null.asInstanceOf[AnyVal]

  // Getters for testing
  def getGraph: Graph = graph

  def getSession: Session = session

  def getSignatures: Map[String, Signature] = parsedSign

  def getTags: Seq[String] = tags
}

object SimpleTensorflowBundleModel extends  ModelFactory {

  override def create(input: ModelToServe): Option[Model] = {
    try {
      Some(new SimpleTensorflowBundleModel(input.location.getBytes()))
    }catch{
      case t: Throwable => None
    }
  }

  override def restore(bytes: Array[Byte]): Model = new SimpleTensorflowBundleModel(bytes)
}
