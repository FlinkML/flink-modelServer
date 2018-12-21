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

package com.lightbend.modelserver.java.typeschema;

import com.lightbend.model.Model;
import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSchemaCompatibility;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.util.InstantiationUtil;

import java.io.IOException;

public class ModelSerializerConfigSnapshot extends SimpleTypeSerializerSnapshot<Model> {

    private static final int VERSION = 2;

     private Class<ModelTypeSerializer> serializerClass = ModelTypeSerializer.class;


    @Override
    public int getCurrentVersion() {
        return VERSION;
    }

    @Override
    public void writeSnapshot(DataOutputView out) throws IOException {
        out.writeUTF(serializerClass.getName());
    }

    @Override
    public void readSnapshot(int readVersion, DataInputView in, ClassLoader classLoader) throws IOException {
        switch (readVersion) {
            case 2:
                read(in, classLoader);
                break;
            default:
                throw new IOException("Unrecognized version: " + readVersion);
        }
    }

    @Override
    public TypeSerializer<Model> restoreSerializer() {
        return InstantiationUtil.instantiate(serializerClass);
    }

    @Override
    public TypeSerializerSchemaCompatibility<Model> resolveSchemaCompatibility(TypeSerializer<Model> newSerializer) {
        return newSerializer.getClass() == serializerClass ?
                TypeSerializerSchemaCompatibility.compatibleAsIs() :
                TypeSerializerSchemaCompatibility.incompatible();
    }

    private void read(DataInputView in, ClassLoader classLoader) throws IOException {
        final String className = in.readUTF();
        this.serializerClass = cast(resolveClassName(className, classLoader, false));
    }

    private static Class<?> resolveClassName(String className, ClassLoader cl, boolean allowCanonicalName) throws IOException {
        try {
            return Class.forName(className, false, cl);
        }
        catch (Throwable e) {
            throw new IOException(
                    "Failed to read SimpleTypeSerializerSnapshot: Serializer class not found: " + className, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static  Class<ModelTypeSerializer> cast(Class<?> clazz) throws IOException {
        if (!ModelTypeSerializer.class.isAssignableFrom(clazz)) {
            throw new IOException("Failed to read SimpleTypeSerializerSnapshot. " +
                    "Serializer class name leads to a class that is not a TypeSerializer: " + clazz.getName());
        }

        return (Class<ModelTypeSerializer>) clazz;
    }
}
