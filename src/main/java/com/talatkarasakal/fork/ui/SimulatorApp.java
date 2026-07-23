package com.talatkarasakal.fork.ui;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.File;

/** Entry point for the graphical interface. */
public final class SimulatorApp {

    private SimulatorApp() {
    }

    public static void main(String[] args) {
        File first = args.length > 0 ? new File(args[0]) : null;
        File second = args.length > 1 ? new File(args[1]) : null;
        launch(first, second);
    }

    /**
     * Opens the window, optionally with two input files already selected. The files may be given in
     * either order; the window works out which one is the workflow definition.
     *
     * @throws HeadlessLaunchException if there is no display to open a window on
     */
    public static void launch(File first, File second) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessLaunchException(
                    "Grafik arayüz açılamıyor: bu ortamda ekran bulunmuyor. "
                            + "Komut satırı kullanımı: java -jar <jar> <workflow>.txt <job>.txt");
        }

        // Native menu bar and a proper application name on macOS.
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Ayrık Olay Simülasyonu");
        System.setProperty("sun.java2d.uiScale.enabled", "true");

        SwingUtilities.invokeLater(() -> {
            UiTheme.install();
            SimulatorFrame frame = new SimulatorFrame();
            frame.preselect(first, second);
            frame.setVisible(true);
        });
    }

    /** Thrown when {@link #launch(File, File)} is called without a display available. */
    public static class HeadlessLaunchException extends IllegalStateException {
        public HeadlessLaunchException(String message) {
            super(message);
        }
    }
}
