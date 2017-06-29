package com.lightbend.modelServer


import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.model.winerecord.WineRecord
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.{CoProcessFunction, RichCoProcessFunction}
import org.apache.flink.util.Collector
import com.lightbend.modelServer.model.{Model, PMMLModel, TensorFlowModel}

/**
  * Created by boris on 5/8/17.
  *
  * Main class processing data using models
  *
  * see http://dataartisans.github.io/flink-training/exercises/eventTimeJoin.html for details
  */

object DataProcessor {
  def apply() = new DataProcessor
}

class DataProcessor extends RichCoProcessFunction[WineRecord, ModelToServe, Double]{

  // The managed keyed state see https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/stream/state.html
  var modelState: ValueState[ModelToServeStats] = _
  var newModelState: ValueState[ModelToServeStats] = _
  // The raw state - https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/stream/state.html#raw-and-managed-state
  var currentModel : Option[Model] = None
  var newModel : Option[Model] = None

  override def open(parameters: Configuration): Unit = {
    val modelDesc = new ValueStateDescriptor[ModelToServeStats]("currentModel", createTypeInformation[ModelToServeStats])
    modelDesc.setQueryable("currentModel")
    modelState = getRuntimeContext.getState(modelDesc)
    val newModelDesc = new ValueStateDescriptor[ModelToServeStats]("newModel", createTypeInformation[ModelToServeStats])
    newModelState = getRuntimeContext.getState(newModelDesc)
   }


  override def processElement2(model: ModelToServe, ctx: CoProcessFunction.Context, out: Collector[Double]): Unit = {

    println(s"New model - $model")
    newModelState.update(new ModelToServeStats(model))
    newModel = model.modelType match {
      case ModelDescriptor.ModelType.PMML => PMMLModel(model.model)             // PMML
      case ModelDescriptor.ModelType.TENSORFLOW => TensorFlowModel(model.model) // Tensorflow
      case _ => None // Not supported yet
    }
  }

  override def processElement1(record: WineRecord, ctx: CoProcessFunction.Context, out: Collector[Double]): Unit = {

    // See if we have update for the model
    newModel match {
      case Some(model) => {
        // Clean up current model
        currentModel match {
          case Some(m) => m.cleanup()
          case _ =>
        }
        // Update model
        currentModel = Some(model)
        modelState.update(newModelState.value())
        newModel = None
      }
      case _ =>
    }
    currentModel match {
      case Some(model) => {
        val start = System.currentTimeMillis()
        val quality = model.score(record.asInstanceOf[AnyVal]).asInstanceOf[Double]
        val duration = System.currentTimeMillis() - start
        modelState.update(modelState.value().incrementUsage(duration))
        println(s"Calculated quality - $quality calculated in $duration ms")
      }
      case _ => println("No model available - skipping")
    }
  }

  override def onTimer(timestamp: Long, ctx: CoProcessFunction.OnTimerContext, out: Collector[Double]): Unit = {}
}
