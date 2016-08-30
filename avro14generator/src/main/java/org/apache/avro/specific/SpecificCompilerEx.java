package org.apache.avro.specific;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.avro.Schema;


public class SpecificCompilerEx extends SpecificCompiler {
    private static final Method ENQUEUE;

    static {
        try {
            ENQUEUE = SpecificCompiler.class.getDeclaredMethod("enqueue", Schema.class);
            ENQUEUE.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("unable to access SpecificCompiler.enqueue()", e);
        }
    }

    public SpecificCompilerEx(Collection<Schema> schemas) {
        this(toArray(schemas));
    }

    public SpecificCompilerEx(Schema... schemas) {
        super(schemas[0]);
        try {
            for (int i = 1; i < schemas.length; i++) {
                ENQUEUE.invoke(this, schemas[i]);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Schema[] toArray(Collection<Schema> schemas) {
        Schema[] arr = new Schema[schemas.size()];
        int i=0;
        for (Schema s : schemas) {
            arr[i++] = s;
        }
        return arr;
    }

    public void compile(Path outputRoot) throws IOException {
        compile(outputRoot.toFile());
    }

    public void compile(File outputRoot) throws IOException {
        Collection<SpecificCompiler.OutputFile> outFiles = compile();
        for (SpecificCompiler.OutputFile file : outFiles) {
            File written = file.writeToDestination(new File(file.path.replaceAll("[/\\\\]", File.separator)), outputRoot);
            System.out.println("wrote " + written.getCanonicalPath());
        }
    }

    public static void compile(Collection<Schema> schemas, Path destRoot) {
        try {
            List<Schema> asList = new ArrayList<>(schemas);
            SpecificCompiler compiler = new SpecificCompiler(asList.get(0));
            for (Schema schema : asList.subList(1, schemas.size())) {
                ENQUEUE.invoke(compiler, schema);
            }
            Collection<SpecificCompiler.OutputFile> outFiles = compiler.compile();
            File destDir = destRoot.toFile();
            for (SpecificCompiler.OutputFile file : outFiles) {
                File written = file.writeToDestination(new File(file.path.replaceAll("[/\\\\]", File.separator)), destDir);
                System.out.println("wrote " + written.getCanonicalPath());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
