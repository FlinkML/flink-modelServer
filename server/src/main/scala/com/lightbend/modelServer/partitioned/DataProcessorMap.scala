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

  private var currentModels = Map[String, Model]()
  private var newModels = Map[String, Model]()

  @transient private var checkpointedState: ListState[ModelWithType] = _

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    checkpointedState.clear()
    currentModels.foreach(entry => checkpointedState.add(ModelWithType(true, entry._1, Some(entry._2))))
    newModels.foreach(entry => checkpointedState.add(ModelWithType(false, entry._1, Some(entry._2))))
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
    val checkPointDescriptor = new ListStateDescriptor[ModelWithType] (
        "modelState",
        new ModelWithTypeSerializer)
    checkpointedState = context.getOperatorStateStore.getListState (checkPointDescriptor)

    if (context.isRestored) {
      val nm = new ListBuffer[(String, Model)]()
      val cm = new ListBuffer[(String, Model)]()
      checkpointedState.get().iterator().asScala.foreach(modelWithType => {
        modelWithType.model match {
          case Some(model) =>
            modelWithType.isCurrent match {
              case true => cm += (modelWithType.dataType -> model)
              case _ => nm += (modelWithType.dataType -> model)
            }
          case _ =>
        }
      })
      currentModels = Map(cm: _*)
      newModels = Map(nm: _*)
    }
  }

  override def flatMap2(model: ModelToServe, out: Collector[ServingResult]): Unit = {

    println(s"New model - $model")
    ModelToServe.toModel(model) match {
      case Some(md) => newModels += (model.dataType -> md)
      case _ =>
    }
  }

  override def flatMap1(record: DataToServe, out: Collector[ServingResult]): Unit = {
    // See if we need to update
    newModels.contains(record.getType) match {
      case true =>
        currentModels.contains(record.getType) match {
          case true => currentModels(record.getType).cleanup()
          case _ =>
        }
        currentModels += (record.getType -> newModels(record.getType))
        newModels -= record.getType
      case _ =>
    }
    // actually process
    currentModels.contains(record.getType) match {
      case true =>
        val start = System.currentTimeMillis()
        val result = currentModels(record.getType).score(record.getRecord)
        val duration = System.currentTimeMillis() - start
        out.collect(ServingResult(duration, result))
      case _ =>
    }
  }
}