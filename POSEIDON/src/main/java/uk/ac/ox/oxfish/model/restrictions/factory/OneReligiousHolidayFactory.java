package uk.ac.ox.oxfish.model.restrictions.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.restrictions.RegionalRestrictions;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class OneReligiousHolidayFactory implements AlgorithmFactory<RegionalRestrictions>{

	private DoubleParameter startDayOfYear = new FixedDoubleParameter(1); 
	private DoubleParameter endDayOfYear = new FixedDoubleParameter(180); 

	private DoubleParameter upperLeftCornerX = new FixedDoubleParameter(0); 
	private DoubleParameter upperLeftCornerY = new FixedDoubleParameter(33); 
	private DoubleParameter lowerRightCornerX = new FixedDoubleParameter(49); 
	private DoubleParameter lowerRightCornerY = new FixedDoubleParameter(49); 

	
	public DoubleParameter getStartDayOfYear(){
		return startDayOfYear;
	}
	public void setStartDayOfYear(DoubleParameter startDayOfYear){
		this.startDayOfYear=startDayOfYear;
	}

	public DoubleParameter getEndDayOfYear(){
		return endDayOfYear;
	}
	public void setEndDayOfYear(DoubleParameter endDayOfYear){
		this.endDayOfYear=endDayOfYear;
	}
	
	public DoubleParameter getUpperLeftCornerX(){return upperLeftCornerX;}
	public DoubleParameter getUpperLeftCornerY(){return upperLeftCornerY;}
	public DoubleParameter getLowerRightCornerX(){return lowerRightCornerX;}
	public DoubleParameter getLowerRightCornerY(){return lowerRightCornerY;}
	
	public void setUpperLeftCornerX(DoubleParameter value){this.upperLeftCornerX = value;}
	public void setUpperLeftCornerY(DoubleParameter value){this.upperLeftCornerY = value;}
	public void setLowerRightCornerX(DoubleParameter value){this.lowerRightCornerX = value;}
	public void setLowerRightCornerY(DoubleParameter value){this.lowerRightCornerY = value;}
	
	@Override
	public RegionalRestrictions apply(FishState t) {
		RegionalRestrictions restrictions = new RegionalRestrictions();
		restrictions.addAnnualRestriction(
				t.getMap().getSeaTile(
						upperLeftCornerX.apply(t.random).intValue(),
						upperLeftCornerY.apply(t.random).intValue()),
				t.getMap().getSeaTile(
						lowerRightCornerX.apply(t.random).intValue(),
						lowerRightCornerY.apply(t.random).intValue()), 
				startDayOfYear.apply(t.random).intValue(),
				endDayOfYear.apply(t.random).intValue());
		return restrictions;
	}

}
