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

package com.lightbend.modelServer.keyed

import com.lightbend.model.winerecord.WineRecord
import com.lightbend.modelServer.model.Model
import com.lightbend.modelServer.typeschema.ModelTypeSerializer
import com.lightbend.modelServer.{ModelToServe, ModelToServeStats}
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.util.Collector

/**
  * Created by boris on 5/8/17.
  *
  * Main class processing data using models
  *
  * see http://dataartisans.github.io/flink-training/exercises/eventTimeJoin.html for details
  */

object DataProcessorKeyed {
  def apply() = new DataProcessorKeyed
}

class DataProcessorKeyed extends CoProcessFunction[WineRecord, ModelToServe, Double] with CheckpointedFunction {

  // The managed keyed state see https://ci.apache.org/projects/flink/flink-docs-release-1.4/dev/stream/state.html
  var modelState: ValueState[ModelToServeStats] = _
  var newModelState: ValueState[ModelToServeStats] = _

  var currentModel : Option[Model] = None
  var newModel : Option[Model] = None

  @transient private var checkpointedState: ListState[Option[Model]] = null

  override def open(parameters: Configuration): Unit = {
    val modelDesc = new ValueStateDescriptor[ModelToServeStats](
      "currentModel",   // state name
      createTypeInformation[ModelToServeStats]) // type information
    modelDesc.setQueryable("currentModel")
    modelState = getRuntimeContext.getState(modelDesc)
    val newModelDesc = new ValueStateDescriptor[ModelToServeStats](
      "newModel",         // state name
      createTypeInformation[ModelToServeStats])  // type information
    newModelState = getRuntimeContext.getState(newModelDesc)
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    checkpointedState.clear()
    checkpointedState.add(currentModel)
    checkpointedState.add(newModel)
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
    val descriptor = new ListStateDescriptor[Option[Model]] (
      "modelState",
      new ModelTypeSerializer)

    checkpointedState = context.getOperatorStateStore.getListState (descriptor)

    if (context.isRestored) {
      val iterator = checkpointedState.get().iterator()
      currentModel = iterator.next()
      newModel = iterator.next()
    }
  }

  override def processElement2(model: ModelToServe, ctx: CoProcessFunction[WineRecord, ModelToServe, Double]#Context, out: Collector[Double]): Unit = {

    println(s"New model - $model")
    newModelState.update(new ModelToServeStats(model))
    newModel = ModelToServe.toModel(model)
  }

  override def processElement1(record: WineRecord, ctx: CoProcessFunction[WineRecord, ModelToServe, Double]#Context, out: Collector[Double]): Unit = {

    // See if we have update for the model
    newModel match {
      case Some(model) => {
        // Clean up current model
        currentModel match {
          case Some(m) => m.cleanup()
          case _ =>
        }
        // Update model
        currentModel = newModel
        modelState.update(newModelState.value())
        newModel = None
      }
      case _ =>
    }

    // Actually process data
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
}
