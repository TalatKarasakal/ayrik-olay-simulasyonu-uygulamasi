package com.talatkarasakal.fork.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.prefs.Preferences;

/**
 * Design tokens and look-and-feel setup for the simulator window.
 *
 * <p>Colours are read through the static accessors rather than cached in fields, so a theme switch
 * followed by a repaint is all that is needed to restyle custom-painted components.
 */
public final class UiTheme {

    private static final String PREF_DARK = "ui.dark";
    private static final Preferences PREFS = Preferences.userNodeForPackage(UiTheme.class);

    private static boolean dark = PREFS.getBoolean(PREF_DARK, true);

    /** Categorical palette for charts — readable against both the light and dark surfaces. */
    private static final Color[] SERIES = {
            new Color(0x7C6CFA), new Color(0x22D3EE), new Color(0x34D399), new Color(0xFBBF24),
            new Color(0xF472B6), new Color(0x60A5FA), new Color(0xA78BFA), new Color(0xFB923C),
    };

    private UiTheme() {
    }

    // --- look and feel -------------------------------------------------------------------

    /** Installs the bundled fonts and applies the remembered theme. Call once, on the EDT. */
    public static void install() {
        FlatInterFont.installLazy();
        FlatJetBrainsMonoFont.installLazy();
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#7C6CFA"));
        apply(dark);
    }

    /** Switches the look and feel and refreshes every open window. */
    public static void apply(boolean useDark) {
        dark = useDark;
        PREFS.putBoolean(PREF_DARK, useDark);

        if (useDark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

        UIManager.put("defaultFont", new Font(FlatInterFont.FAMILY, Font.PLAIN, 13));

        // Softer, more contemporary geometry than the FlatLaf defaults.
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 1);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("TabbedPane.tabHeight", 36);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.tabSeparatorsFullHeight", false);
        UIManager.put("TabbedPane.selectedBackground", surface());
        UIManager.put("TabbedPane.underlineColor", accent());
        UIManager.put("TabbedPane.tabInsets", new java.awt.Insets(6, 16, 6, 16));
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("TableHeader.height", 34);
        UIManager.put("Table.gridColor", border());
        UIManager.put("SplitPane.dividerSize", 6);
        UIManager.put("SplitPaneDivider.gripDotCount", 0);
        UIManager.put("MenuBar.borderColor", border());
        UIManager.put("Panel.background", background());
        UIManager.put("ToolTip.background", surfaceRaised());
        UIManager.put("ToolTip.foreground", text());

        FlatLaf.updateUI();
    }

    public static boolean isDark() {
        return dark;
    }

    // --- palette -------------------------------------------------------------------------

    /** Window background — the canvas the cards sit on. */
    public static Color background() {
        return dark ? new Color(0x0F1116) : new Color(0xF4F5F8);
    }

    /** Card background. */
    public static Color surface() {
        return dark ? new Color(0x171A21) : new Color(0xFFFFFF);
    }

    /** Nested surface: table stripes, chart tracks, inactive chips. */
    public static Color surfaceRaised() {
        return dark ? new Color(0x1F232C) : new Color(0xEDEFF4);
    }

    public static Color border() {
        return dark ? new Color(0x2A2F3A) : new Color(0xDDE1E9);
    }

    public static Color text() {
        return dark ? new Color(0xE7E9EE) : new Color(0x12151C);
    }

    /** Secondary text: labels, captions, axis ticks. */
    public static Color muted() {
        return dark ? new Color(0x98A0AE) : new Color(0x5C6675);
    }

    public static Color accent() {
        return dark ? new Color(0x7C6CFA) : new Color(0x5B4BE0);
    }

    public static Color success() {
        return dark ? new Color(0x34D399) : new Color(0x059669);
    }

    public static Color warning() {
        return dark ? new Color(0xFBBF24) : new Color(0xD97706);
    }

    public static Color danger() {
        return dark ? new Color(0xF87171) : new Color(0xDC2626);
    }

    public static Color info() {
        return dark ? new Color(0x22D3EE) : new Color(0x0891B2);
    }

    /** Stable colour for the n-th series in a chart. */
    public static Color series(int index) {
        Color base = SERIES[Math.floorMod(index, SERIES.length)];
        return dark ? base : darken(base, 0.18f);
    }

    // --- fonts ---------------------------------------------------------------------------

    public static Font uiFont(int style, float size) {
        return new Font(FlatInterFont.FAMILY, style, Math.round(size));
    }

    public static Font monoFont(float size) {
        return new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, Math.round(size));
    }

    // --- colour helpers ------------------------------------------------------------------

    public static Color alpha(Color color, float amount) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.round(Math.max(0f, Math.min(1f, amount)) * 255));
    }

    public static Color mix(Color from, Color to, float ratio) {
        float t = Math.max(0f, Math.min(1f, ratio));
        return new Color(
                Math.round(from.getRed() + (to.getRed() - from.getRed()) * t),
                Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t),
                Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t));
    }

    public static Color darken(Color color, float amount) {
        return mix(color, Color.BLACK, amount);
    }

    /**
     * Traffic-light colour for a utilization or load percentage: calm when low, amber when busy,
     * red once a station is effectively saturated.
     */
    public static Color loadColor(double percent) {
        if (!Double.isFinite(percent)) {
            return muted();
        }
        if (percent >= 90) {
            return danger();
        }
        if (percent >= 70) {
            return warning();
        }
        return success();
    }
}
