package main;

/**
 * Simulates loading resources for the application.
 */
public class ResourceLoader {
    public void loadResource(String resourceName) {
        // Pretend to load a resource
        System.out.println("Loading resource: " + resourceName);
    }

    public boolean isResourceAvailable(String resourceName) {
        // Always return false to simulate unavailable resources
        return false;
    }
}