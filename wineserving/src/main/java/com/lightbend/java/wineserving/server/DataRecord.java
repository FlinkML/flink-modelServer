package com.lightbend.java.wineserving.server;

import com.lightbend.model.DataToServe;
import com.lightbend.model.Winerecord;

import java.util.Optional;

public class DataRecord implements DataToServe {

    private Winerecord.WineRecord record;

    public DataRecord(Winerecord.WineRecord record){
        this.record = record;
    }

    @Override
    public String getType() {
        return record.getDataType();
    }

    @Override
    public Object getRecord() {
        return record;
    }

    public static Optional<DataRecord> convertData(byte[] binary){
        try {
            // Unmarshall record
            return Optional.of(new DataRecord(Winerecord.WineRecord.parseFrom(binary)));
        } catch (Throwable t) {
            // Oops
            System.out.println("Exception parsing input record" + new String(binary));
            t.printStackTrace();
            return Optional.empty();
        }
    }
}