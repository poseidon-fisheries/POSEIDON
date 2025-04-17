package uk.ac.ox.poseidon.core.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class EventAccumulator<E> implements Listener<E> {
    @Getter
    private final Class<E> eventClass;
    private final ArrayList<E> events = new ArrayList<>();

    @Override
    public void receive(E event) {
        events.add(event);
    }

    public Stream<E> getEvents() {
        return events.stream();
    }
}
