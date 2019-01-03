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

package com.lightbend.modelServer.model.PMML

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.modelServer.model.{Model, ModelFactory}
import org.dmg.pmml.{FieldName, PMML}
import org.jpmml.evaluator.visitors._
import org.jpmml.evaluator.{FieldValue, ModelEvaluatorFactory, TargetField}
import org.jpmml.model.PMMLUtil

import scala.collection._

// Abstract class for any PMML model processing. It has to be extended by the user
// implement score method, based on his own model
abstract class PMMLModel(inputStream: Array[Byte]) extends Model {

  var arguments = mutable.Map[FieldName, FieldValue]()

  // Marshall PMML
  val pmml = PMMLUtil.unmarshal(new ByteArrayInputStream(inputStream))

  // Optimize model// Optimize model
  PMMLModelBase.optimize(pmml)

  // Create and verify evaluator
  val evaluator = ModelEvaluatorFactory.newInstance.newModelEvaluator(pmml)
  evaluator.verify()

  // Get input/target fields
  val inputFields = evaluator.getInputFields
  val target: TargetField = evaluator.getTargetFields.get(0)
  val tname = target.getName

  override def cleanup(): Unit = {}

  override def toBytes : Array[Byte] = {
    var stream = new ByteArrayOutputStream()
    PMMLUtil.marshal(pmml, stream)
    stream.toByteArray
  }

  override def getType: Long = ModelDescriptor.ModelType.PMML.value
}

object PMMLModelBase{

  // List of PMML optimizers (https://groups.google.com/forum/#!topic/jpmml/rUpv8hOuS3A)
  private val optimizers = Array(new ExpressionOptimizer, new FieldOptimizer, new PredicateOptimizer, new GeneralRegressionModelOptimizer, new NaiveBayesModelOptimizer, new RegressionModelOptimizer)

  // OPtimize PMML model
  def optimize(pmml : PMML) = this.synchronized {
    optimizers.foreach(opt =>
      try {
        opt.applyTo(pmml)
      } catch {
        case t: Throwable => {
          println(s"Error optimizing model for optimizer $opt")
          t.printStackTrace()
        }
      }
    )
  }
}