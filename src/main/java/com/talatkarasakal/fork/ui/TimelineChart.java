package com.talatkarasakal.fork.ui;

import com.talatkarasakal.fork.JobResult;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gantt-style view of the run: one row per job, spanning from its release time to its completion.
 * Bars are coloured by job type, and a job that never finished is drawn as a hollow bar running to
 * the end of the simulation.
 */
public class TimelineChart extends JComponent {

    private static final int ROW_HEIGHT = 26;
    private static final int BAR_HEIGHT = 14;
    private static final int LABEL_WIDTH = 74;
    private static final int AXIS_HEIGHT = 22;
    private static final int RIGHT_PAD = 16;

    private List<JobResult> jobs = new ArrayList<>();
    private int simulationEndTime;
    private Map<String, Integer> typeColorIndex = new LinkedHashMap<>();

    public TimelineChart() {
        setOpaque(false);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public void setJobs(List<JobResult> jobs, int simulationEndTime) {
        this.jobs = jobs == null ? new ArrayList<>() : new ArrayList<>(jobs);
        this.simulationEndTime = simulationEndTime;

        // Assign a stable colour slot per job type, in first-seen order.
        typeColorIndex = new LinkedHashMap<>();
        for (JobResult job : this.jobs) {
            typeColorIndex.computeIfAbsent(job.jobTypeId(), k -> typeColorIndex.size());
        }

        revalidate();
        repaint();
    }

    /** Job type → colour, so a legend elsewhere can stay in sync with the bars. */
    public Map<String, Color> legend() {
        Map<String, Color> legend = new LinkedHashMap<>();
        typeColorIndex.forEach((type, index) -> legend.put(type, UiTheme.series(index)));
        return legend;
    }

    @Override
    public Dimension getPreferredSize() {
        int rows = Math.max(jobs.size(), 1);
        return new Dimension(420, AXIS_HEIGHT + rows * ROW_HEIGHT + 10);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int index = (event.getY() - AXIS_HEIGHT) / ROW_HEIGHT;
        if (index < 0 || index >= jobs.size()) {
            return null;
        }
        JobResult job = jobs.get(index);
        String status = !job.completed() ? "Tamamlanmadı"
                : job.isLate() ? job.tardiness() + " sn geç" : "Zamanında";
        return String.format("<html><b>%s</b> &nbsp;(%s)<br>Başlangıç: %d sn<br>Bitiş: %s<br>"
                        + "Son teslim: %d sn<br>Durum: %s</html>",
                job.jobId(), job.jobTypeId(), job.startTime(),
                job.completed() ? job.completionTime() + " sn" : "—",
                job.startTime() + job.duration() * 60, status);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (jobs.isEmpty()) {
                paintEmpty(g2);
                return;
            }

            int plotX = LABEL_WIDTH;
            int plotWidth = getWidth() - plotX - RIGHT_PAD;
            if (plotWidth <= 0) {
                return;
            }

            int span = Math.max(1, axisMax());
            paintAxis(g2, plotX, plotWidth, span);

            Font labelFont = UiTheme.uiFont(Font.PLAIN, 11f);
            g2.setFont(labelFont);
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < jobs.size(); i++) {
                JobResult job = jobs.get(i);
                int rowY = AXIS_HEIGHT + i * ROW_HEIGHT;
                int barY = rowY + (ROW_HEIGHT - BAR_HEIGHT) / 2;

                g2.setColor(UiTheme.muted());
                g2.drawString(job.jobId(), 0, barY + (BAR_HEIGHT + fm.getAscent()) / 2 - 1);

                // Idle track behind the bar, so short jobs still read against a full-width row.
                g2.setColor(UiTheme.alpha(UiTheme.surfaceRaised(), 0.6f));
                g2.fillRoundRect(plotX, barY, plotWidth, BAR_HEIGHT, BAR_HEIGHT, BAR_HEIGHT);

                int end = job.completed() ? job.completionTime() : span;
                int x1 = plotX + scale(job.startTime(), span, plotWidth);
                int x2 = plotX + scale(end, span, plotWidth);
                int barWidth = Math.max(x2 - x1, 3);

                Color color = UiTheme.series(typeColorIndex.getOrDefault(job.jobTypeId(), 0));
                if (job.completed()) {
                    g2.setColor(color);
                    g2.fillRoundRect(x1, barY, barWidth, BAR_HEIGHT, BAR_HEIGHT, BAR_HEIGHT);
                } else {
                    g2.setColor(UiTheme.alpha(color, 0.25f));
                    g2.fillRoundRect(x1, barY, barWidth, BAR_HEIGHT, BAR_HEIGHT, BAR_HEIGHT);
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1f, new float[]{3f, 3f}, 0f));
                    g2.drawRoundRect(x1, barY, barWidth, BAR_HEIGHT, BAR_HEIGHT, BAR_HEIGHT);
                    g2.setStroke(new BasicStroke(1f));
                }

                // Deadline marker: a thin tick the bar should not run past.
                int deadline = job.startTime() + job.duration() * 60;
                if (deadline > 0 && deadline <= span) {
                    int dx = plotX + scale(deadline, span, plotWidth);
                    g2.setColor(job.isLate() ? UiTheme.danger() : UiTheme.alpha(UiTheme.muted(), 0.7f));
                    g2.fillRect(dx, barY - 3, 2, BAR_HEIGHT + 6);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    /** The axis always covers the whole run, including deadlines that fall past the last finish. */
    private int axisMax() {
        int max = simulationEndTime;
        for (JobResult job : jobs) {
            max = Math.max(max, job.completionTime());
            max = Math.max(max, job.startTime() + job.duration() * 60);
        }
        return max;
    }

    private void paintAxis(Graphics2D g2, int plotX, int plotWidth, int span) {
        g2.setFont(UiTheme.uiFont(Font.PLAIN, 10f));
        FontMetrics fm = g2.getFontMetrics();

        int ticks = Math.max(2, Math.min(8, plotWidth / 80));
        for (int i = 0; i <= ticks; i++) {
            int value = (int) Math.round((double) span * i / ticks);
            int x = plotX + scale(value, span, plotWidth);

            g2.setColor(UiTheme.alpha(UiTheme.border(), 0.8f));
            g2.drawLine(x, AXIS_HEIGHT - 4, x, AXIS_HEIGHT + jobs.size() * ROW_HEIGHT);

            String label = value + "s";
            g2.setColor(UiTheme.muted());
            int labelX = Math.min(x - fm.stringWidth(label) / 2, getWidth() - fm.stringWidth(label));
            g2.drawString(label, Math.max(labelX, 0), AXIS_HEIGHT - 9);
        }
    }

    private static int scale(int value, int span, int plotWidth) {
        double ratio = Math.max(0, Math.min(1, (double) value / span));
        return (int) Math.round(ratio * plotWidth);
    }

    private void paintEmpty(Graphics2D g2) {
        g2.setFont(UiTheme.uiFont(Font.PLAIN, 12f));
        g2.setColor(UiTheme.muted());
        String message = "Zaman çizelgesi için önce bir simülasyon çalıştırın";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(message, (getWidth() - fm.stringWidth(message)) / 2, getHeight() / 2);
    }
}
