package com.talatkarasakal.fork.ui;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A horizontal bar list: a label, a proportional track, and the formatted value. Used for both the
 * station utilization breakdown and the average tardiness per job type.
 */
public class MetricBars extends JComponent {

    private static final int ROW_HEIGHT = 30;
    private static final int BAR_HEIGHT = 12;
    private static final int GAP = 12;

    /**
     * One row of the chart.
     *
     * @param value     magnitude used to size the bar
     * @param valueText the label drawn at the trailing edge
     * @param color     resolved lazily so bars follow a theme switch
     * @param tooltip   HTML shown on hover, or {@code null} for none
     */
    public record Bar(String label, double value, String valueText,
                      Supplier<Color> color, String tooltip) {
    }

    private List<Bar> bars = new ArrayList<>();
    private String emptyMessage = "Veri yok";
    private double explicitMax;

    public MetricBars() {
        setOpaque(false);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public void setBars(List<Bar> bars) {
        this.bars = bars == null ? new ArrayList<>() : new ArrayList<>(bars);
        revalidate();
        repaint();
    }

    public void setEmptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage;
        repaint();
    }

    /**
     * Pins the value that fills the track completely — pass 100 for percentages so bars stay
     * comparable across runs. Zero or less means scale to the largest value present.
     */
    public void setMax(double max) {
        this.explicitMax = max;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(320, Math.max(bars.size(), 1) * ROW_HEIGHT + 8);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int index = (event.getY() - 4) / ROW_HEIGHT;
        if (index < 0 || index >= bars.size()) {
            return null;
        }
        return bars.get(index).tooltip();
    }

    private double max() {
        if (explicitMax > 0) {
            return explicitMax;
        }
        double max = bars.stream()
                .mapToDouble(Bar::value)
                .filter(Double::isFinite)
                .max()
                .orElse(0);
        return max > 0 ? max : 1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (bars.isEmpty()) {
                paintEmpty(g2);
                return;
            }

            Font labelFont = UiTheme.uiFont(Font.BOLD, 12f);
            Font valueFont = UiTheme.monoFont(11.5f);
            FontMetrics labelMetrics = g2.getFontMetrics(labelFont);
            FontMetrics valueMetrics = g2.getFontMetrics(valueFont);

            int labelWidth = 0;
            int valueWidth = 0;
            for (Bar bar : bars) {
                labelWidth = Math.max(labelWidth, labelMetrics.stringWidth(bar.label()));
                valueWidth = Math.max(valueWidth, valueMetrics.stringWidth(bar.valueText()));
            }
            labelWidth = Math.min(labelWidth, 130);

            int trackX = labelWidth + GAP;
            int trackWidth = getWidth() - trackX - valueWidth - GAP;
            if (trackWidth <= 8) {
                return;
            }

            double max = max();

            for (int i = 0; i < bars.size(); i++) {
                Bar bar = bars.get(i);
                int rowY = 4 + i * ROW_HEIGHT;
                int barY = rowY + (ROW_HEIGHT - BAR_HEIGHT) / 2;
                int baseline = rowY + (ROW_HEIGHT + labelMetrics.getAscent()) / 2 - 2;

                g2.setFont(labelFont);
                g2.setColor(UiTheme.text());
                g2.drawString(clip(labelMetrics, bar.label(), labelWidth), 0, baseline);

                g2.setColor(UiTheme.surfaceRaised());
                g2.fillRoundRect(trackX, barY, trackWidth, BAR_HEIGHT, BAR_HEIGHT, BAR_HEIGHT);

                double value = Double.isFinite(bar.value()) ? Math.max(0, bar.value()) : 0;
                int filled = (int) Math.round(trackWidth * Math.min(value / max, 1.0));
                if (filled > 0) {
                    g2.setColor(bar.color().get());
                    g2.fillRoundRect(trackX, barY, Math.max(filled, BAR_HEIGHT), BAR_HEIGHT,
                            BAR_HEIGHT, BAR_HEIGHT);
                }

                g2.setFont(valueFont);
                g2.setColor(UiTheme.muted());
                g2.drawString(bar.valueText(), trackX + trackWidth + GAP, baseline);
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintEmpty(Graphics2D g2) {
        g2.setFont(UiTheme.uiFont(Font.PLAIN, 12f));
        g2.setColor(UiTheme.muted());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(emptyMessage, Math.max((getWidth() - fm.stringWidth(emptyMessage)) / 2, 0),
                getHeight() / 2);
    }

    private static String clip(FontMetrics fm, String text, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() > 1 && fm.stringWidth(sb + "…") > maxWidth) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb + "…";
    }
}
