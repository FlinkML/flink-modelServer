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

import java.io.Serializable;

/**
 * Created by boris on 6/28/17.
 */
public class ModelToServe implements Serializable {

    private String name;
    private String description;
    private Modeldescriptor.ModelDescriptor.ModelType modelType;
    private byte[] modelData;
    private String modelDataLocation;
    private String dataType;

    public ModelToServe(String name, String description, Modeldescriptor.ModelDescriptor.ModelType modelType,
                        byte[] dataContent, String modelDataLocation, String dataType){
        this.name = name;
        this.description = description;
        this.modelType = modelType;
        this.modelData = dataContent;
        this.modelDataLocation = modelDataLocation;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Modeldescriptor.ModelDescriptor.ModelType getModelType() {
        return modelType;
    }

    public String getDataType() {
        return dataType;
    }

    public byte[] getModelData() {
        return modelData;
    }

    public String getModelDataLocation() {
        return modelDataLocation;
    }

    @Override
    public String toString() {
        return "ModelToServe{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", modelType=" + modelType +
                ", dataType='" + dataType + '\'' +
                '}';
    }
}