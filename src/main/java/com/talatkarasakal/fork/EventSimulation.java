package com.talatkarasakal.fork;

import java.util.ArrayList;
import java.util.List;

public class EventSimulation {
    private List<Event> queue;

    public EventSimulation() {
        this.queue = new ArrayList<>();
    }

    public void addEvent(Event event) {
        queue.add(event);
        queue.sort((e1, e2) -> Integer.compare(e1.getTime(), e2.getTime()));
    }

    public Event getNextEvent() {
        if (!queue.isEmpty()) {
            return queue.remove(0);
        }
        return null;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
