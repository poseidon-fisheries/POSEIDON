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

package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * a user-unfriendly way to generate MPA by supplying a string of 0 and 1s long map-width*map-height where each 1 represents
 * a protected cell
 * Created by carrknight on 10/24/16.
 */
public class ProtectedAreaChromosomeFactory implements AlgorithmFactory<ProtectedAreasOnly> {


    //assumes 50 by 50 map!
    private String chromosome = Strings.repeat(Strings.repeat("0100000000", 5), 50);


    private WeakHashMap<FishState, ProtectedAreasOnlyFactory> delegates = new WeakHashMap<>();

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public ProtectedAreasOnly apply(FishState state) {
        chromosome = chromosome.trim();

        if (!delegates.containsKey(state)) {

            Preconditions.checkArgument(chromosome.length() == state.getMap().getWidth() * state.getMap().getHeight());

            char[] geneArray = chromosome.toCharArray();

            List<StartingMPA> mpas = new LinkedList<>();
            for (int i = 0; i < state.getMap().getWidth() * state.getMap().getHeight(); i++) {
                int gene = Integer.parseInt(String.valueOf(geneArray[i]));
                if (gene != 0) {
                    assert gene == 1;
                    mpas.add(new StartingMPA(i % state.getMap().getWidth(), i / state.getMap().getWidth(), 0, 0));
                }
            }

            ProtectedAreasOnlyFactory delegate = new ProtectedAreasOnlyFactory();
            delegate.setStartingMPAs(mpas);
            delegates.put(state, delegate);
        }
        return delegates.get(state).apply(state);
    }

    /**
     * Getter for property 'chromosome'.
     *
     * @return Value for property 'chromosome'.
     */
    public String getChromosome() {
        return chromosome;
    }

    /**
     * Setter for property 'chromosome'.
     *
     * @param chromosome Value to set for property 'chromosome'.
     */
    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }
}

