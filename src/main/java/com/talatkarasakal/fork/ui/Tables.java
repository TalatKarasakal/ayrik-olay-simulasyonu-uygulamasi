package com.talatkarasakal.fork.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Shared styling and cell renderers for the result tables. */
public final class Tables {

    private Tables() {
    }

    /** Applies the window's table styling and returns a scroll pane ready to drop into a card. */
    public static JScrollPane wrap(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setFont(UiTheme.uiFont(Font.PLAIN, 12.5f));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.putClientProperty("JTable.rowSelectionBackground", UiTheme.alpha(UiTheme.accent(), 0.22f));

        JTableHeader header = table.getTableHeader();
        header.setFont(UiTheme.uiFont(Font.BOLD, 11.5f));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scroll;
    }

    /** Right-aligned numbers with a monospaced figure font, so columns line up. */
    public static TableCellRenderer numberRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                String text = value == null ? "—" : String.valueOf(value);
                Component c = super.getTableCellRendererComponent(table, text, selected, focused, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(UiTheme.monoFont(12f));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
                if (!selected) {
                    setForeground(value == null ? UiTheme.muted() : UiTheme.text());
                }
                return c;
            }
        };
    }

    /** Left-aligned text with comfortable padding. */
    public static TableCellRenderer textRenderer(boolean bold) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setFont(UiTheme.uiFont(bold ? Font.BOLD : Font.PLAIN, 12.5f));
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                return c;
            }
        };
    }

    /** Renders a job status as a rounded, colour-coded pill. */
    public static TableCellRenderer statusRenderer() {
        return new ChipRenderer() {
            @Override
            protected Color chipColor(Object value) {
                return value instanceof JobsTableModel.Status status ? status.color() : UiTheme.muted();
            }

            @Override
            protected String chipText(Object value) {
                return value instanceof JobsTableModel.Status status ? status.label() : "—";
            }
        };
    }

    /** Renders the FIFO / EDD scheduling policy as a neutral pill. */
    public static TableCellRenderer policyRenderer() {
        return new ChipRenderer() {
            @Override
            protected Color chipColor(Object value) {
                return "FIFO".equals(value) ? UiTheme.info() : UiTheme.accent();
            }

            @Override
            protected String chipText(Object value) {
                return value == null ? "—" : String.valueOf(value);
            }
        };
    }

    /** Renders a boolean flag as a yes/no pill rather than a checkbox. */
    public static TableCellRenderer flagRenderer(String yes, String no) {
        return new ChipRenderer() {
            @Override
            protected Color chipColor(Object value) {
                return Boolean.TRUE.equals(value) ? UiTheme.success() : UiTheme.muted();
            }

            @Override
            protected String chipText(Object value) {
                return Boolean.TRUE.equals(value) ? yes : no;
            }
        };
    }

    /** Renders a percentage as an inline bar with the value alongside it. */
    public static TableCellRenderer utilizationRenderer() {
        return new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                double percent = value instanceof Double d ? d : 0;
                return new UtilizationCell(percent, selected ? table.getSelectionBackground() : null);
            }
        };
    }

    /** Base renderer painting a filled pill centred in the cell. */
    private abstract static class ChipRenderer extends JLabel implements TableCellRenderer {

        private Color chip = UiTheme.muted();
        private Color rowBackground;

        protected ChipRenderer() {
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        protected abstract Color chipColor(Object value);

        protected abstract String chipText(Object value);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            chip = chipColor(value);
            rowBackground = selected ? table.getSelectionBackground() : null;
            setText(chipText(value));
            setFont(UiTheme.uiFont(Font.BOLD, 11f));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (rowBackground != null) {
                    g2.setColor(rowBackground);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                FontMetrics fm = g2.getFontMetrics(getFont());
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int chipWidth = Math.min(textWidth + 20, getWidth() - 8);
                int chipHeight = 20;
                int x = (getWidth() - chipWidth) / 2;
                int y = (getHeight() - chipHeight) / 2;

                g2.setColor(UiTheme.alpha(chip, 0.18f));
                g2.fillRoundRect(x, y, chipWidth, chipHeight, chipHeight, chipHeight);
                g2.setColor(UiTheme.alpha(chip, 0.55f));
                g2.drawRoundRect(x, y, chipWidth, chipHeight, chipHeight, chipHeight);

                g2.setColor(chip);
                g2.setFont(getFont());
                g2.drawString(text, (getWidth() - textWidth) / 2f,
                        y + (chipHeight + fm.getAscent() - fm.getDescent()) / 2f);
            } finally {
                g2.dispose();
            }
        }
    }

    /** A percentage cell: a rounded track, a colour-coded fill, and the value on the right. */
    private static final class UtilizationCell extends JLabel {

        private final double percent;
        private final Color rowBackground;

        UtilizationCell(double percent, Color rowBackground) {
            this.percent = percent;
            this.rowBackground = rowBackground;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (rowBackground != null) {
                    g2.setColor(rowBackground);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                Font font = UiTheme.monoFont(11.5f);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();
                String label = Format.percent(percent);
                int labelWidth = fm.stringWidth("100.0%") + 10;

                int trackX = 10;
                int trackWidth = getWidth() - trackX - labelWidth - 10;
                int barHeight = 8;
                int barY = (getHeight() - barHeight) / 2;

                if (trackWidth > 12) {
                    g2.setColor(UiTheme.surfaceRaised());
                    g2.fillRoundRect(trackX, barY, trackWidth, barHeight, barHeight, barHeight);

                    double clamped = Double.isFinite(percent) ? Math.max(0, Math.min(100, percent)) : 0;
                    int filled = (int) Math.round(trackWidth * clamped / 100.0);
                    if (filled > 0) {
                        g2.setColor(UiTheme.loadColor(percent));
                        g2.fillRoundRect(trackX, barY, Math.max(filled, barHeight), barHeight,
                                barHeight, barHeight);
                    }
                }

                g2.setColor(UiTheme.text());
                g2.drawString(label, getWidth() - 10 - fm.stringWidth(label),
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2f);
            } finally {
                g2.dispose();
            }
        }
    }
}
