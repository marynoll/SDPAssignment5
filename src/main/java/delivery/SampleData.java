package delivery;

import java.util.List;

public class SampleData {

    private static final Location WAREHOUSE = new Location("Central Warehouse", 51.1694, 71.4491);
    private static final Location CUSTOMER = new Location("Customer, Left Bank", 51.1283, 71.4306);

    private static Route route(String id, double km, int minutes, double cost,
                               double co2, int traffic, boolean available) {
        return new Route(id, WAREHOUSE, CUSTOMER, km, minutes, cost, co2, traffic, available);
    }

    public static List<Route> routes() {
        return List.of(
                route("R1", 18, 25, 5200, 3.9, 8, true),   // highway express
                route("R2", 14, 30, 3400, 2.0, 6, true),   // city center
                route("R3", 22, 30, 4500, 4.1, 4, true),   // ring road
                route("R4", 16, 50, 3000, 0.9, 2, true),   // green corridor
                route("R5", 15, 45, 2400, 2.5, 5, true),   // back streets
                route("R6", 12, 28, 4000, 2.8, 20, true),  // bridge shortcut, heavy traffic
                route("R7", 20, 20, 3500, 4.5, 0, false),  // night route, unavailable
                route("R8", 30, 60, 2200, 5.0, 0, true));  // scenic long route
    }
}
