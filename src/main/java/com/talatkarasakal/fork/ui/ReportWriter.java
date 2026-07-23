package com.talatkarasakal.fork.ui;

import com.talatkarasakal.fork.JobResult;
import com.talatkarasakal.fork.SimulationResult;
import com.talatkarasakal.fork.StationResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Renders a finished run as a plain-text report suitable for saving or pasting into a document. */
public final class ReportWriter {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private ReportWriter() {
    }

    public static String toText(SimulationResult result) {
        StringBuilder sb = new StringBuilder();

        heading(sb, "AYRIK OLAY SİMÜLASYONU — SONUÇ RAPORU");
        sb.append("Oluşturulma: ").append(LocalDateTime.now().format(TIMESTAMP)).append('\n');
        sb.append("Hesaplama süresi: ").append(Format.millis(result.durationMillis())).append("\n\n");

        heading(sb, "ÖZET");
        line(sb, "Toplam iş", Format.count(result.jobs().size()));
        line(sb, "Tamamlanan iş", Format.count(result.completedJobCount()));
        line(sb, "Geciken iş", Format.count(result.lateJobCount()));
        line(sb, "Ortalama gecikme", Format.seconds(result.overallAverageTardiness()));
        line(sb, "Simülasyon süresi", Format.count(result.simulationEndTime()) + " sn ("
                + Format.span(result.simulationEndTime()) + ")");
        line(sb, "Ort. istasyon kullanımı", Format.percent(result.averageUtilization()));
        StationResult busiest = result.busiestStation();
        if (busiest != null) {
            line(sb, "En yoğun istasyon",
                    busiest.stationId() + " (" + Format.percent(busiest.utilization()) + ")");
        }
        sb.append('\n');

        heading(sb, "İŞLER");
        sb.append(String.format("%-10s %-8s %10s %8s %12s %12s %12s  %s%n",
                "İş ID", "Tip", "Başlangıç", "Süre", "Son Teslim", "Bitiş", "Gecikme", "Durum"));
        for (JobResult job : result.jobs()) {
            sb.append(String.format("%-10s %-8s %10d %8d %12d %12s %12s  %s%n",
                    job.jobId(), job.jobTypeId(), job.startTime(), job.duration(),
                    job.startTime() + job.duration() * 60,
                    job.completed() ? String.valueOf(job.completionTime()) : "—",
                    job.isLate() ? String.valueOf(job.tardiness()) : "—",
                    !job.completed() ? "Tamamlanmadı" : job.isLate() ? "Geç" : "Zamanında"));
        }
        sb.append('\n');

        heading(sb, "İSTASYONLAR");
        sb.append(String.format("%-12s %9s %-8s %-12s %14s %10s  %s%n",
                "İstasyon", "Kapasite", "Politika", "Çoklu Görev", "Meşgul Süre", "Kullanım",
                "Görev Tipleri"));
        for (StationResult station : result.stations()) {
            sb.append(String.format("%-12s %9d %-8s %-12s %14d %10s  %s%n",
                    station.stationId(), station.maxCapacity(), station.schedulingPolicy(),
                    station.multiFlag() ? "Evet" : "Hayır", station.totalProcessTime(),
                    Format.percent(station.utilization()),
                    String.join(", ", station.taskTypes())));
        }
        sb.append('\n');

        if (!result.tardinessByType().isEmpty()) {
            heading(sb, "İŞ TİPİNE GÖRE ORTALAMA GECİKME");
            for (Map.Entry<String, Double> entry : result.tardinessByType().entrySet()) {
                line(sb, entry.getKey(), Format.seconds(entry.getValue()));
            }
            sb.append('\n');
        }

        if (!result.warnings().isEmpty()) {
            heading(sb, "AYRIŞTIRMA UYARILARI");
            result.warnings().forEach(w -> sb.append("  • ").append(w).append('\n'));
            sb.append('\n');
        }

        return sb.toString();
    }

    private static void heading(StringBuilder sb, String text) {
        sb.append(text).append('\n');
        sb.append("=".repeat(text.length())).append('\n');
    }

    private static void line(StringBuilder sb, String label, String value) {
        sb.append(String.format("  %-28s %s%n", label + ":", value));
    }
}
