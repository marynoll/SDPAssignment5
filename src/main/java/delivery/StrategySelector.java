package delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class StrategySelector {

    private record Rule(Predicate<DeliveryRequest> condition, RouteStrategy strategy) {}

    private final List<Rule> rules = new ArrayList<>();
    private final RouteStrategy defaultStrategy;

    public StrategySelector(RouteStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }

    public StrategySelector addRule(Predicate<DeliveryRequest> condition, RouteStrategy strategy) {
        rules.add(new Rule(condition, strategy));
        return this;
    }

    public RouteStrategy select(DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request is missing");
        }
        for (Rule rule : rules) {
            if (rule.condition().test(request)) {
                return rule.strategy();
            }
        }
        return defaultStrategy;
    }
}
