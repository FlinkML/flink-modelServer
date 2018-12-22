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

package com.lightbend.model.tensorflow;

/**
 * Created by boris on 5/26/17.
 */

import com.lightbend.model.Model;
import com.lightbend.model.Modeldescriptor;
import org.tensorflow.Graph;
import org.tensorflow.Session;

// Base Tensorflow processing
public abstract class TensorflowModel implements Model {
    // Tensorflow graph
    protected Graph graph = new Graph();
    // Tensorflow session
    protected Session session;

    // Constructor
    public TensorflowModel(byte[] inputStream) {
        graph.importGraphDef(inputStream);
        session = new Session(graph);
    }

    @Override
    public void cleanup() {
        session.close();
        graph.close();
    }

    @Override
    public byte[] getBytes() {
        return graph.toGraphDef();
    }

    public Graph getGraph() {
        return graph;
    }

    @Override
    public long getType() {
        return (long) Modeldescriptor.ModelDescriptor.ModelType.TENSORFLOW.getNumber();
    }
}