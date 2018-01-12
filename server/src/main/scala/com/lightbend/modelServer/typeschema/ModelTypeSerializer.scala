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

package com.lightbend.modelServer.typeschema


import com.lightbend.modelServer.ModelToServe
import org.apache.flink.api.common.typeutils.{CompatibilityResult, GenericTypeSerializerConfigSnapshot, TypeSerializer, TypeSerializerConfigSnapshot}
import com.lightbend.modelServer.model.Model
import org.apache.flink.core.memory.{DataInputView, DataOutputView}

class ModelTypeSerializer extends TypeSerializer[Option[Model]] {


  override def createInstance(): Option[Model] = None

  override def canEqual(obj: scala.Any): Boolean = obj.isInstanceOf[ModelTypeSerializer]

  override def duplicate(): TypeSerializer[Option[Model]] = new ModelTypeSerializer

  override def ensureCompatibility(configSnapshot: TypeSerializerConfigSnapshot): CompatibilityResult[Option[Model]] =
    configSnapshot match{
      case _ : ModelSerializerConfigSnapshot => CompatibilityResult.compatible()
      case _ => CompatibilityResult.requiresMigration()
    }

  override def serialize(record: Option[Model], target: DataOutputView): Unit = {
    record match {
      case Some(model) =>
        target.writeBoolean(true)
        val content = model.toBytes()
        target.writeLong(model.getType)
        target.writeLong(content.length)
        target.write(content)
      case _ => target.writeBoolean(false)
    }
  }

  override def isImmutableType: Boolean = false

  override def getLength: Int = -1

  override def snapshotConfiguration(): TypeSerializerConfigSnapshot = new ModelSerializerConfigSnapshot

  override def copy(from: Option[Model]): Option[Model] = ModelToServe.copy(from)

  override def copy(from: Option[Model], reuse: Option[Model]): Option[Model] = ModelToServe.copy(from)

  override def copy(source: DataInputView, target: DataOutputView): Unit = {
    val exist = source.readBoolean()
    target.writeBoolean(exist)
    exist match {
      case true =>
        target.writeLong (source.readLong () )
        val clen = source.readLong ().asInstanceOf[Int]
        target.writeLong (clen)
        val content = new Array[Byte] (clen)
        source.read (content)
        target.write (content)
      case _ =>
    }
  }

  override def deserialize(source: DataInputView): Option[Model] =
    source.readBoolean() match {
      case true =>
        val t = source.readLong().asInstanceOf[Int]
        val size = source.readLong().asInstanceOf[Int]
        val content = new Array[Byte] (size)
        source.read (content)
        ModelToServe.restore(t, content)
      case _ => None
    }

  override def deserialize(reuse: Option[Model], source: DataInputView): Option[Model] = deserialize(source)

  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[ModelTypeSerializer]

  override def hashCode(): Int = 42
}

object ModelTypeSerializer{

  def apply : ModelTypeSerializer = new ModelTypeSerializer()
}


object ModelSerializerConfigSnapshot {
  val VERSION = 1
}

class ModelSerializerConfigSnapshot
                                extends TypeSerializerConfigSnapshot{

  import ModelSerializerConfigSnapshot._

//  def this() {this(classOf[T])}

  override def getVersion = VERSION

  var typeClass = classOf[Model]

  override def write(out: DataOutputView): Unit = {
    super.write(out)
    // write only the classname to avoid Java serialization
    out.writeUTF(classOf[Model].getName)
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case null => false
      case value if value == this => true
      case _ =>  (obj.getClass == getClass) && typeClass == obj.asInstanceOf[GenericTypeSerializerConfigSnapshot[_]].getTypeClass
    }
  }

  override def hashCode: Int = 42
}
