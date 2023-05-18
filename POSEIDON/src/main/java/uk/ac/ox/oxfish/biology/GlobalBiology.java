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

package uk.ac.ox.oxfish.biology;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The biology object containing general model-wise information like what species are modeled
 * Created by carrknight on 4/11/15.
 */
public class GlobalBiology {

    /**
     * an unmodifiable list of species.
     */
    private final Species species[];


    private final List<Species> unmodifiableView;

    public GlobalBiology(Species... species) {


        this.species = species;
        for (int i = 0; i < species.length; i++) //now assign a number to each
            species[i].resetIndexTo(i);
        unmodifiableView = Collections.unmodifiableList(Arrays.asList(species));
    }


    /**
     * instantiate a list of random species
     *
     * @param numberOfSpecies the number of species
     */
    public static GlobalBiology genericListOfSpecies(int numberOfSpecies) {
        Species[] generics = new Species[numberOfSpecies];
        for (int i = 0; i < numberOfSpecies; i++)
            generics[i] = new Species("Species " + i);
        return new GlobalBiology(generics);
    }

    public static GlobalBiology fromNames(final Collection<String> names) {
        final Species[] species = names.stream().map(Species::new).toArray(Species[]::new);
        return new GlobalBiology(species);
    }

    public static GlobalBiology listOfSpeciesWithNames(String... names) {
        Species[] generics = new Species[names.length];
        for (int i = 0; i < names.length; i++)
            generics[i] = new Species(names[i]);
        return new GlobalBiology(generics);
    }

    /**
     * @return an unmodifiable list of all the species available
     */
    public List<Species> getSpecies() {
        return unmodifiableView;
    }

    public Species getSpecie(int order) {
        return species[order];
    }

    public Species getSpecie(String name) {
        return unmodifiableView.stream().
            filter(s -> s.getName().trim().equalsIgnoreCase(name.trim())).
            findFirst().orElseGet(() -> null);
    }

    public int getSize() {
        return species.length;
    }

}
