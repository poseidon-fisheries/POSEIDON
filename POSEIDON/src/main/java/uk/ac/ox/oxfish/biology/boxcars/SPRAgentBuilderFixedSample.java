package uk.ac.ox.oxfish.biology.boxcars;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.CatchAtLengthFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;

public class SPRAgentBuilderFixedSample implements CatchAtLengthFactory {

    private String surveyTag = "spr_agent";

    private String speciesName = "Species 0";

    private LinkedHashMap<String, Integer> tagsToSample =
        new LinkedHashMap<>();
    private DoubleParameter assumedLinf = new FixedDoubleParameter(86);
    private DoubleParameter assumedKParameter = new FixedDoubleParameter(0.4438437);
    private DoubleParameter assumedNaturalMortality = new FixedDoubleParameter(0.3775984);
    private DoubleParameter simulatedMaxAge = new FixedDoubleParameter(100);
    //these aren't "real" virgin recruits, this is just the number of simulated ones
    //used by Peter in his formula
    private DoubleParameter simulatedVirginRecruits = new FixedDoubleParameter(1000);
    private DoubleParameter assumedLengthBinCm = new FixedDoubleParameter(5);
    private DoubleParameter assumedVarA = new FixedDoubleParameter(0.00853);
    private DoubleParameter assumedVarB = new FixedDoubleParameter(3.137);
    private DoubleParameter assumedLengthAtMaturity = new FixedDoubleParameter(50);
    private boolean useTNCFormula = true;
    /**
     * if using TNC formula, shall we remove the smallest percentile of catches from the SPR?
     * Both in real world and in the simulated one, it tends to improve numerical stability by quite a lot
     */
    private boolean removeSmallestPercentile = false;

    {
        //   tagsToSample.put("population9",100);
    }

    @Override
    public SPRAgent apply(final FishState fishState) {
        final MersenneTwisterFast random = fishState.getRandom();


        return new SPRAgent(
            surveyTag,
            fishState.getBiology().getSpeciesByCaseInsensitiveName(speciesName),
            new CatchSamplerFixedSample(
                tagsToSample,
                fishState.getBiology().getSpeciesByCaseInsensitiveName(speciesName)
            ),
            assumedLinf.applyAsDouble(random),
            assumedKParameter.applyAsDouble(random),
            assumedNaturalMortality.applyAsDouble(random),
            (int) simulatedMaxAge.applyAsDouble(random),
            simulatedVirginRecruits.applyAsDouble(random),
            assumedLengthBinCm.applyAsDouble(random),
            assumedVarA.applyAsDouble(random),
            assumedVarB.applyAsDouble(random),
            assumedLengthAtMaturity.applyAsDouble(random),
            useTNCFormula ? new SPR(removeSmallestPercentile) : new LbSPRFormula()
        );

    }

    public String getSurveyTag() {
        return surveyTag;
    }

    public void setSurveyTag(final String surveyTag) {
        this.surveyTag = surveyTag;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(final String speciesName) {
        this.speciesName = speciesName;
    }

    public LinkedHashMap<String, Integer> getTagsToSample() {
        return tagsToSample;
    }

    public void setTagsToSample(final LinkedHashMap<String, Integer> tagsToSample) {
        this.tagsToSample = tagsToSample;
    }

    public DoubleParameter getAssumedLinf() {
        return assumedLinf;
    }

    public void setAssumedLinf(final DoubleParameter assumedLinf) {
        this.assumedLinf = assumedLinf;
    }

    public DoubleParameter getAssumedKParameter() {
        return assumedKParameter;
    }

    public void setAssumedKParameter(final DoubleParameter assumedKParameter) {
        this.assumedKParameter = assumedKParameter;
    }

    public DoubleParameter getAssumedNaturalMortality() {
        return assumedNaturalMortality;
    }

    public void setAssumedNaturalMortality(final DoubleParameter assumedNaturalMortality) {
        this.assumedNaturalMortality = assumedNaturalMortality;
    }

    public DoubleParameter getSimulatedMaxAge() {
        return simulatedMaxAge;
    }

    public void setSimulatedMaxAge(final DoubleParameter simulatedMaxAge) {
        this.simulatedMaxAge = simulatedMaxAge;
    }

    public DoubleParameter getSimulatedVirginRecruits() {
        return simulatedVirginRecruits;
    }

    public void setSimulatedVirginRecruits(final DoubleParameter simulatedVirginRecruits) {
        this.simulatedVirginRecruits = simulatedVirginRecruits;
    }

    public DoubleParameter getAssumedLengthBinCm() {
        return assumedLengthBinCm;
    }

    public void setAssumedLengthBinCm(final DoubleParameter assumedLengthBinCm) {
        this.assumedLengthBinCm = assumedLengthBinCm;
    }

    public DoubleParameter getAssumedVarA() {
        return assumedVarA;
    }

    public void setAssumedVarA(final DoubleParameter assumedVarA) {
        this.assumedVarA = assumedVarA;
    }

    public DoubleParameter getAssumedVarB() {
        return assumedVarB;
    }

    public void setAssumedVarB(final DoubleParameter assumedVarB) {
        this.assumedVarB = assumedVarB;
    }

    public DoubleParameter getAssumedLengthAtMaturity() {
        return assumedLengthAtMaturity;
    }

    public void setAssumedLengthAtMaturity(final DoubleParameter assumedLengthAtMaturity) {
        this.assumedLengthAtMaturity = assumedLengthAtMaturity;
    }

    public boolean isUseTNCFormula() {
        return useTNCFormula;
    }

    public void setUseTNCFormula(final boolean useTNCFormula) {
        this.useTNCFormula = useTNCFormula;
    }

    public boolean isRemoveSmallestPercentile() {
        return removeSmallestPercentile;
    }

    public void setRemoveSmallestPercentile(final boolean removeSmallestPercentile) {
        this.removeSmallestPercentile = removeSmallestPercentile;
    }
}
