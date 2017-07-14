package com.lightbend.modelServer

/**
  * Created by boris on 5/14/17.
  *
  * Main class processing data using models
  *
  */
import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.model.winerecord.WineRecord
import com.lightbend.modelServer.model.{Model, PMMLModel, TensorFlowModel}
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.util.Collector

object DataProcessorMap{
  def apply() : DataProcessorMap = new DataProcessorMap()

  private val factories = Map(ModelDescriptor.ModelType.PMML -> PMMLModel,
    ModelDescriptor.ModelType.TENSORFLOW -> TensorFlowModel)
}

class DataProcessorMap extends RichCoFlatMapFunction[WineRecord, ModelToServe, Double]{


  // The raw state - https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/stream/state.html#raw-and-managed-state
  var currentModel : Option[Model] = None
  var newModel : Option[Model] = None

  override def flatMap2(model: ModelToServe, out: Collector[Double]): Unit = {

    import DataProcessorMap._

    println(s"New model - $model")
    factories.get(model.modelType) match{
      case Some(factory) => factory.create(model)
      case _ => None
    }
  }

  override def flatMap1(record: WineRecord, out: Collector[Double]): Unit = {
    // See if we need to update
    newModel match {
      case Some(model) => {
        // close current model first
        currentModel match {
          case Some(m) => m.cleanup();
          case _ =>
        }
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