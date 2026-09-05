package feedme.domain.tidbit.urgency;

import java.time.Duration;

public record Urgency(String name, Duration lowerBound, Duration upperBound) {}
