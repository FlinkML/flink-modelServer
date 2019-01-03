package com.lightbend.model;

import java.util.HashMap;
import java.util.Map;

// Factories resolver for Wine data
public class SimpleFactoryResolver implements ModelFacroriesResolver {

    private static final Map<Integer, ModelFactory> factories = new HashMap<Integer, ModelFactory>() {
        {
            put(Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOW.getNumber(), SimpleTensorflowModelFactory.getInstance());
            put(Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOWSAVED.getNumber(), SimpleTensorflowBundleModelFactory.getInstance());
            put(Modeldescriptor.ModelDescriptor.ModelType.PMML.getNumber(), SimplePMMLModelFactory.getInstance());
        }
    };

    @Override
    public ModelFactory getFactory(int type) {
        return factories.get(type);
    }
}
