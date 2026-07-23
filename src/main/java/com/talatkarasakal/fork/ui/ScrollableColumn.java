package com.talatkarasakal.fork.ui;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

/**
 * A vertically scrolling column that is always exactly as wide as its viewport.
 *
 * <p>Without this, a single wide child (a long subtitle, say) grows the whole column's preferred
 * width and the scroll pane clips the rest of the dashboard instead of letting it reflow.</p>
 */
public class ScrollableColumn extends JPanel implements Scrollable {

    public ScrollableColumn(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 18;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
