package feedme.domain.tidbit.urgency;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.time.Duration;

public enum BuiltinUrgency {
    // buzzing phone, phone call
    Emergency("emergency", Duration.ZERO, Duration.ofSeconds(15)),
    // push notification, popup, alert
    Push("push", Duration.ofSeconds(1), Duration.ofMinutes(10)),
    // silent notification, indicator
    Nudge("nudge", Duration.ofMinutes(1), Duration.ofHours(2)),
    // viewed regularly, in a list
    Agenda("agenda", Duration.ofMinutes(30), Duration.ofHours(8)),
    // have to search for it, archived
    Backlog("backlog", Duration.ofHours(8), Duration.ofDays(7));

    public final Urgency urgency;
    private final BiMap<String, BuiltinUrgency> map = HashBiMap.create();
    BuiltinUrgency(String name, Duration lowerBound, Duration upperBound) {
        this.urgency = new Urgency(name, lowerBound, upperBound);
        map.put(name, this);
    }
}
