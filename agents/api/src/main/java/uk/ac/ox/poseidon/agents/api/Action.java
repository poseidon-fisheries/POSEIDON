package uk.ac.ox.poseidon.agents.api;

import com.vividsolutions.jts.geom.Coordinate;

import java.time.LocalDateTime;

public interface Action {
    Agent getAgent();

    LocalDateTime getDateTime();

    Coordinate getCoordinate();

    String getCode();
}
