package com.talatkarasakal.fork;

import java.util.ArrayList;
import java.util.List;

/**
 * A single station's configuration and load after a simulation run, flattened for display.
 *
 * @param utilization busy time as a percentage of the simulation length
 */
public record StationResult(String stationId,
                            int maxCapacity,
                            boolean multiFlag,
                            boolean fifoFlag,
                            List<String> taskTypes,
                            int totalProcessTime,
                            double utilization) {

    public StationResult {
        taskTypes = List.copyOf(taskTypes);
    }

    /** "FIFO" or "EDD" — the scheduling policy the station used. */
    public String schedulingPolicy() {
        return fifoFlag ? "FIFO" : "EDD";
    }

    public static StationResult of(Station station, double utilization) {
        List<String> taskTypes = new ArrayList<>(station.getTaskSpeeds().keySet());
        taskTypes.sort(String::compareTo);
        return new StationResult(
                station.getStationID(),
                station.getMaxCapacity(),
                station.isMultiFlag(),
                station.isFifoFlag(),
                taskTypes,
                station.getTotalProcessTime(),
                utilization);
    }
}
