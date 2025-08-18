import java.io.*;
import java.net.*;
import java.nio.file.*;

public class DownloadWrapper {
    public static void main(String[] args) {
        String url = "https://github.com/gradle/gradle/raw/v8.10.2/gradle/wrapper/gradle-wrapper.jar";
        String outputPath = "gradle/wrapper/gradle-wrapper.jar";
        
        try {
            System.out.println("Downloading Gradle Wrapper...");
            
            // Create directory if it doesn't exist
            Path wrapperDir = Paths.get("gradle/wrapper");
            if (!Files.exists(wrapperDir)) {
                Files.createDirectories(wrapperDir);
                System.out.println("Created directory: " + wrapperDir);
            }
            
            // Download the file
            URL downloadUrl = new URL(url);
            try (InputStream in = downloadUrl.openStream();
                 FileOutputStream out = new FileOutputStream(outputPath)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                
                System.out.println("Successfully downloaded gradle-wrapper.jar (" + totalBytes + " bytes)");
                
                // Verify the file exists
                File jarFile = new File(outputPath);
                if (jarFile.exists() && jarFile.length() > 0) {
                    System.out.println("✓ Gradle Wrapper is ready!");
                    System.out.println("You can now run: gradlew.bat clean build");
                } else {
                    System.out.println("✗ Download failed or file is empty");
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error downloading Gradle Wrapper: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
