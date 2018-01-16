package com.lightbend.modelServer.typeschema

import com.lightbend.modelServer.{ModelToServe, ModelWithType}
import com.lightbend.modelServer.model.Model
import org.apache.flink.api.common.typeutils.{CompatibilityResult, GenericTypeSerializerConfigSnapshot, TypeSerializer, TypeSerializerConfigSnapshot}
import org.apache.flink.core.memory.{DataInputView, DataOutputView}

class ModelWithTypeSerializer extends TypeSerializer[ModelWithType] {

  override def createInstance(): ModelWithType = ModelWithType(false, "", None)

  override def canEqual(obj: scala.Any): Boolean = obj.isInstanceOf[ModelWithTypeSerializer]

  override def duplicate(): TypeSerializer[ModelWithType] = new ModelWithTypeSerializer

  override def ensureCompatibility(configSnapshot: TypeSerializerConfigSnapshot): CompatibilityResult[ModelWithType] =
    configSnapshot match{
      case _ : ModelWithTypeSerializerConfigSnapshot => CompatibilityResult.compatible()
      case _ => CompatibilityResult.requiresMigration()
    }

  override def serialize(model: ModelWithType, target: DataOutputView): Unit = {
    target.writeBoolean(model.isCurrent)
    target.writeChars(model.dataType)
    model.model match {
      case Some(m) =>
        target.writeBoolean(true)
        val content = m.toBytes()
        target.writeLong(m.getType)
        target.writeLong(content.length)
        target.write(content)
      case _ => target.writeBoolean(false)
    }
  }

  override def isImmutableType: Boolean = false

  override def getLength: Int = -1

  override def snapshotConfiguration(): TypeSerializerConfigSnapshot = new ModelWithTypeSerializerConfigSnapshot

  override def copy(from: ModelWithType): ModelWithType =
    ModelWithType(from.isCurrent, from.dataType, ModelToServe.copy(from.model))

  override def copy(from: ModelWithType, reuse: ModelWithType): ModelWithType = copy(from)

  override def copy(source: DataInputView, target: DataOutputView): Unit = {
    target.writeBoolean(source.readBoolean())
    target.writeChars(source.readLine())
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

  override def deserialize(source: DataInputView): ModelWithType = {
    val current = source.readBoolean()
    val dataType = source.readLine()
    source.readBoolean() match {
      case true =>
        val t = source.readLong().asInstanceOf[Int]
        val size = source.readLong().asInstanceOf[Int]
        val content = new Array[Byte](size)
        source.read(content)
        ModelWithType(current, dataType, ModelToServe.restore(t, content))
      case _ => ModelWithType(current, dataType, None)
    }
  }

  override def deserialize(reuse: ModelWithType, source: DataInputView): ModelWithType = deserialize(source)

  override def equals(obj: scala.Any): Boolean = obj.isInstanceOf[ModelWithTypeSerializer]

  override def hashCode(): Int = 42
}

object ModelWithTypeSerializer{

  def apply : ModelWithTypeSerializer = new ModelWithTypeSerializer()
}


object ModelWithTypeSerializerConfigSnapshot {
  val VERSION = 1
}

class ModelWithTypeSerializerConfigSnapshot
  extends TypeSerializerConfigSnapshot{

  import ModelWithTypeSerializerConfigSnapshot._

  //  def this() {this(classOf[T])}

  override def getVersion = VERSION

  var typeClass = classOf[ModelWithType]

  override def write(out: DataOutputView): Unit = {
    super.write(out)
    // write only the classname to avoid Java serialization
    out.writeUTF(classOf[ModelWithType].getName)
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
