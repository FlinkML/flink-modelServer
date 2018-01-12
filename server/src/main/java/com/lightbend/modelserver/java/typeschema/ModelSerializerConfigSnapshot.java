package com.lightbend.modelserver.java.typeschema;

import com.lightbend.model.Model;
import org.apache.flink.api.common.typeutils.GenericTypeSerializerConfigSnapshot;
import org.apache.flink.api.common.typeutils.TypeSerializerConfigSnapshot;

public class ModelSerializerConfigSnapshot extends TypeSerializerConfigSnapshot {

    private static final int VERSION = 1;
    private static final int HASHCODE = 42;

    @Override public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        return (obj.getClass() == getClass()) &&
                Model.class == ((GenericTypeSerializerConfigSnapshot<?>)obj).getTypeClass();
    }

    @Override public int hashCode() { return HASHCODE; }

    @Override public int getVersion() { return VERSION; }
}
