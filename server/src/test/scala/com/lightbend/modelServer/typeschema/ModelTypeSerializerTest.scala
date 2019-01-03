package com.lightbend.modelServer.typeschema

import java.io.File
import java.nio.file.{Files, Paths}

import com.lighbend.modelServer.SimpleFactoryResolver
import com.lightbend.model.Modeldescriptor.ModelDescriptor
import com.lightbend.modelServer.ModelToServe
import org.apache.flink.api.common.typeutils.TypeSerializer
import com.lightbend.modelServer.model.Model
import com.lightbend.modelserver.java.typeschema.SerializerTestBase

class ModelTypeSerializerTest extends SerializerTestBase[Option[Model]] {

  private val tfmodeloptimized = "model/TF/optimized/optimized_WineQuality.pb"
  private val tfmodelsaved = "model/TF/saved/"
  private val pmmlmodel = "model/PMML/winequalityDecisionTreeClassification.pmml"

  ModelToServe.setResolver(new SimpleFactoryResolver)

  override protected def createSerializer(): TypeSerializer[Option[Model]] = new ModelTypeSerializer

  override protected def getLength: Int = -1

  override protected def getTypeClass: Class[Option[Model]] = classOf[Option[Model]]

  override protected def getTestData: Array[Option[Model]] = {
    // Get PMML model from File
    var model = getModel(pmmlmodel)
    // Create model from binary
    val pmml = ModelToServe.restore(ModelDescriptor.ModelType.PMML.getNumber, model)
    // Get TF Optimized model from file
    model = getModel(tfmodeloptimized)
    val tfoptimized = ModelToServe.restore(ModelDescriptor.ModelType.TENSORFLOW.getNumber, model)
    // Get TF bundled model location
    val classLoader = getClass.getClassLoader
    val file = new File(classLoader.getResource(tfmodelsaved).getFile)
    val location = file.getPath
    // Create model from location
    val tfbundled = ModelToServe.restore(ModelDescriptor.ModelType.TENSORFLOWSAVED.getNumber, location.getBytes)
    Array[Option[Model]](pmml, tfoptimized, tfbundled)
  }

  private def getModel(fileName: String) : Array[Byte] = {
    val classLoader = getClass.getClassLoader
    val file = new File(classLoader.getResource(fileName).getFile)
    Files.readAllBytes(Paths.get(file.getPath))
  }
}
