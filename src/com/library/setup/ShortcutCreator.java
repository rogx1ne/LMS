package com.library.setup;

import java.io.*;

/**
 * Creates desktop shortcuts for LMS application
 */
public class ShortcutCreator {
    
    private File installDirectory;
    private File launcherScript;
    private SystemEnvironment.OSType osType;
    
    public ShortcutCreator(File installDirectory, File launcherScript, SystemEnvironment.OSType osType) {
        this.installDirectory = installDirectory;
        this.launcherScript = launcherScript;
        this.osType = osType;
    }
    
    /**
     * Create desktop shortcut
     */
    public void createShortcut() throws Exception {
        if (osType == SystemEnvironment.OSType.WINDOWS) {
            createWindowsShortcut();
        } else if (osType == SystemEnvironment.OSType.LINUX) {
            createLinuxDesktopFile();
        }
    }
    
    /**
     * Create .desktop file for Linux
     */
    private void createLinuxDesktopFile() throws IOException {
        // Create .desktop file in user's applications directory
        String userHome = System.getProperty("user.home");
        File applicationsDir = new File(userHome, ".local/share/applications");
        
        if (!applicationsDir.exists()) {
            applicationsDir.mkdirs();
        }
        
        File desktopFile = new File(applicationsDir, "lms.desktop");
        
        StringBuilder content = new StringBuilder();
        content.append("[Desktop Entry]\n");
        content.append("Version=1.0\n");
        content.append("Type=Application\n");
        content.append("Name=Library Management System\n");
        content.append("Comment=Library Management Application\n");
        content.append("Exec=").append(launcherScript.getAbsolutePath()).append("\n");
        content.append("Path=").append(installDirectory.getAbsolutePath()).append("\n");
        content.append("Terminal=false\n");
        content.append("Categories=Office;Education;Java;\n");
        
        // Check if icon exists
        File iconFile = new File(installDirectory, "icon.png");
        if (iconFile.exists()) {
            content.append("Icon=").append(iconFile.getAbsolutePath()).append("\n");
        } else {
            content.append("Icon=applications-office\n");
        }
        
        try (FileWriter writer = new FileWriter(desktopFile)) {
            writer.write(content.toString());
        }
        
        // Make desktop file executable
        Runtime.getRuntime().exec("chmod +x " + desktopFile.getAbsolutePath());
        
        System.out.println("Created desktop shortcut: " + desktopFile.getAbsolutePath());
        
        // Also create on desktop if it exists
        File desktop = new File(userHome, "Desktop");
        if (desktop.exists() && desktop.isDirectory()) {
            File desktopShortcut = new File(desktop, "LMS.desktop");
            try (FileWriter writer = new FileWriter(desktopShortcut)) {
                writer.write(content.toString());
            }
            Runtime.getRuntime().exec("chmod +x " + desktopShortcut.getAbsolutePath());
            System.out.println("Created desktop shortcut: " + desktopShortcut.getAbsolutePath());
        }
    }
    
    /**
     * Create Windows shortcut using VBScript
     */
    private void createWindowsShortcut() throws Exception {
        String userHome = System.getProperty("user.home");
        File desktop = new File(userHome, "Desktop");
        
        File shortcutFile = new File(desktop, "Library Management System.lnk");
        
        // Create VBScript to generate shortcut
        File vbsScript = new File(installDirectory, "create_shortcut.vbs");
        
        StringBuilder vbsContent = new StringBuilder();
        vbsContent.append("Set oWS = WScript.CreateObject(\"WScript.Shell\")\n");
        vbsContent.append("sLinkFile = \"").append(shortcutFile.getAbsolutePath()).append("\"\n");
        vbsContent.append("Set oLink = oWS.CreateShortcut(sLinkFile)\n");
        vbsContent.append("oLink.TargetPath = \"").append(launcherScript.getAbsolutePath()).append("\"\n");
        vbsContent.append("oLink.WorkingDirectory = \"").append(installDirectory.getAbsolutePath()).append("\"\n");
        vbsContent.append("oLink.Description = \"Library Management System\"\n");
        vbsContent.append("oLink.WindowStyle = 1\n");
        
        // Check if icon exists
        File iconFile = new File(installDirectory, "icon.ico");
        if (iconFile.exists()) {
            vbsContent.append("oLink.IconLocation = \"").append(iconFile.getAbsolutePath()).append("\"\n");
        }
        
        vbsContent.append("oLink.Save\n");
        
        try (FileWriter writer = new FileWriter(vbsScript)) {
            writer.write(vbsContent.toString());
        }
        
        // Execute VBScript
        Process process = Runtime.getRuntime().exec("cscript //NoLogo " + vbsScript.getAbsolutePath());
        process.waitFor();
        
        // Delete temporary VBS file
        vbsScript.delete();
        
        if (shortcutFile.exists()) {
            System.out.println("Created desktop shortcut: " + shortcutFile.getAbsolutePath());
        } else {
            System.err.println("Failed to create desktop shortcut");
        }
        
        // Also create in Start Menu
        createStartMenuShortcut();
    }
    
    /**
     * Create Start Menu shortcut (Windows only)
     */
    private void createStartMenuShortcut() throws Exception {
        String appData = System.getenv("APPDATA");
        if (appData == null) {
            return;
        }
        
        File startMenuDir = new File(appData, "Microsoft\\Windows\\Start Menu\\Programs");
        File lmsFolder = new File(startMenuDir, "Library Management System");
        
        if (!lmsFolder.exists()) {
            lmsFolder.mkdirs();
        }
        
        File shortcutFile = new File(lmsFolder, "Library Management System.lnk");
        
        // Create VBScript
        File vbsScript = new File(installDirectory, "create_startmenu.vbs");
        
        StringBuilder vbsContent = new StringBuilder();
        vbsContent.append("Set oWS = WScript.CreateObject(\"WScript.Shell\")\n");
        vbsContent.append("sLinkFile = \"").append(shortcutFile.getAbsolutePath()).append("\"\n");
        vbsContent.append("Set oLink = oWS.CreateShortcut(sLinkFile)\n");
        vbsContent.append("oLink.TargetPath = \"").append(launcherScript.getAbsolutePath()).append("\"\n");
        vbsContent.append("oLink.WorkingDirectory = \"").append(installDirectory.getAbsolutePath()).append("\"\n");
        vbsContent.append("oLink.Description = \"Library Management System\"\n");
        vbsContent.append("oLink.Save\n");
        
        try (FileWriter writer = new FileWriter(vbsScript)) {
            writer.write(vbsContent.toString());
        }
        
        Process process = Runtime.getRuntime().exec("cscript //NoLogo " + vbsScript.getAbsolutePath());
        process.waitFor();
        
        vbsScript.delete();
        
        if (shortcutFile.exists()) {
            System.out.println("Created Start Menu shortcut: " + shortcutFile.getAbsolutePath());
        }
    }
}
