package com.talatkarasakal.fork.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatTest {

    // --- percent -------------------------------------------------------------------------

    @Test
    void percent_usesTurkishDecimalComma() {
        assertEquals("47,5%", Format.percent(47.5));
    }

    @Test
    void percent_roundsToOneDecimal() {
        assertEquals("33,3%", Format.percent(100.0 / 3));
    }

    @Test
    void percent_handlesNonFiniteValues() {
        assertEquals("—", Format.percent(Double.POSITIVE_INFINITY));
        assertEquals("—", Format.percent(Double.NaN));
    }

    @Test
    void percent_canExceedOneHundred() {
        // Stations with capacity > 1 legitimately report more than 100%.
        assertEquals("157,1%", Format.percent(157.14));
    }

    // --- seconds -------------------------------------------------------------------------

    @Test
    void seconds_dropsTheDecimalForWholeNumbers() {
        assertEquals("60 sn", Format.seconds(60));
    }

    @Test
    void seconds_keepsOneDecimalOtherwise() {
        assertEquals("47,5 sn", Format.seconds(47.5));
    }

    @Test
    void seconds_handlesNonFiniteValues() {
        assertEquals("—", Format.seconds(Double.NaN));
    }

    // --- span ----------------------------------------------------------------------------

    @Test
    void span_showsSecondsUnderAMinute() {
        assertEquals("45 sn", Format.span(45));
    }

    @Test
    void span_showsMinutesAndSeconds() {
        assertEquals("7 dk", Format.span(420));
        assertEquals("7 dk 30 sn", Format.span(450));
    }

    @Test
    void span_showsHoursForLongRuns() {
        assertEquals("1 sa", Format.span(3600));
        assertEquals("1 sa 12 dk", Format.span(4320));
    }

    @Test
    void span_isZeroForNonPositiveInput() {
        assertEquals("0 sn", Format.span(0));
        assertEquals("0 sn", Format.span(-5));
    }

    // --- count and millis ----------------------------------------------------------------

    @Test
    void count_groupsThousands() {
        assertEquals("1.234", Format.count(1234));
    }

    @Test
    void millis_switchesToSecondsAboveOneThousand() {
        assertEquals("22 ms", Format.millis(22));
        assertEquals("1,50 sn", Format.millis(1500));
    }
}
