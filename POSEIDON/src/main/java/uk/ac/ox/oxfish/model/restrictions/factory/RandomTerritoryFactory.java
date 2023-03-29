package uk.ac.ox.oxfish.model.restrictions.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.restrictions.ReputationalRestrictions;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class RandomTerritoryFactory implements AlgorithmFactory<ReputationalRestrictions> {

    private DoubleParameter numberOfTerritorySeaTiles = new FixedDoubleParameter(5);
    private DoubleParameter upperLeftCornerX = new FixedDoubleParameter(0);
    private DoubleParameter upperLeftCornerY = new FixedDoubleParameter(0);
    private DoubleParameter lowerRightCornerX = new FixedDoubleParameter(49);
    private DoubleParameter lowerRightCornerY = new FixedDoubleParameter(16);

    public DoubleParameter getNumberOfTerritorySeaTiles() {
        return numberOfTerritorySeaTiles;
    }

    public void setNumberOfTerritorySeaTiles(DoubleParameter numberOfTerritorySeaTiles) {
        this.numberOfTerritorySeaTiles = numberOfTerritorySeaTiles;
    }

    public DoubleParameter getUpperLeftCornerX() {
        return upperLeftCornerX;
    }

    public void setUpperLeftCornerX(DoubleParameter value) {
        this.upperLeftCornerX = value;
    }

    public DoubleParameter getUpperLeftCornerY() {
        return upperLeftCornerY;
    }

    public void setUpperLeftCornerY(DoubleParameter value) {
        this.upperLeftCornerY = value;
    }

    public DoubleParameter getLowerRightCornerX() {
        return lowerRightCornerX;
    }

    public void setLowerRightCornerX(DoubleParameter value) {
        this.lowerRightCornerX = value;
    }

    public DoubleParameter getLowerRightCornerY() {
        return lowerRightCornerY;
    }

    public void setLowerRightCornerY(DoubleParameter value) {
        this.lowerRightCornerY = value;
    }


    @Override
    public ReputationalRestrictions apply(FishState t) {
        ReputationalRestrictions restrictions = new ReputationalRestrictions();
        int nTerr = (int) numberOfTerritorySeaTiles.applyAsDouble(t.random);
        //System.out.println("Giving "+nTerr+" territories");
        restrictions.addTerritories(t.getMap(), t.getRandom(),
            nTerr,
            (int) upperLeftCornerX.applyAsDouble(t.random),
            (int) upperLeftCornerY.applyAsDouble(t.random),
            (int) lowerRightCornerX.applyAsDouble(t.random),
            (int) lowerRightCornerY.applyAsDouble(t.random)
        );
        return restrictions;
    }
}
