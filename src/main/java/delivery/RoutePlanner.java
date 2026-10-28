package delivery;

import java.util.List;

public class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(RouteStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy must not be null");
        }
        this.strategy = strategy;
    }

    public RouteStrategy getStrategy() {
        return strategy;
    }

    public RouteDecision plan(List<Route> routes, DeliveryRequest request) {
        return strategy.selectRoute(routes, request);
    }
}