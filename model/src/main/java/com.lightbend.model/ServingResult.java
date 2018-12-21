package com.lightbend.model;

public class ServingResult {

    private long duration;
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
