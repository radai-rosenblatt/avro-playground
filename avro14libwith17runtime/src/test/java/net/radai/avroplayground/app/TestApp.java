package net.radai.avroplayground.app;

import net.radai.avroplayground.a.Type;
import net.radai.avroplayground.b.RecordB;
import net.radai.avroplayground.lib.Lib;
import org.junit.Assert;
import org.junit.Test;


public class TestApp {

    @Test
    public void testLibUsage() throws Exception {
        RecordB b = new RecordB();
        b.bla = "such wow";
        b.entityType = Type.B;
        try {
            byte[] blob = Lib.serialize(b); //unambiguous with 1.4
            Assert.fail("should have exploded");
        } catch (InstantiationError e) {
            //this is because lib 1.4 uses new BinaryEncoder(), and the class was made abstract (need to use EncoderFactory)
        }
    }
}
