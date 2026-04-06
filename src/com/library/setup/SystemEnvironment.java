package com.library.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects OS type and checks for required dependencies (Java 8, Oracle 10g XE)
 */
public class SystemEnvironment {
    
    public enum OSType {
        WINDOWS, LINUX, MAC, UNKNOWN
    }
    
    private OSType osType;
    private String osName;
    private String osVersion;
    private boolean javaInstalled;
    private String javaVersion;
    private String javaHome;
    private boolean oracleInstalled;
    private String oracleHome;
    private List<String> warnings;
    
    public SystemEnvironment() {
        this.warnings = new ArrayList<>();
        detectOS();
        detectJava();
        detectOracle();
    }
    
    public static OSType detectOSType() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            return OSType.WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OSType.LINUX;
        } else if (osName.contains("mac")) {
            return OSType.MAC;
        } else {
            return OSType.UNKNOWN;
        }
    }
    
    private void detectOS() {
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        
        String osLower = osName.toLowerCase();
        if (osLower.contains("win")) {
            osType = OSType.WINDOWS;
        } else if (osLower.contains("nix") || osLower.contains("nux") || osLower.contains("aix")) {
            osType = OSType.LINUX;
        } else if (osLower.contains("mac")) {
            osType = OSType.MAC;
        } else {
            osType = OSType.UNKNOWN;
            warnings.add("Unknown operating system: " + osName);
        }
    }
    
    private void detectJava() {
        javaVersion = System.getProperty("java.version");
        javaHome = System.getProperty("java.home");
        
        // Check if Java is installed and get version
        if (javaVersion != null && !javaVersion.isEmpty()) {
            javaInstalled = true;
            
            // Check if it's Java 8 (1.8.x)
            if (!javaVersion.startsWith("1.8")) {
                warnings.add("Java 8 (1.8.x) is recommended, but found: " + javaVersion);
            }
        } else {
            javaInstalled = false;
            warnings.add("Java is not installed or not detected");
        }
    }
    
    private void detectOracle() {
        // Check for Oracle environment variables
        oracleHome = System.getenv("ORACLE_HOME");
        
        if (oracleHome != null && !oracleHome.isEmpty()) {
            File oracleDir = new File(oracleHome);
            if (oracleDir.exists() && oracleDir.isDirectory()) {
                oracleInstalled = true;
                
                // Try to detect if it's Oracle 10g
                try {
                    String versionOutput = executeCommand(getOracleVersionCommand());
                    if (versionOutput != null && !versionOutput.contains("10g") && !versionOutput.contains("10.")) {
                        warnings.add("Oracle 10g XE is recommended. Found: " + versionOutput);
                    }
                } catch (Exception e) {
                    warnings.add("Could not verify Oracle version: " + e.getMessage());
                }
            } else {
                oracleInstalled = false;
                warnings.add("ORACLE_HOME points to non-existent directory: " + oracleHome);
            }
        } else {
            oracleInstalled = false;
            
            // Try alternative detection methods
            if (osType == OSType.WINDOWS) {
                // Check common Windows installation paths
                File[] oraclePaths = {
                    new File("C:\\oraclexe"),
                    new File("C:\\oracle\\product\\10.2.0"),
                    new File("C:\\app\\oracle\\product\\10.2.0")
                };
                for (File path : oraclePaths) {
                    if (path.exists()) {
                        oracleInstalled = true;
                        oracleHome = path.getAbsolutePath();
                        warnings.add("Oracle found at " + oracleHome + " but ORACLE_HOME not set");
                        break;
                    }
                }
            } else if (osType == OSType.LINUX) {
                // Check common Linux installation paths
                File[] oraclePaths = {
                    new File("/u01/app/oracle/product/10.2.0/db_1"),
                    new File("/usr/lib/oracle/xe"),
                    new File("/opt/oracle")
                };
                for (File path : oraclePaths) {
                    if (path.exists()) {
                        oracleInstalled = true;
                        oracleHome = path.getAbsolutePath();
                        warnings.add("Oracle found at " + oracleHome + " but ORACLE_HOME not set");
                        break;
                    }
                }
            }
            
            if (!oracleInstalled) {
                warnings.add("Oracle Database not detected. ORACLE_HOME environment variable not set.");
            }
        }
    }
    
    private String getOracleVersionCommand() {
        if (osType == OSType.WINDOWS) {
            return oracleHome + "\\bin\\sqlplus.exe -version";
        } else {
            return oracleHome + "/bin/sqlplus -version";
        }
    }
    
    private String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isEnvironmentReady() {
        return javaInstalled && oracleInstalled && warnings.isEmpty();
    }
    
    public boolean isJavaCompatible() {
        return javaInstalled && javaVersion.startsWith("1.8");
    }
    
    // Getters
    public OSType getOsType() { return osType; }
    public String getOsName() { return osName; }
    public String getOsVersion() { return osVersion; }
    public boolean isJavaInstalled() { return javaInstalled; }
    public String getJavaVersion() { return javaVersion; }
    public String getJavaHome() { return javaHome; }
    public boolean isOracleInstalled() { return oracleInstalled; }
    public String getOracleHome() { return oracleHome; }
    public List<String> getWarnings() { return warnings; }
    
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== System Environment ===\n");
        sb.append("OS: ").append(osName).append(" ").append(osVersion).append(" (").append(osType).append(")\n");
        sb.append("Java: ").append(javaInstalled ? "Installed (" + javaVersion + ")" : "NOT INSTALLED").append("\n");
        sb.append("Java Home: ").append(javaHome != null ? javaHome : "N/A").append("\n");
        sb.append("Oracle: ").append(oracleInstalled ? "Installed" : "NOT INSTALLED").append("\n");
        sb.append("Oracle Home: ").append(oracleHome != null ? oracleHome : "N/A").append("\n");
        sb.append("\n");
        
        if (!warnings.isEmpty()) {
            sb.append("=== Warnings ===\n");
            for (String warning : warnings) {
                sb.append("• ").append(warning).append("\n");
            }
        }
        
        sb.append("\nEnvironment Ready: ").append(isEnvironmentReady() ? "YES" : "NO");
        return sb.toString();
    }
}
