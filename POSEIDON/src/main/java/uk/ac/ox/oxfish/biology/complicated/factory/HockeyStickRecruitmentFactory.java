package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.HockeyStickRecruitment;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class HockeyStickRecruitmentFactory implements AlgorithmFactory<HockeyStickRecruitment> {



    private DoubleParameter virginRecruits = new FixedDoubleParameter(6000000);

    private DoubleParameter lengthAtMaturity = new FixedDoubleParameter(50);

    private DoubleParameter virginSpawningBiomass = new FixedDoubleParameter(1000000);

    private DoubleParameter hinge = new FixedDoubleParameter(.3);


    @Override
    public HockeyStickRecruitment apply(FishState state) {
        return new HockeyStickRecruitment(
                false,
                hinge.apply(state.getRandom()),
                virginRecruits.apply(state.getRandom()),
                lengthAtMaturity.apply(state.getRandom()),
                virginSpawningBiomass.apply(state.getRandom())

        );
    }

    public DoubleParameter getVirginRecruits() {
        return virginRecruits;
    }

    public void setVirginRecruits(DoubleParameter virginRecruits) {
        this.virginRecruits = virginRecruits;
    }

    public DoubleParameter getLengthAtMaturity() {
        return lengthAtMaturity;
    }

    public void setLengthAtMaturity(DoubleParameter lengthAtMaturity) {
        this.lengthAtMaturity = lengthAtMaturity;
    }

    public DoubleParameter getVirginSpawningBiomass() {
        return virginSpawningBiomass;
    }

    public void setVirginSpawningBiomass(DoubleParameter virginSpawningBiomass) {
        this.virginSpawningBiomass = virginSpawningBiomass;
    }

    public DoubleParameter getHinge() {
        return hinge;
    }

    public void setHinge(DoubleParameter hinge) {
        this.hinge = hinge;
    }
}
