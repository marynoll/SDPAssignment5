package delivery;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class BalancedRouteStrategy extends AbstractRouteStrategy {

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    public BalancedRouteStrategy(double timeWeight, double costWeight,
                                 double distanceWeight, double emissionWeight) {
        if (timeWeight < 0 || costWeight < 0 || distanceWeight < 0 || emissionWeight < 0) {
            throw new IllegalArgumentException("Weights must not be negative");
        }
        double sum = timeWeight + costWeight + distanceWeight + emissionWeight;
        if (Math.abs(sum - 1.0) > 1e-9) {
            throw new IllegalArgumentException("Weights must sum to 1.0 but sum is " + sum);
        }
        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public String getStrategyName() {
        return "Balanced";
    }

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return normalize(route.effectiveMinutes(), candidates, Route::effectiveMinutes) * timeWeight
                + normalize(totalCost(route, request), candidates, r -> totalCost(r, request)) * costWeight
                + normalize(route.distanceKm(), candidates, Route::distanceKm) * distanceWeight
                + normalize(route.carbonEmission(), candidates, Route::carbonEmission) * emissionWeight;
    }

    private double normalize(double value, List<Route> candidates, ToDoubleFunction<Route> field) {
        double min = candidates.stream().mapToDouble(field).min().orElse(0);
        double max = candidates.stream().mapToDouble(field).max().orElse(0);
        return max == min ? 0 : (value - min) / (max - min);
    }

    @Override
    protected String explain(Route route, DeliveryRequest request, double score) {
        return String.format("Best weighted score %.2f (time %.2f, cost %.2f, distance %.2f, emission %.2f)",
                score, timeWeight, costWeight, distanceWeight, emissionWeight);
    }
}