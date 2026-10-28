package delivery;

import java.util.List;

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

        System.out.println("=== 1. RUNTIME STRATEGY SWITCHING (same planner, request #101) ===");
        RoutePlanner planner = new RoutePlanner(fastest);
        System.out.println(planner.plan(routes, requestA).explain());
        planner.setStrategy(cheapest);
        System.out.println(planner.plan(routes, requestA).explain());
        planner.setStrategy(eco);
        System.out.println(planner.plan(routes, requestA).explain());
        planner.setStrategy(balanced);
        System.out.println(planner.plan(routes, requestA).explain());
    }
}