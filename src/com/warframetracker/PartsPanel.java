package com.warframetracker;

import javax.swing.*;
import javax.swing.border.*;

import com.warframetracker.WarframeTracker.ItemTableModel;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Right-hand panel showing the parts list for a selected TrackerItem.
 * Each part row has a checkbox (obtained) and a label (part name).
 * Parts can be added, renamed, and removed.
 */
public class PartsPanel extends JPanel {

    private static final Color BG_DARK      = WarframeTracker.BG_DARK;
    private static final Color BG_PANEL     = WarframeTracker.BG_PANEL;
    private static final Color BG_ROW_ALT   = WarframeTracker.BG_ROW_ALT;
    private static final Color BG_HEADER    = WarframeTracker.BG_HEADER;
    private static final Color ACCENT_GOLD  = WarframeTracker.ACCENT_GOLD;
    private static final Color ACCENT_BLUE  = WarframeTracker.ACCENT_BLUE;
    private static final Color ACCENT_RED   = WarframeTracker.ACCENT_RED;
    private static final Color ACCENT_GREEN = WarframeTracker.ACCENT_GREEN;
    private static final Color TEXT_PRIMARY = WarframeTracker.TEXT_PRIMARY;
    private static final Color TEXT_DIM     = WarframeTracker.TEXT_DIM;
    private static final Color BORDER_COLOR = WarframeTracker.BORDER_COLOR;
    private static final Color STATUS_OWNED   = WarframeTracker.STATUS_OWNED;
    private static final Color STATUS_FARMING = WarframeTracker.STATUS_FARMING;
    private static final Color STATUS_WANT    = WarframeTracker.STATUS_WANT;

    private final WarframeTracker owner;
    private TrackerItem currentItem;

    // ── Widgets ───────────────────────────────────────────────────────────
    private final JLabel     titleLabel    = new JLabel("Select an item");
    private final JLabel     progressLabel = new JLabel("");
    private final JPanel     partsListPanel;
    private final JScrollPane scroll;

    public PartsPanel(WarframeTracker owner) {
        this.owner = owner;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));

        // Header
        add(buildHeader(), BorderLayout.NORTH);

        // Scrollable parts list
        partsListPanel = new JPanel();
        partsListPanel.setLayout(new BoxLayout(partsListPanel, BoxLayout.Y_AXIS));
        partsListPanel.setBackground(BG_DARK);

        scroll = new JScrollPane(partsListPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Add-part bar at bottom
        add(buildAddBar(), BorderLayout.SOUTH);

        renderEmpty();
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG_HEADER);
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_GOLD),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(ACCENT_GOLD);

        progressLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        progressLabel.setForeground(TEXT_DIM);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(titleLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(progressLabel);

        h.add(left, BorderLayout.WEST);
        return h;
    }

    // ── Add Bar ───────────────────────────────────────────────────────────
    private JPanel buildAddBar() {
        JPanel bar = new JPanel(new BorderLayout(6, 0));
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JTextField nameField = owner.styledTextField(14);
        nameField.putClientProperty("JTextField.placeholderText", "Part name…");

        JButton addBtn = owner.accentButton("＋ Add Part", ACCENT_BLUE);
        addBtn.addActionListener(e -> {
            if (currentItem == null) { owner.showError("Select an item first."); return; }
            String n = nameField.getText().trim();
            if (n.isEmpty()) { owner.showError("Enter a part name."); return; }
            currentItem.getParts().add(new TrackerItem.Part(n, false));
            nameField.setText("");
            owner.saveAndRefresh();
            rebuildPartRows();
        });

        // Allow pressing Enter in the field
        nameField.addActionListener(addBtn.getActionListeners()[0]);

        bar.add(nameField, BorderLayout.CENTER);
        bar.add(addBtn,    BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────
    public void setItem(TrackerItem item) {
        this.currentItem = item;
        if (item == null) { renderEmpty(); return; }
        titleLabel.setText(item.getName());
        rebuildPartRows();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Render helpers
    // ─────────────────────────────────────────────────────────────────────
    private void renderEmpty() {
        titleLabel.setText("Select an item");
        progressLabel.setText("");
        partsListPanel.removeAll();

        JLabel hint = new JLabel("← Select a Warframe or Weapon");
        hint.setForeground(TEXT_DIM);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 13));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        partsListPanel.add(hint);
        partsListPanel.revalidate();
        partsListPanel.repaint();
    }

    private void rebuildPartRows() {
        partsListPanel.removeAll();

        if (currentItem == null) { renderEmpty(); return; }

        List<TrackerItem.Part> parts = currentItem.getParts();

        if (parts.isEmpty()) {
            JLabel hint = new JLabel("No parts yet — add one below");
            hint.setForeground(TEXT_DIM);
            hint.setFont(new Font("SansSerif", Font.ITALIC, 13));
            hint.setAlignmentX(Component.LEFT_ALIGNMENT);
            hint.setBorder(BorderFactory.createEmptyBorder(20, 16, 0, 0));
            partsListPanel.add(hint);
        } else {
            for (int i = 0; i < parts.size(); i++) {
                partsListPanel.add(buildPartRow(parts.get(i), i));
            }
        }

        // Update progress label
        updateProgress();

        partsListPanel.revalidate();
        partsListPanel.repaint();
        scroll.revalidate();
    }

    private JPanel buildPartRow(TrackerItem.Part part, int index) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(index % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setPreferredSize(new Dimension(0, 42));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 12, 4, 8)));

        // ── Checkbox ──────────────────────────────────────────────────────
        JCheckBox check = new JCheckBox();
        check.setSelected(part.isObtained());
        check.setBackground(index % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
        check.setFocusPainted(false);
        styleCheckbox(check, part.isObtained());

        // ── Part name label ───────────────────────────────────────────────
        JLabel nameLbl = new JLabel(part.getName());
        nameLbl.setFont(new Font("SansSerif", part.isObtained() ? Font.ITALIC : Font.PLAIN, 13));
        nameLbl.setForeground(part.isObtained() ? STATUS_OWNED.brighter() : TEXT_PRIMARY);

        // ── Obtained badge ────────────────────────────────────────────────
        JLabel badge = buildStatusBadge(part.isObtained());

        // Wire checkbox
        check.addItemListener(e -> {
            part.setObtained(check.isSelected());
            styleCheckbox(check, check.isSelected());
            nameLbl.setFont(new Font("SansSerif",
                check.isSelected() ? Font.ITALIC : Font.PLAIN, 13));
            nameLbl.setForeground(check.isSelected()
                ? STATUS_OWNED.brighter() : TEXT_PRIMARY);
            badge.setText(check.isSelected() ? "  OBTAINED  " : "  MISSING  ");
            badge.setBackground(check.isSelected()
                ? STATUS_OWNED.darker().darker() : STATUS_WANT.darker().darker());
            badge.setForeground(check.isSelected()
                ? STATUS_OWNED.brighter() : STATUS_WANT.brighter());
            badge.setBorder(BorderFactory.createLineBorder(
                check.isSelected() ? STATUS_OWNED.darker() : STATUS_WANT.darker(), 1));
            updateProgress();

            // Save quietly — no full UI rebuild, just persist and update the Parts column
            owner.quietSave();
            updatePartsColumnOnly();
        });

        // ── Rename button ─────────────────────────────────────────────────
        JButton renameBtn = iconButton("✎", ACCENT_GOLD);
        renameBtn.setToolTipText("Rename part");
        renameBtn.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(this, "Rename part:", part.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                part.setName(newName.trim());
                owner.saveAndRefresh();
                rebuildPartRows();
            }
        });

        // ── Remove button ─────────────────────────────────────────────────
        JButton removeBtn = iconButton("✕", ACCENT_RED);
        removeBtn.setToolTipText("Remove part");
        removeBtn.addActionListener(e -> {
            currentItem.getParts().remove(part);
            owner.saveAndRefresh();
            rebuildPartRows();
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(check);
        left.add(nameLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        right.add(badge);
        right.add(renameBtn);
        right.add(removeBtn);

        row.add(left,  BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JLabel buildStatusBadge(boolean obtained) {
        JLabel lbl = new JLabel(obtained ? "  OBTAINED  " : "  MISSING  ");
        lbl.setOpaque(true);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setBackground(obtained ? STATUS_OWNED.darker().darker() : STATUS_WANT.darker().darker());
        lbl.setForeground(obtained ? STATUS_OWNED.brighter()        : STATUS_WANT.brighter());
        lbl.setBorder(BorderFactory.createLineBorder(
            obtained ? STATUS_OWNED.darker() : STATUS_WANT.darker(), 1));
        return lbl;
    }

    private void styleCheckbox(JCheckBox box, boolean checked) {
        box.setForeground(checked ? STATUS_OWNED.brighter() : TEXT_DIM);
    }

    private JButton iconButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color.darker().darker());
        btn.setForeground(color.brighter());
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(color.darker().darker()); }
        });
        return btn;
    }

    private void updateProgress() {
        if (currentItem == null) { progressLabel.setText(""); return; }
        List<TrackerItem.Part> parts = currentItem.getParts();
        if (parts.isEmpty()) { progressLabel.setText("No parts defined"); return; }
        long obtained = currentItem.obtainedCount();
        long total    = parts.size();
        String pct    = total > 0 ? " (" + (obtained * 100 / total) + "%)" : "";
        progressLabel.setText(obtained + " / " + total + " obtained" + pct);
        if      (obtained == total) progressLabel.setForeground(STATUS_OWNED.brighter());
        else if (obtained  > 0)     progressLabel.setForeground(STATUS_FARMING.brighter());
        else                        progressLabel.setForeground(STATUS_WANT.brighter());
    }
    
    private void updatePartsColumnOnly() {
    	// Find which row this item is in and only refresh that cell
        // fireTableDataChanged() clears selection — fireTableCellUpdated() does not
        ItemTableModel model = warframeModelContains()
            ? owner.warframeModel
            : owner.weaponModel;

        if (model == null) return;

        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getItem(i) == currentItem) {
                model.fireTableCellUpdated(i, 3); // column 3 = Parts progress
                break;
            }
        }
    }

    private boolean warframeModelContains() {
        return owner.warframeModel != null &&
               currentItem != null &&
               currentItem.getCategory() == TrackerItem.Category.WARFRAME;
    }
}
