package com.library.ui;

import com.library.dao.UserDAO;
import com.library.model.UserProfile;
import com.library.service.CurrentUserContext;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DashboardFrame extends JFrame {

    // --- COLOR PALETTE ---
    private static final Color COLOR_BLUE_DARK  = new Color(31, 62, 109);
    private static final Color COLOR_GREEN      = new Color(46, 125, 50);
    private static final Color COLOR_WHITE      = Color.WHITE;
    private static final Color COLOR_BG_CONTENT = new Color(245, 248, 250); // Very light grey-blue for content area

    // Panels
    private JPanel pnlSidebar;
    private JPanel pnlContent;
    private CardLayout cardLayout;

    // Sidebar behavior
    private static final int SIDEBAR_WIDTH_EXPANDED = 280;
    private static final int SIDEBAR_WIDTH_COLLAPSED = 88;
    private static final String CLIENT_PROP_FULL_TEXT = "lms.fullText";
    private boolean sidebarCollapsed = false;

    private JLabel lblTitle;
    private JLabel lblUserName;
    private JButton btnToggleSidebar;
    private JButton btnLogout;
    private JTextField txtGlobalSearch;
    private final JList<NavItem> suggestionList = new JList<>(new DefaultListModel<>());
    private final JPopupMenu suggestionPopup = new JPopupMenu();
    private final List<NavItem> navItems = new ArrayList<>();
    private final List<JButton> menuButtons = new ArrayList<>();
    private JPanel pnlMenu;
    private JPanel pnlUser;
    private final UserDAO userDAO = new UserDAO();

    // Module instances (kept for programmatic navigation)
    private HomePanel homePanel;
    private BookModulePanel bookModulePanel;
    private ProcurementModulePanel procurementModulePanel;
    private StudentView studentView;
    private CirculationModulePanel circulationModulePanel;
    private AdminModulePanel adminModulePanel; // may be null for non-admin

    public DashboardFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Library Management System - Dashboard");
        setSize(1100, 750); // Slightly wider to accommodate the table
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- TOP BAR (Global Search) ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(COLOR_WHITE);
        pnlTop.setPreferredSize(new Dimension(getWidth(), 60));
        pnlTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        pnlSearch.setBackground(COLOR_WHITE);
        
        txtGlobalSearch = new JTextField(25);
        ModuleTheme.styleInput(txtGlobalSearch);
        txtGlobalSearch.putClientProperty("lms.disableEnterTraversal", Boolean.TRUE);
        txtGlobalSearch.setToolTipText("Search modules (e.g., 'book', 'issue', 'users')...");
        
        buildNavigationItems();
        initSuggestionPopup();
        wireGlobalSearch();

        JLabel lblSearch = new JLabel("Global Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlSearch.add(lblSearch);
        pnlSearch.add(txtGlobalSearch);
        pnlTop.add(pnlSearch, BorderLayout.WEST);

        // --- 1. LEFT SIDEBAR ---
        pnlSidebar = new JPanel();
        pnlSidebar.setBackground(COLOR_WHITE);
        pnlSidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH_EXPANDED, getHeight())); // Fixed width sidebar
        pnlSidebar.setLayout(new BorderLayout());
        pnlSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220))); // Subtle divider line

        // A. Sidebar Header (Logo + Title)
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_WHITE);

        JPanel pnlBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        pnlBrand.setBackground(COLOR_WHITE);

        ImageIcon logoIcon = loadIconSafely("lib/icons/logo_small.png", 64, 64);
        JLabel lblLogo = new JLabel(logoIcon);

        lblTitle = new JLabel("<html>LMS</html>");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(COLOR_BLUE_DARK);

        pnlBrand.add(lblLogo);
        pnlBrand.add(lblTitle);

        btnToggleSidebar = new JButton("<<");
        btnToggleSidebar.setBorderPainted(false);
        btnToggleSidebar.setFocusPainted(false);
        btnToggleSidebar.setContentAreaFilled(false);
        btnToggleSidebar.setForeground(COLOR_BLUE_DARK);
        btnToggleSidebar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleSidebar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleSidebar.setToolTipText("Collapse sidebar");
        btnToggleSidebar.addActionListener(e -> setSidebarCollapsed(!sidebarCollapsed));

        JPanel pnlToggle = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlToggle.setBackground(COLOR_WHITE);
        pnlToggle.add(btnToggleSidebar);

        pnlHeader.add(pnlBrand, BorderLayout.CENTER);
        pnlHeader.add(pnlToggle, BorderLayout.EAST);

        // B. Sidebar Menu (The Links)
        pnlMenu = new JPanel();
        pnlMenu.setBackground(COLOR_WHITE);
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setBorder(new EmptyBorder(40, 30, 0, 0)); // Padding: Top, Left

        boolean isAdministrator = CurrentUserContext.isAdministrator();

        // Add Menu Items (Matches your sketch)
        pnlMenu.add(createMenuButton("HOME", "lib/icons/home.png", "H"));
        pnlMenu.add(Box.createVerticalStrut(15));
        pnlMenu.add(createMenuButton("BOOK", "lib/icons/book.png", "B"));
        pnlMenu.add(Box.createVerticalStrut(15));
        pnlMenu.add(createMenuButton("TRANSACTION", "lib/icons/cart.png", "T"));
        pnlMenu.add(Box.createVerticalStrut(15));
        pnlMenu.add(createMenuButton("STUDENT", "lib/icons/student.png", "S"));
        pnlMenu.add(Box.createVerticalStrut(15));
        if (isAdministrator) {
            pnlMenu.add(createMenuButton("ADMIN", "lib/icons/settings.png", "A"));
            pnlMenu.add(Box.createVerticalStrut(15));
        }
        pnlMenu.add(createMenuButton("CIRCULATION", "lib/icons/sync.png", "C"));

        // C. Sidebar Footer (User Profile)
        pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        pnlUser.setBackground(COLOR_WHITE);
        pnlUser.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblUserIcon = new JLabel(loadIconSafely("lib/icons/user.png", 32, 32));
        lblUserIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUserName = new JLabel("<html><b>" + CurrentUserContext.getDisplayName() + "</b><br><span style='font-size:10px; color:gray'>Logged In</span></html>");
        lblUserName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserName.setForeground(COLOR_BLUE_DARK);
        lblUserName.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Logout Button (Small text link style)
        btnLogout = new JButton("Logout");
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setForeground(Color.RED);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            CurrentUserContext.clear();
            this.dispose();
            new LoginFrame().setVisible(true); // Uncomment when LoginFrame is ready
        });
        btnLogout.putClientProperty(CLIENT_PROP_FULL_TEXT, "Logout");

        pnlUser.add(lblUserIcon);
        pnlUser.add(lblUserName);
        pnlUser.add(btnLogout);

        MouseAdapter userPopup = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 1) {
                    showUserDetailsPopup(pnlUser);
                }
            }
        };
        pnlUser.addMouseListener(userPopup);
        lblUserIcon.addMouseListener(userPopup);
        lblUserName.addMouseListener(userPopup);

        // Assemble Sidebar
        pnlSidebar.add(pnlHeader, BorderLayout.NORTH);
        pnlSidebar.add(pnlMenu, BorderLayout.CENTER);
        pnlSidebar.add(pnlUser, BorderLayout.SOUTH);

        // --- 2. MAIN CONTENT AREA (Right Side) ---
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(COLOR_BG_CONTENT);

        // --- PAGE 0: HOME ---
        homePanel = new HomePanel();
        pnlContent.add(homePanel, "HOME");

        // --- PAGE 1: BOOK (MODULE) ---
        bookModulePanel = new BookModulePanel();
        new BookController(bookModulePanel);
        pnlContent.add(bookModulePanel, "BOOK");

        // --- PAGE 2: TRANSACTION (Placeholder) ---
        procurementModulePanel = new ProcurementModulePanel();
        new ProcurementController(procurementModulePanel);
        pnlContent.add(procurementModulePanel, "TRANSACTION");

        // --- PAGE 3: STUDENT (MVC INTEGRATION) ---
        // 1. Create the View
        studentView = new StudentView();
        // 2. Create the Controller (connects View to Logic/DAO)
        new StudentController(studentView);
        // 3. Add the View to the dashboard
        pnlContent.add(studentView, "STUDENT");

        // --- PAGE 4: CIRCULATION ---
        circulationModulePanel = new CirculationModulePanel();
        new CirculationController(circulationModulePanel);
        pnlContent.add(circulationModulePanel, "CIRCULATION");

        // --- PAGE 5: ADMIN (ADMIN USER ONLY) ---
        if (isAdministrator) {
            adminModulePanel = new AdminModulePanel();
            new AdminController(adminModulePanel);
            pnlContent.add(adminModulePanel, "ADMIN");
        }

        // Default View - Set to HOME
        cardLayout.show(pnlContent, "HOME");

        // Add to Frame
        add(pnlTop, BorderLayout.NORTH);
        add(pnlSidebar, BorderLayout.WEST);
        add(pnlContent, BorderLayout.CENTER);

        setupKeyboardShortcuts();
        setSidebarCollapsed(false);
    }

    private void setupKeyboardShortcuts() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        String[] keys = {"F1", "F2", "F3", "F4", "F5", "F6"};
        String[] modules = {"HOME", "BOOK", "TRANSACTION", "STUDENT", "ADMIN", "CIRCULATION"};

        for (int i = 0; i < keys.length; i++) {
            final String mod = modules[i];
            im.put(KeyStroke.getKeyStroke(keys[i]), keys[i]);
            am.put(keys[i], new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cardLayout.show(pnlContent, mod);
                }
            });
        }

        im.put(KeyStroke.getKeyStroke("control F"), "focusSearch");
        am.put("focusSearch", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtGlobalSearch.requestFocusInWindow();
            }
        });
    }

    private void buildNavigationItems() {
        boolean isAdmin = CurrentUserContext.isAdministrator();

        // Top-level modules
        navItems.add(NavItem.module("HOME", "Home", "start", "dashboard", "main"));
        navItems.add(NavItem.module("BOOK", "Book Module", "books", "catalog", "inventory", "accession", "stock"));
        navItems.add(NavItem.module("TRANSACTION", "Transaction / Procurement", "transaction", "procurement", "order", "seller", "purchase", "bill"));
        navItems.add(NavItem.module("STUDENT", "Student Module", "student", "registration", "card", "receipt"));
        navItems.add(NavItem.module("CIRCULATION", "Circulation", "issue", "return", "fine", "due"));
        if (isAdmin) navItems.add(NavItem.module("ADMIN", "Admin", "admin", "users", "audit", "import", "export", "excel"));

        // Book sub-sections
        navItems.add(NavItem.section("BOOK", "ADD", "Book → Add Book", "add", "new", "copy", "entry"));
        navItems.add(NavItem.section("BOOK", "REGISTER", "Book → Accession Register", "accession", "register", "record"));
        navItems.add(NavItem.section("BOOK", "STOCK", "Book → Stock", "stock", "available", "quantity", "low"));

        // Procurement sub-sections
        navItems.add(NavItem.section("TRANSACTION", "SELLER", "Transaction → Seller Master", "seller", "vendor", "supplier"));
        navItems.add(NavItem.section("TRANSACTION", "ADD_ORDER", "Transaction → Add Order", "order", "purchase", "entry", "bill"));
        navItems.add(NavItem.section("TRANSACTION", "VIEW_ORDER", "Transaction → View Order", "order", "history", "view", "receipt"));
        navItems.add(NavItem.section("TRANSACTION", "BILL_ENTRY", "Transaction → Bill Entry", "bill", "invoice", "cost"));
        navItems.add(NavItem.section("TRANSACTION", "BILL_REPORT", "Transaction → Bill Report", "bill", "spending", "audit"));
        navItems.add(NavItem.section("TRANSACTION", "BILL_ACCESSION", "Transaction → Bill Accession", "bill", "accession", "bulk"));

        // Circulation sub-sections
        navItems.add(NavItem.section("CIRCULATION", "ISSUE", "Circulation → Issue Book", "issue", "borrow", "lend"));
        navItems.add(NavItem.section("CIRCULATION", "RETURN", "Circulation → Return Book", "return", "fine", "late"));

        // Admin sub-sections
        if (isAdmin) {
            navItems.add(NavItem.section("ADMIN", "USERS", "Admin → User Management", "users", "credentials", "role", "librarian"));
            navItems.add(NavItem.section("ADMIN", "DATA", "Admin → Import / Export", "import", "export", "excel", "poi"));
            navItems.add(NavItem.section("ADMIN", "AUDIT", "Admin → Audit Logs", "audit", "logs", "history", "actions"));
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
            l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            l.setBackground(isSelected ? new Color(230, 240, 255) : Color.WHITE);
            l.setForeground(Color.BLACK);
            return l;
        });

        JScrollPane sc = new JScrollPane(suggestionList);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.setPreferredSize(new Dimension(400, 200));
        suggestionPopup.add(sc);

        suggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    NavItem selected = suggestionList.getSelectedValue();
                    if (selected != null) {
                        navigate(selected);
                        suggestionPopup.setVisible(false);
                    }
                }
            }
        });
    }

    private void wireGlobalSearch() {
        txtGlobalSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshSuggestions(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshSuggestions(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshSuggestions(); }
        });

        txtGlobalSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    NavItem selected = null;
                    if (suggestionPopup.isVisible()) {
                        selected = suggestionList.getSelectedValue();
                    }
                    
                    if (selected == null) {
                        // If nothing selected or popup hidden, try to find a quick match
                        refreshSuggestions();
                        selected = suggestionList.getSelectedValue();
                    }

                    if (selected != null) {
                        navigate(selected);
                        suggestionPopup.setVisible(false);
                        e.consume();
                    }
                    return;
                }

                if (!suggestionPopup.isVisible()) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && !txtGlobalSearch.getText().trim().isEmpty()) {
                        refreshSuggestions();
                    }
                    return;
                }

                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    int next = Math.min(suggestionList.getModel().getSize() - 1, suggestionList.getSelectedIndex() + 1);
                    suggestionList.setSelectedIndex(Math.max(0, next));
                    suggestionList.ensureIndexIsVisible(suggestionList.getSelectedIndex());
                    e.consume();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                    int prev = Math.max(0, suggestionList.getSelectedIndex() - 1);
                    suggestionList.setSelectedIndex(prev);
                    suggestionList.ensureIndexIsVisible(prev);
                    e.consume();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    suggestionPopup.setVisible(false);
                    e.consume();
                }
            }
        });

        txtGlobalSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (fo == null || !SwingUtilities.isDescendingFrom(fo, suggestionPopup)) {
                        suggestionPopup.setVisible(false);
                    }
                });
            }
        });
    }

    private void refreshSuggestions() {
        String q = txtGlobalSearch.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            suggestionPopup.setVisible(false);
            return;
        }

        DefaultListModel<NavItem> model = (DefaultListModel<NavItem>) suggestionList.getModel();
        model.clear();

        List<NavItem> matches = new ArrayList<>();
        String[] tokens = q.split("\\s+");

        for (NavItem item : navItems) {
            if (item.matches(tokens)) matches.add(item);
        }

        matches.sort(java.util.Comparator.<NavItem>comparingInt(i -> i.score(q)).reversed());

        for (int i = 0; i < Math.min(matches.size(), 10); i++) {
            model.addElement(matches.get(i));
        }

        if (model.isEmpty()) {
            suggestionPopup.setVisible(false);
        } else {
            suggestionList.setSelectedIndex(0);
            if (!suggestionPopup.isVisible() && txtGlobalSearch.isShowing()) {
                suggestionPopup.show(txtGlobalSearch, 0, txtGlobalSearch.getHeight());
                txtGlobalSearch.requestFocusInWindow();
            }
        }
    }

    private void navigate(NavItem item) {
        if (item.sectionKey == null) navigateTo(item.moduleKey);
        else navigateTo(item.moduleKey, item.sectionKey);
        txtGlobalSearch.setText("");
        ModuleTheme.showToast(this, "Navigated to " + item.title);
    }

    private void performGlobalSearch() {
        // Handled by NavItem logic
    }
    public void navigateTo(String moduleKey) {
        if (moduleKey == null || moduleKey.trim().isEmpty()) return;
        cardLayout.show(pnlContent, moduleKey.trim());
    }

    public void navigateTo(String moduleKey, String sectionKey) {
        if (moduleKey == null || moduleKey.trim().isEmpty()) return;
        navigateTo(moduleKey);

        if (sectionKey == null || sectionKey.trim().isEmpty()) return;
        String section = sectionKey.trim();

        switch (moduleKey.trim()) {
            case "BOOK":
                if (bookModulePanel != null) bookModulePanel.showSection(section);
                break;
            case "TRANSACTION":
                if (procurementModulePanel != null) procurementModulePanel.showSection(section);
                break;
            case "CIRCULATION":
                if (circulationModulePanel != null) circulationModulePanel.showSection(section);
                break;
            case "ADMIN":
                if (adminModulePanel != null) adminModulePanel.showSection(section);
                break;
            default:
                // no-op (modules without sub-sections or unknown key)
                break;
        }
    }

    private void setSidebarCollapsed(boolean collapsed) {
        sidebarCollapsed = collapsed;

        pnlSidebar.setPreferredSize(new Dimension(collapsed ? SIDEBAR_WIDTH_COLLAPSED : SIDEBAR_WIDTH_EXPANDED, getHeight()));

        if (pnlMenu != null) {
            pnlMenu.setBorder(new EmptyBorder(40, collapsed ? 12 : 30, 0, 0));
        }
        if (pnlUser != null && pnlUser.getLayout() instanceof FlowLayout) {
            FlowLayout fl = (FlowLayout) pnlUser.getLayout();
            fl.setHgap(collapsed ? 12 : 30);
            fl.setVgap(20);
        }

        if (lblTitle != null) lblTitle.setVisible(!collapsed);
        if (lblUserName != null) lblUserName.setVisible(!collapsed);

        if (btnToggleSidebar != null) {
            btnToggleSidebar.setText(collapsed ? ">>" : "<<");
            btnToggleSidebar.setToolTipText(collapsed ? "Expand sidebar" : "Collapse sidebar");
        }

        // Menu buttons: icon-only in collapsed mode
        for (JButton btn : menuButtons) {
            String fullText = (String) btn.getClientProperty(CLIENT_PROP_FULL_TEXT);
            if (fullText == null) fullText = btn.getText();

            if (collapsed) {
                btn.setText("");
                btn.setToolTipText(fullText);
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setIconTextGap(0);
            } else {
                btn.setText(fullText);
                btn.setToolTipText(null);
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setIconTextGap(12);
            }
        }

        if (btnLogout != null) {
            if (collapsed) {
                btnLogout.setText("");
                btnLogout.setToolTipText("Logout");
                btnLogout.setIcon(new LetterIcon("O", 22, new Color(220, 80, 80), Color.WHITE));
            } else {
                btnLogout.setIcon(null);
                btnLogout.setText((String) btnLogout.getClientProperty(CLIENT_PROP_FULL_TEXT));
                btnLogout.setToolTipText(null);
            }
        }

        revalidate();
        repaint();
    }

    // --- HELPER: Create Menu Buttons (Icon + Text) ---
    private JButton createMenuButton(String text, String iconPath, String fallbackLetter) {
        JButton btn = new JButton(text);

        btn.putClientProperty(CLIENT_PROP_FULL_TEXT, text);
        btn.setIcon(loadMenuIcon(iconPath, fallbackLetter));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(12);

        // STYLE: Minimalist
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(COLOR_BLUE_DARK); // Dark Blue Text
        btn.setBackground(COLOR_WHITE);     // White Background
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // Make it look like just text
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(220, 44));

        // HOVER EFFECT: Slide right or Change Color
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(COLOR_GREEN); // Turn Green on hover
            }
            public void mouseExited(MouseEvent e) {
                btn.setForeground(COLOR_BLUE_DARK); // Back to Blue
            }
        });

        // CLICK ACTION: Switch Page
        btn.addActionListener(e -> cardLayout.show(pnlContent, text));

        menuButtons.add(btn);
        return btn;
    }

    private Icon loadMenuIcon(String path, String fallbackLetter) {
        ImageIcon icon = loadIconSafely(path, 22, 22);
        if (icon != null) return icon;
        return new LetterIcon(fallbackLetter, 22, COLOR_BLUE_DARK, COLOR_WHITE);
    }

    private void showUserDetailsPopup(Component invoker) {
        String userId = CurrentUserContext.getUserId();

        UserProfile profile = userDAO.getUserProfile(userId);

        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        addInfo(menu, "User ID", userId);
        addInfo(menu, "Name", CurrentUserContext.getDisplayName());
        addInfo(menu, "Role", CurrentUserContext.getRole());

        if (profile != null) {
            addInfo(menu, "Email", safe(profile.getEmail()));
            addInfo(menu, "Phone", profile.getPhoneNumber() == 0 ? "-" : String.valueOf(profile.getPhoneNumber()));
            addInfo(menu, "Status", safe(profile.getStatus()));
        } else {
            addInfo(menu, "Email", "-");
            addInfo(menu, "Phone", "-");
            addInfo(menu, "Status", "-");
        }

        menu.addSeparator();
        JMenuItem close = new JMenuItem("Close");
        close.addActionListener(e -> menu.setVisible(false));
        menu.add(close);

        int x = 10;
        int y = -menu.getPreferredSize().height - 10; // prefer showing above the user panel
        try {
            Point onScreen = invoker.getLocationOnScreen();
            if (onScreen.y + y < 0) y = invoker.getHeight() + 6; // fallback to below
        } catch (IllegalComponentStateException ignored) {
            y = invoker.getHeight() + 6;
        }
        menu.show(invoker, x, y);
    }

    private void addInfo(JPopupMenu menu, String label, String value) {
        JMenuItem item = new JMenuItem(label + ": " + (value == null || value.trim().isEmpty() ? "-" : value.trim()));
        item.setEnabled(false);
        menu.add(item);
    }

    private String safe(String s) {
        return s == null ? "-" : s.trim();
    }

    // --- HELPER: Content Page Placeholder ---
    private JPanel createPagePlaceholder(String title) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ModuleTheme.WHITE);
        wrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(ModuleTheme.WHITE);
        inner.setBorder(ModuleTheme.sectionBorder(title));

        JLabel lbl = new JLabel(title + " - MODULE UNDER DEVELOPMENT");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(new Color(140, 140, 140));
        inner.add(lbl);

        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    private ImageIcon loadIconSafely(String path, int w, int h) {
        File f = new File(path);
        if (f.exists()) return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        return null;
    }

    private static final class NavItem {
        final String moduleKey;
        final String sectionKey;
        final String title;
        final String keywords;

        private NavItem(String moduleKey, String sectionKey, String title, String keywords) {
            this.moduleKey = moduleKey;
            this.sectionKey = sectionKey;
            this.title = title;
            this.keywords = keywords == null ? "" : keywords;
        }

        static NavItem module(String moduleKey, String title, String... keywords) {
            return new NavItem(moduleKey, null, title, String.join(" ", keywords));
        }

        static NavItem section(String moduleKey, String sectionKey, String title, String... keywords) {
            return new NavItem(moduleKey, sectionKey, title, String.join(" ", keywords));
        }

        boolean matches(String[] tokens) {
            String hay = (title + " " + keywords).toLowerCase();
            for (String t : tokens) {
                if (!hay.contains(t)) return false;
            }
            return true;
        }

        int score(String query) {
            String t = title.toLowerCase();
            if (t.equals(query)) return 100;
            if (t.startsWith(query)) return 80;
            if (t.contains(query)) return 50;
            return 10;
        }

        @Override public String toString() { return title; }
    }
}
