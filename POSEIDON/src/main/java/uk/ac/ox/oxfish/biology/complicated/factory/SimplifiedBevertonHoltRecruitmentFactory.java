package uk.ac.ox.oxfish.biology.complicated.factory;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;

/**
 * this is the formula used in MERA for recruitment where there is no relative fecundity, and "PHI" is just the ratio
 * SSB0/Virgin recruits
 */
public class SimplifiedBevertonHoltRecruitmentFactory implements AlgorithmFactory<RecruitmentBySpawningBiomass> {


    private List<Double> maturity = Lists.newArrayList(.0d,.5d,.1d);


    /**
     * the number of recruits you'd get in a "virgin" state.
     */
    private DoubleParameter virginRecruits = new FixedDoubleParameter(40741397);



    /**
     * logistic growth parameter
     */
    private DoubleParameter steepness = new FixedDoubleParameter(0.6);


    private DoubleParameter spawningStockBiomass = new FixedDoubleParameter(10000);

    @Override
    public RecruitmentBySpawningBiomass apply(FishState fishState) {
        return new RecruitmentBySpawningBiomass(
                virginRecruits.apply(fishState.getRandom()).intValue(),
                steepness.apply(fishState.getRandom()),
                spawningStockBiomass.apply(fishState.getRandom())/
                        virginRecruits.apply(fishState.getRandom()),
                false,
                Doubles.toArray(maturity),
                null,
                0,
                false
        );


    }

    public List<Double> getMaturity() {
        return maturity;
    }

    public void setMaturity(List<Double> maturity) {
        this.maturity = maturity;
    }

    public DoubleParameter getVirginRecruits() {
        return virginRecruits;
    }

    public void setVirginRecruits(DoubleParameter virginRecruits) {
        this.virginRecruits = virginRecruits;
    }

    public DoubleParameter getSteepness() {
        return steepness;
    }

    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    public DoubleParameter getSpawningStockBiomass() {
        return spawningStockBiomass;
    }

    public void setSpawningStockBiomass(DoubleParameter spawningStockBiomass) {
        this.spawningStockBiomass = spawningStockBiomass;
    }
}
