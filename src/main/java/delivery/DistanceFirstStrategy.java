package delivery;

import java.util.List;

public class DistanceFirstStrategy extends AbstractRouteStrategy {

    @Override
    public String getStrategyName() {
        return "DistanceFirst";
    }

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.distanceKm();
    }

    @Override
    protected String explain(Route route, DeliveryRequest request, double score) {
        return String.format("Shortest valid distance: %.1f km", route.distanceKm());
    }
}