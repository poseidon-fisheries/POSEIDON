package uk.ac.ox.poseidon.agents.api;

import com.vividsolutions.jts.geom.Coordinate;

import java.time.LocalDateTime;
import java.util.Optional;

public interface Action {
    Agent getAgent();

    Optional<LocalDateTime> getDateTime();

    Optional<Coordinate> getCoordinate();

    String getCode();
}
