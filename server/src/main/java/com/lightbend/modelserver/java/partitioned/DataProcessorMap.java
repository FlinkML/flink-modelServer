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

package com.lightbend.modelserver.java.partitioned;

import com.lightbend.model.*;
import com.lightbend.modelserver.java.typeschema.ModelWithTypeSerializer;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class DataProcessorMap extends RichCoFlatMapFunction<Winerecord.WineRecord, ModelToServe, Double> implements CheckpointedFunction {

    Map<String, Model> currentModels = new HashMap<>();
    Map<String, Model> newModels = new HashMap<>();

    private transient ListState<ModelWithType> checkpointedState = null;

    @Override public void snapshotState(FunctionSnapshotContext context) throws Exception {
        checkpointedState.clear();
        for(Map.Entry<String, Model> entry : currentModels.entrySet())
            checkpointedState.add(new ModelWithType(true, entry.getKey(), Optional.of(entry.getValue())));
        for(Map.Entry<String, Model> entry : newModels.entrySet())
            checkpointedState.add(new ModelWithType(false, entry.getKey(), Optional.of(entry.getValue())));
    }

    @Override public void initializeState(FunctionInitializationContext context) throws Exception {

        ListStateDescriptor<ModelWithType> descriptor = new ListStateDescriptor<> (
                "modelState",
                new ModelWithTypeSerializer());

        checkpointedState = context.getOperatorStateStore().getListState (descriptor);

        if (context.isRestored()) {
            Iterator<ModelWithType> iterator = checkpointedState.get().iterator();
            while(iterator.hasNext()){
                ModelWithType current = iterator.next();
                if(current.getModel().isPresent()){
                    if(current.isCurrent())
                        currentModels.put(current.getDataType(), current.getModel().get());
                    else
                        newModels.put(current.getDataType(), current.getModel().get());
                }
            }
        }
    }

    @Override public void flatMap1(Winerecord.WineRecord record, Collector<Double> out) throws Exception {

        // See if we need to update
        if(newModels.containsKey(record.getDataType())){
            if(currentModels.containsKey(record.getDataType()))
                currentModels.get(record.getDataType()).cleanup();
            currentModels.put(record.getDataType(), newModels.get(record.getDataType()));
            newModels.remove(record.getDataType());
        }
        if(currentModels.containsKey(record.getDataType())){
            long start = System.currentTimeMillis();
            double quality = (double) currentModels.get(record.getDataType()).score(record);
            long duration = System.currentTimeMillis() - start;
            System.out.println("Calculated quality - " + quality + " calculated in " + duration + " ms");
        }
        else
            System.out.println("No model available - skipping");
    }

    @Override public void flatMap2(ModelToServe model, Collector<Double> out) throws Exception {
        System.out.println("New model - " + model);
        Optional<Model> m = DataConverter.toModel(model);
        if(m.isPresent())
            newModels.put(model.getDataType(), m.get());
    }
}
