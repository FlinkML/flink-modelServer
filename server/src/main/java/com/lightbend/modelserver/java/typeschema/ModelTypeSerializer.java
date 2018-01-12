package com.lightbend.modelserver.java.typeschema;

import com.lightbend.model.DataConverter;
import com.lightbend.model.Model;
import org.apache.flink.api.common.typeutils.CompatibilityResult;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerConfigSnapshot;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;

public class ModelTypeSerializer extends TypeSerializer<Model> {

    @Override public Model createInstance() { return null; }

    @Override public boolean canEqual(Object obj) { return obj instanceof ModelTypeSerializer; }

    @Override public TypeSerializer<Model> duplicate() { return new ModelTypeSerializer(); }

    @Override public CompatibilityResult<Model> ensureCompatibility(TypeSerializerConfigSnapshot configSnapshot) {
        if(configSnapshot instanceof ModelSerializerConfigSnapshot) return CompatibilityResult.compatible();
        return CompatibilityResult.requiresMigration();
    }

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

    @Override public TypeSerializerConfigSnapshot snapshotConfiguration() { return new ModelSerializerConfigSnapshot(); }


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
