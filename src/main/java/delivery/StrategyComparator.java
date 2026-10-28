package delivery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StrategyComparator {

    public Map<String, RouteDecision> compareAll(List<RouteStrategy> strategies,
                                                 List<Route> routes, DeliveryRequest request) {
        Map<String, RouteDecision> results = new LinkedHashMap<>();
        RoutePlanner planner = new RoutePlanner(strategies.get(0));
        for (RouteStrategy strategy : strategies) {
            planner.setStrategy(strategy);
            try {
                results.put(strategy.getStrategyName(), planner.plan(routes, request));
            } catch (NoSuitableRouteException e) {
            }
        }
        return results;
    }
}