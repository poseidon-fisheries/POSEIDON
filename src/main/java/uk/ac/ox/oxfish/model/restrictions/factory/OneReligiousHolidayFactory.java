package uk.ac.ox.oxfish.model.restrictions.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.restrictions.RegionalRestrictions;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class OneReligiousHolidayFactory implements AlgorithmFactory<RegionalRestrictions>{

	private DoubleParameter startDayOfYear = new FixedDoubleParameter(35); 
	private DoubleParameter endDayOfYear = new FixedDoubleParameter(90); 
	
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
	
	@Override
	public RegionalRestrictions apply(FishState t) {
		RegionalRestrictions restrictions = new RegionalRestrictions();
		restrictions.addAnnualRestriction(t.getMap().getSeaTile(0,0),t.getMap().getSeaTile(t.getMap().getWidth()-1,t.getMap().getHeight()-1), startDayOfYear.apply(t.random).intValue(),endDayOfYear.apply(t.random).intValue());
		return restrictions;
	}

}
