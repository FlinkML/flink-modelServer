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

/**
  * Created by boris on 5/9/17.
  *
  * Class for PMML model
  */

import java.io.ByteArrayOutputStream

import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.model.winerecord.WineRecord
import com.lightbend.modelServer.ModelToServe
import com.lightbend.modelServer.model.PMML.PMMLModel
import com.lightbend.modelServer.model.{Model, ModelFactory}
import org.jpmml.evaluator.Computable
import org.jpmml.model.PMMLUtil

import scala.collection.JavaConversions._
import scala.collection._


class SpecificPMMLModel(inputStream: Array[Byte]) extends PMMLModel(inputStream) {

  override def score(input: AnyVal): AnyVal = {
    val inputs = input.asInstanceOf[WineRecord]
    arguments.clear()
    inputFields.foreach(field => {
      arguments.put(field.getName, field.prepare(getValueByName(inputs, field.getName.getValue)))
    })

    // Calculate Output// Calculate Output
    val result = evaluator.evaluate(arguments)

    // Prepare output
    result.get(tname) match {
      case c : Computable => c.getResult.toString.toDouble
      case v : Any => v.asInstanceOf[Double]
    }
  }

  private def getValueByName(inputs : WineRecord, name: String) : Double =
    SpecificPMMLModel.names.get(name) match {
    case Some(index) => {
     val v = inputs.getFieldByNumber(index + 1)
      v.asInstanceOf[Double]
    }
    case _ => .0
  }

  override def toBytes : Array[Byte] = {
    var stream = new ByteArrayOutputStream()
    PMMLUtil.marshal(pmml, stream)
    stream.toByteArray
  }

  override def getType: Long = ModelDescriptor.ModelType.PMML.value
}

object SpecificPMMLModel extends ModelFactory{
  private val names = Map("fixed acidity" -> 0,
    "volatile acidity" -> 1,"citric acid" ->2,"residual sugar" -> 3,
    "chlorides" -> 4,"free sulfur dioxide" -> 5,"total sulfur dioxide" -> 6,
    "density" -> 7,"pH" -> 8,"sulphates" ->9,"alcohol" -> 10)

  override def create(input: ModelToServe): Option[Model] = {
    try {
      Some(new SpecificPMMLModel(input.model))
    }catch{
      case t: Throwable => None
    }
  }

  override def restore(bytes: Array[Byte]): Model = new SpecificPMMLModel(bytes)
}