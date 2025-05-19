/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;

/**
 * Barrowman (2000) recruitments are R0 whenever Depletion > hinge. Otherwise recruits are R0* depletion/hinge
 */
public class HockeyStickRecruitment extends YearlyRecruitmentProcess {


    private final double hinge;

    final private double virginRecruits;

    final private double lengthAtMaturity;

    final private double virginSpawningBiomass;
    private NoiseMaker noiseMaker = new NoNoiseMaker();

    public HockeyStickRecruitment(
        boolean recruitEveryday,
        double hinge,
        double virginRecruits,
        double lengthAtMaturity,
        double virginSpawningBiomass
    ) {
        super(recruitEveryday);
        this.hinge = hinge;
        this.virginRecruits = virginRecruits;
        this.lengthAtMaturity = lengthAtMaturity;
        this.virginSpawningBiomass = virginSpawningBiomass;
    }

    @Override
    protected double computeYearlyRecruitment(Species species, Meristics meristics, StructuredAbundance abundance) {
        double depletion = LinearSSBRatioSpawning.
            computeDepletion(species, meristics, abundance, lengthAtMaturity, virginSpawningBiomass);

        return Math.min(1d, depletion / hinge) * virginRecruits * (1 + noiseMaker.get());
    }

    @Override
    public void addNoise(NoiseMaker noiseMaker) {
        this.noiseMaker = noiseMaker;
    }

    public double getHinge() {
        return hinge;
    }

    public double getVirginRecruits() {
        return virginRecruits;
    }

    public double getLengthAtMaturity() {
        return lengthAtMaturity;
    }

    public double getVirginSpawningBiomass() {
        return virginSpawningBiomass;
    }
}
