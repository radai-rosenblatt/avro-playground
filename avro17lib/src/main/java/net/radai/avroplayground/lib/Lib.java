package net.radai.avroplayground.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;


public class Lib {

    public static byte[] serialize(GenericRecord generic) throws IOException {
        return serialize(new GenericDatumWriter(generic.getSchema()), generic);
    }

    public static byte[] serialize(SpecificRecord specific) throws IOException {
        return serialize(new SpecificDatumWriter(specific.getClass()), specific);
    }

    private static byte[] serialize(DatumWriter writer, Object record) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            writer.write(record, EncoderFactory.get().directBinaryEncoder(os, null));
            return os.toByteArray();
        }
    }

    public static GenericRecord deserialize(byte[] blob, Schema writtenAs) throws IOException {
        GenericDatumReader reader = new GenericDatumReader(writtenAs);
        return (GenericRecord) reader.read(null, DecoderFactory.get().binaryDecoder(blob, null));
    }

    public static <T extends SpecificRecord> T deserialize(byte[] blob, Class<T> as) throws IOException {
        SpecificDatumReader<T> reader = new SpecificDatumReader<>(as);
        return reader.read(null, DecoderFactory.get().binaryDecoder(blob, null));
    }
}
