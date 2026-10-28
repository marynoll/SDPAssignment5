package delivery;

import java.util.List;

public record RouteDecision(
        Route route,
        String strategyName,
        double score,
        String reason,
        List<String> rejectedRoutes) {

    public RouteDecision {
        rejectedRoutes = List.copyOf(rejectedRoutes);
    }

    public String explain() {
        StringBuilder sb = new StringBuilder();
        sb.append("Strategy: ").append(strategyName).append("\n");
        sb.append("Selected: Route ").append(route.id()).append("\n");
        sb.append("Reason: ").append(reason).append("\n");
        for (String rejected : rejectedRoutes) {
            sb.append("  ").append(rejected).append("\n");
        }
        return sb.toString();
    }
}
