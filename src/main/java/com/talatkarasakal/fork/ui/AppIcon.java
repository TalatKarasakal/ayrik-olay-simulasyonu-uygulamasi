package com.talatkarasakal.fork.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The window / dock icon, drawn at runtime rather than shipped as image files: three stacked
 * timeline bars on a rounded accent tile.
 */
public final class AppIcon {

    private static final int[] SIZES = {16, 32, 64, 128, 256};

    private AppIcon() {
    }

    public static List<Image> images() {
        return java.util.Arrays.stream(SIZES).mapToObj(AppIcon::render).map(Image.class::cast).toList();
    }

    static BufferedImage render(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.scale(size / 64.0, size / 64.0);

            g2.setColor(new Color(0x7C6CFA));
            g2.fillRoundRect(2, 2, 60, 60, 18, 18);

            g2.setColor(new Color(0xFFFFFF));
            g2.fillRoundRect(12, 17, 30, 8, 4, 4);
            g2.fillRoundRect(20, 29, 28, 8, 4, 4);
            g2.fillRoundRect(15, 41, 21, 8, 4, 4);
        } finally {
            g2.dispose();
        }
        return image;
    }
}
