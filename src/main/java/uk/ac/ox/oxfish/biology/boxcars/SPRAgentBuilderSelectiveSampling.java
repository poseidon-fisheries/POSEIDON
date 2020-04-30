package uk.ac.ox.oxfish.biology.boxcars;


import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * creates an SPR agent but this time it has a fixed probability of sampling a boat for each matching tag
 */
public class SPRAgentBuilderSelectiveSampling implements AlgorithmFactory<SPRAgent>  {


    private  String surveyTag = "spr_agent";

    private  String speciesName = "Species 0";

    private LinkedHashMap<String,Double> probabilityOfSamplingEachTag =
            new LinkedHashMap<>();

    private DoubleParameter assumedLinf = new FixedDoubleParameter(86);

    private  DoubleParameter assumedKParameter = new FixedDoubleParameter(0.4438437) ;

    private  DoubleParameter assumedNaturalMortality = new FixedDoubleParameter(0.3775984) ;

    private  DoubleParameter simulatedMaxAge = new FixedDoubleParameter(100) ;

    //these aren't "real" virgin recruits, this is just the number of simulated ones
    //used by Peter in his formula
    private  DoubleParameter simulatedVirginRecruits = new FixedDoubleParameter(1000) ;

    private  DoubleParameter assumedLengthBinCm = new FixedDoubleParameter(5);

    private  DoubleParameter assumedVarA = new FixedDoubleParameter(0.00853);

    private  DoubleParameter assumedVarB = new FixedDoubleParameter(3.137);

    private  DoubleParameter assumedLengthAtMaturity = new FixedDoubleParameter(50);


    public SPRAgentBuilderSelectiveSampling() {
    }

    public SPRAgentBuilderSelectiveSampling(String surveyTag, String speciesName,
                                            LinkedHashMap<String,Double> probabilityOfSamplingEachTag,
                                            double assumedLinf,
                                            double assumedKParameter,
                                            double assumedNaturalMortality,
                                            int simulatedMaxAge,
                                            double simulatedVirginRecruits,
                                            double assumedLengthBinCm,
                                            double assumedVarA,
                                            double assumedVarB,
                                            double assumedLengthAtMaturity) {
        this.surveyTag = surveyTag;
        this.speciesName = speciesName;
        this.probabilityOfSamplingEachTag = probabilityOfSamplingEachTag;
        this.assumedLinf = new FixedDoubleParameter(assumedLinf);
        this.assumedKParameter = new FixedDoubleParameter(assumedKParameter);
        this.assumedNaturalMortality = new FixedDoubleParameter(assumedNaturalMortality);
        this.simulatedMaxAge = new FixedDoubleParameter(simulatedMaxAge);
        this.simulatedVirginRecruits = new FixedDoubleParameter(simulatedVirginRecruits);
        this.assumedLengthBinCm = new FixedDoubleParameter(assumedLengthBinCm);
        this.assumedVarA = new FixedDoubleParameter(assumedVarA);
        this.assumedVarB = new FixedDoubleParameter(assumedVarB);
        this.assumedLengthAtMaturity = new FixedDoubleParameter(assumedLengthAtMaturity);
    }




    @Override
    public SPRAgent apply(FishState fishState) {
        final MersenneTwisterFast random = fishState.getRandom();


        return new SPRAgent(surveyTag,
                fishState.getBiology().getSpecie(speciesName),
                new Predicate<Fisher>() {
                    @Override
                    public boolean test(Fisher fisher) {
                        for (Map.Entry<String, Double> tagProbability : probabilityOfSamplingEachTag.entrySet()) {
                            if(fisher.getTags().contains(tagProbability.getKey()) && random.nextDouble()<tagProbability.getValue()) {
                                return true;
                            }


                        }
                        return false;
                    }
                },
                assumedLinf.apply(random),
                assumedKParameter.apply(random),
                assumedNaturalMortality.apply(random),
                simulatedMaxAge.apply(random).intValue(),
                simulatedVirginRecruits.apply(random),
                assumedLengthBinCm.apply(random),
                assumedVarA.apply(random),
                assumedVarB.apply(random),
                assumedLengthAtMaturity.apply(random));

    }


    public String getSurveyTag() {
        return surveyTag;
    }

    public void setSurveyTag(String surveyTag) {
        this.surveyTag = surveyTag;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public Map<String, Double> getProbabilityOfSamplingEachTag() {
        return probabilityOfSamplingEachTag;
    }

    public void setProbabilityOfSamplingEachTag(LinkedHashMap<String, Double> probabilityOfSamplingEachTag) {
        this.probabilityOfSamplingEachTag = probabilityOfSamplingEachTag;
    }

    public DoubleParameter getAssumedLinf() {
        return assumedLinf;
    }

    public void setAssumedLinf(DoubleParameter assumedLinf) {
        this.assumedLinf = assumedLinf;
    }

    public DoubleParameter getAssumedKParameter() {
        return assumedKParameter;
    }

    public void setAssumedKParameter(DoubleParameter assumedKParameter) {
        this.assumedKParameter = assumedKParameter;
    }

    public DoubleParameter getAssumedNaturalMortality() {
        return assumedNaturalMortality;
    }

    public void setAssumedNaturalMortality(DoubleParameter assumedNaturalMortality) {
        this.assumedNaturalMortality = assumedNaturalMortality;
    }

    public DoubleParameter getSimulatedMaxAge() {
        return simulatedMaxAge;
    }

    public void setSimulatedMaxAge(DoubleParameter simulatedMaxAge) {
        this.simulatedMaxAge = simulatedMaxAge;
    }

    public DoubleParameter getSimulatedVirginRecruits() {
        return simulatedVirginRecruits;
    }

    public void setSimulatedVirginRecruits(DoubleParameter simulatedVirginRecruits) {
        this.simulatedVirginRecruits = simulatedVirginRecruits;
    }

    public DoubleParameter getAssumedLengthBinCm() {
        return assumedLengthBinCm;
    }

    public void setAssumedLengthBinCm(DoubleParameter assumedLengthBinCm) {
        this.assumedLengthBinCm = assumedLengthBinCm;
    }

    public DoubleParameter getAssumedVarA() {
        return assumedVarA;
    }

    public void setAssumedVarA(DoubleParameter assumedVarA) {
        this.assumedVarA = assumedVarA;
    }

    public DoubleParameter getAssumedVarB() {
        return assumedVarB;
    }

    public void setAssumedVarB(DoubleParameter assumedVarB) {
        this.assumedVarB = assumedVarB;
    }

    public DoubleParameter getAssumedLengthAtMaturity() {
        return assumedLengthAtMaturity;
    }

    public void setAssumedLengthAtMaturity(DoubleParameter assumedLengthAtMaturity) {
        this.assumedLengthAtMaturity = assumedLengthAtMaturity;
    }
}
