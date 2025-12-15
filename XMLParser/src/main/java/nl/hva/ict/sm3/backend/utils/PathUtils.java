package nl.hva.ict.sm3.backend.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
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
                    // Handle jar: URLs - extract the path part
                    String urlString = url.toString();
                    if (urlString.startsWith("jar:")) {
                        // For jar resources, try to extract path or use alternative
                        // For now, fall through to filesystem search
                    } else {
                        throw e;
                    }
                }
            }

            // Try to find via classpath root
            URL rootUrl = PathUtils.class.getResource("/");
            if (rootUrl != null) {
                try {
                    URI projectRootURI = rootUrl.toURI();
                    String rootPath = projectRootURI.getPath();
                    
                    // Handle jar:file: URIs
                    if (rootPath == null && rootUrl.toString().startsWith("jar:")) {
                        // Extract path from jar URL: jar:file:/path/to.jar!/ 
                        String jarUrl = rootUrl.toString();
                        int exclMark = jarUrl.indexOf('!');
                        if (exclMark > 0) {
                            String jarPath = jarUrl.substring(4, exclMark); // Remove "jar:" prefix
                            // For jar resources, try filesystem fallback
                            rootPath = new File(jarPath).getParent();
                        }
                    }
                    
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

}
