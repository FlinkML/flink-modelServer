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

// Abstract class for any Tensorflow (optimized export) model processing. It has to be extended by the user
// implement score method, based on his own model

abstract class TensorFlowModel(inputStream : Array[Byte]) extends Model {

  // Model graph
  val graph = new Graph
  graph.importGraphDef(inputStream)
  // Create tensorflow session
  val session = new Session(graph)

  // Cleanup
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

  // Convert tensorflow model to bytes
  override def toBytes(): Array[Byte] = inputStream   //graph.toGraphDef

  // Get model type
  override def getType: Long = ModelDescriptor.ModelType.TENSORFLOW.value

  override def equals(obj: Any): Boolean = {
    obj match {
      case tfModel: TensorFlowModel =>
        tfModel.toBytes.toList == inputStream.toList
      case _ => false
    }
  }
}