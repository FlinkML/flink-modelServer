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

package com.lightbend.model;

import java.util.Optional;

// Intermediate representation used for checkpointing
public class ModelWithType {

    // Is model current
    private boolean current;
    // Model data type
    private String dataType;
    // MOdel
    private Optional<Model> model;

    public ModelWithType(){
        current = false;
        dataType = "";
        this.model = Optional.empty();
    }

    public ModelWithType(boolean current, String dataType, Optional<Model> model){
        this.current = current;
        this.dataType = dataType;
        this.model = model;
    }

    public boolean isCurrent() { return current; }

    public void setCurrent(boolean current) { this.current = current; }

    public String getDataType() { return dataType; }

    public void setDataType(String dataType) { this.dataType = dataType; }

    public Optional<Model> getModel() { return model; }

    public void setModel(Optional<Model> model) { this.model = model; }

    @Override
    public String toString() {
        return "ModelWithType{" +
                "current=" + current +
                ", dataType='" + dataType + '\'' +
                ", model=" + model +
                '}';
    }
}
