package uk.ac.ox.oxfish.biology.boxcars;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.CatchAtLengthFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * creates an SPR Agent (a method that yearly computes SPRs) by sampling
 * a random proportion of boats
 */
public class SPRAgentBuilder implements CatchAtLengthFactory {

    private String surveyTag = "spr_agent";

    private String speciesName = "Species 0";

    /**
     * if more formulas come up we can turn this into a full strategy
     */
    private boolean useTNCFormula = true;


    private DoubleParameter probabilityOfSamplingEachBoat =
        new FixedDoubleParameter(0.33);

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

    /**
     * if using TNC formula, shall we remove the smallest percentile of catches from the SPR?
     * Both in real world and in the simulated one, it tends to improve numerical stability by quite a lot
     */
    private boolean removeSmallestPercentile = false;


    public SPRAgentBuilder() {
    }

    public SPRAgentBuilder(
        final String surveyTag, final String speciesName,
        final double probabilityOfSamplingEachBoat,
        final double assumedLinf,
        final double assumedKParameter,
        final double assumedNaturalMortality,
        final int simulatedMaxAge,
        final double simulatedVirginRecruits,
        final double assumedLengthBinCm,
        final double assumedVarA,
        final double assumedVarB,
        final double assumedLengthAtMaturity
    ) {
        this.surveyTag = surveyTag;
        this.speciesName = speciesName;
        this.probabilityOfSamplingEachBoat = new FixedDoubleParameter(probabilityOfSamplingEachBoat);
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
    public SPRAgent apply(final FishState fishState) {
        final MersenneTwisterFast random = fishState.getRandom();
        final double samplingProbability = probabilityOfSamplingEachBoat.applyAsDouble(random);

        Preconditions.checkArgument(
            fishState.getBiology().getSpeciesByCaseInsensitiveName(speciesName.trim()) != null,
            "There is no species " + speciesName
        );
        return new SPRAgent(
            surveyTag,
            fishState.getBiology().getSpeciesByCaseInsensitiveName(speciesName),
            fisher -> random.nextDouble() < samplingProbability,
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

    public DoubleParameter getProbabilityOfSamplingEachBoat() {
        return probabilityOfSamplingEachBoat;
    }

    public void setProbabilityOfSamplingEachBoat(final DoubleParameter probabilityOfSamplingEachBoat) {
        this.probabilityOfSamplingEachBoat = probabilityOfSamplingEachBoat;
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
