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

package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.base.Preconditions;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.regs.FinedProtectedAreas;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by carrknight on 2/13/17.
 */
public class FinedProtectedAreasFactory implements AlgorithmFactory<FinedProtectedAreas> {


    /**
     * makes sure you only schedule yourself to build the MPAs once
     */
    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, FinedProtectedAreas> mpaBuilder =
        new uk.ac.ox.oxfish.utility.Locker<>();
    private String beingCaughtProbability = "1,.2";
    private String hourlyFines = "100,2000";
    private boolean canContemplateCheating = false;
    private List<StartingMPA> mpas = new LinkedList<>();

    {
        mpas.add(new StartingMPA(0, 0, 5, 5));
        mpas.add(new StartingMPA(20, 20, 5, 5));

    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FinedProtectedAreas apply(final FishState state) {


        return mpaBuilder.presentKey(
            state.getUniqueID(),
            () -> {

                final FinedProtectedAreas regs = new FinedProtectedAreas(state.getRandom(), canContemplateCheating);


                final List<Double> probabilities =
                    Arrays.stream(beingCaughtProbability.trim().split(",")).
                        map(Double::parseDouble).
                        collect(Collectors.toList());

                final List<Double> fines =
                    Arrays.stream(hourlyFines.trim().split(",")).
                        map(Double::parseDouble).
                        collect(Collectors.toList());

                Preconditions.checkArgument(probabilities.size() == fines.size());
                Preconditions.checkArgument(probabilities.size() == mpas.size());


                final Startable startable = new Startable() {
                    @Override
                    public void start(final FishState model) {
                        for (int i = 0; i < mpas.size(); i++) {
                            final MasonGeometry geometry = mpas.get(i).buildMPA(model.getMap());
                            regs.registerEnforcement(
                                geometry,
                                probabilities.get(i),
                                fines.get(i)
                            );

                        }
                    }

                    @Override
                    public void turnOff() {

                    }
                };
                state.registerStartable(startable);
                return regs;

            }
        );


    }


    /**
     * Getter for property 'beingCaughtProbability'.
     *
     * @return Value for property 'beingCaughtProbability'.
     */
    public String getBeingCaughtProbability() {
        return beingCaughtProbability;
    }

    /**
     * Setter for property 'beingCaughtProbability'.
     *
     * @param beingCaughtProbability Value to set for property 'beingCaughtProbability'.
     */
    public void setBeingCaughtProbability(final String beingCaughtProbability) {
        this.beingCaughtProbability = beingCaughtProbability;
    }

    /**
     * Getter for property 'hourlyFines'.
     *
     * @return Value for property 'hourlyFines'.
     */
    public String getHourlyFines() {
        return hourlyFines;
    }

    /**
     * Setter for property 'hourlyFines'.
     *
     * @param hourlyFines Value to set for property 'hourlyFines'.
     */
    public void setHourlyFines(final String hourlyFines) {
        this.hourlyFines = hourlyFines;
    }

    /**
     * Getter for property 'mpas'.
     *
     * @return Value for property 'mpas'.
     */
    public List<StartingMPA> getMpas() {
        return mpas;
    }

    /**
     * Setter for property 'mpas'.
     *
     * @param mpas Value to set for property 'mpas'.
     */
    public void setMpas(final List<StartingMPA> mpas) {
        this.mpas = mpas;
    }

    /**
     * Getter for property 'canContemplateCheating'.
     *
     * @return Value for property 'canContemplateCheating'.
     */
    public boolean isCanContemplateCheating() {
        return canContemplateCheating;
    }

    /**
     * Setter for property 'canContemplateCheating'.
     *
     * @param canContemplateCheating Value to set for property 'canContemplateCheating'.
     */
    public void setCanContemplateCheating(final boolean canContemplateCheating) {
        this.canContemplateCheating = canContemplateCheating;
    }
}
