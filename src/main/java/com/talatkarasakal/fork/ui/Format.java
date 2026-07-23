package com.talatkarasakal.fork.ui;

import java.text.NumberFormat;
import java.util.Locale;

/** Turkish-locale number formatting shared by the tables, charts and KPI cards. */
public final class Format {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");

    private Format() {
    }

    public static String percent(double value) {
        if (!Double.isFinite(value)) {
            return "—";
        }
        return String.format(TR, "%.1f%%", value);
    }

    public static String count(long value) {
        return NumberFormat.getIntegerInstance(TR).format(value);
    }

    public static String seconds(double value) {
        if (!Double.isFinite(value)) {
            return "—";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(TR);
        nf.setMaximumFractionDigits(value == Math.rint(value) ? 0 : 1);
        return nf.format(value) + " sn";
    }

    /** Seconds rendered as a human-readable clock span, e.g. {@code 1 sa 12 dk} or {@code 45 sn}. */
    public static String span(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "0 sn";
        }
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;

        if (hours > 0) {
            return minutes > 0 ? hours + " sa " + minutes + " dk" : hours + " sa";
        }
        if (minutes > 0) {
            return secs > 0 ? minutes + " dk " + secs + " sn" : minutes + " dk";
        }
        return secs + " sn";
    }

    public static String millis(long value) {
        if (value < 1000) {
            return value + " ms";
        }
        return String.format(TR, "%.2f sn", value / 1000.0);
    }
}
