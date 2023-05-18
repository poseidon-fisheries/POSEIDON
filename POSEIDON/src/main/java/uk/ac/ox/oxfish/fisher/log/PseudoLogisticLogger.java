/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.log;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticInputMaker;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.bandit.BanditSwitch;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Tries to fill up a LogisticLog when you are not actually using a LogitDestination
 * Created by carrknight on 1/21/17.
 */
public class PseudoLogisticLogger implements TripListener {

    /**
     * the discretization allegedly used to make the logit choice
     */
    private final MapDiscretization discretization;

    /**
     * Needed to have an idea on the choices possible and to give them a number
     */
    private final BanditSwitch switcher;

    /**
     * you use this to simulate what the "x" would be when observed by the
     * destination strategies
     */
    private final LogisticInputMaker inputter;

    /**
     * you update this
     */
    private final LogisticLog log;


    private final Fisher fisher;

    private final FishState state;


    public PseudoLogisticLogger(
        MapDiscretization discretization,
        LogisticInputMaker inputter, LogisticLog log,
        Fisher fisher,
        FishState state,
        Set<Integer> allowedGroups
    ) {
        this.discretization = discretization;
        //only model arms for which we have both at least a tile in the map AND is listed in the input file
        switcher = new BanditSwitch(
            discretization.getNumberOfGroups(),
            integer -> discretization.isValid(integer) && allowedGroups.contains(integer)
        );

        this.inputter = inputter;
        this.log = log;
        this.fisher = fisher;
        this.state = state;
    }

    public PseudoLogisticLogger(
        MapDiscretization discretization,
        ObservationExtractor[] commonExtractors,
        LogisticLog log,
        Fisher fisher,
        FishState state,
        MersenneTwisterFast random
    ) {
        this.discretization = discretization;
        //only model arms for which we have both at least a tile in the map AND is listed in the input file
        switcher = new BanditSwitch(
            discretization.getNumberOfGroups(),
            integer -> discretization.isValid(integer)
        );
        ObservationExtractor[][] extractors = new ObservationExtractor[switcher.getNumberOfArms()][];
        for (int arm = 0; arm < extractors.length; arm++)
            extractors[arm] = commonExtractors;

        this.inputter = new LogisticInputMaker(extractors, new Function<Integer, SeaTile>() {
            @Override
            public SeaTile apply(Integer arm) {
                List<SeaTile> group = discretization.getGroup(switcher.getGroup(arm));
                return group.get(random.nextInt(group.size()));
            }
        });
        this.log = log;
        this.fisher = fisher;
        this.state = state;
    }

    @Override
    public void reactToFinishedTrip(TripRecord record, Fisher fisher) {
        //if we recorded an input at the end of the last trip, now we reveal the choice
        if (log.waitingForChoice()) {
            if (record.getMostFishedTileInTrip() == null || discretization.getGroup(
                record.getMostFishedTileInTrip()) == null) {
                log.reset();
            } else {
                Preconditions.checkArgument(log != null);
                Preconditions.checkArgument(switcher != null);
                Preconditions.checkArgument(discretization != null);
                Preconditions.checkArgument(record.getMostFishedTileInTrip() != null);
                Preconditions.checkArgument(discretization.getGroup(
                    record.getMostFishedTileInTrip()) != null);
                Preconditions.checkArgument(switcher.getArm(discretization.getGroup(
                    record.getMostFishedTileInTrip())) != null);
                //you have to turn the tile fished into the map group first and then from that to the bandit arm
                log.recordChoice(
                    switcher.getArm(
                        discretization.getGroup(
                            record.getMostFishedTileInTrip())
                    ),
                    state.getYear(),
                    state.getDayOfTheYear()
                );
            }

        }
        assert !log.waitingForChoice();
        log.recordInput(inputter.getRegressionInput(this.fisher, state));


    }

    /**
     * Getter for property 'discretization'.
     *
     * @return Value for property 'discretization'.
     */
    public MapDiscretization getDiscretization() {
        return discretization;
    }

    /**
     * Getter for property 'switcher'.
     *
     * @return Value for property 'switcher'.
     */
    public BanditSwitch getSwitcher() {
        return switcher;
    }

    /**
     * Getter for property 'inputter'.
     *
     * @return Value for property 'inputter'.
     */
    public LogisticInputMaker getInputter() {
        return inputter;
    }

    /**
     * Getter for property 'log'.
     *
     * @return Value for property 'log'.
     */
    public LogisticLog getLog() {
        return log;
    }
}
