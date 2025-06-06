/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;
import uk.ac.ox.oxfish.regulations.quantities.NumberOfActiveFads;
import uk.ac.ox.oxfish.regulations.quantities.YearlyActionCount;
import uk.ac.ox.poseidon.agents.api.Agent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.maybeGetFadManager;

public abstract class PurseSeinerAction
    implements Action, Locatable,
    NumberOfActiveFads.Getter,
    YearlyActionCount.Getter,
    uk.ac.ox.poseidon.agents.api.Action {

    private final Fisher fisher;
    private final SeaTile location;
    private final int step;
    private final LocalDate date;
    private final double duration;
    private final boolean permitted;
    private LocalTime time;

    protected PurseSeinerAction(
        final Fisher fisher,
        final SeaTile location,
        final double duration
    ) {
        this.fisher = fisher;
        this.location = location;
        final FishState fishState = fisher.grabState();
        this.step = fishState.getStep();
        this.date = fishState.getDate();
        this.duration = duration;
        this.permitted = checkIfPermitted();
    }

    public boolean checkIfPermitted() {
        return getFisher().grabState().getRegulations().isPermitted(this);
    }

    public Fisher getFisher() {
        return fisher;
    }

    @Override
    public Agent getAgent() {
        return fisher;
    }

    @Override
    public Optional<LocalDateTime> getDateTime() {
        return Optional
            .ofNullable(getDate())
            .map(date -> LocalDateTime.of(date, getTime().orElse(LocalTime.MIN)));
    }

    public LocalDate getDate() {
        return date;
    }

    public Optional<LocalTime> getTime() {
        return Optional.ofNullable(time);
    }

    public void setTime(final double hoursLeftInTheDay) {
        final int LAST_SECOND_OF_DAY = (60 * 60 * 24) - 1;
        final double second = ((24 - hoursLeftInTheDay) / 24) * LAST_SECOND_OF_DAY;
        this.setTime(LocalTime.ofSecondOfDay((long) second));
    }

    public void setTime(final LocalTime time) {
        this.time = time;
    }

    @Override
    public Optional<Coordinate> getCoordinate() {
        return Optional
            .ofNullable(location)
            .map(fisher.grabState().getMap()::getCoordinates);
    }

    public int getStep() {
        return step;
    }

    public double getDuration() {
        return duration;
    }

    public boolean isPermitted() {
        return permitted;
    }

    @Override
    public SeaTile getLocation() {
        return location;
    }

    /**
     * This method is used to map an action to its weight in fisher preferences.
     * It normally returns the class of the action itself, but is overridden
     * in search action so that they can be weighted according to what kind
     * of action opportunity the agent is searching for.
     */
    public Class<? extends PurseSeinerAction> getClassForWeighting() {
        return this.getClass();
    }

    @Override
    public long getNumberOfActiveFads() {
        return maybeGetFadManager(fisher)
            .map(FadManager::getNumberOfActiveFads)
            .orElse(0);
    }

    @Override
    public long getYearlyActionCount(final int year, final String actionCode) {
        return maybeGetFadManager(fisher)
            .map(fm -> fm.getYearlyActionCounter().getCount(
                year,
                fisher,
                actionCode
            ))
            .orElseThrow(() -> new RuntimeException(
                "FAD manager not found for agent " + fisher
            ));
    }

}
