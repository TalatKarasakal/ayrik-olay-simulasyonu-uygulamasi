package com.talatkarasakal.fork.ui;

import com.talatkarasakal.fork.StationResult;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Table model over the per-station configuration and load of a run. */
public class StationsTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "İstasyon", "Kapasite", "Politika", "Çoklu Görev", "Görev Tipleri",
            "Meşgul Süre (sn)", "Kullanım"
    };

    private List<StationResult> rows = new ArrayList<>();

    public void setRows(List<StationResult> rows) {
        this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public StationResult rowAt(int index) {
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
            case 1, 5 -> Integer.class;
            case 3 -> Boolean.class;
            case 6 -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        StationResult station = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> station.stationId();
            case 1 -> station.maxCapacity();
            case 2 -> station.schedulingPolicy();
            case 3 -> station.multiFlag();
            case 4 -> station.taskTypes().isEmpty() ? "—" : String.join(", ", station.taskTypes());
            case 5 -> station.totalProcessTime();
            case 6 -> station.utilization();
            default -> null;
        };
    }
}
