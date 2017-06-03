package com.lightbend.modelServer.model

/**
  * Created by boris on 6/2/17.
  */
import com.esotericsoftware.kryo.{Kryo, Serializer}
import com.esotericsoftware.kryo.io.{Input, Output}


class TensorFlowModelSerializerKryo extends Serializer[TensorFlowModel]{
  
  super.setAcceptsNull(false)
  super.setImmutable(true)

  /** Reads bytes and returns a new object of the specified concrete type.
    * <p>
    * Before Kryo can be used to read child objects, {@link Kryo#reference(Object)} must be called with the parent object to
    * ensure it can be referenced by the child objects. Any serializer that uses {@link Kryo} to read a child object may need to
    * be reentrant.
    * <p>
    * This method should not be called directly, instead this serializer can be passed to {@link Kryo} read methods that accept a
    * serialier.
    *
    * @return May be null if { @link #getAcceptsNull()} is true. */
  
  override def read(kryo: Kryo, input: Input, `type`: Class[TensorFlowModel]): TensorFlowModel = {
    val bytes = Stream.continually(input.readByte()).takeWhile(_ != -1).toArray
    TensorFlowModel(bytes).get
  }

  /** Writes the bytes for the object to the output.
    * <p>
    * This method should not be called directly, instead this serializer can be passed to {@link Kryo} write methods that accept a
    * serialier.
    *
    * @param value May be null if { @link #getAcceptsNull()} is true. */
  
  override def write(kryo: Kryo, output: Output, value: TensorFlowModel): Unit = {
    output.write(value.graph.toGraphDef)
  }
}