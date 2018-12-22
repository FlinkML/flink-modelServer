package com.lightbend.model;

// Base interface for the data to serve
public interface DataToServe {
    String getType();
    Object getRecord();
}
