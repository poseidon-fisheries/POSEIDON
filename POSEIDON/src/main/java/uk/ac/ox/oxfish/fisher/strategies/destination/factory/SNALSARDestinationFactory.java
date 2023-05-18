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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureThresholdAnswer;
import uk.ac.ox.oxfish.fisher.erotetic.InverseEroteticAnswer;
import uk.ac.ox.oxfish.fisher.erotetic.ThresholdAnswer;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.*;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.SNALSARDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * SNALSAR destination strategy Factory
 * Created by carrknight on 6/7/16.
 */
public class SNALSARDestinationFactory implements AlgorithmFactory<SNALSARDestinationStrategy> {


    //1:safe
    //2: not known to have failed
    //3: legal
    //4: appropriate
    //5: known to have acceptable profits
    //6: randomize


    /**
     * how does the agent choose what's safe and what isn't
     */
    private AlgorithmFactory<? extends SafetyFeatureExtractor<SeaTile>> safety =
        new EverywhereTrueExtractorFactory();

    /**
     * how does the agent choose what's socially acceptable and what isn't
     */
    private AlgorithmFactory<? extends SocialAcceptabilityFeatureExtractor<SeaTile>> sociallyAcceptable =
        new NobodyFishesHereFactory();

    /**
     * how does the agent choose what's legal and what isn't
     */
    private AlgorithmFactory<? extends LegalityFeatureExtractor<SeaTile>> legal =
        new FollowRulesExtractorFactory();

    /**
     * how does the agent associate a profit level to a specific feature
     */
    private AlgorithmFactory<? extends ProfitFeatureExtractor<SeaTile>> profit =
        new RememberedProfitsExtractorFactory();


    /**
     * What is the threshold the agent gives to profits below which this tile is considered a failure!
     */
    private AlgorithmFactory<? extends ProfitThresholdExtractor<SeaTile>> failureThreshold =
        new FixedProfitThresholdFactory();


    /**
     * What is the threshold the agent gives to profits above which this tile is considered a success?
     */
    private AlgorithmFactory<? extends ProfitThresholdExtractor<SeaTile>> acceptableThreshold =
        new AverageProfitsThresholdFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SNALSARDestinationStrategy apply(FishState state) {

        //object that will start up all our feature extractors
        FisherStartable extractorStarter = new FisherStartable() {
            @Override
            public void start(FishState model, Fisher fisher) {
                fisher.addFeatureExtractor(
                    SNALSARutilities.SAFE_FEATURE,
                    safety.apply(model)
                );
                fisher.addFeatureExtractor(
                    SNALSARutilities.SOCIALLY_APPROPRIATE_FEATURE,
                    sociallyAcceptable.apply(model)
                );
                fisher.addFeatureExtractor(
                    SNALSARutilities.LEGAL_FEATURE,
                    legal.apply(model)
                );
                fisher.addFeatureExtractor(
                    SNALSARutilities.PROFIT_FEATURE,
                    profit.apply(model)
                );
                fisher.addFeatureExtractor(
                    SNALSARutilities.FAILURE_THRESHOLD,
                    failureThreshold.apply(model)
                );
                fisher.addFeatureExtractor(
                    SNALSARutilities.ACCEPTABLE_THRESHOLD,
                    acceptableThreshold.apply(model)
                );
            }

            @Override
            public void turnOff(Fisher fisher) {

            }
        };

        SNALSARDestinationStrategy strategy = new SNALSARDestinationStrategy(
            new ThresholdAnswer<>(0, 0, SNALSARutilities.SAFE_FEATURE), //safety above 0 is safe
            //take the complement of the failures:
            new InverseEroteticAnswer<>(
                new FeatureThresholdAnswer<>(
                    0,
                    SNALSARutilities.PROFIT_FEATURE,
                    SNALSARutilities.FAILURE_THRESHOLD,
                    false
                )),
            //everything above 0 is legal
            new ThresholdAnswer<>(0, 0, SNALSARutilities.LEGAL_FEATURE),
            //everything above 0 is appropriate,
            new ThresholdAnswer<>(0, 0, SNALSARutilities.SOCIALLY_APPROPRIATE_FEATURE),
            new FeatureThresholdAnswer<>(
                0,
                SNALSARutilities.PROFIT_FEATURE,
                SNALSARutilities.ACCEPTABLE_THRESHOLD,
                true
            ),
            new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
            extractorStarter
        );


        return strategy;


    }


    public AlgorithmFactory<? extends SafetyFeatureExtractor<SeaTile>> getSafety() {
        return safety;
    }

    public void setSafety(
        AlgorithmFactory<? extends SafetyFeatureExtractor<SeaTile>> safety
    ) {
        this.safety = safety;
    }

    public AlgorithmFactory<? extends SocialAcceptabilityFeatureExtractor<SeaTile>> getSociallyAcceptable() {
        return sociallyAcceptable;
    }

    public void setSociallyAcceptable(
        AlgorithmFactory<? extends SocialAcceptabilityFeatureExtractor<SeaTile>> sociallyAcceptable
    ) {
        this.sociallyAcceptable = sociallyAcceptable;
    }

    public AlgorithmFactory<? extends LegalityFeatureExtractor<SeaTile>> getLegal() {
        return legal;
    }

    public void setLegal(
        AlgorithmFactory<? extends LegalityFeatureExtractor<SeaTile>> legal
    ) {
        this.legal = legal;
    }

    public AlgorithmFactory<? extends ProfitFeatureExtractor<SeaTile>> getProfit() {
        return profit;
    }

    public void setProfit(
        AlgorithmFactory<? extends ProfitFeatureExtractor<SeaTile>> profit
    ) {
        this.profit = profit;
    }

    public AlgorithmFactory<? extends ProfitThresholdExtractor<SeaTile>> getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(
        AlgorithmFactory<? extends ProfitThresholdExtractor<SeaTile>> failureThreshold
    ) {
        this.failureThreshold = failureThreshold;
    }

    public AlgorithmFactory<? extends ProfitThresholdExtractor<SeaTile>> getAcceptableThreshold() {
        return acceptableThreshold;
    }

    public void setAcceptableThreshold(
        AlgorithmFactory<? extends ProfitThresholdExtractor<SeaTile>> acceptableThreshold
    ) {
        this.acceptableThreshold = acceptableThreshold;
    }
}
