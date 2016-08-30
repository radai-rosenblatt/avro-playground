package org.apache.avro;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.avro.specific.SpecificCompiler;
import org.apache.avro.specific.SpecificCompilerEx;


public class TreeCompiler {
    private static final Method ENQUEUE;

    static {
        try {
            ENQUEUE = SpecificCompiler.class.getDeclaredMethod("enqueue", Schema.class);
            ENQUEUE.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("unable to access SpecificCompiler.enqueue()", e);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("expecting 2 arguments: [source path] [dest path]");
        }
        Path source = Paths.get(args[0]);
        if (Files.notExists(source)) {
            throw new IllegalArgumentException("source path " + source + " does not exist");
        }
        Path dest = Paths.get(args[1]);
        if (Files.notExists(dest)) {
            if (!dest.toFile().mkdirs()) {
                throw new IllegalStateException("unable to create output path " + dest);
            }
            System.out.println("created output path " + dest);
        }
        final Set<Path> remaining = new HashSet<>();
        Files.walkFileTree(
            source,
            new HashSet<>(Collections.singletonList(FileVisitOption.FOLLOW_LINKS)),
            Integer.MAX_VALUE,
            new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    remaining.add(file.normalize());
                    return FileVisitResult.CONTINUE;
                }
            });
        System.out.println("found " + remaining.size() + " files to compile in " + source);

        //parse files into Schema objects

        Schema.Names names = new Schema.Names();
        while (!remaining.isEmpty()) {
            Throwable firstThisIteration = null;
            Set<Path> parsedThisIteration = new HashSet<>();
            for (Path r : remaining) {
                try {
                    //copy names, because a failed compilation will pollute it
                    Schema.Names namesCopy = new Schema.Names();
                    namesCopy.putAll(names);

                    Schema schema = Schema.parse(Schema.MAPPER.readTree(Schema.FACTORY.createJsonParser(r.toFile())), namesCopy);
                    System.out.println("successfully compiled " + r);
                    parsedThisIteration.add(r);
                    names.add(schema); //now that we know it was successfully compiled
                } catch (Throwable t) {
                    if (firstThisIteration == null) {
                        firstThisIteration = t;
                    }
                }
            }
            if (parsedThisIteration.isEmpty()) {
                if (firstThisIteration != null) {
                    throw new IllegalStateException("cannot make progress", firstThisIteration);
                } else {
                    throw new IllegalStateException("cannot make progress for unknown reasons");
                }
            }
            remaining.removeAll(parsedThisIteration);
        }

        //generate java files from Schema objects

        SpecificCompilerEx compiler = new SpecificCompilerEx(names.values());
        compiler.compile(dest);
    }
}
