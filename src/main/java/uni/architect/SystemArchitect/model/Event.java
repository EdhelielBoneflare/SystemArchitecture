package uni.architect.SystemArchitect.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Event implements Comparable<Event> {
    public enum EventType {
        GENERATION,
        COMPLETION,
        END
    }

    private EventType type;
    private double time;
    private int objectNumber;

    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }
}
