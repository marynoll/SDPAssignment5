package delivery;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        List<Route> routes = SampleData.routes();

        RouteStrategy fastest = new FastestRouteStrategy();
        RouteStrategy cheapest = new CheapestRouteStrategy();
        RouteStrategy eco = new EcoFriendlyRouteStrategy();
        RouteStrategy balanced = new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20);

        DeliveryRequest requestA = new DeliveryRequest(101, DeliveryPriority.URGENT, 10000, 120, false);
        DeliveryRequest requestB = new DeliveryRequest(102, DeliveryPriority.ECONOMY, 4000, 120, false);
        DeliveryRequest requestC = new DeliveryRequest(103, DeliveryPriority.ECO, 10000, 120, true);
        DeliveryRequest requestD = new DeliveryRequest(104, DeliveryPriority.STANDARD, 10000, 120, false);
        DeliveryRequest requestE = new DeliveryRequest(107, DeliveryPriority.SHORT_DISTANCE, 10000, 120, false);

        System.out.println("=== 1. RUNTIME STRATEGY SWITCHING (same planner, request #101) ===");
        RoutePlanner planner = new RoutePlanner(fastest);
        System.out.println(planner.plan(routes, requestA).explain());
        planner.setStrategy(cheapest);
        System.out.println(planner.plan(routes, requestA).explain());
        planner.setStrategy(eco);
        System.out.println(planner.plan(routes, requestA).explain());
        planner.setStrategy(balanced);
        System.out.println(planner.plan(routes, requestA).explain());
        System.out.println("=== 2. AUTOMATIC STRATEGY SELECTION ===");
        StrategySelector selector = new StrategySelector(balanced)
                .addRule(r -> r.priority() == DeliveryPriority.URGENT, fastest)
                .addRule(r -> r.priority() == DeliveryPriority.ECONOMY, cheapest)
                .addRule(r -> r.priority() == DeliveryPriority.ECO, eco)
                .addRule(r -> r.priority() == DeliveryPriority.SHORT_DISTANCE, new DistanceFirstStrategy());
        for (DeliveryRequest request : List.of(requestA, requestB, requestC, requestD, requestE)) {
            planner.setStrategy(selector.select(request));
            System.out.println("Request #" + request.id() + " (" + request.priority() + ")");
            System.out.println(planner.plan(routes, request).explain());
            System.out.println("=== 3. STRATEGY COMPARISON MODE ===");
            DeliveryRequest request105 = new DeliveryRequest(105, DeliveryPriority.STANDARD, 7000, 90, false);
            List<RouteStrategy> all = List.of(fastest, cheapest, eco, balanced, new DistanceFirstStrategy());
            Map<String, RouteDecision> results = new StrategyComparator().compareAll(all, routes, request105);
            System.out.println("DELIVERY REQUEST #" + request105.id());
            for (RouteStrategy strategy : all) {
                RouteDecision d = results.get(strategy.getStrategyName());
                System.out.println(strategy.getStrategyName() + " Strategy:");
                if (d == null) {
                    System.out.println("  no suitable route");
                } else {
                    System.out.printf("  Route %s | Time: %d min | Cost: %.0f | CO2: %.1f kg | Score: %.2f%n",
                            d.route().id(), d.route().effectiveMinutes(), d.route().cost(),
                            d.route().carbonEmission(), d.score());
                }
            }

            System.out.println();
            System.out.println("=== 4. FAILURE SCENARIOS ===");
            try {
                planner.setStrategy(fastest);
                planner.plan(List.of(), requestA);
            } catch (NoSuitableRouteException e) {
                System.out.println("Empty list -> " + e.getMessage());
            }
            try {
                planner.plan(routes, new DeliveryRequest(106, DeliveryPriority.URGENT, 100, 5, false));
            } catch (NoSuitableRouteException e) {
                System.out.println("Impossible request -> " + e.getMessage());
            }
            try {
                new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5);
            } catch (IllegalArgumentException e) {
                System.out.println("Bad weights -> " + e.getMessage());
            }
        }
    }
}