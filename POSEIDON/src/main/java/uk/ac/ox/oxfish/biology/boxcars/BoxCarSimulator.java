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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.RecruitmentBySpawningJackKnifeMaturity;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * given a bunch of box car objects simulates a virgin population
 */
public class BoxCarSimulator {

    final private double initialRecruits;

    final private FixedBoxcarAging dailyStep;

    final private YearlyRecruitmentProcess recruitmentProcess;

    final private GrowthBinByList meristics;

    final private NaturalMortalityProcess mortalityProcess;


    public BoxCarSimulator(
        double initialRecruits,
        FixedBoxcarAging dailyStep,
        YearlyRecruitmentProcess recruitmentProcess,
        GrowthBinByList meristics, NaturalMortalityProcess mortalityProcess
    ) {
        this.initialRecruits = initialRecruits;
        this.dailyStep = dailyStep;
        this.recruitmentProcess = recruitmentProcess;
        this.meristics = meristics;
        this.mortalityProcess = mortalityProcess;
    }

    public static void main(String[] args) {
        FixedBoxcarBertalannfyAging aging = new FixedBoxcarBertalannfyAging();
        EquallySpacedBertalanffyFactory meristics = new EquallySpacedBertalanffyFactory();
        meristics.setCmPerBin(5d);
        meristics.setMaxLengthInCm(new FixedDoubleParameter(125));
        meristics.setRecruitLengthInCm(new FixedDoubleParameter(0));
        RecruitmentBySpawningJackKnifeMaturity maturity = new RecruitmentBySpawningJackKnifeMaturity();
        maturity.setCumulativePhi(new FixedDoubleParameter(6));
        maturity.setSteepness(new FixedDoubleParameter(0.8));
        maturity.setVirginRecruits(new FixedDoubleParameter(10000));

        ProportionalMortalityProcess process = new ProportionalMortalityProcess(.2);

        FishState state = new FishState();

        GrowthBinByList meristicInstance = meristics.apply(state);
        BoxCarSimulator simulator = new BoxCarSimulator(
            10000,
            aging.apply(state),
            maturity.apply(state),
            meristicInstance,
            process
        );
        StructuredAbundance structuredAbundance = simulator.virginCondition(state, 1000);

        System.out.println(structuredAbundance);

        double spawningBiomass = 0;
        //compute the cumulative spawning biomass
        for (int i = 0; i < structuredAbundance.getBins(); i++) {
            if (meristicInstance.getWeight(0, i) > 0 & meristicInstance.getLength(0, i) >=
                maturity.getLengthAtMaturity())
                spawningBiomass += meristicInstance.getWeight(0, i) * structuredAbundance.getAbundance(0, i);


        }
        System.out.println(spawningBiomass);

    }

    public StructuredAbundance virginCondition(
        FishState state,
        int yearsToVirgin
    ) {
        Species species = new Species("simulated", meristics);
        dailyStep.start(species);

        //create a single local biology hosting everything
        AbundanceLocalBiology biology = new AbundanceLocalBiology(new GlobalBiology(species));
        biology.getAbundance(species).asMatrix()[0][0] = initialRecruits;

        //now step on it
        for (int year = 0; year < yearsToVirgin; year++) {
            for (int day = 0; day < 365; day++) {
                dailyStep.ageLocally(
                    biology,
                    species,
                    state,
                    false,
                    1
                );
                double recruit = recruitmentProcess.recruit(
                    species,
                    species.getMeristics(),
                    biology.getAbundance(species),
                    day,
                    1
                );
                //          double recruit = initialRecruits/365d;
                mortalityProcess.cull(meristics, false, biology.getAbundance(species), 1);
                biology.getAbundance(species).asMatrix()[0][0] += recruit;

            }


        }

        return biology.getAbundance(species);
    }

}
