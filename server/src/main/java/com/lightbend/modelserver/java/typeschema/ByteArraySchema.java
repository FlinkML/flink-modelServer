package com.lightbend.modelserver.java.typeschema;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.PrimitiveArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class ByteArraySchema implements DeserializationSchema<byte[]>, SerializationSchema<byte[]> {

    private long serialVersionUID = 1234567L;

    @Override
    public byte[] deserialize(byte[] message) throws IOException {
        return message;
    }

    @Override
    public boolean isEndOfStream(byte[] nextElement) {
        return false;
    }

    @Override
    public byte[] serialize(byte[] element) {
        return element;
    }

    @Override
    public TypeInformation<byte[]> getProducedType() { return PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO; }
}