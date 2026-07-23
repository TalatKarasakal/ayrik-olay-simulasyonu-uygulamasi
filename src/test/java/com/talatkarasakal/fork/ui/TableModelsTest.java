package com.talatkarasakal.fork.ui;

import com.talatkarasakal.fork.JobResult;
import com.talatkarasakal.fork.StationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableModelsTest {

    private static JobResult job(String id, int completion, int tardiness, boolean completed) {
        return new JobResult(id, "JT1", 10, 2, completion, tardiness, completed);
    }

    // --- jobs ----------------------------------------------------------------------------

    @Test
    void jobsModel_exposesEveryColumn() {
        JobsTableModel model = new JobsTableModel();
        model.setRows(List.of(job("J1", 200, 70, true)));

        assertEquals(1, model.getRowCount());
        assertEquals("J1", model.getValueAt(0, 0));
        assertEquals("JT1", model.getValueAt(0, 1));
        assertEquals(10, model.getValueAt(0, 2));
        assertEquals(2, model.getValueAt(0, 3));
        assertEquals(130, model.getValueAt(0, 4), "deadline = start + duration*60");
        assertEquals(200, model.getValueAt(0, 5));
        assertEquals(190, model.getValueAt(0, 6), "turnaround = completion - start");
        assertEquals(70, model.getValueAt(0, 7));
        assertEquals(JobsTableModel.Status.LATE, model.getValueAt(0, 8));
    }

    @Test
    void jobsModel_leavesUnfinishedJobCellsEmpty() {
        JobsTableModel model = new JobsTableModel();
        model.setRows(List.of(job("J1", 0, 0, false)));

        assertNull(model.getValueAt(0, 5), "no completion time");
        assertNull(model.getValueAt(0, 6), "no turnaround");
        assertNull(model.getValueAt(0, 7), "no tardiness");
        assertEquals(JobsTableModel.Status.INCOMPLETE, model.getValueAt(0, 8));
    }

    @Test
    void jobsModel_marksOnTimeJobs() {
        JobsTableModel model = new JobsTableModel();
        model.setRows(List.of(job("J1", 120, 0, true)));

        assertEquals(JobsTableModel.Status.ON_TIME, model.getValueAt(0, 8));
        assertNull(model.getValueAt(0, 7), "an on-time job shows no tardiness");
    }

    @Test
    void jobsModel_columnClassesDriveSorting() {
        JobsTableModel model = new JobsTableModel();
        assertEquals(String.class, model.getColumnClass(0));
        assertEquals(Integer.class, model.getColumnClass(2));
        assertEquals(JobsTableModel.Status.class, model.getColumnClass(8));
    }

    @Test
    void jobsModel_setRowsReplacesPreviousData() {
        JobsTableModel model = new JobsTableModel();
        model.setRows(List.of(job("J1", 100, 0, true), job("J2", 100, 0, true)));
        model.setRows(List.of(job("J3", 100, 0, true)));

        assertEquals(1, model.getRowCount());
        assertEquals("J3", model.getValueAt(0, 0));
    }

    @Test
    void jobsModel_handlesNullRows() {
        JobsTableModel model = new JobsTableModel();
        model.setRows(null);
        assertEquals(0, model.getRowCount());
    }

    @Test
    void status_labelsAreHumanReadable() {
        assertEquals("Zamanında", JobsTableModel.Status.ON_TIME.label());
        assertEquals("Geç", JobsTableModel.Status.LATE.label());
        assertEquals("Tamamlanmadı", JobsTableModel.Status.INCOMPLETE.label());
    }

    // --- stations ------------------------------------------------------------------------

    @Test
    void stationsModel_exposesEveryColumn() {
        StationsTableModel model = new StationsTableModel();
        model.setRows(List.of(new StationResult("S1", 2, true, true, List.of("T1", "T2"), 660, 157.1)));

        assertEquals("S1", model.getValueAt(0, 0));
        assertEquals(2, model.getValueAt(0, 1));
        assertEquals("FIFO", model.getValueAt(0, 2));
        assertEquals(true, model.getValueAt(0, 3));
        assertEquals("T1, T2", model.getValueAt(0, 4));
        assertEquals(660, model.getValueAt(0, 5));
        assertEquals(157.1, (Double) model.getValueAt(0, 6), 1e-9);
    }

    @Test
    void stationsModel_showsADashWhenNoTaskTypes() {
        StationsTableModel model = new StationsTableModel();
        model.setRows(List.of(new StationResult("S1", 1, false, false, List.of(), 0, 0)));

        assertEquals("—", model.getValueAt(0, 4));
        assertEquals("EDD", model.getValueAt(0, 2));
    }

    @Test
    void stationsModel_columnClassesDriveSorting() {
        StationsTableModel model = new StationsTableModel();
        assertEquals(String.class, model.getColumnClass(0));
        assertEquals(Integer.class, model.getColumnClass(1));
        assertEquals(Boolean.class, model.getColumnClass(3));
        assertEquals(Double.class, model.getColumnClass(6));
    }
}
