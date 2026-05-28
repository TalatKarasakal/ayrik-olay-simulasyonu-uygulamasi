package com.talatkarasakal.fork;

import java.util.HashMap;
import java.util.Map;

public class StationM {
    private Map<String, Station> stations;

    public StationM() {
        this.stations = new HashMap<>();
    }

    public Map<String, Station> getStations() {
        return stations;
    }

    public void addStation(Station station) {
        if (station != null) {
            stations.put(station.getStationID(), station);
        }
    }

    public Station getBestStationForTask(TaskType task, int currentTime) {
        Station bestStation = null;
        int minQueueLength = Integer.MAX_VALUE;

        for (String stationID : stations.keySet()) {
            Station station = stations.get(stationID);
            if (station.canHandleTaskType(task.getTaskTypeID())) {
                int queueLength = station.getQueueLengthForTask(task.getTaskTypeID());
                if (queueLength < minQueueLength) {
                    minQueueLength = queueLength;
                    bestStation = station;
                }
            }
        }

        return bestStation;
    }

    public Station getStationByTaskAndJob(String taskTypeID, String jobID) {
        for (String stationID : stations.keySet()) {
            Station station = stations.get(stationID);
            if (station.containsTaskOfJob(taskTypeID, jobID)) {
                return station;
            }
        }
        return null;
    }
}
