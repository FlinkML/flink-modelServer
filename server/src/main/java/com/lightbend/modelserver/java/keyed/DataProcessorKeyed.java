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
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Iterator;

public class DataProcessorKeyed extends CoProcessFunction<Winerecord.WineRecord, ModelToServe, Double> implements CheckpointedFunction{

    ValueState<ModelToServeStats> modelState;
    ValueState<ModelToServeStats> newModelState;

    Model currentModel = null;
    Model newModel = null;

    private transient ListState<Model> checkpointedState = null;

    @Override public void open(Configuration parameters){
        ValueStateDescriptor<ModelToServeStats> modelDesc = new ValueStateDescriptor<>(
                "currentModel",   // state name
                TypeInformation.of(new TypeHint<ModelToServeStats>() {})); // type information
        modelDesc.setQueryable("currentModel");
        modelState = getRuntimeContext().getState(modelDesc);

        ValueStateDescriptor<ModelToServeStats> newModelDesc = new ValueStateDescriptor<>(
                "newModel",         // state name
                TypeInformation.of(new TypeHint<ModelToServeStats>() {})); // type information
        newModelState = getRuntimeContext().getState(newModelDesc);
    }


    @Override public void snapshotState(FunctionSnapshotContext context) throws Exception {
        checkpointedState.clear();
        checkpointedState.add(currentModel);
        checkpointedState.add(newModel);
    }

    @Override public void initializeState(FunctionInitializationContext context) throws Exception {

        ListStateDescriptor<Model> descriptor = new ListStateDescriptor<> (
                "modelState",
                new ModelTypeSerializer());

        checkpointedState = context.getOperatorStateStore().getListState (descriptor);

        if (context.isRestored()) {
            Iterator<Model> iterator = checkpointedState.get().iterator();
            currentModel = iterator.next();
            newModel = iterator.next();
        }
    }

    @Override public void processElement1(Winerecord.WineRecord value, Context ctx, Collector<Double> out) throws Exception {

        // See if we have update for the model
        if(newModel != null){
            // Clean up current model
            if (currentModel != null)
                currentModel.cleanup();
            // Update model
            currentModel = newModel;
            modelState.update(newModelState.value());
            newModel = null;
        }
        // Process data
        if (currentModel != null){
            long start = System.currentTimeMillis();
            double quality = (double)currentModel.score(value);
            long duration = System.currentTimeMillis() - start;
            modelState.update(modelState.value().incrementUsage(duration));
            System.out.println("Calculated quality - " + quality + " calculated in " + duration + " ms");
            out.collect(quality);
        }
        else
            System.out.println("No model available - skipping");
     }

    @Override
    public void processElement2(ModelToServe model, Context ctx, Collector<Double> out) throws Exception {
        System.out.println("New model - " + model);
        newModelState.update(new ModelToServeStats(model));
        newModel = DataConverter.toModel(model).orElse(null);
    }
}