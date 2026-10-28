package delivery;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRouteStrategy implements RouteStrategy {

    protected static final double DISTANCE_RATE = 50.0;     // money per km
    protected static final double URGENT_SURCHARGE = 0.20;  // +20% of base cost

    @Override
    public final RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request is missing");
        }
        if (routes == null || routes.isEmpty()) {
            throw new NoSuitableRouteException("Route list is empty");
        }

        List<Route> valid = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        for (Route route : routes) {
            String problem = findProblem(route, request);
            if (problem == null) {
                valid.add(route);
            } else {
                rejected.add("Route " + route.id() + " rejected: " + problem);
            }
        }
        if (valid.isEmpty()) {
            throw new NoSuitableRouteException(
                    "No route satisfies the constraints. " + String.join(" ", rejected));
        }

        Route best = null;
        double bestScore = Double.MAX_VALUE;
        for (Route route : valid) {
            double score = score(route, request, valid);
            if (score < bestScore) {
                bestScore = score;
                best = route;
            }
        }
        return new RouteDecision(best, getStrategyName(), bestScore, explain(best, request, bestScore), rejected);
    }

    private String findProblem(Route route, DeliveryRequest request) {
        if (!route.available()) {
            return "route is unavailable.";
        }
        if (totalCost(route, request) > request.maxBudget()) {
            return "budget exceeded.";
        }
        if (route.effectiveMinutes() > request.maxDeliveryMinutes()) {
            return "delivery deadline exceeded.";
        }
        return null;
    }

    protected double totalCost(Route route, DeliveryRequest request) {
        double surcharge = request.priority() == DeliveryPriority.URGENT
                ? route.cost() * URGENT_SURCHARGE : 0;
        return route.cost() + route.distanceKm() * DISTANCE_RATE + surcharge;
    }

    protected abstract double score(Route route, DeliveryRequest request, List<Route> candidates);

    protected abstract String explain(Route route, DeliveryRequest request, double score);
}