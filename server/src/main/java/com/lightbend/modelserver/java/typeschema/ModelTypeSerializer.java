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

import com.lightbend.model.DataConverter;
import com.lightbend.model.Model;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;

// Type serializer for model
public class ModelTypeSerializer extends TypeSerializer<Model> {

    @Override public Model createInstance() { return null; }

    @Override public boolean canEqual(Object obj) { return obj instanceof ModelTypeSerializer; }

    @Override public TypeSerializer<Model> duplicate() { return new ModelTypeSerializer(); }

    @Override public void serialize(Model model, DataOutputView target) throws IOException {

        if (model == null)
            target.writeBoolean(false);
        else{
            target.writeBoolean(true);
            byte[] content = model.getBytes();
            target.writeLong(model.getType());
            target.writeLong(content.length);
            target.write(content);
        }
    }

    @Override public boolean isImmutableType() { return false; }

    @Override public int getLength() { return -1; }

    @Override public TypeSerializerSnapshot<Model> snapshotConfiguration() { return new ModelSerializerConfigSnapshot(); }


    @Override public boolean equals(Object obj) { return obj instanceof ModelTypeSerializer; }

    @Override public int hashCode() { return 42; }

    @Override public Model copy(Model from) { return DataConverter.copy(from); }

    @Override public Model copy(Model from, Model reuse) { return DataConverter.copy(from); }

    @Override public void copy(DataInputView source, DataOutputView target) throws IOException {
        boolean exist = source.readBoolean();
        target.writeBoolean(exist);
        if(!exist) return;
        target.writeLong (source.readLong () );
        int clen = (int) source.readLong ();
        target.writeLong (clen);
        byte[] content = new byte[clen];
        source.read (content);
        target.write (content);
    }

    @Override public Model deserialize(DataInputView source) throws IOException {
        boolean exist = source.readBoolean();
        if(!exist) return null;

        int type = (int) source.readLong();
        int size = (int) source.readLong();
        byte[] content = new byte[size];
        source.read(content);
        return DataConverter.restore(type, content);
     }

    @Override public Model deserialize(Model reuse, DataInputView source) throws IOException { return deserialize(source); }
}
