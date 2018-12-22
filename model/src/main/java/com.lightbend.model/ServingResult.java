package com.lightbend.model;

// Model serving definition. Can be extended in the future
public class ServingResult {

    // Serving duration
    private long duration;
    // Serving result
    private Object result;

    public ServingResult(long duration, Object result){
        this.duration = duration;
        this.result = result;
    }

    public long getDuration() {
        return duration;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "Model serving in " + duration + "ms with result " + result;
    }
}
