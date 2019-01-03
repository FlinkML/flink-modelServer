package com.lightbend.modelServer.typeschema

import java.io.IOException

import com.lightbend.modelServer.{ModelToServe, ModelWithType}
import org.apache.flink.api.common.typeutils._
import org.apache.flink.core.memory.{DataInputView, DataOutputView}
import org.apache.flink.util.InstantiationUtil

// Serializer for Model with State
class ModelWithTypeSerializer extends TypeSerializer[ModelWithType] {

  override def createInstance(): ModelWithType = ModelWithType(false, "", None)

  override def canEqual(obj: scala.Any): Boolean = obj.isInstanceOf[ModelWithTypeSerializer]

  override def duplicate(): TypeSerializer[ModelWithType] = new ModelWithTypeSerializer

  override def serialize(model: ModelWithType, target: DataOutputView): Unit = {
    target.writeBoolean(model.isCurrent)
    target.writeUTF(model.dataType)
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

  override def snapshotConfiguration(): TypeSerializerSnapshot[ModelWithType] = new ModelWithTypeSerializerConfigSnapshot

  override def copy(from: ModelWithType): ModelWithType =
    ModelWithType(from.isCurrent, from.dataType, ModelToServe.copy(from.model))

  override def copy(from: ModelWithType, reuse: ModelWithType): ModelWithType = copy(from)

  override def copy(source: DataInputView, target: DataOutputView): Unit = {
    target.writeBoolean(source.readBoolean())
    target.writeUTF(source.readUTF())
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
    val dataType = source.readUTF()
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

object ModelWithTypeSerializerConfigSnapshot{

  val CURRENT_VERSION = 1
}

// Snapshot configuration for Model with type serializer
// See https://github.com/apache/flink/blob/master/flink-core/src/main/java/org/apache/flink/api/common/typeutils/SimpleTypeSerializerSnapshot.java
class ModelWithTypeSerializerConfigSnapshot extends SimpleTypeSerializerSnapshot[ModelWithType]{

  import ModelWithTypeSerializerConfigSnapshot._

  private var serializerClass = classOf[ModelWithTypeSerializer]

  override def getCurrentVersion: Int = CURRENT_VERSION

  override def writeSnapshot(out: DataOutputView): Unit = {
    out.writeUTF(serializerClass.getName)
  }

  override def readSnapshot(readVersion: Int, in: DataInputView, classLoader: ClassLoader): Unit = {
    readVersion match {
      case 1 =>
        val className = in.readUTF
        resolveClassName(className, classLoader, false)
       case _ =>
        throw new IOException("Unrecognized version: " + readVersion)
    }
  }

  override def restoreSerializer(): TypeSerializer[ModelWithType] = InstantiationUtil.instantiate(serializerClass)

  override def resolveSchemaCompatibility(newSerializer: TypeSerializer[ModelWithType]): TypeSerializerSchemaCompatibility[ModelWithType] =
    if (newSerializer.getClass eq serializerClass) TypeSerializerSchemaCompatibility.compatibleAsIs()
    else TypeSerializerSchemaCompatibility.incompatible()

  private def resolveClassName(className: String, cl: ClassLoader, allowCanonicalName: Boolean): Unit =
    try
      serializerClass = cast(Class.forName(className, false, cl))
    catch {
      case e: Throwable =>
        throw new IOException("Failed to read SimpleTypeSerializerSnapshot: Serializer class not found: " + className, e)
  }

  @SuppressWarnings(Array("unchecked"))
  @throws[IOException]
  private def cast[T](clazz: Class[_]) : Class[ModelWithTypeSerializer]   = {
    if (!classOf[ModelWithTypeSerializer].isAssignableFrom(clazz)) throw new IOException("Failed to read SimpleTypeSerializerSnapshot. " + "Serializer class name leads to a class that is not a TypeSerializer: " + clazz.getName)
    clazz.asInstanceOf[Class[ModelWithTypeSerializer]]
  }
}
