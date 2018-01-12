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
import com.lightbend.model.winerecord.WineRecord
import com.lightbend.modelServer.ModelToServe
import com.lightbend.modelServer.model.Model
import com.lightbend.modelServer.typeschema.ModelTypeSerializer
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.util.Collector

object DataProcessorMap{
  def apply() : DataProcessorMap = new DataProcessorMap()
}

class DataProcessorMap extends RichCoFlatMapFunction[WineRecord, ModelToServe, Double] with CheckpointedFunction{

  var currentModel : Option[Model] = None
  var newModel : Option[Model] = None
  @transient private var checkpointedState: ListState[Option[Model]] = null

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

  override def flatMap2(model: ModelToServe, out: Collector[Double]): Unit = {

    println(s"New model - $model")
    newModel = ModelToServe.toModel(model)
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