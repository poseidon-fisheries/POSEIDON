package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.complicated.AbundanceDiffuser;
import uk.ac.ox.oxfish.biology.complicated.SpreadYearlyRecruitDecorator;
import uk.ac.ox.oxfish.biology.complicated.YearlyRecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.LinkedHashMap;
import java.util.Map;

public class SingleSpeciesBoxcarPulseRecruitmentFactory implements AlgorithmFactory<SingleSpeciesAbundanceInitializer> {


    private final SingleSpeciesRegularBoxcarFactory delegate = new SingleSpeciesRegularBoxcarFactory();


    private LinkedHashMap<Integer, DoubleParameter> spawningDayAndProportionOfYearlyRecruits = new LinkedHashMap<>();

    @Override
    public SingleSpeciesAbundanceInitializer apply(FishState state) {

        FishYAML yaml = new FishYAML();


        spawningDayAndProportionOfYearlyRecruits = forceThroughYaml(
            spawningDayAndProportionOfYearlyRecruits,
            DoubleParameter.class
        );


        final SingleSpeciesAbundanceInitializer delegateOutput = delegate.apply(state);

        return new SingleSpeciesAbundanceInitializer(
            delegateOutput.getSpeciesName(),
            delegateOutput.getInitialAbundance(),
            delegateOutput.getIntialAbundanceAllocator(),
            delegateOutput.getAging(),
            delegateOutput.getMeristics(),
            delegateOutput.getScaling(),
            new SpreadYearlyRecruitDecorator(
                spawningDayAndProportionOfYearlyRecruits,
                (YearlyRecruitmentProcess) delegateOutput.getRecruitmentProcess(),
                state.getRandom()
            ),
            delegateOutput.getDiffuser(),
            delegateOutput.getRecruitmentAllocator(),
            delegateOutput.getHabitabilityAllocator(),
            delegateOutput.getMortality(),
            delegateOutput.isDaily(),
            delegateOutput.isRounding()

        );


    }

    public static <KEY, VALUE> LinkedHashMap<KEY, VALUE> forceThroughYaml(
        LinkedHashMap<KEY, ?> original,
        Class<VALUE> valueClass
    ) {

        FishYAML yaml = new FishYAML();
        LinkedHashMap<KEY, VALUE> correctedMap = new LinkedHashMap<>();
        for (Map.Entry<KEY, ?> entry : original.entrySet()) {
            Object toConvert = entry.getValue();
            final VALUE converted = yaml.loadAs(yaml.dump(toConvert), valueClass);
            correctedMap.put(entry.getKey(), converted);

        }

        return correctedMap;

    }

    public String getSpeciesName() {
        return delegate.getSpeciesName();
    }

    public void setSpeciesName(String speciesName) {
        delegate.setSpeciesName(speciesName);
    }

    public DoubleParameter getInitialBtOverK() {
        return delegate.getInitialBtOverK();
    }

    public void setInitialBtOverK(DoubleParameter initialBtOverK) {
        delegate.setInitialBtOverK(initialBtOverK);
    }

    public DoubleParameter getLInfinity() {
        return delegate.getLInfinity();
    }

    public void setLInfinity(DoubleParameter LInfinity) {
        delegate.setLInfinity(LInfinity);
    }

    public DoubleParameter getK() {
        return delegate.getK();
    }

    public void setK(DoubleParameter k) {
        delegate.setK(k);
    }

    public DoubleParameter getAllometricAlpha() {
        return delegate.getAllometricAlpha();
    }

    public void setAllometricAlpha(DoubleParameter allometricAlpha) {
        delegate.setAllometricAlpha(allometricAlpha);
    }

    public DoubleParameter getAllometricBeta() {
        return delegate.getAllometricBeta();
    }

    public void setAllometricBeta(DoubleParameter allometricBeta) {
        delegate.setAllometricBeta(allometricBeta);
    }

    public double getCmPerBin() {
        return delegate.getCmPerBin();
    }

    public void setCmPerBin(double cmPerBin) {
        delegate.setCmPerBin(cmPerBin);
    }

    public int getNumberOfBins() {
        return delegate.getNumberOfBins();
    }

    public void setNumberOfBins(int numberOfBins) {
        delegate.setNumberOfBins(numberOfBins);
    }

    public DoubleParameter getYearlyMortality() {
        return delegate.getYearlyMortality();
    }

    public void setYearlyMortality(DoubleParameter yearlyMortality) {
        delegate.setYearlyMortality(yearlyMortality);
    }

    public DoubleParameter getVirginRecruits() {
        return delegate.getVirginRecruits();
    }

    public void setVirginRecruits(DoubleParameter virginRecruits) {
        delegate.setVirginRecruits(virginRecruits);
    }

    public DoubleParameter getSteepness() {
        return delegate.getSteepness();
    }

    public void setSteepness(DoubleParameter steepness) {
        delegate.setSteepness(steepness);
    }

    public DoubleParameter getCumulativePhi() {
        return delegate.getCumulativePhi();
    }

    public void setCumulativePhi(DoubleParameter cumulativePhi) {
        delegate.setCumulativePhi(cumulativePhi);
    }

    public DoubleParameter getLengthAtMaturity() {
        return delegate.getLengthAtMaturity();
    }

    public void setLengthAtMaturity(DoubleParameter lengthAtMaturity) {
        delegate.setLengthAtMaturity(lengthAtMaturity);
    }

    public AlgorithmFactory<? extends BiomassAllocator> getInitialAbundanceAllocator() {
        return delegate.getInitialAbundanceAllocator();
    }

    public void setInitialAbundanceAllocator(AlgorithmFactory<? extends BiomassAllocator> initialAbundanceAllocator) {
        delegate.setInitialAbundanceAllocator(initialAbundanceAllocator);
    }

    public AlgorithmFactory<? extends AbundanceDiffuser> getDiffuser() {
        return delegate.getDiffuser();
    }

    public void setDiffuser(AlgorithmFactory<? extends AbundanceDiffuser> diffuser) {
        delegate.setDiffuser(diffuser);
    }

    public AlgorithmFactory<? extends BiomassAllocator> getRecruitAllocator() {
        return delegate.getRecruitAllocator();
    }

    public void setRecruitAllocator(AlgorithmFactory<? extends BiomassAllocator> recruitAllocator) {
        delegate.setRecruitAllocator(recruitAllocator);
    }

    public AlgorithmFactory<? extends BiomassAllocator> getHabitabilityAllocator() {
        return delegate.getHabitabilityAllocator();
    }

    public void setHabitabilityAllocator(AlgorithmFactory<? extends BiomassAllocator> habitabilityAllocator) {
        delegate.setHabitabilityAllocator(habitabilityAllocator);
    }


    public LinkedHashMap<Integer, DoubleParameter> getSpawningDayAndProportionOfYearlyRecruits() {
        return spawningDayAndProportionOfYearlyRecruits;
    }

    public void setSpawningDayAndProportionOfYearlyRecruits(LinkedHashMap<Integer, DoubleParameter> spawningDayAndProportionOfYearlyRecruits) {
        this.spawningDayAndProportionOfYearlyRecruits = spawningDayAndProportionOfYearlyRecruits;
    }
}
