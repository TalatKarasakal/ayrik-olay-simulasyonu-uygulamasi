package com.talatkarasakal.fork.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * An amber strip listing the problems the parsers reported. On the command line these messages
 * terminate the run; here they are shown without interrupting the user, because a workflow file
 * with one bad line still produces a useful simulation.
 */
public class WarningBanner extends JPanel {

    private static final int MAX_SHOWN = 6;
    private static final int RADIUS = 12;

    private final JLabel icon = new JLabel();
    private final JLabel message = new JLabel();

    public WarningBanner() {
        super(new BorderLayout(12, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        icon.setVerticalAlignment(JLabel.TOP);
        add(icon, BorderLayout.WEST);
        add(message, BorderLayout.CENTER);
        applyColors();
    }

    /** Shows the banner when there is something to report, hides it otherwise. */
    public void setWarnings(List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            setVisible(false);
            return;
        }

        StringBuilder html = new StringBuilder("<html><body>");
        html.append("<b>Ayrıştırma uyarıları (").append(warnings.size()).append(")</b>");
        warnings.stream().limit(MAX_SHOWN).forEach(w ->
                html.append("<div style='margin-top:4px'>• ").append(escape(w)).append("</div>"));
        if (warnings.size() > MAX_SHOWN) {
            html.append("<div style='margin-top:4px'>… ve ")
                    .append(warnings.size() - MAX_SHOWN)
                    .append(" uyarı daha. Tamamı için Günlük sekmesine bakın.</div>");
        }
        html.append("</body></html>");

        message.setText(html.toString());
        setVisible(true);
        revalidate();
        repaint();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", " ");
    }

    private void applyColors() {
        icon.setIcon(Icons.warning(20, UiTheme::warning));
        message.setFont(UiTheme.uiFont(Font.PLAIN, 12f));
        message.setForeground(UiTheme.text());
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (message != null) {
            applyColors();
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color warning = UiTheme.warning();
            g2.setColor(UiTheme.mix(UiTheme.surface(), warning, 0.12f));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
            g2.setColor(UiTheme.alpha(warning, 0.45f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
