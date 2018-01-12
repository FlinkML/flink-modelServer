package com.lightbend.modelserver.java.partitioned;

import com.lightbend.model.DataConverter;
import com.lightbend.model.Model;
import com.lightbend.model.ModelToServe;
import com.lightbend.model.Winerecord;
import com.lightbend.modelserver.java.typeschema.ModelTypeSerializer;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Iterator;

public class DataProcessorMap extends RichCoFlatMapFunction<Winerecord.WineRecord, ModelToServe, Double> implements CheckpointedFunction {

    Model currentModel = null;
    Model newModel = null;

    private transient ListState<Model> checkpointedState = null;

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

    @Override public void flatMap1(Winerecord.WineRecord value, Collector<Double> out) throws Exception {

        // See if we have update for the model
        if(newModel != null){
            // Clean up current model
            if (currentModel != null)
                currentModel.cleanup();
            // Update model
            currentModel = newModel;
            newModel = null;
        }
        // Process data
        if (currentModel != null){
            long start = System.currentTimeMillis();
            double quality = (double)currentModel.score(value);
            long duration = System.currentTimeMillis() - start;
            System.out.println("Calculated quality - " + quality + " calculated in " + duration + " ms");
        }
        else
            System.out.println("No model available - skipping");
    }

    @Override public void flatMap2(ModelToServe model, Collector<Double> out) throws Exception {
        System.out.println("New model - " + model);
        newModel = DataConverter.toModel(model).orElse(null);
    }
}
