package delivery;

public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double cost,
        double carbonEmission,
        int trafficDelayMinutes,
        boolean available) {

    public Route {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Route id is required");
        }
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Route " + id + " needs origin and destination");
        }
        if (distanceKm < 0 || estimatedMinutes < 0 || cost < 0
                || carbonEmission < 0 || trafficDelayMinutes < 0) {
            throw new IllegalArgumentException("Route " + id + " has a negative value");
        }
    }

    public int effectiveMinutes() {
        return estimatedMinutes + trafficDelayMinutes;
    }
}