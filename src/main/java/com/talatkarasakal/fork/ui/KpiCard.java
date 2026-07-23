package com.talatkarasakal.fork.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Supplier;

/**
 * A single headline metric: a caption, a large value, and a hint line underneath. An accent stripe
 * down the leading edge colour-codes the metric at a glance.
 */
public class KpiCard extends Card {

    private final JLabel caption = new JLabel();
    private final JLabel value = new JLabel("—");
    private final JLabel hint = new JLabel(" ");

    /** Resolved lazily so the stripe follows a theme switch. */
    private Supplier<Color> accent = UiTheme::accent;

    public KpiCard(String captionText) {
        super();
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 16));

        caption.setText(captionText);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        column.add(caption);
        column.add(Box.createVerticalStrut(6));
        column.add(value);
        column.add(Box.createVerticalStrut(4));
        column.add(hint);

        body().add(column);
        applyColors();
    }

    public void setValue(String text) {
        value.setText(text);
    }

    public void setHint(String text) {
        hint.setText(text == null || text.isBlank() ? " " : text);
    }

    /** Colour of the leading stripe and the value text. */
    public void setAccent(Supplier<Color> accentSupplier) {
        this.accent = accentSupplier;
        applyColors();
        repaint();
    }

    public void clear() {
        setValue("—");
        setHint(null);
    }

    private void applyColors() {
        // JPanel's constructor calls updateUI() before this class's fields are assigned.
        if (caption == null) {
            return;
        }
        caption.setFont(UiTheme.uiFont(Font.PLAIN, 12f));
        caption.setForeground(UiTheme.muted());
        value.setFont(UiTheme.uiFont(Font.BOLD, 26f));
        value.setForeground(accent == null ? UiTheme.text() : accent.get());
        hint.setFont(UiTheme.uiFont(Font.PLAIN, 11f));
        hint.setForeground(UiTheme.muted());
    }

    @Override
    public void updateUI() {
        super.updateUI();
        applyColors();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 170), Math.max(d.height, 108));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(accent == null ? UiTheme.accent() : accent.get());
            g2.fillRoundRect(7, 16, 4, getHeight() - 32, 4, 4);
        } finally {
            g2.dispose();
        }
    }
}
