package com.lightbend.model;

// Base interface for ModelFactories resolver
public interface ModelFacroriesResolver {

    ModelFactory getFactory(int type);
}