package com.lightbend.modelServer.typeschema

import org.apache.flink.core.testutils.CommonTestUtils
import org.junit.Assert.{assertArrayEquals, assertEquals}

import org.junit.Test


class ByteArraySchemaTest {

  @Test
  def testSerializationDesirailization(): Unit = {
    val bytes =  "hello world".getBytes

    assertArrayEquals(bytes, new ByteArraySchema().serialize(bytes))
    assertEquals(bytes, new ByteArraySchema().deserialize(bytes))
  }

  @Test
  def testSerializability(): Unit = {
    val schema = new ByteArraySchema
    val copy = CommonTestUtils.createCopySerializable(schema)
    assertEquals(schema.getProducedType, copy.getProducedType)
  }
}
