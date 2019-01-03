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
import com.lightbend.model.ModelWithType;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;
import java.util.Optional;

// Serializer for model with type
public class ModelWithTypeSerializer extends TypeSerializer<ModelWithType> {

    @Override public ModelWithType createInstance() { return new ModelWithType(); }

    @Override public boolean canEqual(Object obj) { return obj instanceof ModelWithTypeSerializer; }

    @Override public TypeSerializer<ModelWithType> duplicate() { return new ModelWithTypeSerializer(); }

    @Override public void serialize(ModelWithType model, DataOutputView target) throws IOException {

        target.writeBoolean(model.isCurrent());
        target.writeUTF(model.getDataType());
        if (model.getModel().isPresent()) {
            target.writeBoolean(true);
            byte[] content = model.getModel().get().getBytes();
            target.writeLong(model.getModel().get().getType());
            target.writeLong(content.length);
            target.write(content);
        }
        else{
            target.writeBoolean(false);
        }
    }

    @Override public boolean isImmutableType() { return false; }

    @Override public int getLength() { return -1; }

    @Override public TypeSerializerSnapshot<ModelWithType> snapshotConfiguration() { return new ModelWithTypeSerializerConfigSnapshot(); }


    @Override public boolean equals(Object obj) { return obj instanceof ModelWithTypeSerializer; }

    @Override public int hashCode() { return 42; }

    @Override public ModelWithType copy(ModelWithType from) {
        Model model = DataConverter.copy(from.getModel().orElse(null));
        return new ModelWithType(from.isCurrent(), from.getDataType(),
                (model == null) ? Optional.empty() : Optional.of(model));
    }

    @Override public ModelWithType copy(ModelWithType from, ModelWithType reuse) { return copy(from); }

    @Override public void copy(DataInputView source, DataOutputView target) throws IOException {
        target.writeBoolean(source.readBoolean());
        target.writeUTF(source.readUTF());
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

    @Override public ModelWithType deserialize(DataInputView source) throws IOException {

        boolean current = source.readBoolean();
        String dataType = source.readUTF();
        boolean exist = source.readBoolean();
        Optional<Model> model = Optional.empty();
        if(exist) {
            int type = (int) source.readLong();
            int size = (int) source.readLong();
            byte[] content = new byte[size];
            source.read(content);
            Model m = DataConverter.restore(type, content);
            if(m != null)
                model = Optional.of(m);
        }
        return new ModelWithType(current, dataType, model);
    }

    @Override public ModelWithType deserialize(ModelWithType reuse, DataInputView source) throws IOException { return deserialize(source); }
}
