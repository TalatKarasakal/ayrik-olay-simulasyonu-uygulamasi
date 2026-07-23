package com.talatkarasakal.fork.ui;

import com.talatkarasakal.fork.JobResult;

import javax.swing.table.AbstractTableModel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Table model over the per-job outcomes of a run. */
public class JobsTableModel extends AbstractTableModel {

    /** Outcome of a job, with the label and colour used for its chip. */
    public enum Status {
        ON_TIME("Zamanında", UiTheme::success),
        LATE("Geç", UiTheme::warning),
        INCOMPLETE("Tamamlanmadı", UiTheme::danger);

        private final String label;
        private final Supplier<Color> color;

        Status(String label, Supplier<Color> color) {
            this.label = label;
            this.color = color;
        }

        public String label() {
            return label;
        }

        public Color color() {
            return color.get();
        }

        @Override
        public String toString() {
            return label;
        }

        static Status of(JobResult job) {
            if (!job.completed()) {
                return INCOMPLETE;
            }
            return job.isLate() ? LATE : ON_TIME;
        }
    }

    private static final String[] COLUMNS = {
            "İş ID", "Tip", "Başlangıç (sn)", "Süre (dk)", "Son Teslim (sn)",
            "Bitiş (sn)", "Geçen Süre (sn)", "Gecikme (sn)", "Durum"
    };

    private List<JobResult> rows = new ArrayList<>();

    public void setRows(List<JobResult> rows) {
        this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public JobResult rowAt(int index) {
        return rows.get(index);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 0, 1 -> String.class;
            case 8 -> Status.class;
            default -> Integer.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        JobResult job = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> job.jobId();
            case 1 -> job.jobTypeId();
            case 2 -> job.startTime();
            case 3 -> job.duration();
            case 4 -> job.startTime() + job.duration() * 60;
            case 5 -> job.completed() ? job.completionTime() : null;
            case 6 -> job.completed() ? job.turnaround() : null;
            case 7 -> job.isLate() ? job.tardiness() : null;
            case 8 -> Status.of(job);
            default -> null;
        };
    }
}
