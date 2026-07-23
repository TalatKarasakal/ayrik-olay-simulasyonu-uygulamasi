package com.talatkarasakal.fork.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * A rounded surface panel with an optional title / subtitle header — the basic building block
 * every screen in the window is composed from.
 */
public class Card extends JPanel {

    private static final int RADIUS = 14;

    private final JPanel body = new JPanel(new BorderLayout());
    private JPanel header;
    private JLabel titleLabel;
    private JLabel subtitleLabel;

    public Card() {
        this(null, null);
    }

    public Card(String title) {
        this(title, null);
    }

    public Card(String title, String subtitle) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        body.setOpaque(false);
        add(body, BorderLayout.CENTER);

        if (title != null) {
            buildHeader(title, subtitle);
        }
        applyColors();
    }

    private void buildHeader(String title, String subtitle) {
        header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(titleLabel);

        if (subtitle != null) {
            subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titles.add(Box.createVerticalStrut(2));
            titles.add(subtitleLabel);
        }

        header.add(titles, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
    }

    /** The content area — add your own components here. Uses {@link BorderLayout} by default. */
    public JPanel body() {
        return body;
    }

    /** Places a component at the trailing edge of the header (a toggle, a filter, a legend). */
    public void setAccessory(JComponent accessory) {
        if (header == null) {
            throw new IllegalStateException("Card has no header — construct it with a title first");
        }
        header.add(accessory, BorderLayout.EAST);
    }

    public void setTitle(String title) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
    }

    public void setSubtitle(String subtitle) {
        if (subtitleLabel != null) {
            subtitleLabel.setText(subtitle);
            subtitleLabel.setVisible(subtitle != null && !subtitle.isBlank());
        }
    }

    private void applyColors() {
        if (titleLabel != null) {
            titleLabel.setFont(UiTheme.uiFont(Font.BOLD, 14f));
            titleLabel.setForeground(UiTheme.text());
        }
        if (subtitleLabel != null) {
            subtitleLabel.setFont(UiTheme.uiFont(Font.PLAIN, 12f));
            subtitleLabel.setForeground(UiTheme.muted());
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        applyColors();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            g2.setColor(surfaceColor());
            g2.fillRoundRect(0, 0, w - 1, h - 1, RADIUS, RADIUS);

            g2.setColor(borderColor());
            g2.setStroke(borderStroke());
            g2.drawRoundRect(0, 0, w - 1, h - 1, RADIUS, RADIUS);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    /** Subclasses can tint the card — used to highlight the active file slot, for example. */
    protected Color surfaceColor() {
        return UiTheme.surface();
    }

    protected Color borderColor() {
        return UiTheme.border();
    }

    /** Subclasses can swap in a dashed outline — used for the empty drop zones. */
    protected Stroke borderStroke() {
        return new BasicStroke(1f);
    }
}
