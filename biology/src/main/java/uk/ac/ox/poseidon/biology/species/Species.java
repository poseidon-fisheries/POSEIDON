/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology.species;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Comparator;

import static java.util.Comparator.nullsFirst;
import static uk.ac.ox.poseidon.core.utils.Utils.multiStringKey;

/**
 * A species, optionally narrowed to a life stage (e.g. "adult"/"juvenile") — the unit of species
 * identity tracked throughout the model, from biology to markets. Identity ({@code equals}/
 * {@code hashCode}/{@link #compareTo}) is based on {@code code} and {@code lifeStage} only,
 * {@code name} is purely cosmetic. Built via
 * {@link Factories#species()}/{@link Factories#species(String)}/
 * {@link Factories#species(String, String, String)}.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Species implements Comparable<Species> {

    @EqualsAndHashCode.Include
    @NonNull
    private final String code;

    @EqualsAndHashCode.Include
    private final String lifeStage;

    private final String name;

    /** A composite string key combining {@code code} and {@code lifeStage}. */
    @Getter(lazy = true)
    private final String key = multiStringKey(code, lifeStage);

    /**
     * @param code      the species' code; blank-only values aren't specially treated, only
     *                   {@code null}/blank {@code lifeStage}/{@code name} are normalized
     * @param lifeStage the life stage, or {@code null} for the species as a whole; a blank string
     *                  is normalized to {@code null}
     * @param name      the display name, or {@code null}; a blank string is normalized to
     *                  {@code null}
     */
    public Species(
        @NonNull final String code,
        final String lifeStage,
        final String name
    ) {
        this.code = code;
        this.lifeStage = lifeStage != null && lifeStage.trim().isEmpty() ? null : lifeStage;
        this.name = name != null && name.trim().isEmpty() ? null : name;
    }

    /**
     * Returns true if a species "covers" another species, i.e., if it's the same according to its
     * code and life stage, or if it doesn't specify a life stage and the other does.
     * <p>
     * I'm adding this to handle the case where we have a price for a species as a whole, but
     * vessels catch that species at different life stages.
     * <p>
     * FIXME: this is needed, but a bit of a kludge and I'm not sure that this is the right approach
     *  in general. We probably need some kind of standard species ontology (with the possibility
     *  of encoding supra-species functional groups as well)
     *
     * @param other the species to check against
     * @return whether this species covers {@code other}
     */
    public boolean covers(final Species other) {
        return this.code.equals(other.code) &&
            (this.lifeStage == null || this.lifeStage.equals(other.lifeStage));
    }

    /** @return {@code "code"}, plus {@code " - name"} and/or {@code " (lifeStage)"} if present */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.code);
        if (this.name != null && !this.code.isEmpty() && !this.code.equals(this.name)) {
            sb.append(" - ").append(this.name);
        }
        if (this.lifeStage != null && !this.lifeStage.isEmpty()) {
            sb.append(" (").append(this.lifeStage).append(")");
        }
        return sb.toString();
    }

    /** Orders by {@code code}, then by {@code lifeStage} ({@code null} sorting first). */
    @Override
    public int compareTo(final @NonNull Species o) {
        return Comparator
            .comparing(Species::getCode)
            .thenComparing(Species::getLifeStage, nullsFirst(String::compareTo))
            .compare(this, o);
    }
}
