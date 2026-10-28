package delivery;

import java.util.List;

public class CheapestRouteStrategy extends AbstractRouteStrategy {

    @Override
    public String getStrategyName() {
        return "Cheapest";
    }

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return totalCost(route, request);
    }

    @Override
    protected String explain(Route route, DeliveryRequest request, double score) {
        double distanceCost = route.distanceKm() * DISTANCE_RATE;
        double surcharge = totalCost(route, request) - route.cost() - distanceCost;
        return String.format("Total cost %.0f = base %.0f + distance %.1f km x %.0f = %.0f + priority surcharge %.0f",
                totalCost(route, request), route.cost(), route.distanceKm(), DISTANCE_RATE, distanceCost, surcharge);
    }
}