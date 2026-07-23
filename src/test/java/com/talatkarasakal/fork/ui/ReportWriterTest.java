package com.talatkarasakal.fork.ui;

import com.talatkarasakal.fork.JobResult;
import com.talatkarasakal.fork.SimulationResult;
import com.talatkarasakal.fork.SimulationRunner;
import com.talatkarasakal.fork.StationResult;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportWriterTest {

    private SimulationResult sample() {
        return new SimulationResult(
                List.of(new JobResult("J1", "JT1", 0, 5, 170, -130, true),
                        new JobResult("J2", "JT2", 20, 2, 200, 60, true),
                        new JobResult("J3", "JT1", 30, 1, 0, 0, false)),
                List.of(new StationResult("S1", 2, true, true, List.of("T1", "T2"), 660, 157.1),
                        new StationResult("S2", 1, false, false, List.of("T2"), 180, 42.9)),
                Map.of("JT2", 60.0),
                420,
                List.of("Line 3: Undefined task type TX in job type JT1"),
                "log output",
                22L);
    }

    @Test
    void report_containsEverySection() {
        String report = ReportWriter.toText(sample());

        assertTrue(report.contains("AYRIK OLAY SİMÜLASYONU — SONUÇ RAPORU"));
        assertTrue(report.contains("ÖZET"));
        assertTrue(report.contains("İŞLER"));
        assertTrue(report.contains("İSTASYONLAR"));
        assertTrue(report.contains("İŞ TİPİNE GÖRE ORTALAMA GECİKME"));
        assertTrue(report.contains("AYRIŞTIRMA UYARILARI"));
    }

    @Test
    void report_summarisesTheCounts() {
        String report = ReportWriter.toText(sample());

        assertTrue(report.contains("Toplam iş:"));
        assertTrue(report.contains("Tamamlanan iş:"));
        assertTrue(report.contains("Geciken iş:"));
        assertTrue(report.contains("En yoğun istasyon:"));
    }

    @Test
    void report_listsEveryJobAndStation() {
        String report = ReportWriter.toText(sample());

        assertTrue(report.contains("J1"));
        assertTrue(report.contains("J2"));
        assertTrue(report.contains("J3"));
        assertTrue(report.contains("S1"));
        assertTrue(report.contains("S2"));
    }

    @Test
    void report_marksJobStatuses() {
        String report = ReportWriter.toText(sample());

        assertTrue(report.contains("Zamanında"));
        assertTrue(report.contains("Geç"));
        assertTrue(report.contains("Tamamlanmadı"));
    }

    @Test
    void report_includesTheWarningText() {
        assertTrue(ReportWriter.toText(sample()).contains("Undefined task type TX"));
    }

    @Test
    void report_omitsEmptyOptionalSections() {
        SimulationResult clean = new SimulationResult(List.of(), List.of(), Map.of(), 0,
                List.of(), "", 0L);
        String report = ReportWriter.toText(clean);

        assertFalse(report.contains("AYRIŞTIRMA UYARILARI"));
        assertFalse(report.contains("İŞ TİPİNE GÖRE ORTALAMA GECİKME"));
    }

    @Test
    void report_survivesARealRun() throws Exception {
        URL workflow = getClass().getClassLoader().getResource("sample_workflow.txt");
        URL job = getClass().getClassLoader().getResource("sample_job.txt");
        assertNotNull(workflow);
        assertNotNull(job);

        SimulationResult result = SimulationRunner.run(
                new File(workflow.toURI()), new File(job.toURI()));

        String report = ReportWriter.toText(result);
        assertFalse(report.isBlank());
        assertTrue(report.contains("S1"));
        assertTrue(report.contains("J1"));
    }
}
