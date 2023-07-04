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

package uk.ac.ox.oxfish.model.data.collectors;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

/**
 * like a normal counter but has arrays ready to make catch data faster to read and write
 * Created by carrknight on 8/14/15.
 */
public class FisherDailyCounter extends Counter {

    private static final long serialVersionUID = 9169901162655878415L;
    private final double[] landings;

    private final double[] catches;

    private final double[] earnings;

    private final double[][][] landingsPerBin;

    public FisherDailyCounter(final int numberOfSpecies) {
        super(IntervalPolicy.EVERY_DAY);
        landings = new double[numberOfSpecies];
        earnings = new double[numberOfSpecies];
        catches = new double[numberOfSpecies];
        landingsPerBin = new double[numberOfSpecies][][];
        super.addColumn(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
        super.addColumn(FisherYearlyTimeSeries.EFFORT);
    }

    @Override
    public void step(final SimState simState) {
        super.step(simState);

        for (int i = 0; i < landings.length; i++) {
            landings[i] = 0;
            earnings[i] = 0;
            catches[i] = 0;
            if (landingsPerBin[i] != null) {
                for (int subdivision = 0; subdivision < landingsPerBin[i].length; subdivision++)
                    for (int bin = 0; bin < landingsPerBin[i][0].length; bin++)
                        landingsPerBin[i][subdivision][bin] = 0;
            }
        }
    }

    /**
     * increment catch earnings column by this
     *
     * @param add by how much to increment
     */
    public void countLanding(final Species species, final double add) {
        landings[species.getIndex()] += add;
    }


    public void countLandinngPerBin(final Species species, final Catch catchOfTheDay) {

        if (landingsPerBin[species.getIndex()] == null)
            landingsPerBin[species.getIndex()] = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for (int subdivision = 0; subdivision < landingsPerBin[species.getIndex()].length; subdivision++)
            for (int bin = 0; bin < landingsPerBin[species.getIndex()][0].length; bin++)
                landingsPerBin[species.getIndex()][subdivision][bin] += catchOfTheDay.getWeightCaught(
                    species,
                    subdivision,
                    bin
                );

    }

    public void countEarnings(final Species species, final double add) {
        earnings[species.getIndex()] += add;
    }

    public void countCatches(final Species species, final double add) {
        catches[species.getIndex()] += add;
    }

    public double getLandingsPerSpecie(final int index) {
        return landings[index];
    }

    public double getEarningsPerSpecie(final int index) {
        return earnings[index];
    }

    public double getCatchesPerSpecie(final int index) {
        return catches[index];
    }


    public double getSpecificLandings(final Species species, final int bin) {
        if (landingsPerBin[species.getIndex()] == null)
            return 0d;
        else {
            double sum = 0;
            for (int subdivision = 0; subdivision < landingsPerBin[species.getIndex()].length; subdivision++)
                sum += landingsPerBin[species.getIndex()][subdivision][bin];
            return sum;
        }
    }

    public double getSpecificLandings(final Species species, final int subdivision, final int bin) {
        if (landingsPerBin[species.getIndex()] == null)
            return 0d;
        else {
            return landingsPerBin[species.getIndex()][subdivision][bin];
        }
    }


}
