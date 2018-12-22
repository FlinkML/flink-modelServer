package com.lightbend.java.wineserving.model;

import com.lightbend.model.ModelFacroriesResolver;
import com.lightbend.model.ModelFactory;
import com.lightbend.model.Modeldescriptor;

import java.util.HashMap;
import java.util.Map;

// Factories resolver for Wine data
public class WineFactoryResolver implements ModelFacroriesResolver {

    private static final Map<Integer, ModelFactory> factories = new HashMap<Integer, ModelFactory>() {
        {
            put(Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOW.getNumber(), WineTensorflowModelFactory.getInstance());
            put(Modeldescriptor.ModelDescriptor.ModelType.PMML.getNumber(), WinePMMLModelFactory.getInstance());
        }
    };

    @Override
    public ModelFactory getFactory(int type) {
        return factories.get(type);
    }
}
