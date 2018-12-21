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

package com.lightbend.modelServer.model.tensorflow

import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.modelServer.model.Model
import org.tensorflow.{Graph, Session}

/**
  * Created by boris on 5/26/17.
  * Implementation of tensorflow model
  */

abstract class TensorFlowModel(inputStream : Array[Byte]) extends Model(inputStream){

  val graph = new Graph
  graph.importGraphDef(inputStream)
  val session = new Session(graph)


  override def cleanup(): Unit = {
    try{
      session.close
    }catch {
      case t: Throwable =>    // Swallow
    }
    try{
      graph.close
    }catch {
      case t: Throwable =>    // Swallow
    }
  }

  override def toBytes(): Array[Byte] = graph.toGraphDef

  override def getType: Long = ModelDescriptor.ModelType.TENSORFLOW.value
}