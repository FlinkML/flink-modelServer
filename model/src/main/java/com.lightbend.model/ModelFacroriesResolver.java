package com.lightbend.model;

// Base interface for ModelFactories resolver. The implementation of this trait should return model factory
// base on a model type. Currently the following types are defined:
//        TENSORFLOW  = 0;
//        TENSORFLOWSAVED  = 1;
//        PMML = 2;
// Additional types can be defined as required

public interface ModelFacroriesResolver {

    ModelFactory getFactory(int type);
}