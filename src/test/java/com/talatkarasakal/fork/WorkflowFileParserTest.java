package com.talatkarasakal.fork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowFileParserTest {

    private WorkflowFileParser parser;
    private File workflowFile;

    @BeforeEach
    void setUp() throws Exception {
        parser = new WorkflowFileParser();
        URL url = getClass().getClassLoader().getResource("sample_workflow.txt");
        assertNotNull(url, "sample_workflow.txt must be on the classpath");
        workflowFile = new File(url.toURI());
        parser.parse(workflowFile);
    }

    // --- TASKTYPES happy paths ---

    @Test
    void taskTypeIdsAreParsed() {
        Map<String, TaskType> tt = parser.getTaskTypes();
        assertTrue(tt.containsKey("T1"));
        assertTrue(tt.containsKey("T2"));
    }

    @Test
    void taskTypeSizesAreCorrect() {
        Map<String, TaskType> tt = parser.getTaskTypes();
        assertEquals(2.0, tt.get("T1").getDefaultSize(), 1e-9);
        assertEquals(3.0, tt.get("T2").getDefaultSize(), 1e-9);
    }

    // --- JOBTYPES happy paths ---

    @Test
    void jobTypeIdsAreParsed() {
        Map<String, JobType> jt = parser.getJobTypes();
        assertTrue(jt.containsKey("JT1"));
        assertTrue(jt.containsKey("JT2"));
    }

    @Test
    void jobType_JT1_hasTwoTasksInOrder() {
        JobType jt1 = parser.getJobTypes().get("JT1");
        assertEquals(2, jt1.getTasks().size());
        assertEquals("T1", jt1.getTasks().get(0).getTaskTypeID());
        assertEquals("T2", jt1.getTasks().get(1).getTaskTypeID());
    }

    @Test
    void jobType_JT2_hasSingleTask() {
        JobType jt2 = parser.getJobTypes().get("JT2");
        assertEquals(1, jt2.getTasks().size());
        assertEquals("T2", jt2.getTasks().get(0).getTaskTypeID());
    }

    // --- STATIONS happy paths ---

    @Test
    void twoStationsAreParsed() {
        assertEquals(2, parser.getStations().size());
    }

    @Test
    void stationS1_propertiesAreCorrect() {
        Station s1 = parser.getStations().get("S1");
        assertNotNull(s1);
        assertEquals(2, s1.getMaxCapacity());
        assertTrue(s1.isMultiFlag());
        assertTrue(s1.isFifoFlag());
    }

    @Test
    void stationS2_propertiesAreCorrect() {
        Station s2 = parser.getStations().get("S2");
        assertNotNull(s2);
        assertEquals(1, s2.getMaxCapacity());
        assertFalse(s2.isMultiFlag());
        assertTrue(s2.isFifoFlag());
    }

    @Test
    void stationS1_taskSpeedsAreSet() {
        Station s1 = parser.getStations().get("S1");
        assertEquals(1.0, s1.getSpeedForTask("T1"), 1e-9);
        assertEquals(1.0, s1.getSpeedForTask("T2"), 1e-9);
    }

    @Test
    void stationS2_handlesOnlyT2() {
        Station s2 = parser.getStations().get("S2");
        assertTrue(s2.canHandleTaskType("T2"));
        assertFalse(s2.canHandleTaskType("T1"));
    }

    // --- Edge cases ---

    @Test
    void nonWorkflowFormatFile_returnsEmptyStations() throws Exception {
        WorkflowFileParser freshParser = new WorkflowFileParser();
        URL url = getClass().getClassLoader().getResource("bad_workflow.txt");
        assertNotNull(url);
        Map<String, Station> stations = freshParser.parse(new File(url.toURI()));
        assertTrue(stations.isEmpty());
    }

    @Test
    void noErrorMessages_afterValidParse() {
        assertTrue(parser.getErrorMessages().isEmpty());
    }

    // --- Error case ---

    @Test
    void nonExistentFile_throwsFileNotFoundException() {
        WorkflowFileParser freshParser = new WorkflowFileParser();
        assertThrows(FileNotFoundException.class,
                () -> freshParser.parse(new File("no_such_file_xyz.txt")));
    }
}
