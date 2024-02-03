package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.ArrayFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * a factory that instead of a formula is just given the selectivity per bin.
 * In order to make it compatible with the way heterogeneous gears are instantiated by SnakeYAML I
 * can't use a list but most use a string for the list of bins....
 */
public class SelectivityFromListGearFactory implements HomogeneousGearFactory {

    private DoubleParameter litersOfGasConsumedPerHour = new FixedDoubleParameter(0);

    private String selectivityPerBin = "0,0.5,1";

    private int numberOfSubdivisions = 1;

    private DoubleParameter averageCatchability = new FixedDoubleParameter(0.0001);

    @Override
    public HomogeneousAbundanceGear apply(final FishState fishState) {
        final MersenneTwisterFast random = fishState.getRandom();
        final List<Double> selectivityPerBin = Arrays.stream(this.selectivityPerBin.split(","))
            .map(Double::parseDouble).
            collect(Collectors.toList());
        final double[][] selectivity = new double[numberOfSubdivisions][selectivityPerBin.size()];
        for (int bin = 0; bin < selectivityPerBin.size(); bin++) {
            for (int subdivision = 0; subdivision < selectivity.length; subdivision++) {
                selectivity[subdivision][bin] = selectivityPerBin.get(bin);
            }
        }

        return new HomogeneousAbundanceGear(
            litersOfGasConsumedPerHour.applyAsDouble(random),
            new FixedProportionFilter(averageCatchability.applyAsDouble(random), false),
            new ArrayFilter(false, selectivity)
        );
    }


    public String getSelectivityPerBin() {
        return selectivityPerBin;
    }

    public void setSelectivityPerBin(final String selectivityPerBin) {
        this.selectivityPerBin = selectivityPerBin;
    }

    public DoubleParameter getLitersOfGasConsumedPerHour() {
        return litersOfGasConsumedPerHour;
    }

    public void setLitersOfGasConsumedPerHour(final DoubleParameter litersOfGasConsumedPerHour) {
        this.litersOfGasConsumedPerHour = litersOfGasConsumedPerHour;
    }

    public int getNumberOfSubdivisions() {
        return numberOfSubdivisions;
    }

    public void setNumberOfSubdivisions(final int numberOfSubdivisions) {
        this.numberOfSubdivisions = numberOfSubdivisions;
    }

    @Override
    public DoubleParameter getAverageCatchability() {
        return averageCatchability;
    }

    @Override
    public void setAverageCatchability(final DoubleParameter averageCatchability) {
        this.averageCatchability = averageCatchability;
    }
}
