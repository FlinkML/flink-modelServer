package com.lightbend.wineserving.model

import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.modelServer.model.{ModelFactory, ModelFactoryResolver}

// Model factory resolver - requires specific factories
object WineFactoryResolver extends ModelFactoryResolver{

  private val factories = Map(ModelDescriptor.ModelType.PMML.value -> WinePMMLModel,
                              ModelDescriptor.ModelType.TENSORFLOW.value -> WineTensorFlowModel)

  override def getFactory(`type`: Int): Option[ModelFactory] = factories.get(`type`)
}
