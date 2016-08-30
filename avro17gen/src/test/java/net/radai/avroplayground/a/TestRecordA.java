package net.radai.avroplayground.a;

import net.radai.avroplayground.b.RecordB;
import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;


public class TestRecordA {

    @Test
    public void testSchemaGenerationBug() throws Exception {
        Schema schema = RecordA.SCHEMA$;
        String correctPackage = Type.class.getPackage().getName();
        String schemaPackageOnGrandparent = schema.getField("items").schema().getElementType().getField("entityType").schema().getNamespace();
        String schemaPackageOnParent = RecordB.SCHEMA$.getField("entityType").schema().getNamespace();

        Assert.assertEquals(correctPackage, schemaPackageOnGrandparent); //this works on avro 1.7
        Assert.assertEquals(correctPackage, schemaPackageOnParent);
    }
}
