package com.knitos.ota;

import android.app.Activity;
import android.os.Bundle;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * KnitOS OTA Update Manager
 * One-click update system for KnitOS
 */
public class MainActivity extends Activity {
    
    private static final String TAG = "KnitOS_OTA";
    private static final String MANIFEST_URL = 
        "https://raw.githubusercontent.com/KnitOS/ota/main/knitos_update.json";
    
    private TextView mStatusText;
    private TextView mVersionText;
    private Button mUpdateButton;
    private ProgressBar mProgressBar;
    private UpdateEngine mUpdateEngine;
    private ExecutorService mExecutor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mStatusText = findViewById(R.id.status_text);
        mVersionText = findViewById(R.id.version_text);
        mUpdateButton = findViewById(R.id.update_button);
        mProgressBar = findViewById(R.id.progress_bar);
        
        mExecutor = Executors.newSingleThreadExecutor();
        mUpdateEngine = new UpdateEngine();
        
        // Display current version
        String currentVersion = getSystemProperty("ro.knitos.version");
        mVersionText.setText("Current Version: " + currentVersion);
        
        // Setup update button
        mUpdateButton.setOnClickListener(v -> checkForUpdates());
        
        // Check for updates on startup
        checkForUpdates();
    }
    
    private void checkForUpdates() {
        mStatusText.setText("Checking for updates...");
        mUpdateButton.setEnabled(false);
        
        mExecutor.execute(() -> {
            try {
                UpdateInfo updateInfo = fetchUpdateManifest();
                
                if (updateInfo != null && isNewer(updateInfo.version)) {
                    runOnUiThread(() -> showUpdateAvailable(updateInfo));
                } else {
                    runOnUiThread(() -> {
                        mStatusText.setText("Your system is up to date");
                        mUpdateButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking updates", e);
                runOnUiThread(() -> {
                    mStatusText.setText("Error checking for updates");
                    mUpdateButton.setEnabled(true);
                });
            }
        });
    }
    
    private UpdateInfo fetchUpdateManifest() throws Exception {
        URL url = new URL(MANIFEST_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String json = new String(conn.getInputStream().readAllBytes());
            return parseUpdateJson(json);
        }
        
        return null;
    }
    
    private UpdateInfo parseUpdateJson(String json) {
        // Simple JSON parsing (in production use proper library)
        try {
            UpdateInfo info = new UpdateInfo();
            info.version = extractJsonValue(json, "version");
            info.downloadUrl = extractJsonValue(json, "download_url");
            info.checksum = extractJsonValue(json, "checksum_sha256");
            info.changelog = extractJsonValue(json, "changelog");
            return info;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON", e);
            return null;
        }
    }
    
    private String extractJsonValue(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", keyIndex);
        int startIndex = json.indexOf("\"", colonIndex) + 1;
        int endIndex = json.indexOf("\"", startIndex);
        
        return json.substring(startIndex, endIndex);
    }
    
    private boolean isNewer(String newVersion) {
        String currentVersion = getSystemProperty("ro.knitos.version");
        return newVersion.compareTo(currentVersion) > 0;
    }
    
    private void showUpdateAvailable(UpdateInfo updateInfo) {
        mStatusText.setText("Update available: " + updateInfo.version);
        mUpdateButton.setText("Download and Update");
        mUpdateButton.setEnabled(true);
        
        mUpdateButton.setOnClickListener(v -> {
            downloadAndUpdate(updateInfo);
        });
    }
    
    private void downloadAndUpdate(UpdateInfo updateInfo) {
        mStatusText.setText("Downloading update...");
        mUpdateButton.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        
        mExecutor.execute(() -> {
            try {
                File updateFile = downloadPackage(updateInfo.downloadUrl);
                
                // Verify checksum
                if (!verifyChecksum(updateFile, updateInfo.checksum)) {
                    throw new SecurityException("Checksum verification failed");
                }
                
                // Apply update
                applyUpdate(updateFile);
                
            } catch (Exception e) {
                Log.e(TAG, "Update failed", e);
                runOnUiThread(() -> {
                    mStatusText.setText("Update failed: " + e.getMessage());
                    mUpdateButton.setEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                });
            }
        });
    }
    
    private File downloadPackage(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        File tempFile = File.createTempFile("knitos_update_", ".zip");
        tempFile.deleteOnExit();
        
        try (var input = conn.getInputStream();
             var output = new java.io.FileOutputStream(tempFile)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;
            long contentLength = conn.getContentLengthLong();
            
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                
                // Update progress
                int progress = (int)((totalBytes * 100) / contentLength);
                runOnUiThread(() -> mProgressBar.setProgress(progress));
            }
        }
        
        return tempFile;
    }
    
    private boolean verifyChecksum(File file, String expectedChecksum) {
        // Implement SHA-256 verification
        // For now, return true (implement proper checksum verification)
        return true;
    }
    
    private void applyUpdate(File updateFile) throws Exception {
        runOnUiThread(() -> {
            mStatusText.setText("Installing update...");
            mProgressBar.setIndeterminate(true);
        });
        
        // Use UpdateEngine for A/B devices
        mUpdateEngine.applyPayload(
            "file://" + updateFile.getAbsolutePath(),
            0, // offset
            0, // size
            0, // metadata size
            null // headers
        );
        
        // Alternative: Use RecoverySystem for non-A/B devices
        // RecoverySystem.installPackage(this, updateFile);
    }
    
    private String getSystemProperty(String key) {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            return (String) SystemProperties.getMethod("get", String.class)
                .invoke(null, key);
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private static class UpdateInfo {
        String version;
        String downloadUrl;
        String checksum;
        String changelog;
    }
}
