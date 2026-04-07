import java.io.File;
import com.library.setup.InstallationManager;

/**
 * 007 Security Test - Direct InstallationManager Testing
 * Bypasses GUI to test installation logic directly
 */
public class TestInstallation {
    
    public static void main(String[] args) {
        System.out.println("🧪 007 SECURITY TEST - Direct Installation Manager Test");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        try {
            // Set environment variables for Oracle connection
            System.setProperty("LMS_DB_URL", "jdbc:oracle:thin:@localhost:1521:xe");
            System.setProperty("LMS_DB_USER", "PRJ2531H");
            System.setProperty("LMS_DB_PASSWORD", "PRJ2531H");
            
            // Create test installation directory
            File installDir = new File("/tmp/lms-test-install");
            if (!installDir.exists()) {
                installDir.mkdirs();
            }
            
            // Create installation manager with test admin data
            InstallationManager.InstallationProgressListener listener = new InstallationManager.InstallationProgressListener() {
                @Override
                public void onProgress(String message) {
                    System.out.println("PROGRESS: " + message);
                }
                
                @Override
                public void onError(String error) {
                    System.out.println("ERROR: " + error);
                }
                
                @Override
                public void onComplete() {
                    System.out.println("COMPLETE: Installation finished");
                }
            };
            
            InstallationManager manager = new InstallationManager(
                installDir,
                "ADMIN",           // adminUserId
                "Administrator",   // adminName  
                "admin@lms.com",  // adminEmail
                "1234567890",     // adminPhone
                "admin123",       // adminPassword
                listener
            );
            
            System.out.println("📋 Starting installation test...");
            manager.install();
            
            System.out.println("✅ Installation test completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Installation test failed:");
            e.printStackTrace();
        }
    }
}