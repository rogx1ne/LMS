package com.library.ui;

import com.library.service.CurrentUserContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HomePanel extends JPanel {

    private static final String CLIENT_PROP_DISABLE_ENTER_TRAVERSAL = "lms.disableEnterTraversal";

    private final JTextField txtSearch = new JTextField();
    private final DefaultListModel<NavItem> suggestionModel = new DefaultListModel<>();
    private final JList<NavItem> suggestionList = new JList<>(suggestionModel);
    private final JPopupMenu suggestionPopup = new JPopupMenu();

    private final List<NavItem> allItems = new ArrayList<>();

    public HomePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        buildNavigationItems();
        initSuggestionPopup();
        wireSearch();
    }

    private JPanel buildSearchBar() {
        JPanel wrap = new JPanel(new BorderLayout(10, 0));
        wrap.setBackground(ModuleTheme.WHITE);
        wrap.setBorder(ModuleTheme.sectionBorder("Quick Navigation"));

        JLabel lbl = new JLabel("Search modules / sections");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(ModuleTheme.BLUE_DARK);
        lbl.setBorder(new EmptyBorder(0, 6, 0, 0));

        ModuleTheme.styleInput(txtSearch);
        txtSearch.putClientProperty(CLIENT_PROP_DISABLE_ENTER_TRAVERSAL, Boolean.TRUE);
        txtSearch.setToolTipText("Try: book, stock, issue, return, seller, order, users, audit...");

        wrap.add(lbl, BorderLayout.WEST);
        wrap.add(txtSearch, BorderLayout.CENTER);
        return wrap;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(ModuleTheme.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // --- LOGO ---
        ImageIcon logoIcon = loadIconSafely("lib/icons/logo_full.png", 350, 350);
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon);
            center.add(lblLogo, gbc);
        }

        // --- WELCOME MESSAGE ---
        gbc.gridy = 1;
        JLabel lblWelcome = new JLabel("Welcome to Library Management System");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(ModuleTheme.BLUE_DARK);
        center.add(lblWelcome, gbc);

        gbc.gridy = 2;
        JLabel lblSub = new JLabel("Use the search bar above or select a module from the sidebar.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSub.setForeground(Color.GRAY);
        center.add(lblSub, gbc);

        return center;
    }

    private void buildNavigationItems() {
        boolean isAdmin = CurrentUserContext.isAdministrator();

        // Top-level modules
        allItems.add(NavItem.module("HOME", "Home", "start", "dashboard"));
        allItems.add(NavItem.module("BOOK", "Book Module", "books", "catalog", "inventory", "accession", "stock"));
        allItems.add(NavItem.module("TRANSACTION", "Transaction / Procurement", "transaction", "procurement", "order", "seller", "purchase"));
        allItems.add(NavItem.module("STUDENT", "Student Module", "student", "registration", "card", "receipt"));
        allItems.add(NavItem.module("CIRCULATION", "Circulation", "issue", "return", "fine", "due"));
        if (isAdmin) allItems.add(NavItem.module("ADMIN", "Admin", "admin", "users", "audit", "import", "export", "excel"));

        // Book sub-sections
        allItems.add(NavItem.section("BOOK", "ADD", "Book → Add Book", "add", "new", "copy", "entry"));
        allItems.add(NavItem.section("BOOK", "REGISTER", "Book → Accession Register", "accession", "register", "record"));
        allItems.add(NavItem.section("BOOK", "STOCK", "Book → Stock", "stock", "available", "quantity", "low"));

        // Procurement sub-sections
        allItems.add(NavItem.section("TRANSACTION", "SELLER", "Transaction → Seller Master", "seller", "vendor", "supplier"));
        allItems.add(NavItem.section("TRANSACTION", "ADD_ORDER", "Transaction → Add Order", "order", "purchase", "entry", "bill"));
        allItems.add(NavItem.section("TRANSACTION", "VIEW_ORDER", "Transaction → View Order", "order", "history", "view", "receipt"));

        // Circulation sub-sections
        allItems.add(NavItem.section("CIRCULATION", "ISSUE", "Circulation → Issue Book", "issue", "borrow", "lend"));
        allItems.add(NavItem.section("CIRCULATION", "RETURN", "Circulation → Return Book", "return", "fine", "late"));

        // Admin sub-sections
        if (isAdmin) {
            allItems.add(NavItem.section("ADMIN", "USERS", "Admin → User Management", "users", "credentials", "role", "librarian"));
            allItems.add(NavItem.section("ADMIN", "DATA", "Admin → Import / Export", "import", "export", "excel", "poi"));
            allItems.add(NavItem.section("ADMIN", "AUDIT", "Admin → Audit Logs", "audit", "logs", "history", "actions"));
        }
    }

    private void initSuggestionPopup() {
        suggestionPopup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        suggestionPopup.setFocusable(false);

        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(8);
        suggestionList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        suggestionList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value == null ? "" : value.title);
            l.setOpaque(true);
            l.setBorder(new EmptyBorder(6, 10, 6, 10));
            l.setBackground(isSelected ? new Color(230, 240, 255) : Color.WHITE);
            l.setForeground(ModuleTheme.BLACK);
            return l;
        });

        JScrollPane sc = new JScrollPane(suggestionList);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.setPreferredSize(new Dimension(520, 180));

        suggestionPopup.add(sc);

        suggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    NavItem selected = suggestionList.getSelectedValue();
                    if (selected != null) navigate(selected);
                }
            }
        });
    }

    private void wireSearch() {
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshSuggestions(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshSuggestions(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshSuggestions(); }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (!suggestionPopup.isVisible()) return;

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    int next = Math.min(suggestionModel.getSize() - 1, suggestionList.getSelectedIndex() + 1);
                    suggestionList.setSelectedIndex(Math.max(0, next));
                    suggestionList.ensureIndexIsVisible(suggestionList.getSelectedIndex());
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    int prev = Math.max(0, suggestionList.getSelectedIndex() - 1);
                    suggestionList.setSelectedIndex(prev);
                    suggestionList.ensureIndexIsVisible(prev);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hidePopup();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    NavItem selected = suggestionList.getSelectedValue();
                    if (selected != null) {
                        navigate(selected);
                        e.consume();
                    }
                }
            }
        });

        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    // If focus moved into the popup, keep it open; otherwise close.
                    Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (fo == null) {
                        hidePopup();
                        return;
                    }
                    if (!SwingUtilities.isDescendingFrom(fo, suggestionPopup)) hidePopup();
                });
            }
        });
    }

    private void refreshSuggestions() {
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim();
        if (q.isEmpty()) {
            hidePopup();
            return;
        }

        List<NavItem> matches = findMatches(q);
        suggestionModel.clear();
        for (NavItem item : matches) suggestionModel.addElement(item);

        if (suggestionModel.isEmpty()) {
            hidePopup();
            return;
        }

        suggestionList.setSelectedIndex(0);
        showPopup();
    }

    private List<NavItem> findMatches(String query) {
        String q = query.toLowerCase(Locale.ENGLISH);
        String[] tokens = q.split("\\s+");

        List<NavItem> matches = new ArrayList<>();
        for (NavItem item : allItems) {
            if (item.matches(tokens)) matches.add(item);
        }

        matches.sort(Comparator.<NavItem>comparingInt(i -> i.score(q)).reversed().thenComparing(i -> i.title));

        int limit = Math.min(matches.size(), 10);
        return matches.subList(0, limit);
    }

    private void showPopup() {
        if (suggestionPopup.isVisible()) return;
        suggestionPopup.show(txtSearch, 0, txtSearch.getHeight());
    }

    private void hidePopup() {
        suggestionPopup.setVisible(false);
    }

    private void navigate(NavItem item) {
        hidePopup();
        txtSearch.setText("");

        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof DashboardFrame)) return;

        DashboardFrame dash = (DashboardFrame) w;
        if (item.sectionKey == null) dash.navigateTo(item.moduleKey);
        else dash.navigateTo(item.moduleKey, item.sectionKey);
    }

    private ImageIcon loadIconSafely(String path, int w, int h) {
        java.io.File f = new java.io.File(path);
        if (f.exists()) return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        return null;
    }

    private static final class NavItem {
        final String moduleKey;
        final String sectionKey; // nullable
        final String title;
        final String keywords;

        private NavItem(String moduleKey, String sectionKey, String title, String keywords) {
            this.moduleKey = moduleKey;
            this.sectionKey = sectionKey;
            this.title = title;
            this.keywords = keywords == null ? "" : keywords;
        }

        static NavItem module(String moduleKey, String title, String... keywords) {
            return new NavItem(moduleKey, null, title, join(keywords));
        }

        static NavItem section(String moduleKey, String sectionKey, String title, String... keywords) {
            return new NavItem(moduleKey, sectionKey, title, join(keywords));
        }

        boolean matches(String[] tokens) {
            String hay = (title + " " + keywords).toLowerCase(Locale.ENGLISH);
            for (String t : tokens) {
                if (t == null || t.isEmpty()) continue;
                if (!hay.contains(t)) return false;
            }
            return true;
        }

        int score(String queryLower) {
            String t = title.toLowerCase(Locale.ENGLISH);
            int s = 0;
            if (t.startsWith(queryLower)) s += 120;
            if (t.contains(queryLower)) s += 60;

            String hay = (t + " " + keywords.toLowerCase(Locale.ENGLISH));
            String[] tokens = queryLower.split("\\s+");
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                if (hay.contains(token)) s += 15;
            }
            return s;
        }

        private static String join(String[] keywords) {
            if (keywords == null || keywords.length == 0) return "";
            StringBuilder sb = new StringBuilder();
            for (String k : keywords) {
                if (k == null) continue;
                String v = k.trim();
                if (v.isEmpty()) continue;
                if (sb.length() > 0) sb.append(' ');
                sb.append(v);
            }
            return sb.toString();
        }

        @Override public String toString() { return title; }
    }
}
