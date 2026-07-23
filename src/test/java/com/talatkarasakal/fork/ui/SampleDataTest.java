package com.talatkarasakal.fork.ui;

import com.talatkarasakal.fork.SimulationResult;
import com.talatkarasakal.fork.SimulationRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class SampleDataTest {

    @Test
    void extract_writesTheBundledWorkflowToDisk() throws Exception {
        File file = SampleData.extract("sample_workflow.txt");

        assertTrue(file.isFile());
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("(TASKTYPES"));
        assertTrue(content.contains("(STATIONS"));
    }

    @Test
    void extract_writesTheBundledJobFileToDisk() throws Exception {
        File file = SampleData.extract("sample_job.txt");

        assertTrue(file.isFile());
        assertTrue(Files.readString(file.toPath(), StandardCharsets.UTF_8).startsWith("J1 JT1"));
    }

    @Test
    void extract_missingResource_throwsWithTheFileName() {
        IOException error = assertThrows(IOException.class,
                () -> SampleData.extract("not_a_bundled_sample.txt"));
        assertTrue(error.getMessage().contains("not_a_bundled_sample.txt"));
    }

    @Test
    void bundledSamples_runEndToEnd() throws Exception {
        // The "load sample data" button is only useful if the bundled files actually simulate.
        SimulationResult result = SimulationRunner.run(
                SampleData.extract("sample_workflow.txt"),
                SampleData.extract("sample_job.txt"));

        assertEquals(8, result.jobs().size());
        assertEquals(3, result.stations().size());
        assertEquals(result.jobs().size(), result.completedJobCount());
        assertTrue(result.warnings().isEmpty(), "the bundled samples must parse cleanly");
    }
}
