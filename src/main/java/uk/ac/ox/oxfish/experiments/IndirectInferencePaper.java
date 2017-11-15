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

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.KernelTransductionFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.*;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.SocialAnnealingProbabilityFactory;
import uk.ac.ox.oxfish.utility.bandit.factory.SoftmaxBanditFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class IndirectInferencePaper {


    /**
     * store list of names of the algorithms to use and their factory; this is used for the model-selection bit
     */
    private final static LinkedHashMap<String,
            AlgorithmFactory<? extends DestinationStrategy>> strategies =
            new LinkedHashMap<>();


    //fill up the strategies map with pre-made models
    static {

        //perfect agents
        LogitRPUEDestinationFactory perfect = new LogitRPUEDestinationFactory();
        SquaresMapDiscretizerFactory discretizer = new SquaresMapDiscretizerFactory();
        discretizer.setHorizontalSplits(new FixedDoubleParameter(2));
        discretizer.setVerticalSplits(new FixedDoubleParameter(2));
        perfect.setDiscretizer(discretizer);
        strategies.put(
                "perfect3by3",
                perfect
        );


        //3 variants of explore-exploit-imitate
        PerTripImitativeDestinationFactory exploreExploit = new PerTripImitativeDestinationFactory();
        exploreExploit.setProbability(new FixedProbabilityFactory(.2,1));
        exploreExploit.setStepSize(new FixedDoubleParameter(5));
        exploreExploit.setAlwaysCopyBest(true);
        exploreExploit.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreExploit.setAutomaticallyIgnoreMPAs(true);
        strategies.put("explore20",exploreExploit);

        PerTripImitativeDestinationFactory exploreExploit80 = new PerTripImitativeDestinationFactory();
        exploreExploit80.setProbability(new FixedProbabilityFactory(.8,1));
        exploreExploit80.setStepSize(new FixedDoubleParameter(5));
        exploreExploit80.setAlwaysCopyBest(true);
        exploreExploit80.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreExploit80.setAutomaticallyIgnoreMPAs(true);
        strategies.put("explore80",exploreExploit80);

        PerTripImitativeDestinationFactory exploreLarge = new PerTripImitativeDestinationFactory();
        exploreLarge.setProbability(new FixedProbabilityFactory(.2,1));
        exploreLarge.setStepSize(new FixedDoubleParameter(20));
        exploreLarge.setAlwaysCopyBest(true);
        exploreLarge.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreLarge.setAutomaticallyIgnoreMPAs(true);
        strategies.put("exploreLarge",exploreLarge);

        //heatmapper (these are the parameters in the original kernel regression)
        HeatmapDestinationFactory heatmap = new HeatmapDestinationFactory();
        ExhaustiveAcquisitionFunctionFactory acquisition = new ExhaustiveAcquisitionFunctionFactory();
        acquisition.setProportionSearched(new FixedDoubleParameter(.1));
        heatmap.setAcquisition(acquisition);
        heatmap.setExplorationStepSize(new FixedDoubleParameter(1));
        heatmap.setProbability(new FixedProbabilityFactory(.5,1));
        KernelTransductionFactory regression = new KernelTransductionFactory();
        regression.setForgettingFactor(new FixedDoubleParameter(0.999989));
        regression.setSpaceBandwidth(new FixedDoubleParameter(20));
        heatmap.setRegression(regression);
        strategies.put("kernel",heatmap);

        //social annealing
        PerTripImitativeDestinationFactory annealing = new PerTripImitativeDestinationFactory();
        annealing.setAlwaysCopyBest(true);
        annealing.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        annealing.setAutomaticallyIgnoreMPAs(true);
        annealing.setStepSize(new FixedDoubleParameter(5));
        annealing.setProbability(new SocialAnnealingProbabilityFactory(.7));
        strategies.put("annealing",annealing);


        //2 softmax bandits (differ in number of splits!)
        BanditDestinationFactory bandit = new BanditDestinationFactory();
        SoftmaxBanditFactory softmax = new SoftmaxBanditFactory();
        bandit.setBandit(softmax);
        SquaresMapDiscretizerFactory banditDiscretizer = new SquaresMapDiscretizerFactory();
        banditDiscretizer.setVerticalSplits(new FixedDoubleParameter(2));
        banditDiscretizer.setHorizontalSplits(new FixedDoubleParameter(2));
        bandit.setDiscretizer(banditDiscretizer);
        strategies.put("bandit3by3",bandit);

        BanditDestinationFactory bandit2 = new BanditDestinationFactory();
        SoftmaxBanditFactory softmax2 = new SoftmaxBanditFactory();
        bandit2.setBandit(softmax2);
        SquaresMapDiscretizerFactory banditDiscretizer2 = new SquaresMapDiscretizerFactory();
        banditDiscretizer2.setVerticalSplits(new FixedDoubleParameter(4));
        banditDiscretizer2.setHorizontalSplits(new FixedDoubleParameter(4));
        bandit2.setDiscretizer(banditDiscretizer2);
        strategies.put("bandit5by5",bandit2);

        //gravitational pull
        GravitationalSearchDestinationFactory gravitational = new GravitationalSearchDestinationFactory();
        strategies.put("gravitational",gravitational);

        //randomizer
        strategies.put(
                "random",
                new RandomThenBackToPortFactory()
        );



    }






}
