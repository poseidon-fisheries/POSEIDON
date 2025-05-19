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

package uk.ac.ox.oxfish.biology;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

/**
 * The biology object containing general model-wise information like what species are modeled
 * Created by carrknight on 4/11/15.
 */
public class GlobalBiology {

    /**
     * an unmodifiable list of species.
     */
    private final Species[] species;

    private final Supplier<Map<String, Species>> speciesByName;
    private final Supplier<Map<String, Species>> speciesByCode;

    private final List<Species> speciesList;

    public GlobalBiology(final Species... species) {

        this.species = species;
        for (int i = 0; i < species.length; i++) //now assign a number to each
            species[i].resetIndexTo(i);

        speciesList = ImmutableList.copyOf(species);

        // Memoizing the name and code maps allows us to
        // build test biologies with mock species that
        // have no names or code
        speciesByName = memoize(() ->
            speciesList
                .stream()
                .collect(toImmutableMap(Species::getName, identity()))
        );
        speciesByCode = memoize(() ->
            speciesList
                .stream()
                .collect(toImmutableMap(Species::getCode, identity()))
        );
    }


    /**
     * instantiate a list of random species
     *
     * @param numberOfSpecies the number of species
     */
    public static GlobalBiology genericListOfSpecies(final int numberOfSpecies) {
        final Species[] generics = new Species[numberOfSpecies];
        for (int i = 0; i < numberOfSpecies; i++)
            generics[i] = new Species("Species " + i);
        return new GlobalBiology(generics);
    }

    public static GlobalBiology fromNames(final Collection<String> names) {
        final Species[] species = names.stream().map(Species::new).toArray(Species[]::new);
        return new GlobalBiology(species);
    }

    /**
     * @return an unmodifiable list of all the species available
     */
    public List<Species> getSpecies() {
        return speciesList;
    }

    public Species getSpecie(final int order) {
        return species[order];
    }

    public Species getSpeciesByCaseInsensitiveName(final String name) {
        return speciesList
            .stream()
            .filter(s -> s.getName().trim().equalsIgnoreCase(name.trim()))
            .findFirst()
            .orElse(null);
    }

    public Species getSpeciesByName(final String speciesName) {
        return speciesByName.get().get(speciesName);
    }

    public Species getSpeciesByCode(final String speciesCode) {
        return speciesByCode.get().get(speciesCode);
    }

    public int getSize() {
        return species.length;
    }

}
