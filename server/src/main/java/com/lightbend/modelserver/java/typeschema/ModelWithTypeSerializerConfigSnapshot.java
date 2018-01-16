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

import com.lightbend.model.ModelWithType;
import org.apache.flink.api.common.typeutils.GenericTypeSerializerConfigSnapshot;
import org.apache.flink.api.common.typeutils.TypeSerializerConfigSnapshot;

public class ModelWithTypeSerializerConfigSnapshot extends TypeSerializerConfigSnapshot {

    private static final int VERSION = 1;
    private static final int HASHCODE = 42;

    @Override public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        return (obj.getClass() == getClass()) &&
                ModelWithType.class == ((GenericTypeSerializerConfigSnapshot<?>)obj).getTypeClass();
    }

    @Override public int hashCode() { return HASHCODE; }

    @Override public int getVersion() { return VERSION; }
}
