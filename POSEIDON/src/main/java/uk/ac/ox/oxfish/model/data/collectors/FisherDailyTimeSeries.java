/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.data.collectors;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.AbstractMarket;

import static tech.units.indriya.unit.Units.KILOGRAM;

/**
 * Dataset for each fisher being updated once a day
 * Created by carrknight on 8/4/15.
 */
public class FisherDailyTimeSeries extends TimeSeries<Fisher> {


    public static final String CASH_COLUMN = FisherYearlyTimeSeries.CASH_COLUMN;
    public static final String CATCHES_COLUMN_NAME = "Catches (kg)";
    private static final long serialVersionUID = 8984044800147873148L;


    public FisherDailyTimeSeries() {
        super(IntervalPolicy.EVERY_DAY);

    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(final FishState state, final Fisher observed) {

        registerGatherer(CASH_COLUMN, (Gatherer<Fisher>) Fisher::getBankBalance, Double.NaN);


        registerGatherer(FisherYearlyTimeSeries.CASH_FLOW_COLUMN,
            new Gatherer<Fisher>() {

                private static final long serialVersionUID = -8952359471788512912L;
                double oldCash = observed.getBankBalance();

                @Override
                public Double apply(final Fisher fisher) {
                    final double flow = fisher.getBankBalance() - oldCash;
                    oldCash = fisher.getBankBalance();
                    return flow;
                }
            }, Double.NaN
        );

        registerGatherer(
            NumberOfActiveFadsGatherer.COLUMN_NAME,
            new NumberOfActiveFadsGatherer(),
            0
        );

        for (final Species species : state.getSpecies()) {
            final String landings = species + " " + AbstractMarket.LANDINGS_COLUMN_NAME;

            registerGatherer(
                landings,
                ((Gatherer<Fisher>) fisher -> fisher.getDailyCounter().getLandingsPerSpecie(species.getIndex())),
                Double.NaN,
                KILOGRAM,
                "Biomass"
            );

            final String catches = species + " " + CATCHES_COLUMN_NAME;

            registerGatherer(
                catches,
                ((Gatherer<Fisher>) fisher -> fisher.getDailyCounter().getCatchesPerSpecie(species.getIndex())),
                Double.NaN,
                KILOGRAM,
                "Biomass"
            );

        }


        super.start(state, observed);

    }

    @Override
    public void step(final SimState simState) {

    /*
        for(int i=0; i< monthlyAverageCatch.length; i++)
        {
            monthlyAverageCatch[i].addObservation(getObserved().getDailyCounter().getLandingsPerSpecie(i));
            monthlyAverageEarnings[i].addObservation(getObserved().getDailyCounter().getEarningsPerSpecie(i));
        }
*/
        super.step(simState);


    }

}
