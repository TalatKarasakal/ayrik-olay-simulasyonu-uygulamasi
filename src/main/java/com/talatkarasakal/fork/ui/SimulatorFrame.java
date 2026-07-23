package com.talatkarasakal.fork.ui;

import com.talatkarasakal.fork.JobResult;
import com.talatkarasakal.fork.SimulationResult;
import com.talatkarasakal.fork.SimulationRunner;
import com.talatkarasakal.fork.StationResult;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * The application window: pick a workflow file and a job file, run the discrete event simulation,
 * and explore the outcome across a dashboard, two result tables, a timeline and the raw log.
 */
public class SimulatorFrame extends JFrame {

    private final FileSelectCard workflowCard =
            new FileSelectCard("Workflow Dosyası", "Workflow dosyası seçilmedi");
    private final FileSelectCard jobCard =
            new FileSelectCard("İş (Job) Dosyası", "İş dosyası seçilmedi");

    private final JButton runButton = new JButton("Simülasyonu Çalıştır");
    private final JButton sampleButton = new JButton("Örnek veriyi yükle");
    private final JButton exportButton = new JButton("Raporu dışa aktar");
    private final JProgressBar progress = new JProgressBar();

    private final KpiCard totalJobsKpi = new KpiCard("Toplam İş");
    private final KpiCard completedKpi = new KpiCard("Tamamlanan");
    private final KpiCard lateKpi = new KpiCard("Geciken");
    private final KpiCard endTimeKpi = new KpiCard("Simülasyon Süresi");
    private final KpiCard utilizationKpi = new KpiCard("Ort. İstasyon Kullanımı");

    private final MetricBars utilizationBars = new MetricBars();
    private final MetricBars tardinessBars = new MetricBars();
    private final JLabel insightLabel = new JLabel();

    private final JobsTableModel jobsModel = new JobsTableModel();
    private final StationsTableModel stationsModel = new StationsTableModel();
    private final JTable jobsTable = new JTable(jobsModel);
    private final JTable stationsTable = new JTable(stationsModel);

    private final TimelineChart timeline = new TimelineChart();
    private final JPanel timelineLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));

    private final LogPanel logPanel = new LogPanel();
    private final JTabbedPane tabs = new JTabbedPane();

    private final WarningBanner warningBanner = new WarningBanner();
    private final JLabel statusLabel = new JLabel("Başlamak için iki girdi dosyası seçin.");
    private final JLabel statusDetailLabel = new JLabel();
    private final JButton themeButton = new JButton();

    private SimulationResult lastResult;

    public SimulatorFrame() {
        super("Ayrık Olay Simülasyonu — Enhanced Fork");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1040, 700));
        setSize(new Dimension(1240, 840));
        setLocationRelativeTo(null);
        setIconImages(AppIcon.images());

        setJMenuBar(buildMenuBar());

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));
        root.setBackground(UiTheme.background());

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);

        workflowCard.setOnChange(f -> onFilesChanged());
        jobCard.setOnChange(f -> onFilesChanged());
        onFilesChanged();
        applyColors();
    }

    // --- layout --------------------------------------------------------------------------

    private JMenuBar buildMenuBar() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenu fileMenu = new JMenu("Dosya");
        JMenuItem runItem = new JMenuItem("Simülasyonu Çalıştır");
        runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));
        runItem.addActionListener(e -> runSimulation());
        JMenuItem sampleItem = new JMenuItem("Örnek veriyi yükle");
        sampleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcut));
        sampleItem.addActionListener(e -> loadSampleData());
        JMenuItem exportItem = new JMenuItem("Raporu dışa aktar…");
        exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
        exportItem.addActionListener(e -> exportReport());
        JMenuItem quitItem = new JMenuItem("Çıkış");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcut));
        quitItem.addActionListener(e -> dispose());
        fileMenu.add(runItem);
        fileMenu.add(sampleItem);
        fileMenu.addSeparator();
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);

        JMenu viewMenu = new JMenu("Görünüm");
        JMenuItem themeItem = new JMenuItem("Temayı değiştir");
        themeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                shortcut | InputEvent.SHIFT_DOWN_MASK));
        themeItem.addActionListener(e -> toggleTheme());
        viewMenu.add(themeItem);

        JMenu helpMenu = new JMenu("Yardım");
        JMenuItem aboutItem = new JMenuItem("Hakkında");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        JMenuBar bar = new JMenuBar();
        bar.add(fileMenu);
        bar.add(viewMenu);
        bar.add(helpMenu);
        return bar;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Ayrık Olay Simülasyonu");
        title.setFont(UiTheme.uiFont(Font.BOLD, 21f));
        title.setForeground(UiTheme.text());
        titleRow.add(title);
        titleRow.add(new Pill("Enhanced Fork", UiTheme::accent));

        JLabel subtitle = new JLabel("İş akışlarını simüle edin, darboğazları ve gecikmeleri görün.");
        subtitle.setFont(UiTheme.uiFont(Font.PLAIN, 12.5f));
        subtitle.setForeground(UiTheme.muted());
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));

        titles.add(titleRow);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        themeButton.setToolTipText("Açık / koyu temayı değiştir");
        themeButton.addActionListener(e -> toggleTheme());
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        headerActions.add(themeButton);
        header.add(headerActions, BorderLayout.EAST);

        return header;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel inputs = new JPanel(new GridLayout(1, 2, 14, 0));
        inputs.setOpaque(false);
        inputs.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputs.add(workflowCard);
        inputs.add(jobCard);
        top.add(inputs);

        top.add(Box.createVerticalStrut(12));
        top.add(buildActionRow());

        warningBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        warningBanner.setVisible(false);
        top.add(Box.createVerticalStrut(12));
        top.add(warningBanner);

        center.add(top, BorderLayout.NORTH);
        center.add(buildTabs(), BorderLayout.CENTER);
        return center;
    }

    private JComponent buildActionRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        sampleButton.setIcon(Icons.sparkles(16, UiTheme::accent));
        sampleButton.setToolTipText("Uygulamayla gelen örnek workflow ve job dosyalarını yükle");
        sampleButton.addActionListener(e -> loadSampleData());

        exportButton.setIcon(Icons.download(16, UiTheme::muted));
        exportButton.setToolTipText("Sonuç özetini metin dosyası olarak kaydet");
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> exportReport());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(sampleButton);
        left.add(exportButton);
        row.add(left, BorderLayout.WEST);

        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.setPreferredSize(new Dimension(160, 6));
        progress.setBorderPainted(false);

        runButton.setIcon(Icons.play(15, () -> Color.WHITE));
        runButton.setFont(UiTheme.uiFont(Font.BOLD, 13f));
        runButton.addActionListener(e -> runSimulation());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(progress);
        right.add(runButton);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private JComponent buildTabs() {
        tabs.addTab("Özet", Icons.chart(16, UiTheme::muted), buildDashboardTab());
        tabs.addTab("İşler", Icons.rows(16, UiTheme::muted), buildJobsTab());
        tabs.addTab("İstasyonlar", Icons.stack(16, UiTheme::muted), buildStationsTab());
        tabs.addTab("Zaman Çizelgesi", Icons.timeline(16, UiTheme::muted), buildTimelineTab());
        tabs.addTab("Günlük", Icons.terminal(16, UiTheme::muted), buildLogTab());
        return tabs;
    }

    private JComponent buildDashboardTab() {
        ScrollableColumn content = new ScrollableColumn(null);
        content.setBorder(BorderFactory.createEmptyBorder(14, 2, 2, 2));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel kpis = new JPanel(new GridLayout(1, 5, 12, 0));
        kpis.setOpaque(false);
        kpis.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        totalJobsKpi.setAccent(UiTheme::accent);
        completedKpi.setAccent(UiTheme::success);
        lateKpi.setAccent(UiTheme::warning);
        endTimeKpi.setAccent(UiTheme::info);
        utilizationKpi.setAccent(UiTheme::accent);
        kpis.add(totalJobsKpi);
        kpis.add(completedKpi);
        kpis.add(lateKpi);
        kpis.add(endTimeKpi);
        kpis.add(utilizationKpi);
        content.add(kpis);

        content.add(Box.createVerticalStrut(14));

        Card utilizationCard = new Card("İstasyon Kullanımı",
                "Meşgul süre / simülasyon süresi — kapasite > 1 ise %100'ü aşabilir");
        utilizationBars.setMax(100);
        utilizationBars.setEmptyMessage("Henüz istasyon verisi yok");
        utilizationCard.body().add(utilizationBars, BorderLayout.CENTER);

        Card tardinessCard = new Card("İş Tipine Göre Ortalama Gecikme",
                "Yalnızca son teslim tarihini aşan işler hesaba katılır");
        tardinessBars.setEmptyMessage("Geciken iş yok 🎉");
        tardinessCard.body().add(tardinessBars, BorderLayout.CENTER);

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0));
        charts.setOpaque(false);
        charts.setAlignmentX(Component.LEFT_ALIGNMENT);
        charts.add(utilizationCard);
        charts.add(tardinessCard);
        content.add(charts);

        content.add(Box.createVerticalStrut(14));

        Card insightCard = new Card("Analiz");
        insightLabel.setVerticalAlignment(JLabel.TOP);
        insightCard.body().add(insightLabel, BorderLayout.CENTER);
        insightCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        insightCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        content.add(insightCard);

        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JComponent buildJobsTab() {
        configureJobsTable();
        Card card = new Card("İşler", "Sütun başlıklarına tıklayarak sıralayabilirsiniz");
        card.body().add(Tables.wrap(jobsTable), BorderLayout.CENTER);
        return pad(card);
    }

    private JComponent buildStationsTab() {
        configureStationsTable();
        Card card = new Card("İstasyonlar", "Kapasite, çizelgeleme politikası ve yük dağılımı");
        card.body().add(Tables.wrap(stationsTable), BorderLayout.CENTER);
        return pad(card);
    }

    private JComponent buildTimelineTab() {
        Card card = new Card("Zaman Çizelgesi",
                "Her çubuk bir işin serbest bırakılmasından tamamlanmasına kadar geçen süredir; "
                        + "ince dikey çizgi son teslim anıdır");

        JScrollPane scroll = new JScrollPane(timeline);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        timelineLegend.setOpaque(false);
        card.body().add(scroll, BorderLayout.CENTER);
        card.body().add(timelineLegend, BorderLayout.SOUTH);
        return pad(card);
    }

    private JComponent buildLogTab() {
        Card card = new Card("Simülasyon Günlüğü",
                "Simülasyon motorunun ürettiği ham olay akışı");
        card.body().add(logPanel, BorderLayout.CENTER);
        return pad(card);
    }

    private JComponent pad(JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 2, 2, 2));
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 4));
        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(statusDetailLabel, BorderLayout.EAST);
        return bar;
    }

    private void configureJobsTable() {
        TableColumnModel columns = jobsTable.getColumnModel();
        columns.getColumn(0).setCellRenderer(Tables.textRenderer(true));
        columns.getColumn(1).setCellRenderer(Tables.textRenderer(false));
        for (int i = 2; i <= 7; i++) {
            columns.getColumn(i).setCellRenderer(Tables.numberRenderer());
        }
        columns.getColumn(8).setCellRenderer(Tables.statusRenderer());
        columns.getColumn(0).setPreferredWidth(90);
        columns.getColumn(1).setPreferredWidth(80);
        columns.getColumn(8).setPreferredWidth(130);
    }

    private void configureStationsTable() {
        TableColumnModel columns = stationsTable.getColumnModel();
        columns.getColumn(0).setCellRenderer(Tables.textRenderer(true));
        columns.getColumn(1).setCellRenderer(Tables.numberRenderer());
        columns.getColumn(2).setCellRenderer(Tables.policyRenderer());
        columns.getColumn(3).setCellRenderer(Tables.flagRenderer("Evet", "Hayır"));
        columns.getColumn(4).setCellRenderer(Tables.textRenderer(false));
        columns.getColumn(5).setCellRenderer(Tables.numberRenderer());
        columns.getColumn(6).setCellRenderer(Tables.utilizationRenderer());
        columns.getColumn(0).setPreferredWidth(110);
        columns.getColumn(4).setPreferredWidth(180);
        columns.getColumn(6).setPreferredWidth(200);
    }

    // --- actions -------------------------------------------------------------------------

    /**
     * Fills the two input slots from files supplied on the command line. The order does not matter:
     * whichever file parses as a workflow definition goes into the workflow slot. Files that do not
     * exist are ignored.
     */
    public void preselect(File first, File second) {
        List<File> files = new ArrayList<>();
        if (first != null && first.isFile()) {
            files.add(first);
        }
        if (second != null && second.isFile()) {
            files.add(second);
        }
        if (files.isEmpty()) {
            return;
        }

        File workflow = null;
        File job = null;
        for (File file : files) {
            boolean isWorkflow;
            try {
                isWorkflow = SimulationRunner.isWorkflowFile(file);
            } catch (IOException e) {
                continue;
            }
            if (isWorkflow && workflow == null) {
                workflow = file;
            } else if (job == null) {
                job = file;
            }
        }

        if (workflow != null) {
            workflowCard.setFile(workflow);
        }
        if (job != null) {
            jobCard.setFile(job);
        }
    }

    private void onFilesChanged() {
        boolean ready = workflowCard.getFile() != null && jobCard.getFile() != null;
        runButton.setEnabled(ready);
        if (ready) {
            setStatus("Hazır — simülasyonu çalıştırabilirsiniz.", UiTheme.text());
        } else {
            setStatus("Başlamak için iki girdi dosyası seçin.", UiTheme.muted());
        }
    }

    private void runSimulation() {
        File workflowFile = workflowCard.getFile();
        File jobFile = jobCard.getFile();
        if (workflowFile == null || jobFile == null) {
            return;
        }

        runButton.setEnabled(false);
        sampleButton.setEnabled(false);
        progress.setVisible(true);
        setStatus("Simülasyon çalışıyor…", UiTheme.accent());

        new SwingWorker<SimulationResult, Void>() {
            @Override
            protected SimulationResult doInBackground() throws Exception {
                return SimulationRunner.run(workflowFile, jobFile);
            }

            @Override
            protected void done() {
                progress.setVisible(false);
                sampleButton.setEnabled(true);
                runButton.setEnabled(true);
                try {
                    showResult(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    showError(e.getCause() == null ? e : e.getCause());
                }
            }
        }.execute();
    }

    private void showResult(SimulationResult result) {
        this.lastResult = result;
        exportButton.setEnabled(true);

        totalJobsKpi.setValue(Format.count(result.jobs().size()));
        totalJobsKpi.setHint(result.stations().size() + " istasyon üzerinde");

        int completed = result.completedJobCount();
        completedKpi.setValue(Format.count(completed));
        completedKpi.setHint(result.jobs().isEmpty() ? null
                : Format.percent(100.0 * completed / result.jobs().size()) + " tamamlanma");

        int late = result.lateJobCount();
        lateKpi.setValue(Format.count(late));
        lateKpi.setAccent(late == 0 ? UiTheme::success : UiTheme::warning);
        lateKpi.setHint(late == 0 ? "Tüm işler zamanında"
                : "Ort. " + Format.seconds(result.overallAverageTardiness()) + " gecikme");

        endTimeKpi.setValue(Format.span(result.simulationEndTime()));
        endTimeKpi.setHint(Format.count(result.simulationEndTime()) + " sn · "
                + Format.millis(result.durationMillis()) + " hesaplama");

        double avgUtilization = result.averageUtilization();
        utilizationKpi.setValue(Format.percent(avgUtilization));
        utilizationKpi.setAccent(() -> UiTheme.loadColor(avgUtilization));
        StationResult busiest = result.busiestStation();
        utilizationKpi.setHint(busiest == null ? null
                : "En yoğun: " + busiest.stationId() + " (" + Format.percent(busiest.utilization()) + ")");

        utilizationBars.setBars(result.stations().stream()
                .map(s -> new MetricBars.Bar(
                        s.stationId(),
                        Double.isFinite(s.utilization()) ? s.utilization() : 0,
                        Format.percent(s.utilization()),
                        () -> UiTheme.loadColor(s.utilization()),
                        String.format("<html><b>%s</b><br>Meşgul süre: %s<br>Kapasite: %d · %s</html>",
                                s.stationId(), Format.seconds(s.totalProcessTime()),
                                s.maxCapacity(), s.schedulingPolicy())))
                .toList());

        List<MetricBars.Bar> tardinessData = new ArrayList<>();
        int colorIndex = 0;
        for (Map.Entry<String, Double> entry : result.tardinessByType().entrySet()) {
            final int index = colorIndex++;
            tardinessData.add(new MetricBars.Bar(
                    entry.getKey(),
                    entry.getValue(),
                    Format.seconds(entry.getValue()),
                    () -> UiTheme.series(index),
                    "<html><b>" + entry.getKey() + "</b><br>Ortalama gecikme: "
                            + Format.seconds(entry.getValue()) + "</html>"));
        }
        tardinessBars.setMax(0);
        tardinessBars.setBars(tardinessData);

        jobsModel.setRows(result.jobs());
        stationsModel.setRows(result.stations());

        timeline.setJobs(result.jobs(), result.simulationEndTime());
        rebuildTimelineLegend();

        logPanel.setLog(result.log());
        insightLabel.setText(buildInsightHtml(result));
        warningBanner.setWarnings(result.warnings());

        tabs.setSelectedIndex(0);
        setStatus("Simülasyon tamamlandı.", UiTheme.success());
        statusDetailLabel.setText(String.format("%d iş · %d istasyon · %s",
                result.jobs().size(), result.stations().size(), Format.millis(result.durationMillis())));
    }

    private String buildInsightHtml(SimulationResult result) {
        List<String> points = new ArrayList<>();

        int total = result.jobs().size();
        int completed = result.completedJobCount();
        if (total == 0) {
            points.add("Çalıştırılacak iş bulunamadı — iş dosyasını kontrol edin.");
        } else if (completed < total) {
            points.add((total - completed) + " iş tamamlanamadı. Bu işlerin ihtiyaç duyduğu görev "
                    + "tipini işleyebilen bir istasyon tanımlı olmayabilir.");
        } else {
            points.add("Tüm işler (" + total + ") başarıyla tamamlandı.");
        }

        int late = result.lateJobCount();
        if (late > 0) {
            points.add(late + " iş son teslim tarihini aştı; ortalama gecikme "
                    + Format.seconds(result.overallAverageTardiness()) + ".");
        } else if (completed > 0) {
            points.add("Hiçbir iş son teslim tarihini aşmadı.");
        }

        StationResult busiest = result.busiestStation();
        if (busiest != null && Double.isFinite(busiest.utilization())) {
            if (busiest.utilization() > 100) {
                points.add("<b>" + busiest.stationId() + "</b> istasyonunun toplam meşgul süresi "
                        + "simülasyon süresinin " + Format.percent(busiest.utilization())
                        + " kadarı — yani " + busiest.maxCapacity() + " kanalını da neredeyse sürekli "
                        + "kullandı. Sistemin darboğazı burası; aynı görev tipini işleyen ikinci bir "
                        + "istasyon eklemek toplam süreyi kısaltır.");
            } else if (busiest.utilization() >= 70) {
                points.add("<b>" + busiest.stationId() + "</b> istasyonu "
                        + Format.percent(busiest.utilization())
                        + " doluluğa ulaştı — sistemin darboğazı burası. Kapasitesini artırmak "
                        + "veya aynı görev tipini işleyen ikinci bir istasyon eklemek toplam süreyi kısaltır.");
            } else {
                points.add("En yoğun istasyon <b>" + busiest.stationId() + "</b> ("
                        + Format.percent(busiest.utilization())
                        + "); istasyonlarda belirgin bir darboğaz görünmüyor.");
            }
        }

        StringBuilder html = new StringBuilder("<html><body style='width:520px'>");
        for (String point : points) {
            html.append("<p style='margin:0 0 8px 0'>• ").append(point).append("</p>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    private void rebuildTimelineLegend() {
        timelineLegend.removeAll();
        Map<String, Color> legend = timeline.legend();
        if (!legend.isEmpty()) {
            JLabel caption = new JLabel("İş tipi:");
            caption.setFont(UiTheme.uiFont(Font.PLAIN, 11.5f));
            caption.setForeground(UiTheme.muted());
            timelineLegend.add(caption);
            legend.forEach((type, color) -> timelineLegend.add(new Pill(type, () -> color)));
        }
        timelineLegend.revalidate();
        timelineLegend.repaint();
    }

    private void loadSampleData() {
        try {
            File workflow = SampleData.extract("sample_workflow.txt");
            File job = SampleData.extract("sample_job.txt");
            workflowCard.setFile(workflow);
            jobCard.setFile(job);
            setStatus("Örnek veri yüklendi — simülasyonu çalıştırabilirsiniz.", UiTheme.text());
        } catch (IOException | UncheckedIOException e) {
            showError(e);
        }
    }

    private void exportReport() {
        if (lastResult == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Raporu kaydet");
        chooser.setSelectedFile(new File("simulasyon-raporu.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(chooser.getSelectedFile().toPath(),
                    ReportWriter.toText(lastResult), StandardCharsets.UTF_8);
            setStatus("Rapor kaydedildi: " + chooser.getSelectedFile().getName(), UiTheme.success());
        } catch (IOException e) {
            showError(e);
        }
    }

    private void toggleTheme() {
        UiTheme.apply(!UiTheme.isDark());
        getContentPane().setBackground(UiTheme.background());
        applyColors();
        repaint();
    }

    private void applyColors() {
        themeButton.setIcon(UiTheme.isDark()
                ? Icons.sun(17, UiTheme::muted)
                : Icons.moon(17, UiTheme::muted));
        runButton.setBackground(UiTheme.accent());
        runButton.setForeground(Color.WHITE);
        runButton.setIcon(Icons.play(15, () -> Color.WHITE));
        sampleButton.setIcon(Icons.sparkles(16, UiTheme::accent));
        exportButton.setIcon(Icons.download(16, UiTheme::muted));

        statusLabel.setFont(UiTheme.uiFont(Font.PLAIN, 12f));
        statusDetailLabel.setFont(UiTheme.uiFont(Font.PLAIN, 12f));
        statusDetailLabel.setForeground(UiTheme.muted());
        insightLabel.setFont(UiTheme.uiFont(Font.PLAIN, 12.5f));
        insightLabel.setForeground(UiTheme.text());

        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setIconAt(i, tabIcon(i));
        }
        rebuildTimelineLegend();
    }

    private javax.swing.Icon tabIcon(int index) {
        return switch (index) {
            case 0 -> Icons.chart(16, UiTheme::muted);
            case 1 -> Icons.rows(16, UiTheme::muted);
            case 2 -> Icons.stack(16, UiTheme::muted);
            case 3 -> Icons.timeline(16, UiTheme::muted);
            default -> Icons.terminal(16, UiTheme::muted);
        };
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void showError(Throwable error) {
        String message = error.getMessage() == null ? error.toString() : error.getMessage();
        setStatus("Simülasyon başarısız.", UiTheme.danger());
        statusDetailLabel.setText("");
        JOptionPane.showMessageDialog(this, message, "Simülasyon hatası", JOptionPane.ERROR_MESSAGE);
    }

    private void showAbout() {
        String html = "<html><body style='width:360px;font-family:sans-serif'>"
                + "<h3 style='margin:0 0 6px 0'>Ayrık Olay Simülasyonu</h3>"
                + "<p style='margin:0 0 10px 0'>Enhanced personal fork — modern Swing arayüzü.</p>"
                + "<p style='margin:0 0 6px 0'>Simülasyon çekirdeği SE116 grup projesine aittir; "
                + "bu fork Maven derlemesi, JUnit 5 testleri, CI ve bu arayüzü ekler.</p>"
                + "<p style='margin:0'>Java " + System.getProperty("java.version") + "</p>"
                + "</body></html>";
        JOptionPane.showMessageDialog(this, html, "Hakkında", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Small rounded label used for the header badge and the timeline legend. */
    private static final class Pill extends JLabel {

        private final java.util.function.Supplier<Color> color;

        Pill(String text, java.util.function.Supplier<Color> color) {
            super(text);
            this.color = color;
            setOpaque(false);
            setFont(UiTheme.uiFont(Font.BOLD, 10.5f));
            setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiTheme.alpha(color.get(), 0.18f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(UiTheme.alpha(color.get(), 0.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            } finally {
                g2.dispose();
            }
            setForeground(color.get());
            super.paintComponent(g);
        }
    }
}
