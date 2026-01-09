package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

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

    public DashboardFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Library Management System - Dashboard");
        setSize(1100, 750); // Slightly wider to accommodate the table
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. LEFT SIDEBAR ---
        pnlSidebar = new JPanel();
        pnlSidebar.setBackground(COLOR_WHITE);
        pnlSidebar.setPreferredSize(new Dimension(280, getHeight())); // Fixed width sidebar
        pnlSidebar.setLayout(new BorderLayout());
        pnlSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220))); // Subtle divider line

        // A. Sidebar Header (Logo + Title)
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        pnlHeader.setBackground(COLOR_WHITE);
        
        ImageIcon logoIcon = loadIconSafely("lib/icons/logo_small.png", 75, 75);
        JLabel lblLogo = new JLabel(logoIcon);
        
        JLabel lblTitle = new JLabel("<html>LIBRARY<br>MANAGEMENT<br>SYSTEM</html>");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(COLOR_BLUE_DARK);

        pnlHeader.add(lblLogo);
        pnlHeader.add(lblTitle);

        // B. Sidebar Menu (The Links)
        JPanel pnlMenu = new JPanel();
        pnlMenu.setBackground(COLOR_WHITE);
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setBorder(new EmptyBorder(40, 30, 0, 0)); // Padding: Top, Left

        // Add Menu Items (Matches your sketch)
        pnlMenu.add(createMenuButton("BOOK", "lib/icons/book.png"));
        pnlMenu.add(Box.createVerticalStrut(15));
        pnlMenu.add(createMenuButton("PURCHASE", "lib/icons/cart.png"));
        pnlMenu.add(Box.createVerticalStrut(15));
        pnlMenu.add(createMenuButton("STUDENT", "lib/icons/student.png"));
        pnlMenu.add(Box.createVerticalStrut(15));
        pnlMenu.add(createMenuButton("CIRCULATION", "lib/icons/sync.png"));
        
        // C. Sidebar Footer (User Profile)
        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        pnlUser.setBackground(COLOR_WHITE);
        
        JLabel lblUserIcon = new JLabel(loadIconSafely("lib/icons/user.png", 32, 32)); 
        JLabel lblUserName = new JLabel("<html><b>ADMIN USER</b><br><span style='font-size:10px; color:gray'>Logged In</span></html>");
        lblUserName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserName.setForeground(COLOR_BLUE_DARK);
        
        // Logout Button (Small text link style)
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setForeground(Color.RED);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            this.dispose();
            // new LoginFrame().setVisible(true); // Uncomment when LoginFrame is ready
        });

        pnlUser.add(lblUserIcon);
        pnlUser.add(lblUserName);
        pnlUser.add(btnLogout);

        // Assemble Sidebar
        pnlSidebar.add(pnlHeader, BorderLayout.NORTH);
        pnlSidebar.add(pnlMenu, BorderLayout.CENTER);
        pnlSidebar.add(pnlUser, BorderLayout.SOUTH);

        // --- 2. MAIN CONTENT AREA (Right Side) ---
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(COLOR_BG_CONTENT);

        // --- PAGE 1: BOOK (Placeholder) ---
        pnlContent.add(createPagePlaceholder("BOOK MANAGEMENT"), "BOOK");

        // --- PAGE 2: PURCHASE (Placeholder) ---
        pnlContent.add(createPagePlaceholder("PURCHASE ORDERS"), "PURCHASE");

        // --- PAGE 3: STUDENT (MVC INTEGRATION) ---
        // 1. Create the View
        StudentView studentView = new StudentView();
        // 2. Create the Controller (connects View to Logic/DAO)
        new StudentController(studentView); 
        // 3. Add the View to the dashboard
        pnlContent.add(studentView, "STUDENT");

        // --- PAGE 4: CIRCULATION (Placeholder) ---
        pnlContent.add(createPagePlaceholder("CIRCULATION"), "CIRCULATION");
        
        // Default View - Set to STUDENT for now so you see your changes immediately
        cardLayout.show(pnlContent, "STUDENT");

        // Add to Frame
        add(pnlSidebar, BorderLayout.WEST);
        add(pnlContent, BorderLayout.CENTER);
    }

    // --- HELPER: Create Menu Buttons (Text Only, Sketch Style) ---
    private JButton createMenuButton(String text, String iconPath) {
        JButton btn = new JButton(text);
        
        // STYLE: Minimalist Text
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(COLOR_BLUE_DARK); // Dark Blue Text
        btn.setBackground(COLOR_WHITE);     // White Background
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // Make it look like just text
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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

        return btn;
    }

    // --- HELPER: Content Page Placeholder ---
    private JPanel createPagePlaceholder(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(COLOR_BG_CONTENT);
        
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lbl.setForeground(new Color(200, 200, 200)); // Light Grey Text
        
        p.add(lbl);
        return p;
    }

    private ImageIcon loadIconSafely(String path, int w, int h) {
        File f = new File(path);
        if (f.exists()) return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        return null; 
    }
}