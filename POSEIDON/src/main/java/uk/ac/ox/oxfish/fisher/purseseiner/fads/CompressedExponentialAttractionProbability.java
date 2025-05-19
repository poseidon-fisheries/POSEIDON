/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;

import java.util.Arrays;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class CompressedExponentialAttractionProbability
    implements AttractionProbabilityFunction {

    private final double[] compressionExponents;
    private final double[] attractableBiomassCoefficients;
    private final double[] biomassInteractionCoefficients;

    public CompressedExponentialAttractionProbability(
        final double[] compressionExponents,
        final double[] attractableBiomassCoefficients,
        final double[] biomassInteractionCoefficients
    ) {
        this.compressionExponents = compressionExponents.clone();
        this.attractableBiomassCoefficients = attractableBiomassCoefficients.clone();
        this.biomassInteractionCoefficients = biomassInteractionCoefficients.clone();
    }

    @Override
    public double apply(
        final Species species,
        final LocalBiology biology,
        final AggregatingFad<?, ?> fad
    ) {
        return apply(
            species,
            biology.getBiomass(species),
            Arrays.stream(fad.getBiomass()).sum()
        );
    }

    public double apply(
        final Species species,
        final double attractableBiomass,
        final double totalFadBiomass
    ) {
        final double b0 = attractableBiomassCoefficients[species.getIndex()];
        final double b1 = biomassInteractionCoefficients[species.getIndex()];
        return 1 - exp(-pow(
            (b0 * attractableBiomass) + (b1 * attractableBiomass * totalFadBiomass),
            compressionExponents[species.getIndex()]
        ));
    }
}
