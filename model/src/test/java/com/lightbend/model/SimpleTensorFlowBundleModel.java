package com.lightbend.model;

import com.lightbend.model.tensorflow.TensorFlowBundleModel;
import org.tensorflow.Graph;
import org.tensorflow.Session;

import java.util.List;
import java.util.Map;

// Tensorflow bundle implementation for testing
public class SimpleTensorFlowBundleModel extends TensorFlowBundleModel {

    public SimpleTensorFlowBundleModel(byte[] inputStream)  {
        super(inputStream);
    }

    @Override
    public Object score(Object input) {
        // Just for test
        return null;
    }

    // Getters for testing

    public Graph getGraph() {
        return graph;
    }

    public Session getSession() {
        return session;
    }

    public Map<String, Signature> getSignatures() {
        return signatures;
    }

    public List<String> getTags() {
        return tags;
    }
}
