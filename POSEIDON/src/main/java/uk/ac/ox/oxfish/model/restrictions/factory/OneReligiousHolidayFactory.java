package uk.ac.ox.oxfish.model.restrictions.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.restrictions.RegionalRestrictions;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class OneReligiousHolidayFactory implements AlgorithmFactory<RegionalRestrictions> {

    private DoubleParameter startDayOfYear = new FixedDoubleParameter(1);
    private DoubleParameter endDayOfYear = new FixedDoubleParameter(180);

    private DoubleParameter upperLeftCornerX = new FixedDoubleParameter(0);
    private DoubleParameter upperLeftCornerY = new FixedDoubleParameter(33);
    private DoubleParameter lowerRightCornerX = new FixedDoubleParameter(49);
    private DoubleParameter lowerRightCornerY = new FixedDoubleParameter(49);


    public DoubleParameter getStartDayOfYear() {
        return startDayOfYear;
    }

    public void setStartDayOfYear(DoubleParameter startDayOfYear) {
        this.startDayOfYear = startDayOfYear;
    }

    public DoubleParameter getEndDayOfYear() {
        return endDayOfYear;
    }

    public void setEndDayOfYear(DoubleParameter endDayOfYear) {
        this.endDayOfYear = endDayOfYear;
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
    public RegionalRestrictions apply(FishState t) {
        RegionalRestrictions restrictions = new RegionalRestrictions();
        restrictions.addAnnualRestriction(
            t.getMap().getSeaTile(
                (int) upperLeftCornerX.applyAsDouble(t.random),
                (int) upperLeftCornerY.applyAsDouble(t.random)
            ),
            t.getMap().getSeaTile(
                (int) lowerRightCornerX.applyAsDouble(t.random),
                (int) lowerRightCornerY.applyAsDouble(t.random)
            ),
            (int) startDayOfYear.applyAsDouble(t.random),
            (int) endDayOfYear.applyAsDouble(t.random)
        );
        return restrictions;
    }

}
