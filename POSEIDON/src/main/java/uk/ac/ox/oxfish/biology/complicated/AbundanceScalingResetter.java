/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import eva2.OptimizerFactory;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.strategies.NelderMeadSimplex;
import eva2.problems.SimpleProblemWrapper;
import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * like abundance resetter, but this one rescales the total biomass available keeping  the new length/age distribution
 */
public class AbundanceScalingResetter implements BiologyResetter {


    private final BiomassAllocator allocator;

    private final Species species;


    private double recordedTotalBiomass;


    public AbundanceScalingResetter(
        BiomassAllocator allocator,
        Species species
    ) {
        this.allocator = allocator;
        this.species = species;
    }

    /**
     * records how much biomass there is
     *
     * @param map
     */
    @Override
    public void recordHowMuchBiomassThereIs(FishState map) {


        recordedTotalBiomass = map.getMap().getTotalBiomass(species);
    }

    /**
     * returns biology layer to biomass recorded previously
     *
     * @param map
     * @param random
     */
    @Override
    public void resetAbundance(NauticalMap map, MersenneTwisterFast random) {


        double[][] currentCatchAtLength = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {

            if (!seaTile.isFishingEvenPossibleHere())
                continue;
            StructuredAbundance abundance = seaTile.getAbundance(species);
            for (int i = 0; i < species.getNumberOfSubdivisions(); i++) {
                for (int j = 0; j < species.getNumberOfBins(); j++) {
                    currentCatchAtLength[i][j] += abundance.asMatrix()[i][j];
                }
            }

        }


        SimpleProblemWrapper problem = new SimpleProblemWrapper();
        problem.setSimpleProblem(new FindCorrectWeightProblem(currentCatchAtLength));
        problem.setDefaultRange(3);
        problem.setParallelThreads(1);
        OptimizationParameters params = OptimizerFactory.makeParams(
            NelderMeadSimplex.createNelderMeadSimplex(

                problem
                , null),
            15, problem

        );
        params.setTerminator(new EvaluationTerminator(500));
        double[] bestMultiplier = OptimizerFactory.optimizeToDouble(
            params
        );


        double[][] correctAbundance = reweightAbundance(
            bestMultiplier[0] + 3,
            currentCatchAtLength
        );

        System.out.println("weight found: " + bestMultiplier[0] + 3);
        System.out.println("error: " + (
            recordedTotalBiomass - FishStateUtilities.weigh(
                new StructuredAbundance(correctAbundance),
                species.getMeristics()
            ))

        );


        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
            resetAbundanceHere(seaTile, map, random, correctAbundance);
        }

    }

    private double[][] reweightAbundance(double multiplier, double[][] currentCatchAtLength) {
        double[][] testCatchAtLength = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        for (int i = 0; i < testCatchAtLength.length; i++)
            for (int j = 0; j < testCatchAtLength[0].length; j++)
                testCatchAtLength[i][j] = currentCatchAtLength[i][j] * multiplier;
        return testCatchAtLength;
    }

    public void resetAbundanceHere(
        SeaTile tile,
        NauticalMap map,
        MersenneTwisterFast random,
        double[][] recordedAbundance
    ) {

        if (!tile.isFishingEvenPossibleHere()) {
            Preconditions.checkArgument(
                allocator.allocate(tile, map, random) == 0 |
                    Double.isNaN(allocator.allocate(tile, map, random)),
                "Allocating biomass on previously unfishable areas is not allowed; " +
                    "keep them empty but don't use always empty local biologies " + "\n" +
                    allocator.allocate(tile, map, random)
            );
            return;
        }

        double[][] abundanceHere = tile.getAbundance(species).asMatrix();
        assert abundanceHere.length == species.getNumberOfSubdivisions();
        assert abundanceHere[0].length == species.getNumberOfBins();
        double weightHere = allocator.allocate(tile, map, random);

        for (int i = 0; i < species.getNumberOfSubdivisions(); i++) {
            for (int j = 0; j < species.getNumberOfBins(); j++) {
                abundanceHere[i][j] = weightHere * recordedAbundance[i][j];
            }
        }


    }

    /**
     * species we are resetting
     *
     * @return
     */
    @Override
    public Species getSpecies() {
        return species;
    }

    private class FindCorrectWeightProblem extends SimpleProblemDouble {
        private final double[][] currentCatchAtLength;

        public FindCorrectWeightProblem(double[][] currentCatchAtLength) {
            this.currentCatchAtLength = currentCatchAtLength;
        }

        @Override
        public double[] evaluate(double[] x) {
            double multiplier = x[0] + 3;
            double[][] testCatchAtLength = reweightAbundance(multiplier, currentCatchAtLength);

            return new double[]{Math.abs(
                recordedTotalBiomass - FishStateUtilities.weigh(
                    new StructuredAbundance(testCatchAtLength),
                    species.getMeristics()
                )
            )};

        }

        @Override
        public int getProblemDimension() {
            return 1;
        }
    }
}
