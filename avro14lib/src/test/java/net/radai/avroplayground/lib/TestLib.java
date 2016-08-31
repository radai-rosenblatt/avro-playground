package net.radai.avroplayground.lib;

import java.io.IOException;
import java.util.Collections;
import net.radai.avroplayground.a.RecordA;
import net.radai.avroplayground.a.Type;
import net.radai.avroplayground.b.RecordB;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;


public class TestLib {

    @Test
    public void testSerializationRoundTrip() throws IOException {
        RecordA a = new RecordA();
        a.bla = "bob";
        RecordB b = new RecordB();
        b.bla = "more bob";
        b.entityType = Type.A;
        a.items = Collections.singletonList(b);
        byte[] serializedBySpecific = Lib.serialize(a);
        GenericRecord deserializedGeneric = Lib.deserialize(serializedBySpecific, a.getSchema());
        Assert.assertNotNull(deserializedGeneric);
        try {
            Lib.deserialize(serializedBySpecific, RecordA.class);
            Assert.fail("should have exploded");
        } catch (ClassCastException e) {
            //this happens because avro 1.4 generates bad schema for A
            //causing the enum to be handled as generic and not specific
        }
    }

    @Test
    public void testSuccessfulRoundTrip() throws IOException {
        RecordB b = new RecordB();
        b.bla = "more bob";
        b.entityType = Type.A;
        byte[] serialized = Lib.serialize(b);
        GenericRecord deserializedGeneric = Lib.deserialize(serialized, b.getSchema());
        Assert.assertNotNull(deserializedGeneric);
        RecordB deserializedSpecific = Lib.deserialize(serialized, RecordB.class);
        Assert.assertEquals(b, deserializedSpecific);
    }
}
