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

package com.lightbend.modelserver.java.keyed;

import com.lightbend.model.*;
import com.lightbend.modelserver.java.typeschema.ModelTypeSerializer;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Optional;

public class DataProcessorKeyed extends CoProcessFunction<DataToServe, ModelToServe, ServingResult>{

    // In Flink class instance is created not for key, but rater key groups
    // https://ci.apache.org/projects/flink/flink-docs-release-1.6/dev/stream/state/state.html#keyed-state-and-operator-state
    // As a result, any key specific sate data has to be in the key specific state

    // current model state
    ValueState<ModelToServeStats> modelState;
    // new model state
    ValueState<ModelToServeStats> newModelState;

    // Current model
    ValueState<Model> currentModel;
    // New model
    ValueState<Model> newModel;

    // Called when an instance is created
    @Override public void open(Configuration parameters){
        // Model state descriptor
        ValueStateDescriptor<ModelToServeStats> modeStatelDesc = new ValueStateDescriptor<>(
                "currentModelState",   // state name
                TypeInformation.of(new TypeHint<ModelToServeStats>() {})); // type information
        // Allow access from queryable client
        modeStatelDesc.setQueryable("currentModelState");
        // Create model state
        modelState = getRuntimeContext().getState(modeStatelDesc);

        // New model descriptor
        ValueStateDescriptor<ModelToServeStats> newModelStateDesc = new ValueStateDescriptor<>(
                "newModelState",         // state name
                TypeInformation.of(new TypeHint<ModelToServeStats>() {})); // type information
        // Created new Model state
        newModelState = getRuntimeContext().getState(newModelStateDesc);

        // Current model descriptor
        ValueStateDescriptor<Model> currentModelDesc = new ValueStateDescriptor<>(
                "currentModel",         // state name
                new ModelTypeSerializer()); // type information
        // Create current model
        currentModel = getRuntimeContext().getState(currentModelDesc);
        // New model descriptor
        ValueStateDescriptor<Model> newModelDesc = new ValueStateDescriptor<>(
                "newModel",         // state name
                new ModelTypeSerializer()); // type information
        // Create new model
        newModel = getRuntimeContext().getState(newModelDesc);
    }

    // Serve data
    @Override public void processElement1(DataToServe value, Context ctx, Collector<ServingResult> out) throws Exception {

        // See if we have update for the model
        if(newModel.value() != null){
            // Clean up current model
            if (currentModel.value() != null)
                currentModel.value().cleanup();
            // Update model
            currentModel.update(newModel.value());
            modelState.update(newModelState.value());
            newModel.update(null);
        }
        // Process data
        if (currentModel.value() != null){
            long start = System.currentTimeMillis();
            // Actually serve
            Object result = currentModel.value().score(value.getRecord());
            long duration = System.currentTimeMillis() - start;
            // Update state
            modelState.update(modelState.value().incrementUsage(duration));
            // Write result
            out.collect(new ServingResult(duration,result));
        }
     }

    // Update model
    @Override
    public void processElement2(ModelToServe model, Context ctx, Collector<ServingResult> out) throws Exception {

        System.out.println("New model - " + model);
        Optional<Model> m = DataConverter.toModel(model);                  // Create a new model
        if(m.isPresent()) {
            // Update new state
            newModelState.update(new ModelToServeStats(model));
            newModel.update(m.get());
        }
    }
}