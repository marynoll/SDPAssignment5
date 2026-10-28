package delivery;

import java.util.List;

public class FastestRouteStrategy extends AbstractRouteStrategy {

    @Override
    public String getStrategyName() {
        return "Fastest";
    }

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.effectiveMinutes(); // planned time + traffic penalty
    }

    @Override
    protected String explain(Route route, DeliveryRequest request, double score) {
        return String.format("Estimated time: %d min, traffic penalty: +%d min, final time: %d min",
                route.estimatedMinutes(), route.trafficDelayMinutes(), route.effectiveMinutes());
    }
}