package dev.applytrack.backend.error;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Class<?> resourceType, Object id) {
        super("%s with id '%s' not found".formatted(resourceType.getSimpleName(), id));
    }
}