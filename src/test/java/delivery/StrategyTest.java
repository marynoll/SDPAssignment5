package delivery;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StrategyTest {

    private final List<Route> routes = SampleData.routes();
    private final Location a = new Location("A", 51.0, 71.0);
    private final Location b = new Location("B", 51.1, 71.1);

    private final DeliveryRequest urgent = new DeliveryRequest(1, DeliveryPriority.URGENT, 10000, 120, false);
    private final DeliveryRequest standard = new DeliveryRequest(2, DeliveryPriority.STANDARD, 10000, 120, false);
    private final DeliveryRequest economy = new DeliveryRequest(3, DeliveryPriority.ECONOMY, 4000, 120, false);

    private Route route(String id, double km, int minutes, double cost, double co2, int traffic, boolean available) {
        return new Route(id, a, b, km, minutes, cost, co2, traffic, available);
    }


    @Test
    void fastestPicksLowestFinalTime() {
        assertEquals("R1", new FastestRouteStrategy().selectRoute(routes, urgent).route().id());
    }

    @Test
    void fastestCountsTrafficPenalty() {
        Route quickButJammed = route("X", 10, 20, 1000, 1, 30, true); // 50 min in reality
        Route slowButClear = route("Y", 10, 40, 1000, 1, 0, true);    // 40 min in reality
        RouteDecision d = new FastestRouteStrategy().selectRoute(List.of(quickButJammed, slowButClear), standard);
        assertEquals("Y", d.route().id());
    }

    @Test
    void cheapestPicksLowestTotalCost() {
        assertEquals("R5", new CheapestRouteStrategy().selectRoute(routes, standard).route().id());
    }

    @Test
    void cheapestAddsUrgentSurcharge() {
        List<Route> one = List.of(route("X", 10, 30, 1000, 1, 0, true));
        double normal = new CheapestRouteStrategy().selectRoute(one, standard).score();
        double rush = new CheapestRouteStrategy().selectRoute(one, urgent).score();
        assertEquals(1500.0, normal, 0.001);  // 1000 + 10 km * 50
        assertEquals(1700.0, rush, 0.001);    // + 20% of 1000
    }

    @Test
    void ecoPicksLowestEmissionScore() {
        assertEquals("R4", new EcoFriendlyRouteStrategy().selectRoute(routes, standard).route().id());
    }

    @Test
    void balancedPicksBestCompromise() {
        RouteStrategy balanced = new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20);
        assertEquals("R2", balanced.selectRoute(routes, standard).route().id());
    }

    @Test
    void balancedWeightsChangeTheResult() {
        RouteStrategy timeOnly = new BalancedRouteStrategy(1.0, 0.0, 0.0, 0.0);
        RouteStrategy emissionOnly = new BalancedRouteStrategy(0.0, 0.0, 0.0, 1.0);
        assertEquals("R1", timeOnly.selectRoute(routes, standard).route().id());
        assertEquals("R4", emissionOnly.selectRoute(routes, standard).route().id());
    }

    @Test
    void balancedRejectsWeightsNotSummingToOne() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5));
    }

    @Test
    void balancedRejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(-0.1, 0.5, 0.3, 0.3));
    }

    @Test
    void unavailableRouteNeverWins() {
        // R7 is the fastest on paper (20 min) but unavailable
        RouteDecision d = new FastestRouteStrategy().selectRoute(routes, urgent);
        assertNotEquals("R7", d.route().id());
        assertTrue(d.rejectedRoutes().stream().anyMatch(s -> s.contains("R7")));
    }

    @Test
    void budgetRestrictionRejectsExpensiveRoutes() {
        RouteDecision d = new CheapestRouteStrategy().selectRoute(routes, economy);
        assertTrue(d.rejectedRoutes().stream().anyMatch(s -> s.contains("R1") && s.contains("budget")));
    }

    @Test
    void deadlineRestrictionRejectsSlowRoutes() {
        DeliveryRequest tight = new DeliveryRequest(4, DeliveryPriority.ECO, 10000, 40, true);
        RouteDecision d = new EcoFriendlyRouteStrategy().selectRoute(routes, tight);
        assertEquals("R2", d.route().id()); // only R1, R2, R3 meet 40 min; R2 is the greenest of them
        assertTrue(d.rejectedRoutes().stream().anyMatch(s -> s.contains("R4") && s.contains("deadline")));
    }

    @Test
    void noValidRouteThrowsExceptionWithReasons() {
        DeliveryRequest impossible = new DeliveryRequest(5, DeliveryPriority.URGENT, 100, 5, false);
        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> new FastestRouteStrategy().selectRoute(routes, impossible));
        assertTrue(e.getMessage().contains("budget exceeded"));
    }

    @Test
    void boundaryDeadlineExactlyEqualIsAccepted() {
        Route r = route("X", 10, 30, 1000, 1, 0, true); // 30 min, total cost 1500
        DeliveryRequest exact = new DeliveryRequest(6, DeliveryPriority.STANDARD, 1500, 30, false);
        assertEquals("X", new FastestRouteStrategy().selectRoute(List.of(r), exact).route().id());
    }

    @Test
    void emptyRouteListThrowsException() {
        assertThrows(NoSuitableRouteException.class,
                () -> new FastestRouteStrategy().selectRoute(List.of(), urgent));
    }

    @Test
    void missingRequestThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new FastestRouteStrategy().selectRoute(routes, null));
    }

    @Test
    void negativeCostRouteIsRejectedAtCreation() {
        assertThrows(IllegalArgumentException.class, () -> route("X", 10, 30, -5, 1, 0, true));
    }

    @Test
    void plannerSwitchesStrategyAtRuntime() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("R1", planner.plan(routes, standard).route().id());
        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("R5", planner.plan(routes, standard).route().id());
        planner.setStrategy(new EcoFriendlyRouteStrategy());
        assertEquals("R4", planner.plan(routes, standard).route().id());
    }

    @Test
    void plannerRejectsNullStrategy() {
        assertThrows(IllegalArgumentException.class, () -> new RoutePlanner(null));
    }

    @Test
    void selectorChoosesStrategyByPriority() {
        RouteStrategy fastest = new FastestRouteStrategy();
        RouteStrategy cheapest = new CheapestRouteStrategy();
        RouteStrategy balanced = new BalancedRouteStrategy(0.25, 0.25, 0.25, 0.25);
        StrategySelector selector = new StrategySelector(balanced)
                .addRule(r -> r.priority() == DeliveryPriority.URGENT, fastest)
                .addRule(r -> r.priority() == DeliveryPriority.ECONOMY, cheapest);
        assertSame(fastest, selector.select(urgent));
        assertSame(cheapest, selector.select(economy));
        assertSame(balanced, selector.select(standard)); // default
    }

    @Test
    void comparisonRunsAllStrategiesOnSameRequest() {
        List<RouteStrategy> all = List.of(new FastestRouteStrategy(), new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy());
        Map<String, RouteDecision> results = new StrategyComparator().compareAll(all, routes, standard);
        assertEquals(3, results.size());
        assertNotEquals(results.get("Fastest").route().id(), results.get("Cheapest").route().id());
    }
}