package uk.ac.ox.oxfish.biology.boxcars;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.CatchAtLengthFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;

public class SPRAgentBuilderFixedSample implements CatchAtLengthFactory {

    private  String surveyTag = "spr_agent";

    private  String speciesName = "Species 0";

    private LinkedHashMap<String,Integer> tagsToSample =
            new LinkedHashMap<>();
    {
     //   tagsToSample.put("population9",100);
    }

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

    private boolean useTNCFormula = true;


    @Override
    public SPRAgent apply(FishState fishState) {
        final MersenneTwisterFast random = fishState.getRandom();


        return new SPRAgent(surveyTag,
                fishState.getBiology().getSpecie(speciesName),
                new CatchSamplerFixedSample(
                        tagsToSample,
                        fishState.getBiology().getSpecie(speciesName)
                ),
                assumedLinf.apply(random),
                assumedKParameter.apply(random),
                assumedNaturalMortality.apply(random),
                simulatedMaxAge.apply(random).intValue(),
                simulatedVirginRecruits.apply(random),
                assumedLengthBinCm.apply(random),
                assumedVarA.apply(random),
                assumedVarB.apply(random),
                assumedLengthAtMaturity.apply(random),
                useTNCFormula ? new SPR() : new LbSPRFormula()
        );

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

    public LinkedHashMap<String, Integer> getTagsToSample() {
        return tagsToSample;
    }

    public void setTagsToSample(LinkedHashMap<String, Integer> tagsToSample) {
        this.tagsToSample = tagsToSample;
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

    public boolean isUseTNCFormula() {
        return useTNCFormula;
    }

    public void setUseTNCFormula(boolean useTNCFormula) {
        this.useTNCFormula = useTNCFormula;
    }
}
