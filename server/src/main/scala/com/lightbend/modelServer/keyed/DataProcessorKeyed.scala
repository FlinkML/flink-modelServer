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

import com.lightbend.modelServer.model.{DataToServe, Model}
import com.lightbend.modelServer.typeschema.ModelTypeSerializer
import com.lightbend.modelServer.{ModelToServe, ModelToServeStats, ServingResult}
import org.apache.flink.api.common.state.{ListState, ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.configuration.Configuration
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

class DataProcessorKeyed extends CoProcessFunction[DataToServe, ModelToServe, ServingResult]{

  // In Flink class instance is created not for key, but rater key groups
  // https://ci.apache.org/projects/flink/flink-docs-release-1.6/dev/stream/state/state.html#keyed-state-and-operator-state
  // As a result, any key specific sate data has to be in the key specific state

  var modelState: ValueState[ModelToServeStats] = _
  var newModelState: ValueState[ModelToServeStats] = _

  var currentModel : ValueState[Option[Model]] = _
  var newModel : ValueState[Option[Model]] = _

  @transient private var checkpointedState: ListState[Option[Model]] = null

  override def open(parameters: Configuration): Unit = {

    val modelStateDesc = new ValueStateDescriptor[ModelToServeStats](
      "currentModelState",                  // state name
      createTypeInformation[ModelToServeStats])     // type information
    modelStateDesc.setQueryable("currentModelState")     // Expose it for queryable state
    modelState = getRuntimeContext.getState(modelStateDesc)
    val newModelStateDesc = new ValueStateDescriptor[ModelToServeStats](
      "newModelState",                      // state name
      createTypeInformation[ModelToServeStats])     // type information
    newModelState = getRuntimeContext.getState(newModelStateDesc)
    val modelDesc = new ValueStateDescriptor[Option[Model]](
      "currentModel",                               // state name
      new ModelTypeSerializer)                      // type information
    currentModel = getRuntimeContext.getState(modelDesc)
    val newModelDesc = new ValueStateDescriptor[Option[Model]](
      "newModel",                                   // state name
      new ModelTypeSerializer)                       // type information
    newModel = getRuntimeContext.getState(newModelDesc)
  }


  override def processElement2(model: ModelToServe, ctx: CoProcessFunction[DataToServe, ModelToServe, ServingResult]#Context, out: Collector[ServingResult]): Unit = {

    if(newModel.value == null) newModel.update(None)
    if(currentModel.value == null) currentModel.update(None)

    println(s"New model - $model")
    ModelToServe.toModel(model) match {
      case Some(md) =>
        newModel.update (Some(md))
        newModelState.update (new ModelToServeStats (model))
      case _ =>
    }
  }

  override def processElement1(record: DataToServe, ctx: CoProcessFunction[DataToServe, ModelToServe, ServingResult]#Context, out: Collector[ServingResult]): Unit = {

    if(newModel.value == null) newModel.update(None)
    if(currentModel.value == null) currentModel.update(None)
    // See if we have update for the model
    newModel.value.foreach { model =>
      // close current model first
      currentModel.value.foreach(_.cleanup())
      // Update model
      currentModel.update(newModel.value)
      modelState.update(newModelState.value())
      newModel.update(None)
    }

    // Actually process data
    currentModel.value match {
      case Some(model) => {
        val start = System.currentTimeMillis()
        val result = model.score(record.getRecord)
        val duration = System.currentTimeMillis() - start
        modelState.update(modelState.value().incrementUsage(duration))
        out.collect(ServingResult(duration, result))
      }
      case _ =>
    }
  }
}
