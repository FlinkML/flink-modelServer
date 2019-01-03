/*
 * Copyright (C) 2017  Lightbend
 *
 * This file is part of flink-ModelServing
 *
 * flink-ModelServing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.lightbend.modelServer.partitioned

/**
  * Created by boris on 5/14/17.
  *
  * Main class processing data using models
  *
  */
import com.lightbend.modelServer.{ModelToServe, ModelWithType, ServingResult}
import com.lightbend.modelServer.model.{DataToServe, Model}
import com.lightbend.modelServer.typeschema.ModelWithTypeSerializer
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, MapState}
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.util.Collector

import scala.collection.JavaConverters._
import scala.collection.mutable.{ListBuffer, Map}

object DataProcessorMap{
  def apply() = new DataProcessorMap
}

class DataProcessorMap extends RichCoFlatMapFunction[DataToServe, ModelToServe, ServingResult] with CheckpointedFunction {

  // Current models
  private var currentModels = Map[String, Model]()
  // New models
  private var newModels = Map[String, Model]()

  // Checkpointing state
  @transient private var checkpointedState: ListState[ModelWithType] = _

  // Create a snapshot
  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    // Clear checkpointing state
    checkpointedState.clear()
    // Populate checkpointing state
    currentModels.foreach(entry => checkpointedState.add(new ModelWithType(true, entry._1, Some(entry._2))))
    newModels.foreach(entry => checkpointedState.add(new ModelWithType(false, entry._1, Some(entry._2))))
  }

  // Restore checkpoint
  override def initializeState(context: FunctionInitializationContext): Unit = {
    // Checkpointing descriptor
    val checkPointDescriptor = new ListStateDescriptor[ModelWithType] (
        "modelState",
        new ModelWithTypeSerializer)
    // Get checkpointing data
    checkpointedState = context.getOperatorStateStore.getListState (checkPointDescriptor)

    // If restored
    if (context.isRestored) {
      // Create state
      val nm = new ListBuffer[(String, Model)]()
      val cm = new ListBuffer[(String, Model)]()
      checkpointedState.get().iterator().asScala.foreach(modelWithType => {
        // For each model in the checkpointed state
        modelWithType.model match {
          case Some(model) =>                     // Model is present
            modelWithType.isCurrent match {
              case true => cm += (modelWithType.dataType -> model)  // Its a current model
              case _ => nm += (modelWithType.dataType -> model)     // Its a new model
            }
          case _ =>
        }
      })
      // Convert lists into maps
      currentModels = Map(cm: _*)
      newModels = Map(nm: _*)
    }
  }

  // Process new model
  override def flatMap2(model: ModelToServe, out: Collector[ServingResult]): Unit = {

    println(s"New model - $model")
    ModelToServe.toModel(model) match {                     // Inflate model
      case Some(md) => newModels += (model.dataType -> md)  // Save a new model
      case _ =>
    }
  }

  // Serve data
  override def flatMap1(record: DataToServe, out: Collector[ServingResult]): Unit = {
    // See if we need to update
    newModels.contains(record.getType) match {    // There is a new model for this type
      case true =>
        currentModels.contains(record.getType) match {  // There is currently a model for this type
          case true => currentModels(record.getType).cleanup()  // Cleanup
          case _ =>
        }
        // Update current models and remove a model from new models
        currentModels += (record.getType -> newModels(record.getType))
        newModels -= record.getType
      case _ =>
    }
    // actually process
    currentModels.contains(record.getType) match {
      case true =>
        val start = System.currentTimeMillis()
        // Actual serving
        val result = currentModels(record.getType).score(record.getRecord)
        val duration = System.currentTimeMillis() - start
        // write result out
        out.collect(ServingResult(duration, result))
      case _ =>
    }
  }
}