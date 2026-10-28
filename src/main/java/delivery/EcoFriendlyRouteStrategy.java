package delivery;

import java.util.List;

public class EcoFriendlyRouteStrategy extends AbstractRouteStrategy {

    private static final double EMISSION_WEIGHT = 1.0;
    private static final double DISTANCE_WEIGHT = 0.05;

    @Override
    public String getStrategyName() {
        return "Eco";
    }

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.carbonEmission() * EMISSION_WEIGHT + route.distanceKm() * DISTANCE_WEIGHT;
    }

    @Override
    protected String explain(Route route, DeliveryRequest request, double score) {
        return String.format("CO2: %.1f kg, distance: %.1f km, eco score: %.2f",
                route.carbonEmission(), route.distanceKm(), score);
    }
}