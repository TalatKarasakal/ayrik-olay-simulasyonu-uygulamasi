package com.talatkarasakal.fork;

import java.util.List;
import java.util.Map;

/**
 * Everything a caller needs to render a finished simulation: the per-job and per-station
 * outcomes, the aggregate metrics, any parser warnings, and the raw console log.
 */
public record SimulationResult(List<JobResult> jobs,
                               List<StationResult> stations,
                               Map<String, Double> tardinessByType,
                               int simulationEndTime,
                               List<String> warnings,
                               String log,
                               long durationMillis) {

    public SimulationResult {
        jobs = List.copyOf(jobs);
        stations = List.copyOf(stations);
        tardinessByType = Map.copyOf(tardinessByType);
        warnings = List.copyOf(warnings);
    }

    public int completedJobCount() {
        return (int) jobs.stream().filter(JobResult::completed).count();
    }

    public int lateJobCount() {
        return (int) jobs.stream().filter(JobResult::isLate).count();
    }

    /** Mean tardiness across every late job, or 0 when nothing ran late. */
    public double overallAverageTardiness() {
        return jobs.stream()
                .filter(JobResult::isLate)
                .mapToInt(JobResult::tardiness)
                .average()
                .orElse(0.0);
    }

    /** Mean station utilization, ignoring values that are not finite (e.g. a zero-length run). */
    public double averageUtilization() {
        return stations.stream()
                .mapToDouble(StationResult::utilization)
                .filter(Double::isFinite)
                .average()
                .orElse(0.0);
    }

    /** The most heavily loaded station — the bottleneck — or {@code null} when there are none. */
    public StationResult busiestStation() {
        return stations.stream()
                .filter(s -> Double.isFinite(s.utilization()))
                .max((a, b) -> Double.compare(a.utilization(), b.utilization()))
                .orElse(null);
    }

    SimulationResult withLog(String log, long durationMillis) {
        return new SimulationResult(jobs, stations, tardinessByType, simulationEndTime,
                warnings, log, durationMillis);
    }
}
