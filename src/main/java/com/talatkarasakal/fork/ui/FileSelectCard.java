package com.talatkarasakal.fork.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * One of the two input slots. Shows an empty dashed drop zone until a file is chosen, then switches
 * to a filled state showing the file name and its folder. Files can be dropped onto the card,
 * picked with the Browse button, or chosen by clicking anywhere on the card.
 */
public class FileSelectCard extends Card {

    private final JLabel icon = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel pathLabel = new JLabel();
    private final JButton browseButton = new JButton();
    private final JButton clearButton = new JButton();

    private final String emptyHint;
    private File file;
    private boolean dragOver;
    private Consumer<File> onChange = f -> {
    };

    public FileSelectCard(String title, String emptyHint) {
        super(title);
        this.emptyHint = emptyHint;
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        icon.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        row.add(icon, BorderLayout.WEST);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(nameLabel);
        texts.add(Box.createVerticalStrut(2));
        texts.add(pathLabel);
        row.add(texts, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        browseButton.setToolTipText("Dosya seç");
        browseButton.addActionListener(e -> chooseFile());
        clearButton.setToolTipText("Seçimi temizle");
        clearButton.addActionListener(e -> setFile(null));
        actions.add(browseButton);
        actions.add(Box.createHorizontalStrut(4));
        actions.add(clearButton);
        row.add(actions, BorderLayout.EAST);

        body().add(row, BorderLayout.CENTER);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseFile();
            }
        });
        installDropTarget();
        refresh();
    }

    // --- state ---------------------------------------------------------------------------

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        refresh();
        onChange.accept(file);
    }

    public void setOnChange(Consumer<File> onChange) {
        this.onChange = onChange == null ? f -> {
        } : onChange;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Dosya seç");
        chooser.setFileFilter(new FileNameExtensionFilter("Metin dosyaları (*.txt)", "txt"));
        chooser.setAcceptAllFileFilterUsed(true);
        if (file != null && file.getParentFile() != null) {
            chooser.setCurrentDirectory(file.getParentFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setFile(chooser.getSelectedFile());
        }
    }

    // --- drag and drop -------------------------------------------------------------------

    private void installDropTarget() {
        setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent e) {
                if (acceptable(e)) {
                    dragOver = true;
                    repaint();
                    e.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    e.rejectDrag();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent e) {
                if (acceptable(e)) {
                    e.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    e.rejectDrag();
                }
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent e) {
                // The copy action is the only one offered, so nothing to reconcile here.
            }

            @Override
            public void dragExit(DropTargetEvent e) {
                dragOver = false;
                repaint();
            }

            @Override
            public void drop(DropTargetDropEvent e) {
                dragOver = false;
                repaint();
                if (!e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    e.rejectDrop();
                    return;
                }
                e.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    Object data = e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    List<?> dropped = (List<?>) data;
                    if (!dropped.isEmpty() && dropped.get(0) instanceof File dropped0) {
                        setFile(dropped0);
                        e.dropComplete(true);
                        return;
                    }
                    e.dropComplete(false);
                } catch (Exception ex) {
                    e.dropComplete(false);
                }
            }

            private boolean acceptable(DropTargetDragEvent e) {
                return e.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
        }));
    }

    // --- appearance ----------------------------------------------------------------------

    private void refresh() {
        boolean hasFile = file != null;

        icon.setIcon(Icons.document(26, hasFile ? UiTheme::accent : UiTheme::muted));
        nameLabel.setText(hasFile ? file.getName() : emptyHint);
        nameLabel.setFont(UiTheme.uiFont(hasFile ? Font.BOLD : Font.PLAIN, 13f));
        nameLabel.setForeground(hasFile ? UiTheme.text() : UiTheme.muted());

        pathLabel.setText(hasFile ? shortenPath(file) : "Sürükleyip bırakın veya tıklayarak seçin");
        pathLabel.setFont(UiTheme.uiFont(Font.PLAIN, 11f));
        pathLabel.setForeground(UiTheme.muted());

        browseButton.setIcon(Icons.folder(16, UiTheme::muted));
        clearButton.setIcon(Icons.clear(16, UiTheme::muted));
        clearButton.setVisible(hasFile);

        setSubtitle(null);
        revalidate();
        repaint();
    }

    /** Keeps the folder line short by showing only the last two path segments. */
    private static String shortenPath(File file) {
        File parent = file.getParentFile();
        if (parent == null) {
            return "";
        }
        File grandParent = parent.getParentFile();
        if (grandParent == null) {
            return parent.getPath();
        }
        return "…" + File.separator + grandParent.getName() + File.separator + parent.getName();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        // Called during a theme switch, before the fields exist on the very first pass.
        if (nameLabel != null) {
            refresh();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 300), Math.max(d.height, 96));
    }

    @Override
    protected Color surfaceColor() {
        if (dragOver) {
            return UiTheme.mix(UiTheme.surface(), UiTheme.accent(), 0.18f);
        }
        if (file != null) {
            return UiTheme.mix(UiTheme.surface(), UiTheme.accent(), 0.06f);
        }
        return UiTheme.surface();
    }

    @Override
    protected Color borderColor() {
        if (dragOver) {
            return UiTheme.accent();
        }
        return file != null ? UiTheme.alpha(UiTheme.accent(), 0.55f) : UiTheme.border();
    }

    @Override
    protected Stroke borderStroke() {
        if (file != null || dragOver) {
            return new BasicStroke(dragOver ? 2f : 1.4f);
        }
        return new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
                new float[]{5f, 4f}, 0f);
    }
}
