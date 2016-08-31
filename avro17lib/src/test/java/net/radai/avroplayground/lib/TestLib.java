package net.radai.avroplayground.lib;

import java.io.IOException;
import java.util.Collections;
import net.radai.avroplayground.a.RecordA;
import net.radai.avroplayground.a.Type;
import net.radai.avroplayground.b.RecordB;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.junit.Assert;
import org.junit.Test;


public class TestLib {

    @Test
    public void testSerializationRoundTrip() throws IOException {
        RecordA a = new RecordA();
        a.setBla("bob");
        RecordB b = new RecordB();
        b.setBla("more bob");
        b.setEntityType(Type.A);
        a.setItems(Collections.singletonList(b));
        //specific classes also implement GenericRecord in avro 1.7
        byte[] serializedAsSpecific = Lib.serialize((SpecificRecord) a);
        byte[] serializedAsGeneric = Lib.serialize((GenericRecord) a);
        Assert.assertArrayEquals(serializedAsSpecific, serializedAsGeneric);
        GenericRecord deserializedGeneric = Lib.deserialize(serializedAsSpecific, a.getSchema());
        Assert.assertNotNull(deserializedGeneric);
        RecordA deserializedSpecific = Lib.deserialize(serializedAsSpecific, RecordA.class);
        Assert.assertEquals(a, deserializedSpecific);
    }
}
