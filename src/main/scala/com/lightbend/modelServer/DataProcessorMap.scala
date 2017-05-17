package com.lightbend.modelServer

import java.io.ByteArrayInputStream

import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.model.winerecord.WineRecord
import com.lightbend.modelServer.model.{Model, PMMLModel}
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.util.Collector

/**
  * Created by boris on 5/14/17.
  *
  * Main class processing data using models
  *
  */
object DataProcessorMap{
  def apply() : DataProcessorMap = new DataProcessorMap()
}

class DataProcessorMap extends RichCoFlatMapFunction[WineRecord, ModelToServe, Double]{


  // The raw state - https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/stream/state.html#raw-and-managed-state
  var currentModel : Option[Model] = None
  var newModel : Option[Model] = None

  override def flatMap2(model: ModelToServe, out: Collector[Double]): Unit = {
    println(s"New model - $model")
    newModel = model.modelType match {
      case ModelDescriptor.ModelType.PMML => Some(new PMMLModel(new ByteArrayInputStream(model.model)))
      case _ => None // Not supported yet
    }
  }

  override def flatMap1(record: WineRecord, out: Collector[Double]): Unit = {
    newModel match {
      case Some(model) => {
        // Update model
        currentModel = Some(model)
        newModel = None
      }
      case _ =>
    }
    currentModel match {
      case Some(model) => {
        val start = System.currentTimeMillis()
        val quality = model.score(record.asInstanceOf[AnyVal]).asInstanceOf[Double]
        val duration = System.currentTimeMillis() - start
        println(s"Subtask ${this.getRuntimeContext.getIndexOfThisSubtask} calculated quality - $quality calculated in $duration ms")
      }
      case _ => println("No model available - skipping")
    }
  }
}