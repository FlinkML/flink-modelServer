package com.lightbend.java.wineserving.model;

import com.lightbend.model.ModelFacroriesResolverInterface;
import com.lightbend.model.ModelFactory;
import com.lightbend.model.Modeldescriptor;

import java.util.HashMap;
import java.util.Map;

public class ModelFactoryResolver implements ModelFacroriesResolverInterface {

    private static final Map<Integer, ModelFactory> factories = new HashMap<Integer, ModelFactory>() {
        {
            put(Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOW.getNumber(), SpecificTensorflowModelFactory.getInstance());
            put(Modeldescriptor.ModelDescriptor.ModelType.PMML.getNumber(), SpecificPMMLModelFactory.getInstance());
        }
    };

    @Override
    public ModelFactory getFactory(int type) {
        return factories.get(type);
    }
}
