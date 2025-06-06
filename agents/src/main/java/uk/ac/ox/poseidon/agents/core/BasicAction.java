/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    public String toString() {
        return "BasicAction{" +
            "code='" + code + '\'' +
            ", agent=" + agent +
            ", dateTime=" + dateTime +
            ", coordinate=" + coordinate +
            '}';
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
