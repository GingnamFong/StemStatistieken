package nl.hva.ict.sm3.backend.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A helper-class used for traversing a directory structure which contains the Dutch election data.
 */
public class PathUtils {

    /**
     * Starting from @{code sourceLocation} searches the folder and any folder contained in it for files with the
     * specified {@code prefix}.
     * @param sourceLocation the starting point for the search.
     * @param prefix files beginning with this prefix are included in the resulting list.
     * @return A {@link List} containing all the files with the specified {@code prefix} find in this folder or any
     * folder contained in starting folder.
     * @throws IOException in case something went wrong while traversing the folders.
     */
    public static List<Path> findFilesToScan(String sourceLocation, String prefix) throws IOException {
        List<Path> filesToScan = new ArrayList<>();
        Files.walkFileTree(Path.of(sourceLocation), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.startsWith(prefix) && fileName.endsWith(".xml"))
                    filesToScan.add(file);
                return FileVisitResult.CONTINUE;
            }
        });
        return filesToScan;
    }

    /**
     * Transforms a {@code resourceName} into an absolute path of that resource. If it does not exist at the expected
     * location it tries to fall back to a folder called {@code data-files} or {@code Downloads}.
     * Also supports filesystem paths when resources are mounted as volumes.
     * @param resourceName the resource to locate.
     * @return a fully qualified absolute path to the resource.
     */
    public static String getResourcePath(String resourceName) {
        // Clean the resource name
        String cleanResourceName = resourceName;
        while (cleanResourceName.startsWith("/")) {
            cleanResourceName = cleanResourceName.substring(1);
        }
        
        // If it is an absolute directory name, we're done
        Path testPath = Path.of(resourceName);
        if (Files.exists(testPath) && Files.isDirectory(testPath)) {
            return resourceName;
        }
        
        // Try filesystem path first (for Docker volume mounts)
        String[] filesystemPaths = {
            "/app/src/main/resources/" + cleanResourceName,
            System.getProperty("user.dir") + "/src/main/resources/" + cleanResourceName,
            "./src/main/resources/" + cleanResourceName
        };
        
        for (String fsPath : filesystemPaths) {
            Path path = Path.of(fsPath);
            if (Files.exists(path) && Files.isDirectory(path)) {
                return fsPath;
            }
        }
        
        // Try classpath resource
        try {
            URL url = PathUtils.class.getResource(resourceName);
            if (url != null) {
                try {
                    return new File(url.toURI()).getPath();
                } catch (IllegalArgumentException e) {
                    // Handle jar: URLs - extract to temp directory
                    String urlString = url.toString();
                    if (urlString.startsWith("jar:")) {
                        return extractJarResource(resourceName);
                    } else {
                        throw e;
                    }
                }
            }

            // Try to find via classpath root
            URL rootUrl = PathUtils.class.getResource("/");
            if (rootUrl != null) {
                // If running from JAR, extract resources
                if ("jar".equals(rootUrl.getProtocol())) {
                    return extractJarResource("/" + cleanResourceName);
                }
                try {
                    URI projectRootURI = rootUrl.toURI();
                    String rootPath = projectRootURI.getPath();
                    
                    if (rootPath != null) {
                        String resourceFilePath = new File(rootPath, cleanResourceName).getPath();
                        if (Files.exists(Path.of(resourceFilePath)) && Files.isDirectory(Path.of(resourceFilePath))) {
                            return resourceFilePath;
                        }
                    }
                    
                    // Try walking up the directory tree
                    String resourceFilePath = null;
                    int attempts = 0;
                    while (resourceFilePath == null && rootPath != null && rootPath.length() > 5 && attempts < 10) {
                        attempts++;
                        // Try data-files folder
                        resourceFilePath = new File(rootPath, "data-files/" + cleanResourceName).getPath();
                        if (!Files.exists(Path.of(resourceFilePath))) {
                            // Try Downloads folder
                            resourceFilePath = new File(rootPath, "Downloads/" + cleanResourceName).getPath();
                        }
                        if (!Files.exists(Path.of(resourceFilePath))) {
                            resourceFilePath = null;
                            // Move up one directory
                            rootPath = new File(rootPath).getParent();
                        }
                    }
                    
                    if (resourceFilePath != null) {
                        return resourceFilePath;
                    }
                } catch (URISyntaxException | IllegalArgumentException e) {
                    // Fall through to return null
                }
            }
            
        } catch (Exception e) {
            // Log but don't fail - try filesystem paths instead
            System.err.println("Warning: Could not resolve resource via classpath: " + resourceName + " - " + e.getMessage());
        }
        
        // Last attempt: try direct filesystem path relative to current working directory
        Path directPath = Path.of(cleanResourceName);
        if (Files.exists(directPath) && Files.isDirectory(directPath)) {
            return directPath.toAbsolutePath().toString();
        }

        return null;
    }
    
    private static String extractJarResource(String resourceName) {
        System.err.println("DEBUG: ===== extractJarResource START =====");
        System.err.println("DEBUG: extractJarResource called with: " + resourceName);
        System.err.flush();
        
        try {
            String trimmedName = resourceName.startsWith("/") ? resourceName.substring(1) : resourceName;
            System.err.println("DEBUG: Trimmed resource name: " + trimmedName);
            System.err.flush();
            
            // Method 1: Try to get JAR location from the resource URL itself
            System.err.println("DEBUG: Method 1: Trying to get resource URL...");
            URL resourceUrl = PathUtils.class.getResource("/" + trimmedName);
            System.err.println("DEBUG: Direct resource URL: " + resourceUrl);
            if (resourceUrl == null) {
                resourceUrl = PathUtils.class.getClassLoader().getResource(trimmedName);
                System.err.println("DEBUG: ClassLoader resource URL: " + resourceUrl);
            }
            
            String jarPath = null;
            if (resourceUrl != null && "jar".equals(resourceUrl.getProtocol())) {
                // Extract JAR path from jar:file:/path/to/jar!/path/in/jar
                String urlString = resourceUrl.toString();
                System.err.println("DEBUG: Resource URL string: " + urlString);
                if (urlString.startsWith("jar:file:")) {
                    int exclamationIndex = urlString.indexOf("!");
                    if (exclamationIndex > 0) {
                        jarPath = urlString.substring(9, exclamationIndex); // Skip "jar:file:"
                        System.err.println("DEBUG: Method 1 SUCCESS - Extracted JAR path: " + jarPath);
                    }
                }
            }
            
            // Method 2: Try ProtectionDomain method (handles jar:file: and jar:nested:)
            if (jarPath == null) {
                System.err.println("DEBUG: Method 2: Trying ProtectionDomain...");
                try {
                    URL codeSourceUrl = PathUtils.class.getProtectionDomain().getCodeSource().getLocation();
                    System.err.println("DEBUG: Code source URL: " + codeSourceUrl);
                    
                    if (codeSourceUrl != null) {
                        String urlString = codeSourceUrl.toString();
                        System.err.println("DEBUG: Code source URL string: " + urlString);
                        
                        // Handle jar:nested: protocol (Spring Boot 3.2+)
                        if (urlString.startsWith("jar:nested:")) {
                            // Format: jar:nested:/app/app.jar/!BOOT-INF/classes/!/
                            int exclamationIndex = urlString.indexOf("!");
                            if (exclamationIndex > 0) {
                                jarPath = urlString.substring(11, exclamationIndex); // Skip "jar:nested:"
                                // Remove trailing slash if present
                                if (jarPath.endsWith("/")) {
                                    jarPath = jarPath.substring(0, jarPath.length() - 1);
                                }
                                System.err.println("DEBUG: Method 2 SUCCESS (nested) - Extracted JAR path: " + jarPath);
                            }
                        }
                        // Handle jar:file: protocol (traditional)
                        else if (urlString.startsWith("jar:file:")) {
                            int exclamationIndex = urlString.indexOf("!");
                            if (exclamationIndex > 0) {
                                jarPath = urlString.substring(9, exclamationIndex); // Skip "jar:file:"
                                System.err.println("DEBUG: Method 2 SUCCESS (file) - Extracted JAR path: " + jarPath);
                            }
                        }
                        // Handle file: protocol
                        else if ("file".equals(codeSourceUrl.getProtocol())) {
                            try {
                                jarPath = codeSourceUrl.toURI().getPath();
                                System.err.println("DEBUG: Method 2 SUCCESS - JAR path from URI: " + jarPath);
                            } catch (URISyntaxException e) {
                                jarPath = codeSourceUrl.getPath();
                                System.err.println("DEBUG: Method 2 SUCCESS - JAR path from getPath: " + jarPath);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG: Method 2 FAILED: " + e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Method 3: Try known Docker/Azure locations FIRST (most reliable)
            if (jarPath == null || !new File(jarPath).exists()) {
                System.err.println("DEBUG: Method 3: Trying known JAR locations...");
                String[] possiblePaths = {
                    "/app/app.jar",
                    System.getProperty("user.dir") + "/app.jar",
                    "./app.jar"
                };
                for (String possiblePath : possiblePaths) {
                    try {
                        File jarFile = new File(possiblePath);
                        System.err.println("DEBUG: Checking path: " + possiblePath + " exists: " + jarFile.exists());
                        if (jarFile.exists() && jarFile.isFile()) {
                            jarPath = jarFile.getAbsolutePath();
                            System.err.println("DEBUG: Method 3 SUCCESS - Found JAR at: " + jarPath);
                            break;
                        }
                    } catch (Exception e) {
                        System.err.println("DEBUG: Error checking " + possiblePath + ": " + e.getMessage());
                    }
                }
            }
            
            // Method 4: Try to find JAR from classpath
            if (jarPath == null || !new File(jarPath).exists()) {
                System.err.println("DEBUG: Method 4: Trying classpath...");
                String classPath = System.getProperty("java.class.path");
                System.err.println("DEBUG: Classpath: " + classPath);
                if (classPath != null && classPath.contains(".jar")) {
                    String separator = System.getProperty("path.separator", ":");
                    String[] paths = classPath.split(separator);
                    for (String path : paths) {
                        if (path.endsWith(".jar")) {
                            File f = new File(path);
                            if (f.exists()) {
                                jarPath = f.getAbsolutePath();
                                System.err.println("DEBUG: Method 4 SUCCESS - Found JAR: " + jarPath);
                                break;
                            }
                        }
                    }
                }
            }
            
            if (jarPath == null) {
                System.err.println("ERROR: All methods failed to find JAR path!");
                throw new IOException("Could not determine JAR file location after trying 4 methods");
            }
            
            // Handle Windows paths (remove leading /)
            if (jarPath.startsWith("/") && jarPath.length() > 2 && jarPath.charAt(2) == ':') {
                jarPath = jarPath.substring(1);
                System.err.println("DEBUG: Fixed Windows path: " + jarPath);
            }
            
            // Verify JAR file exists
            File jarFile = new File(jarPath);
            if (!jarFile.exists()) {
                throw new IOException("JAR file does not exist at: " + jarPath);
            }
            System.err.println("DEBUG: Verified JAR exists: " + jarPath + " (size: " + jarFile.length() + " bytes)");
            
            System.err.println("DEBUG: Final JAR path: " + jarPath);
            System.err.flush();
            
            // Create or get existing FileSystem for JAR
            URI jarUri = URI.create("jar:file:" + jarPath);
            System.err.println("DEBUG: JAR URI: " + jarUri);
            System.err.flush();
            
            // Try to get existing FileSystem first, create if it doesn't exist
            FileSystem fs;
            try {
                fs = FileSystems.getFileSystem(jarUri);
                System.err.println("DEBUG: Reusing existing JAR filesystem");
            } catch (Exception e) {
                System.err.println("DEBUG: Creating new JAR filesystem");
                fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
            }
            
            try {
                System.err.println("DEBUG: Opened JAR filesystem");
                
                // Try BOOT-INF/classes/ first (Spring Boot JAR structure)
                Path resourcePath = null;
                Path bootInfClasses = fs.getPath("/BOOT-INF/classes/");
                System.err.println("DEBUG: Checking for BOOT-INF/classes/: " + Files.exists(bootInfClasses));
                
                if (Files.exists(bootInfClasses)) {
                    System.err.println("DEBUG: Found BOOT-INF/classes/ (Spring Boot JAR)");
                    Path altResourcePath = bootInfClasses.resolve(trimmedName);
                    System.err.println("DEBUG: Trying Spring Boot path: " + altResourcePath);
                    if (Files.exists(altResourcePath)) {
                        resourcePath = altResourcePath;
                        System.err.println("DEBUG: ✓ Found resource in BOOT-INF/classes/!");
                    } else {
                        System.err.println("DEBUG: ✗ Resource not found at: " + altResourcePath);
                    }
                }
                
                // Fallback to root
                if (resourcePath == null) {
                    resourcePath = fs.getPath("/" + trimmedName);
                    System.err.println("DEBUG: Trying root path: " + resourcePath);
                    if (Files.exists(resourcePath)) {
                        System.err.println("DEBUG: ✓ Found resource at root!");
                    } else {
                        System.err.println("DEBUG: ✗ Resource not found at root");
                    }
                }
                
                if (!Files.exists(resourcePath)) {
                    // List JAR contents for debugging
                    System.err.println("DEBUG: Listing JAR contents for debugging...");
                    try {
                        Path root = fs.getPath("/");
                        System.err.println("DEBUG: JAR root contents:");
                        Files.list(root).limit(30).forEach(p -> System.err.println("  - " + p));
                        
                        if (Files.exists(bootInfClasses)) {
                            System.err.println("DEBUG: BOOT-INF/classes/ contents:");
                            Files.list(bootInfClasses).limit(30).forEach(p -> System.err.println("  - " + p));
                            
                            // Check if TK directories exist
                            Path tk2021Path = bootInfClasses.resolve("TK2021");
                            Path tk2023Path = bootInfClasses.resolve("TK2023");
                            Path tk2025Path = bootInfClasses.resolve("TK2025");
                            System.err.println("DEBUG: TK2021 exists: " + Files.exists(tk2021Path));
                            System.err.println("DEBUG: TK2023 exists: " + Files.exists(tk2023Path));
                            System.err.println("DEBUG: TK2025 exists: " + Files.exists(tk2025Path));
                        }
                    } catch (Exception listEx) {
                        System.err.println("DEBUG: Could not list JAR contents: " + listEx.getMessage());
                        listEx.printStackTrace();
                    }
                    throw new IOException("Resource not found in JAR: " + trimmedName + " (searched at: " + resourcePath + ")");
                }
                
                Path tempDir = Files.createTempDirectory("election-resources");
                tempDir.toFile().deleteOnExit();
                Path targetDir = tempDir.resolve(trimmedName);
                Files.createDirectories(targetDir);
                
                System.err.println("DEBUG: Created temp directory: " + targetDir);
                System.err.flush();
                
                if (Files.isDirectory(resourcePath)) {
                    // Check if directory is empty
                    boolean hasFiles = false;
                    long fileCount = 0;
                    try {
                        fileCount = Files.list(resourcePath).count();
                        hasFiles = fileCount > 0;
                        System.err.println("DEBUG: Directory has " + fileCount + " items (hasFiles: " + hasFiles + ")");
                    } catch (IOException e) {
                        System.err.println("DEBUG: Error checking directory: " + e.getMessage());
                    }
                    
                    if (!hasFiles) {
                        System.err.println("WARNING: Directory " + trimmedName + " exists in JAR but is empty!");
                        return targetDir.toString();
                    }
                    
                    // Copy entire directory recursively
                    System.err.println("DEBUG: Starting recursive copy from " + resourcePath + " to " + targetDir);
                    System.err.flush();
                    final Path finalResourcePath = resourcePath;
                    final Path finalTargetDir = targetDir;
                    Files.walkFileTree(resourcePath, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path relativePath = finalResourcePath.relativize(file);
                            Path targetFile = finalTargetDir.resolve(relativePath.toString());
                            Files.createDirectories(targetFile.getParent());
                            Files.copy(file, targetFile);
                            System.err.println("DEBUG: Extracted file: " + relativePath);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    System.err.println("DEBUG: Finished copying files");
                } else {
                    // Single file
                    Files.copy(resourcePath, targetDir.resolve(resourcePath.getFileName().toString()));
                    System.err.println("DEBUG: Extracted single file: " + resourcePath.getFileName());
                }
                
                System.err.println("DEBUG: ✓ Successfully extracted resource to: " + targetDir);
                System.err.println("DEBUG: ===== extractJarResource SUCCESS =====");
                System.err.flush();
                return targetDir.toString();
            } finally {
                // Don't close the FileSystem - it might be reused by other threads
                // The JVM will close it when the application shuts down
            }
        } catch (Exception e) {
            System.err.println("ERROR: ===== extractJarResource FAILED =====");
            System.err.println("ERROR: Exception type: " + e.getClass().getName());
            System.err.println("ERROR: Exception message: " + e.getMessage());
            System.err.println("ERROR: Stack trace:");
            e.printStackTrace();
            System.err.flush();
            throw new RuntimeException("Failed to extract JAR resource: " + resourceName + " - " + e.getMessage(), e);
        }
    }

}
