package com.talatkarasakal.fork;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class WorkflowFileParser {
    private Map<String, TaskType> taskTypes = new HashMap<>();
    private Map<String, JobType> jobTypes = new HashMap<>();
    private Map<String, Station> stations = new HashMap<>();
    private List<String> errorMessages = new ArrayList<>();
    private boolean strict = true;

    /**
     * In strict mode (the default, used by the command line entry point) a parse error
     * terminates the JVM, preserving the original behaviour. The GUI turns strict mode off
     * so that errors can be surfaced in the window instead of killing the application.
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean isStrict() {
        return strict;
    }

    public Map<String, TaskType> getTaskTypes() {
        return taskTypes;
    }

    public Map<String, JobType> getJobTypes() {
        return jobTypes;
    }

    public Map<String, Station> getStations() {
        return stations;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public Map<String, Station> parse(File file) throws FileNotFoundException {
        Scanner sc = new Scanner(file);
        int lineNumber = 1;
        try {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    lineNumber++;
                    continue;
                }

                if (line.startsWith("(TASKTYPES")) {
                    parseTaskTypes(sc, lineNumber, line.substring("(TASKTYPES".length()).trim());
                } else if (line.startsWith("(JOBTYPES")) {
                    parseJobTypes(sc, lineNumber);
                } else if (line.startsWith("(STATIONS")) {
                    parseStations(sc, lineNumber);
                    break;
                }
                lineNumber++;
            }
        } finally {
            sc.close();
            errorReports();
        }
        return stations;
    }

    private void parseTaskTypes(Scanner sc, int lineNumber, String content) {
        System.out.println("Parsing TASKTYPES at line " + lineNumber);
        content = content.replaceAll("[()]", "");
        String[] parts = content.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                if (!taskTypes.containsKey(part)) {
                    double size = 1.0;
                    if (i + 1 < parts.length && parts[i + 1].matches("-?\\d+(\\.\\d+)?")) {
                        size = Double.parseDouble(parts[i + 1]);
                        if (size < 0) {
                            errorMessages.add("Line " + lineNumber + ": " + part + " has a negative task size.");
                            i++;
                            continue;
                        }
                        i++;
                    }
                    taskTypes.put(part, new TaskType(part, size));
                    System.out.println("Added task type: " + part + " with size " + size);
                } else {
                    errorMessages.add("Line " + lineNumber + ": TasktypeID listed twice: " + part);
                }
            } else {
                errorMessages.add("Line " + lineNumber + ": Invalid tasktypeID: " + part);
            }
        }
    }

    private void parseJobTypes(Scanner sc, int lineNumber) {
        System.out.println("Parsing JOBTYPES at line " + lineNumber);
        StringBuilder content = new StringBuilder();

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            content.append(line).append(" ");
            if (line.endsWith("))")) {
                break;
            }
            lineNumber++;
        }

        String[] jobTypeEntries = content.toString().split("\\)\\s*\\(");

        for (String jobTypeEntry : jobTypeEntries) {
            jobTypeEntry = jobTypeEntry.replaceAll("[()]", "");
            String[] parts = jobTypeEntry.trim().split("\\s+");
            if (parts.length == 0) {
                continue;
            }

            String jobTypeID = parts[0];
            System.out.println("Found job type ID: " + jobTypeID);

            if (jobTypes.containsKey(jobTypeID)) {
                errorMessages.add("Line " + lineNumber + ": JobType " + jobTypeID + " already declared.");
                continue;
            }

            List<TaskType> tasks = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                String taskTypeID = parts[i];
                double size = 1.0;

                if (i + 1 < parts.length && parts[i + 1].matches("-?\\d+(\\.\\d+)?")) {
                    size = Double.parseDouble(parts[i + 1]);
                    i++;
                } else if (taskTypes.containsKey(taskTypeID)) {
                    size = taskTypes.get(taskTypeID).getDefaultSize();
                }

                if (!taskTypes.containsKey(taskTypeID)) {
                    errorMessages.add("Line " + lineNumber + ": Undefined task type " + taskTypeID
                            + " in job type " + jobTypeID);
                } else {
                    tasks.add(new TaskType(taskTypeID, size));
                }
            }
            jobTypes.put(jobTypeID, new JobType(jobTypeID, tasks));
            System.out.println("Added job type: " + jobTypeID);
        }
    }

    private void parseStations(Scanner scanner, int lineNumber) {
        System.out.println("Parsing STATIONS at line " + lineNumber);
        StringBuilder stringBuilder = new StringBuilder();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            stringBuilder.append(line).append(" ");
            if (line.endsWith(")")) {
                break;
            }
            lineNumber++;
        }

        String[] stationEntries = stringBuilder.toString().split("\\)\\s*\\(");

        for (String entryStation : stationEntries) {
            entryStation = entryStation.replaceAll("[()]", "");
            String[] parts = entryStation.trim().split("\\s+");

            if (parts.length < 4) {
                errorMessages.add("Line " + lineNumber + ": Insufficient data for station definition.");
                lineNumber++;
                continue;
            }

            String stationID = parts[0];
            int capacity;

            try {
                capacity = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                errorMessages.add("Line " + lineNumber + ": Invalid capacity for station " + stationID);
                lineNumber++;
                continue;
            }

            boolean multiFlag = "Y".equals(parts[2]);
            boolean fifoFlag = "Y".equals(parts[3]);
            double stationSpeed = 1.0;
            double speedVariation = 0.0;

            Station station = new Station(stationID, capacity, multiFlag, fifoFlag, stationSpeed, speedVariation);

            int i = 4;
            while (i < parts.length) {
                String taskTypeID = parts[i];
                double speed = 0.0;

                if (i + 1 < parts.length && parts[i + 1].matches("-?\\d+(\\.\\d+)?")) {
                    speed = Double.parseDouble(parts[i + 1]);
                    i += 2;
                } else if (i == parts.length - 1 && parts[i].matches("-?\\d+(\\.\\d+)?")) {
                    speedVariation = Double.parseDouble(parts[i]);
                    break;
                } else {
                    errorMessages.add("Line " + lineNumber + ": Missing speed for task type " + taskTypeID);
                    break;
                }

                if (!taskTypes.containsKey(taskTypeID)) {
                    errorMessages.add("Line " + lineNumber + ": TaskType " + taskTypeID
                            + " not executed in any STATIONS.");
                }
                station.addTaskType(taskTypeID, speed);
            }

            station.setSpeedVariation(speedVariation);
            stations.put(stationID, station);
            System.out.println("Added station: " + stationID);

            System.out.println("Station Details:");
            System.out.println("  -Station ID: " + station.getStationID());
            System.out.println("  -Max Capacity: " + station.getMaxCapacity());
            System.out.println("  -MultiFlag: " + station.isMultiFlag());
            System.out.println("  -FIFO Flag: " + station.isFifoFlag());
            System.out.println("  -Speed Variation: " + station.getSpeedVariation());
            for (Map.Entry<String, Double> entry : station.getTaskSpeeds().entrySet()) {
                System.out.println("  -Task Type: " + entry.getKey() + ", Speed: " + entry.getValue());
            }

            lineNumber++;
        }
    }

    private void errorReports() {
        if (!errorMessages.isEmpty()) {
            for (String error : errorMessages) {
                System.out.println(error);
            }
            if (strict) {
                System.exit(1);
            }
        }
    }
}
