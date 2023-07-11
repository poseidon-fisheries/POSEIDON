package uk.ac.ox.poseidon.agents.core;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.Agent;

import java.time.LocalDateTime;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BasicAction implements Action {

    private final String code;
    private final Agent agent;
    private final Optional<LocalDateTime> dateTime;
    private final Optional<Coordinate> coordinate;

    public BasicAction(
        final String code,
        final Agent agent
    ) {
        this(code, agent, (LocalDateTime) null, null);
    }

    public BasicAction(
        final String code,
        final Agent agent,
        final LocalDateTime dateTime,
        final Coordinate coordinate
    ) {
        this(
            code,
            agent,
            Optional.ofNullable(dateTime),
            Optional.ofNullable(coordinate)
        );
    }

    public BasicAction(
        final String code,
        final Agent agent,
        final Optional<LocalDateTime> dateTime,
        final Optional<Coordinate> coordinate
    ) {
        this.code = code;
        this.agent = agent;
        this.dateTime = dateTime;
        this.coordinate = coordinate;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public Agent getAgent() {
        return agent;
    }

    @Override
    public Optional<LocalDateTime> getDateTime() {
        return dateTime;
    }

    @Override
    public Optional<Coordinate> getCoordinate() {
        return coordinate;
    }

}
