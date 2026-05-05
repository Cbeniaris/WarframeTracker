package com.warframetracker;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class WarframeTracker extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────
    static final Color BG_DARK      = new Color(10,  12,  18);
    static final Color BG_PANEL     = new Color(18,  22,  32);
    static final Color BG_ROW_ALT   = new Color(24,  29,  42);
    static final Color BG_HEADER    = new Color(12,  15,  24);
    static final Color ACCENT_GOLD  = new Color(198, 155,  50);
    static final Color ACCENT_BLUE  = new Color( 50, 160, 220);
    static final Color ACCENT_RED   = new Color(200,  60,  60);
    static final Color ACCENT_GREEN = new Color( 60, 170,  90);
    static final Color TEXT_PRIMARY = new Color(220, 215, 200);
    static final Color TEXT_DIM     = new Color(130, 125, 115);
    static final Color BORDER_COLOR = new Color( 40,  48,  68);
    static final Color STATUS_WANT    = new Color(180,  80,  80);
    static final Color STATUS_FARMING = new Color(180, 140,  40);
    static final Color STATUS_OWNED   = new Color( 60, 160,  90);

    // ── State ─────────────────────────────────────────────────────────────
    private final List<TrackerItem> items = new ArrayList<>();

    // ── Table models ──────────────────────────────────────────────────────
    ItemTableModel warframeModel;
    ItemTableModel weaponModel;
    JTable warframeTable;
    JTable weaponTable;

    // ── Filter widgets ────────────────────────────────────────────────────
    private JComboBox<String> wfStatusFilter;
    private JComboBox<String> wpStatusFilter;
    private JComboBox<String> wpCategoryFilter;
    private JTextField wfSearch;
    private JTextField wpSearch;

    public WarframeTracker() {
        super("Warframe Tracker");
        loadData();
        buildUI();
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  UI Construction
    // ─────────────────────────────────────────────────────────────────────
    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        applyGlobalDefaults();

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        setContentPane(root);

        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildTabs(),      BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private void applyGlobalDefaults() {
        UIManager.put("Panel.background",                BG_DARK);
        UIManager.put("OptionPane.background",           BG_PANEL);
        UIManager.put("OptionPane.messageForeground",    TEXT_PRIMARY);
        UIManager.put("TextField.background",            BG_ROW_ALT);
        UIManager.put("TextField.foreground",            TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",       ACCENT_GOLD);
        UIManager.put("TextField.border",                BorderFactory.createCompoundBorder(
                           BorderFactory.createLineBorder(BORDER_COLOR),
                           BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        UIManager.put("TextArea.background",             BG_ROW_ALT);
        UIManager.put("TextArea.foreground",             TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",        ACCENT_GOLD);
        UIManager.put("ComboBox.background",             BG_ROW_ALT);
        UIManager.put("ComboBox.foreground",             TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",    ACCENT_GOLD.darker());
        UIManager.put("ComboBox.selectionForeground",    Color.BLACK);
        UIManager.put("Button.background",               BG_PANEL);
        UIManager.put("Button.foreground",               TEXT_PRIMARY);
        UIManager.put("Label.foreground",                TEXT_PRIMARY);
        UIManager.put("ScrollPane.background",           BG_DARK);
        UIManager.put("Viewport.background",             BG_DARK);
        UIManager.put("ScrollBar.background",            BG_PANEL);
        UIManager.put("ScrollBar.thumb",                 BORDER_COLOR);
        UIManager.put("TabbedPane.background",           BG_DARK);
        UIManager.put("TabbedPane.foreground",           TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",             BG_PANEL);
        UIManager.put("TabbedPane.borderHighlightColor", ACCENT_GOLD);
        UIManager.put("List.background",                 BG_PANEL);
        UIManager.put("List.foreground",                 TEXT_PRIMARY);
        UIManager.put("CheckBox.background",             BG_ROW_ALT);
        UIManager.put("CheckBox.foreground",             TEXT_PRIMARY);
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_GOLD),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("WARFRAME  TRACKER");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(ACCENT_GOLD);

        JLabel sub = new JLabel("Acquisition & Parts Tracker");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(TEXT_DIM);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        badges.setOpaque(false);
        badges.add(buildBadge("WANT",    STATUS_WANT));
        badges.add(buildBadge("FARMING", STATUS_FARMING));
        badges.add(buildBadge("OWNED",   STATUS_OWNED));

        header.add(left,   BorderLayout.WEST);
        header.add(badges, BorderLayout.EAST);
        return header;
    }

    private JLabel buildBadge(String label, Color color) {
        long count = items.stream().filter(i -> i.getStatus().name().equals(label)).count();
        JLabel lbl = new JLabel("  " + label + ": " + count + "  ");
        lbl.setOpaque(true);
        lbl.setBackground(color.darker().darker());
        lbl.setForeground(color.brighter());
        lbl.setFont(new Font("Monospaced", Font.BOLD, 11));
        lbl.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
        return lbl;
    }

    // ── Tabs ──────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("⚡  Warframes", buildItemPanel(true));
        tabs.addTab("⚔  Weapons",   buildItemPanel(false));
        return tabs;
    }

    // ── Item Panel (Warframe or Weapon) ───────────────────────────────────
    /**
     * Each tab is a JSplitPane:
     *   LEFT  — the item list table (existing behaviour)
     *   RIGHT — parts panel for the selected item
     */
    private JPanel buildItemPanel(boolean isWarframe) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_DARK);

        // ── Left: item table ──────────────────────────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(BG_DARK);
        leftPanel.add(buildToolbar(isWarframe), BorderLayout.NORTH);

        ItemTableModel model = new ItemTableModel(isWarframe ? getWarframeItems() : getWeaponItems());
        JTable table = buildTable(model);

        if (isWarframe) { warframeModel = model; warframeTable = table; }
        else            { weaponModel   = model; weaponTable   = table; }

        leftPanel.add(styledScroll(table), BorderLayout.CENTER);
        leftPanel.add(buildButtonBar(isWarframe), BorderLayout.SOUTH);

        // ── Right: parts panel ────────────────────────────────────────────
        PartsPanel partsPanel = new PartsPanel(this);

        // Wire selection → parts panel
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) partsPanel.setItem(model.getItem(row));
                else          partsPanel.setItem(null);
            }
        });

        // ── Split ─────────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, partsPanel);
        split.setDividerLocation(620);
        split.setResizeWeight(0.65);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(BG_DARK);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ── Toolbar ───────────────────────────────────────────────────────────
    private JPanel buildToolbar(boolean isWarframe) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JTextField search = styledTextField(16);
        JComboBox<String> statusFilter = styledCombo(new String[]{"All Statuses", "WANT", "FARMING", "OWNED"});

        JLabel searchLbl = new JLabel("Filter:");
        searchLbl.setForeground(TEXT_DIM);
        bar.add(searchLbl);
        bar.add(search);
        bar.add(statusFilter);

        if (isWarframe) {
            wfSearch = search; wfStatusFilter = statusFilter;
            search.addActionListener(e -> refreshTable(warframeTable, warframeModel, getWarframeItems(), wfSearch, wfStatusFilter, null));
            statusFilter.addActionListener(e -> refreshTable(warframeTable, warframeModel, getWarframeItems(), wfSearch, wfStatusFilter, null));
        } else {
            JComboBox<String> catFilter = styledCombo(new String[]{
                "All Types","PRIMARY","SECONDARY","MELEE","SENTINEL_WEAPON","ARCH_GUN","ARCH_MELEE"});
            bar.add(catFilter);
            wpSearch = search; wpStatusFilter = statusFilter; wpCategoryFilter = catFilter;
            search.addActionListener(e -> refreshTable(weaponTable,   weaponModel,   getWeaponItems(),   wpSearch, wpStatusFilter, wpCategoryFilter));
            statusFilter.addActionListener(e -> refreshTable(weaponTable,   weaponModel,   getWeaponItems(),   wpSearch, wpStatusFilter, wpCategoryFilter));
            catFilter.addActionListener(e -> refreshTable(weaponTable,   weaponModel,   getWeaponItems(),   wpSearch, wpStatusFilter, wpCategoryFilter));
        }
        return bar;
    }

    // ── Button Bar ────────────────────────────────────────────────────────
    private JPanel buildButtonBar(boolean isWarframe) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton addBtn    = accentButton("＋ Add",    ACCENT_BLUE);
        JButton editBtn   = accentButton("✎  Edit",   ACCENT_GOLD);
        JButton removeBtn = accentButton("✕  Remove", ACCENT_RED);

        if (isWarframe) {
            addBtn.addActionListener(e -> showAddEditDialog(true, null));
            editBtn.addActionListener(e -> {
                int row = warframeTable.getSelectedRow();
                if (row >= 0) showAddEditDialog(true, warframeModel.getItem(row));
                else showError("Select a Warframe to edit.");
            });
            removeBtn.addActionListener(e -> removeSelected(warframeTable, warframeModel));
        } else {
            addBtn.addActionListener(e -> showAddEditDialog(false, null));
            editBtn.addActionListener(e -> {
                int row = weaponTable.getSelectedRow();
                if (row >= 0) showAddEditDialog(false, weaponModel.getItem(row));
                else showError("Select a Weapon to edit.");
            });
            removeBtn.addActionListener(e -> removeSelected(weaponTable, weaponModel));
        }

        bar.add(removeBtn); bar.add(editBtn); bar.add(addBtn);
        return bar;
    }

    // ── Status Bar ────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 14, 4, 14)));
        JLabel lbl = new JLabel("Data stored at: " + DataStore.getDataFilePath());
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lbl.setForeground(TEXT_DIM);
        bar.add(lbl, BorderLayout.WEST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Table
    // ─────────────────────────────────────────────────────────────────────
    private JTable buildTable(ItemTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(32);
        table.setSelectionBackground(ACCENT_GOLD.darker().darker());
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_HEADER);
        header.setForeground(ACCENT_GOLD);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_GOLD));
        header.setReorderingAllowed(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(180); // Name
        table.getColumnModel().getColumn(1).setPreferredWidth(110); // Category
        table.getColumnModel().getColumn(2).setPreferredWidth(90);  // Status
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Parts
        table.getColumnModel().getColumn(4).setPreferredWidth(160); // Notes

        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new ProgressCellRenderer());

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) showAddEditDialog(
                        model.getItem(row).getCategory() == TrackerItem.Category.WARFRAME,
                        model.getItem(row));
                }
            }
        });
        return table;
    }

    private JScrollPane styledScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getViewport().setBackground(BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return sp;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Add / Edit Dialog
    // ─────────────────────────────────────────────────────────────────────
    private void showAddEditDialog(boolean isWarframe, TrackerItem existing) {
        boolean isEdit = (existing != null);
        String title   = isEdit ? "Edit " + (isWarframe ? "Warframe" : "Weapon")
                                : "Add "  + (isWarframe ? "Warframe" : "Weapon");

        JTextField nameField = styledTextField(26);
        if (isEdit) nameField.setText(existing.getName());

        TrackerItem.Category[] cats = isWarframe
            ? new TrackerItem.Category[]{TrackerItem.Category.WARFRAME}
            : new TrackerItem.Category[]{
                TrackerItem.Category.PRIMARY, TrackerItem.Category.SECONDARY,
                TrackerItem.Category.MELEE, TrackerItem.Category.SENTINEL_WEAPON,
                TrackerItem.Category.ARCH_GUN, TrackerItem.Category.ARCH_MELEE};

        JComboBox<TrackerItem.Category> catBox = new JComboBox<>(cats);
        styleComboBox(catBox);
        if (isEdit) catBox.setSelectedItem(existing.getCategory());

        JComboBox<TrackerItem.Status> statusBox = new JComboBox<>(TrackerItem.Status.values());
        styleComboBox(statusBox);
        if (isEdit) statusBox.setSelectedItem(existing.getStatus());

        JTextArea notesArea = styledTextArea(3, 26);
        if (isEdit) notesArea.setText(existing.getNotes());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addFormRow(form, gc, row++, "Name:", nameField);
        if (!isWarframe) addFormRow(form, gc, row++, "Type:", catBox);
        addFormRow(form, gc, row++, "Status:", statusBox);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.getViewport().setBackground(BG_ROW_ALT);
        notesScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        addFormRow(form, gc, row, "Notes:", notesScroll);

        int result = JOptionPane.showConfirmDialog(this, form, title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showError("Name cannot be empty."); return; }

            if (isEdit) {
                existing.setName(name);
                existing.setCategory((TrackerItem.Category) catBox.getSelectedItem());
                existing.setStatus((TrackerItem.Status) statusBox.getSelectedItem());
                existing.setNotes(notesArea.getText().trim());
            } else {
                TrackerItem item = new TrackerItem(
                    name,
                    (TrackerItem.Category) catBox.getSelectedItem(),
                    (TrackerItem.Status)   statusBox.getSelectedItem(),
                    notesArea.getText().trim());
                items.add(item);
            }
            saveAndRefresh();
        }
    }

    private void removeSelected(JTable table, ItemTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Select an item to remove."); return; }
        TrackerItem item = model.getItem(row);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove \"" + item.getName() + "\" and all its parts?",
            "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            items.remove(item);
            saveAndRefresh();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Data helpers
    // ─────────────────────────────────────────────────────────────────────
    void loadData() {
        try { items.clear(); items.addAll(DataStore.load()); }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load data: " + e.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    void quietSave() {
        try { DataStore.save(items); }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save: " + e.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    void saveAndRefresh() {
        try { DataStore.save(items); }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + e.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshTable(warframeTable, warframeModel, getWarframeItems(), wfSearch, wfStatusFilter, null);
        refreshTable(weaponTable,   weaponModel,   getWeaponItems(),   wpSearch, wpStatusFilter, wpCategoryFilter);
        refreshHeader();
    }

    /** Refreshes a table while preserving the selected item. */
    private void refreshTable(JTable table, ItemTableModel model,
                               List<TrackerItem> newItems,
                               JTextField search,
                               JComboBox<String> statusFilter,
                               JComboBox<String> catFilter) {
        if (model == null || table == null) return;

        // Remember which item was selected by identity
        int selRow = table.getSelectedRow();
        TrackerItem selItem = (selRow >= 0) ? model.getItem(selRow) : null;

        // Apply filters
        String searchStr = search != null ? search.getText().trim().toLowerCase() : "";
        String statusSel = statusFilter != null ? (String) statusFilter.getSelectedItem() : "All Statuses";
        String catSel    = catFilter    != null ? (String) catFilter.getSelectedItem()    : "All Types";

        List<TrackerItem> filtered = newItems.stream()
            .filter(i -> searchStr.isEmpty() || i.getName().toLowerCase().contains(searchStr))
            .filter(i -> "All Statuses".equals(statusSel) || i.getStatus().name().equals(statusSel))
            .filter(i -> "All Types".equals(catSel)       || i.getCategory().name().equals(catSel))
            .collect(Collectors.toList());

        model.setItems(filtered);

        // Re-select the same item if it's still in the filtered list
        if (selItem != null) {
            for (int i = 0; i < filtered.size(); i++) {
                if (filtered.get(i) == selItem) {
                    final int row = i;
                    table.getSelectionModel().setSelectionInterval(row, row);
                    break;
                }
            }
        }
    }

    private void refreshHeader() {
        JPanel root = (JPanel) getContentPane();
        BorderLayout layout = (BorderLayout) root.getLayout();
        Component north = layout.getLayoutComponent(BorderLayout.NORTH);
        if (north != null) root.remove(north);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.revalidate();
        root.repaint();
    }

    private List<TrackerItem> getWarframeItems() {
        return items.stream().filter(i -> i.getCategory() == TrackerItem.Category.WARFRAME)
                             .collect(Collectors.toList());
    }
    private List<TrackerItem> getWeaponItems() {
        return items.stream().filter(i -> i.getCategory() != TrackerItem.Category.WARFRAME)
                             .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Widget factories
    // ─────────────────────────────────────────────────────────────────────
    JTextField styledTextField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(BG_ROW_ALT);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_GOLD);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private JTextArea styledTextArea(int rows, int cols) {
        JTextArea a = new JTextArea(rows, cols);
        a.setBackground(BG_ROW_ALT);
        a.setForeground(TEXT_PRIMARY);
        a.setCaretColor(ACCENT_GOLD);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return a;
    }

    private JComboBox<String> styledCombo(String[] options) {
        JComboBox<String> box = new JComboBox<>(options);
        styleComboBox(box);
        return box;
    }

    <T> void styleComboBox(JComboBox<T> box) {
        box.setBackground(BG_ROW_ALT);
        box.setForeground(TEXT_PRIMARY);
        box.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        box.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    JButton accentButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color.darker().darker());
        btn.setForeground(color.brighter());
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(color.darker().darker()); }
        });
        return btn;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_DIM);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(lbl, gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        panel.add(comp, gc);
    }

    void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Cell Renderers
    // ─────────────────────────────────────────────────────────────────────
    static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setFont(new Font("SansSerif", Font.PLAIN, 13));
            if (sel) { setBackground(ACCENT_GOLD.darker().darker()); setForeground(Color.WHITE); }
            else     { setBackground(row % 2 == 0 ? BG_PANEL : BG_ROW_ALT); setForeground(TEXT_PRIMARY); }
            return this;
        }
    }

    static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            setFont(new Font("SansSerif", Font.BOLD, 11));
            String s = v != null ? v.toString() : "";
            if (!sel) {
                switch (s) {
                    case "WANT"    -> { setBackground(STATUS_WANT.darker());    setForeground(STATUS_WANT.brighter()); }
                    case "FARMING" -> { setBackground(STATUS_FARMING.darker()); setForeground(STATUS_FARMING.brighter()); }
                    case "OWNED"   -> { setBackground(STATUS_OWNED.darker());   setForeground(STATUS_OWNED.brighter()); }
                    default        -> { setBackground(BG_PANEL); setForeground(TEXT_DIM); }
                }
            } else { setBackground(ACCENT_GOLD.darker().darker()); setForeground(Color.WHITE); }
            return this;
        }
    }

    static class ProgressCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            setFont(new Font("Monospaced", Font.BOLD, 12));
            String s = v != null ? v.toString() : "";
            if (!sel) {
                setBackground(row % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
                if (s.isEmpty()) { setForeground(TEXT_DIM); setText("—"); }
                else {
                    // colour by completion
                    String[] parts = s.split("/");
                    try {
                        int have = Integer.parseInt(parts[0].trim());
                        int total= Integer.parseInt(parts[1].trim());
                        setForeground(have == total ? STATUS_OWNED.brighter() :
                                      have  > 0    ? STATUS_FARMING.brighter() :
                                                     STATUS_WANT.brighter());
                    } catch (Exception ex) { setForeground(TEXT_DIM); }
                }
            } else { setBackground(ACCENT_GOLD.darker().darker()); setForeground(Color.WHITE); }
            return this;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Table Model
    // ─────────────────────────────────────────────────────────────────────
    static class ItemTableModel extends AbstractTableModel {
        private static final String[] COLS = {"Name", "Category", "Status", "Parts", "Notes"};
        private List<TrackerItem> data;

        ItemTableModel(List<TrackerItem> data) { this.data = new ArrayList<>(data); }

        void setItems(List<TrackerItem> d) { this.data = new ArrayList<>(d); fireTableDataChanged(); }
        TrackerItem getItem(int row) { return data.get(row); }

        @Override public int    getRowCount()              { return data.size(); }
        @Override public int    getColumnCount()           { return COLS.length; }
        @Override public String getColumnName(int col)     { return COLS[col]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override public Object getValueAt(int row, int col) {
            TrackerItem it = data.get(row);
            return switch (col) {
                case 0 -> it.getName();
                case 1 -> it.getCategory().name().replace("_", " ");
                case 2 -> it.getStatus().name();
                case 3 -> it.partsProgress();
                case 4 -> it.getNotes();
                default -> "";
            };
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new WarframeTracker();
        });
    }
}
