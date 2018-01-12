package com.lightbend.model;

import java.util.Optional;

/**
 * Created by boris on 7/14/17.
 */
public interface ModelFactory {
    Optional<Model> create(ModelToServe descriptor);
    Model restore(byte[] bytes);
}
