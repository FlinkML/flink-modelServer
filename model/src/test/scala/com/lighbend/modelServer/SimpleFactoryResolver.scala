package com.lighbend.modelServer

import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.modelServer.model.{ModelFactory, ModelFactoryResolver}

class SimpleFactoryResolver extends ModelFactoryResolver{

  private val factories = Map(
    ModelDescriptor.ModelType.PMML.value -> SimplePMMLModel,
    ModelDescriptor.ModelType.TENSORFLOW.value -> SimpleTensorflowModel,
    ModelDescriptor.ModelType.TENSORFLOWSAVED.value -> SimpleTensorflowBundleModel
  )

  override def getFactory(`type`: Int): Option[ModelFactory] = factories.get(`type`)
}