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

package com.lightbend.modelServer

import scala.util.Try
import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.modelServer.model.{Model, ModelFactoryResolverTrait}

/**
  * Created by boris on 5/8/17.
  */
object ModelToServe {

  private var resolver : ModelFactoryResolverTrait = _

  def setResolver(res : ModelFactoryResolverTrait) : Unit = resolver = res

  override def toString: String = super.toString
  def fromByteArray(message: Array[Byte]): Try[ModelToServe] = Try{
    val m = ModelDescriptor.parseFrom(message)
    m.messageContent.isData match {
      case true => new ModelToServe(m.name, m.description, m.modeltype, m.getData.toByteArray, m.dataType)
      case _ => throw new Exception("Location based is not yet supported")
    }
  }

  def copy(from: Option[Model]): Option[Model] =
    from match {
      case Some(model) => Some(resolver.getFactory(model.getType.asInstanceOf[Int]).get.restore(model.toBytes()))
      case _ => None
    }

  def restore(t : Int, content : Array[Byte]): Option[Model] = Some(resolver.getFactory(t).get.restore(content))

  def toModel(model: ModelToServe): Option[Model] =
    resolver.getFactory(model.modelType.value) match {
      case Some(factory) => factory.create (model)
      case _ => None
    }
}

case class ModelToServe(name: String, description: String,
                        modelType: ModelDescriptor.ModelType,
                        model : Array[Byte], dataType : String) {}

case class ModelToServeStats(name: String = "", description: String = "",
                             modelType: ModelDescriptor.ModelType = ModelDescriptor.ModelType.PMML,
                             since : Long = 0, var usage : Long = 0, var duration : Double = .0,
                             var min : Long = Long.MaxValue, var max : Long = Long.MinValue){
  def this(m : ModelToServe) = this(m.name, m.description, m.modelType, System.currentTimeMillis())
  def incrementUsage(execution : Long) : ModelToServeStats = {
    usage = usage + 1
    duration = duration + execution
    if(execution < min) min = execution
    if(execution > max) max = execution
    this
  }
}

case class ModelWithType(isCurrent : Boolean, dataType: String, model: Option[Model])

case class ServingResult(duration : Long, result: AnyVal)
