package com.lightbend.wineserving.model

import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.modelServer.model.{ModelFactory, ModelFactoryResolverTrait}

object ModeFactoryResolver extends ModelFactoryResolverTrait{

  private val factories = Map(ModelDescriptor.ModelType.PMML.value -> SpecificPMMLModel,
                              ModelDescriptor.ModelType.TENSORFLOW.value -> SpecificTensorFlowModel)

  override def getFactory(`type`: Int): Option[ModelFactory] = factories.get(`type`)
}
