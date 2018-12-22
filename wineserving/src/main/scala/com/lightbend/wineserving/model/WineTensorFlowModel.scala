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

package com.lightbend.wineserving.model

import com.lightbend.model.winerecord.WineRecord
import com.lightbend.modelServer.ModelToServe
import com.lightbend.modelServer.model.tensorflow.TensorFlowModel
import com.lightbend.modelServer.model.{Model, ModelFactory}
import org.tensorflow.Tensor

// Tensorflow model implementation for wine data
class WineTensorFlowModel(inputStream : Array[Byte]) extends TensorFlowModel(inputStream) {

  override def score(input: AnyVal): AnyVal = {

    // Convert input data
    val record = input.asInstanceOf[WineRecord]
    // Create input tensor
    val data = Array(
      record.fixedAcidity.toFloat,
      record.volatileAcidity.toFloat,
      record.citricAcid.toFloat,
      record.residualSugar.toFloat,
      record.chlorides.toFloat,
      record.freeSulfurDioxide.toFloat,
      record.totalSulfurDioxide.toFloat,
      record.density.toFloat,
      record.pH.toFloat,
      record.sulphates.toFloat,
      record.alcohol.toFloat
    )
    val modelInput = Tensor.create(Array(data))
    // Serve model using tensorflow APIs
    val result = session.runner.feed("dense_1_input", modelInput).fetch("dense_3/Sigmoid").run().get(0)
    // Get result shape
    val rshape = result.shape
    // Map output tensor to shape
    var rMatrix = Array.ofDim[Float](rshape(0).asInstanceOf[Int], rshape(1).asInstanceOf[Int])
    result.copyTo(rMatrix)
    // Get result
    var value = (0, rMatrix(0)(0))
    1 to (rshape(1).asInstanceOf[Int] - 1) foreach { i => {
      if (rMatrix(0)(i) > value._2)
        value = (i, rMatrix(0)(i))
    }
    }
    value._1.toDouble
  }
}

// Factory for wine data PMML model
object WineTensorFlowModel extends  ModelFactory {

  override def create(input: ModelToServe): Option[Model] = {
    try {
      Some(new WineTensorFlowModel(input.model))
    }catch{
      case t: Throwable => None
    }
  }

  override def restore(bytes: Array[Byte]): Model = new WineTensorFlowModel(bytes)
}