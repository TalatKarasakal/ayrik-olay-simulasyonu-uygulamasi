package com.talatkarasakal.fork.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * The demo workflow and job files bundled in the jar. They are copied to a temporary folder on
 * first use so the rest of the application can treat them as ordinary files on disk.
 */
public final class SampleData {

    private static final String RESOURCE_PREFIX = "/samples/";

    private SampleData() {
    }

    /**
     * Materialises a bundled sample onto disk and returns it. Repeated calls overwrite the same
     * path, so the extracted copy always matches what is in the jar.
     *
     * @throws IOException if the resource is missing or cannot be written
     */
    public static java.io.File extract(String name) throws IOException {
        Path directory = Files.createTempDirectory("ayrik-olay-ornek").toAbsolutePath();
        directory.toFile().deleteOnExit();

        Path target = directory.resolve(name);
        try (InputStream in = SampleData.class.getResourceAsStream(RESOURCE_PREFIX + name)) {
            if (in == null) {
                throw new IOException("Örnek dosya jar içinde bulunamadı: " + name);
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        target.toFile().deleteOnExit();
        return target.toFile();
    }
}
