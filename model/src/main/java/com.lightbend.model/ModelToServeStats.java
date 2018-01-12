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

import java.util.Objects;

public class ModelToServeStats {

    private String name;
    private String description;
    private Modeldescriptor.ModelDescriptor.ModelType modelType;
    private long since;
    private long invocations;
    private double duration;
    private long min;
    private long max;

    public ModelToServeStats(){}

    public ModelToServeStats(final String name, final String description, Modeldescriptor.ModelDescriptor.ModelType modelType) {
        this.name = name;
        this.description = description;
        this.modelType = modelType;
        this.since = 0;
        this.invocations = 0;
        this.duration = 0.;
        this.min = Long.MAX_VALUE;
        this.max = Long.MIN_VALUE;
    }

    public ModelToServeStats(final String name, final String description, Modeldescriptor.ModelDescriptor.ModelType modelType,
                             final long since, final long invocations, final double duration, final long min, final long max) {
        this.name = name;
        this.description = description;
        this.modelType = modelType;
        this.since = since;
        this.invocations = invocations;
        this.duration = duration;
        this.min = min;
        this.max = max;
    }

    public ModelToServeStats(ModelToServe model){
        this.name = model.getName();
        this.description = model.getDescription();
        this.modelType = model.getModelType();
        this.since = System.currentTimeMillis();
        this.invocations = 0;
        this.duration = 0.;
        this.min = Long.MAX_VALUE;
        this.max = Long.MIN_VALUE;
    }

    public ModelToServeStats incrementUsage(long execution){
        invocations++;
        duration += execution;
        if(execution < min) min = execution;
        if(execution > max) max = execution;
        return this;
    }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public long getSince() {return since;}

    public void setSince(long since) {this.since = since;}

    public long getInvocations() {return invocations;}

    public void setInvocations(long invocations) {this.invocations = invocations;}

    public double getDuration() {return duration;}

    public void setDuration(double duration) {this.duration = duration;}

    public long getMin() {return min;}

    public void setMin(long min) {this.min = min;}

    public long getMax() {return max;}

    public void setMax(long max) {this.max = max;}

    @Override
    public String toString() {
        return "ModelServingInfo{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", since=" + since +
                ", invocations=" + invocations +
                ", duration=" + duration +
                ", min=" + min +
                ", max=" + max +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ModelToServeStats that = (ModelToServeStats) o;
        return name.equals(that.name) &&
               description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
}