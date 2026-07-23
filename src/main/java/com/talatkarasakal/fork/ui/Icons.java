package com.talatkarasakal.fork.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

/**
 * Vector icons drawn with Java2D. Keeping them in code means they stay crisp at any scale, follow
 * the current theme colour, and add no image assets to the jar.
 *
 * <p>Each icon is drawn on a 24&times;24 grid and scaled to the requested size.</p>
 */
public final class Icons {

    private static final float GRID = 24f;

    private Icons() {
    }

    @FunctionalInterface
    private interface Drawing {
        void draw(Graphics2D g2);
    }

    private static final class VectorIcon implements Icon {
        private final int size;
        private final Supplier<Color> color;
        private final Drawing drawing;

        VectorIcon(int size, Supplier<Color> color, Drawing drawing) {
            this.size = size;
            this.color = color;
            this.drawing = drawing;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.scale(size / GRID, size / GRID);
                g2.setColor(color.get());
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawing.draw(g2);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private static Icon icon(int size, Supplier<Color> color, Drawing drawing) {
        return new VectorIcon(size, color, drawing);
    }

    private static Path2D.Float path() {
        return new Path2D.Float();
    }

    /** A sheet of paper with a folded corner — the workflow / job file slots. */
    public static Icon document(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            Path2D.Float p = path();
            p.moveTo(6, 3);
            p.lineTo(14, 3);
            p.lineTo(19, 8);
            p.lineTo(19, 21);
            p.lineTo(6, 21);
            p.closePath();
            g2.draw(p);

            Path2D.Float fold = path();
            fold.moveTo(14, 3);
            fold.lineTo(14, 8);
            fold.lineTo(19, 8);
            g2.draw(fold);

            g2.draw(new java.awt.geom.Line2D.Float(9, 12.5f, 16, 12.5f));
            g2.draw(new java.awt.geom.Line2D.Float(9, 16.5f, 16, 16.5f));
        });
    }

    /** Stacked rows — the job list. */
    public static Icon rows(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            g2.draw(new RoundRectangle2D.Float(3.5f, 4.5f, 17, 15, 3, 3));
            g2.draw(new java.awt.geom.Line2D.Float(3.5f, 9.5f, 20.5f, 9.5f));
            g2.draw(new java.awt.geom.Line2D.Float(3.5f, 14.5f, 20.5f, 14.5f));
        });
    }

    /** Bar chart — the dashboard and station views. */
    public static Icon chart(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            g2.draw(new java.awt.geom.Line2D.Float(4, 20, 20, 20));
            g2.fill(new RoundRectangle2D.Float(6, 12, 3.6f, 6, 1.6f, 1.6f));
            g2.fill(new RoundRectangle2D.Float(11.2f, 7, 3.6f, 11, 1.6f, 1.6f));
            g2.fill(new RoundRectangle2D.Float(16.4f, 14.5f, 3.6f, 3.5f, 1.6f, 1.6f));
        });
    }

    /** Stacked units — the station list. */
    public static Icon stack(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            g2.draw(new RoundRectangle2D.Float(3.5f, 4f, 17, 6, 2.5f, 2.5f));
            g2.draw(new RoundRectangle2D.Float(3.5f, 14f, 17, 6, 2.5f, 2.5f));
            g2.fill(new Ellipse2D.Float(6.2f, 6.2f, 1.8f, 1.8f));
            g2.fill(new Ellipse2D.Float(6.2f, 16.2f, 1.8f, 1.8f));
        });
    }

    /** Horizontal spans on a track — the timeline. */
    public static Icon timeline(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            g2.fill(new RoundRectangle2D.Float(3, 5, 11, 3.6f, 1.8f, 1.8f));
            g2.fill(new RoundRectangle2D.Float(7, 10.2f, 13, 3.6f, 1.8f, 1.8f));
            g2.fill(new RoundRectangle2D.Float(5, 15.4f, 8, 3.6f, 1.8f, 1.8f));
        });
    }

    /** Terminal prompt — the event log. */
    public static Icon terminal(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            g2.draw(new RoundRectangle2D.Float(3.5f, 4.5f, 17, 15, 3, 3));
            Path2D.Float caret = path();
            caret.moveTo(7.5f, 10);
            caret.lineTo(10.5f, 12.5f);
            caret.lineTo(7.5f, 15);
            g2.draw(caret);
            g2.draw(new java.awt.geom.Line2D.Float(12.5f, 15, 16.5f, 15));
        });
    }

    /** Filled triangle — run the simulation. */
    public static Icon play(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            Path2D.Float p = path();
            p.moveTo(8, 5.5f);
            p.lineTo(18.5f, 12);
            p.lineTo(8, 18.5f);
            p.closePath();
            g2.fill(p);
        });
    }

    /** Open folder — browse for a file. */
    public static Icon folder(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            Path2D.Float p = path();
            p.moveTo(3.5f, 18.5f);
            p.lineTo(3.5f, 6);
            p.lineTo(9.5f, 6);
            p.lineTo(11.5f, 8.5f);
            p.lineTo(20.5f, 8.5f);
            p.lineTo(20.5f, 18.5f);
            p.closePath();
            g2.draw(p);
        });
    }

    /** Circled cross — clear the current selection. */
    public static Icon clear(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            g2.draw(new Ellipse2D.Float(4.5f, 4.5f, 15, 15));
            g2.draw(new java.awt.geom.Line2D.Float(9.5f, 9.5f, 14.5f, 14.5f));
            g2.draw(new java.awt.geom.Line2D.Float(14.5f, 9.5f, 9.5f, 14.5f));
        });
    }

    /** Sun — switch to the light theme. */
    public static Icon sun(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            g2.draw(new Ellipse2D.Float(8.5f, 8.5f, 7, 7));
            for (int i = 0; i < 8; i++) {
                double angle = Math.PI / 4 * i;
                float cx = 12f;
                float cy = 12f;
                float inner = 7.2f;
                float outer = 9.6f;
                g2.draw(new java.awt.geom.Line2D.Float(
                        cx + (float) Math.cos(angle) * inner, cy + (float) Math.sin(angle) * inner,
                        cx + (float) Math.cos(angle) * outer, cy + (float) Math.sin(angle) * outer));
            }
        });
    }

    /** Crescent — switch to the dark theme. */
    public static Icon moon(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            Path2D.Float p = path();
            p.moveTo(19, 14.5f);
            p.curveTo(17.6f, 15.2f, 16, 15.4f, 14.4f, 15f);
            p.curveTo(10.8f, 14.1f, 8.6f, 10.5f, 9.5f, 6.9f);
            p.curveTo(9.7f, 6.2f, 9.9f, 5.6f, 10.3f, 5f);
            p.curveTo(6.6f, 5.9f, 4.2f, 9.5f, 5f, 13.2f);
            p.curveTo(5.9f, 17f, 9.7f, 19.4f, 13.5f, 18.5f);
            p.curveTo(16.1f, 17.9f, 18.1f, 16.5f, 19, 14.5f);
            p.closePath();
            g2.fill(p);
        });
    }

    /** Tray with a down arrow — export the report. */
    public static Icon download(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            g2.draw(new java.awt.geom.Line2D.Float(12, 4, 12, 14.5f));
            Path2D.Float head = path();
            head.moveTo(8, 10.8f);
            head.lineTo(12, 14.8f);
            head.lineTo(16, 10.8f);
            g2.draw(head);
            Path2D.Float tray = path();
            tray.moveTo(5, 17);
            tray.lineTo(5, 19.5f);
            tray.lineTo(19, 19.5f);
            tray.lineTo(19, 17);
            g2.draw(tray);
        });
    }

    /** Sliders — the sample data / settings affordance. */
    public static Icon sparkles(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            Path2D.Float big = path();
            big.moveTo(9.5f, 3.5f);
            big.lineTo(11.2f, 8.3f);
            big.lineTo(16, 10);
            big.lineTo(11.2f, 11.7f);
            big.lineTo(9.5f, 16.5f);
            big.lineTo(7.8f, 11.7f);
            big.lineTo(3, 10);
            big.lineTo(7.8f, 8.3f);
            big.closePath();
            g2.fill(big);

            Path2D.Float small = path();
            small.moveTo(17.5f, 13.5f);
            small.lineTo(18.5f, 16.5f);
            small.lineTo(21.5f, 17.5f);
            small.lineTo(18.5f, 18.5f);
            small.lineTo(17.5f, 21.5f);
            small.lineTo(16.5f, 18.5f);
            small.lineTo(13.5f, 17.5f);
            small.lineTo(16.5f, 16.5f);
            small.closePath();
            g2.fill(small);
        });
    }

    /** Exclamation in a triangle — the warnings banner. */
    public static Icon warning(int size, Supplier<Color> color) {
        return icon(size, color, g2 -> {
            Path2D.Float p = path();
            p.moveTo(12, 4);
            p.lineTo(21, 19.5f);
            p.lineTo(3, 19.5f);
            p.closePath();
            g2.draw(p);
            g2.draw(new java.awt.geom.Line2D.Float(12, 10, 12, 14));
            g2.fill(new Ellipse2D.Float(11.2f, 15.8f, 1.6f, 1.6f));
        });
    }
}
