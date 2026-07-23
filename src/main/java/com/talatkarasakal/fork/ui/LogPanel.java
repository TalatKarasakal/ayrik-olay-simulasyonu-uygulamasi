package com.talatkarasakal.fork.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The raw simulation narration, with a live line filter. The simulation prints a lot, so being able
 * to type "Job J3" and see only the matching lines is what makes the log usable.
 */
public class LogPanel extends JPanel {

    private final JTextArea area = new JTextArea();
    private final JTextField filterField = new JTextField();
    private final JLabel countLabel = new JLabel();

    private List<String> lines = List.of();

    public LogPanel() {
        super(new BorderLayout(0, 10));
        setOpaque(false);

        area.setEditable(false);
        area.setLineWrap(false);
        area.setFont(UiTheme.monoFont(12f));
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(UiTheme.border()));
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(buildToolbar(), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        applyColors();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel();
        bar.setOpaque(false);
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));

        filterField.putClientProperty("JTextField.placeholderText", "Günlükte ara…");
        filterField.putClientProperty("JTextField.showClearButton", true);
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                render();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                render();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                render();
            }
        });

        JButton copyButton = new JButton("Kopyala");
        copyButton.setIcon(Icons.download(15, UiTheme::muted));
        copyButton.setToolTipText("Görünen günlüğü panoya kopyala");
        copyButton.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(area.getText()), null));

        bar.add(filterField);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(countLabel);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(copyButton);
        return bar;
    }

    /** Replaces the log contents; the current filter stays applied. */
    public void setLog(String log) {
        this.lines = log == null || log.isEmpty() ? List.of() : Arrays.asList(log.split("\\R", -1));
        render();
        area.setCaretPosition(0);
    }

    public void clear() {
        setLog("");
    }

    private void render() {
        String query = filterField.getText().trim().toLowerCase(Locale.ROOT);
        List<String> visible = query.isEmpty()
                ? lines
                : lines.stream().filter(l -> l.toLowerCase(Locale.ROOT).contains(query)).toList();

        area.setText(String.join(System.lineSeparator(), visible));
        area.setCaretPosition(0);

        countLabel.setText(query.isEmpty()
                ? visible.size() + " satır"
                : visible.size() + " / " + lines.size() + " satır");
    }

    private void applyColors() {
        area.setBackground(UiTheme.surfaceRaised());
        area.setForeground(UiTheme.text());
        area.setFont(UiTheme.monoFont(12f));
        countLabel.setFont(UiTheme.uiFont(Font.PLAIN, 11.5f));
        countLabel.setForeground(UiTheme.muted());
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (area != null) {
            applyColors();
        }
    }
}
