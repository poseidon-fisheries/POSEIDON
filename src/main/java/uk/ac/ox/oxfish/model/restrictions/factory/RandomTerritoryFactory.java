package uk.ac.ox.oxfish.model.restrictions.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.restrictions.ReputationalRestrictions;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class RandomTerritoryFactory implements AlgorithmFactory<ReputationalRestrictions>{

	private DoubleParameter numberOfTerritorySeaTiles = new FixedDoubleParameter(5); 
	
	public DoubleParameter getNumberOfTerritorySeaTiles(){
		return numberOfTerritorySeaTiles;
	}
	public void setNumberOfTerritorySeaTiles(DoubleParameter numberOfTerritorySeaTiles){
		this.numberOfTerritorySeaTiles=numberOfTerritorySeaTiles;
	}

	private DoubleParameter upperLeftCornerX = new FixedDoubleParameter(0); 
	private DoubleParameter upperLeftCornerY = new FixedDoubleParameter(0); 
	private DoubleParameter lowerRightCornerX = new FixedDoubleParameter(49); 
	private DoubleParameter lowerRightCornerY = new FixedDoubleParameter(16); 

	public DoubleParameter getUpperLeftCornerX(){return upperLeftCornerX;}
	public DoubleParameter getUpperLeftCornerY(){return upperLeftCornerY;}
	public DoubleParameter getLowerRightCornerX(){return lowerRightCornerX;}
	public DoubleParameter getLowerRightCornerY(){return lowerRightCornerY;}
	
	public void setUpperLeftCornerX(DoubleParameter value){this.upperLeftCornerX = value;}
	public void setUpperLeftCornerY(DoubleParameter value){this.upperLeftCornerY = value;}
	public void setLowerRightCornerX(DoubleParameter value){this.lowerRightCornerX = value;}
	public void setLowerRightCornerY(DoubleParameter value){this.lowerRightCornerY = value;}
	
	
	@Override
	public ReputationalRestrictions apply(FishState t) {
		ReputationalRestrictions restrictions = new ReputationalRestrictions();
		restrictions.addTerritories(t.getMap(),t.getRandom(), 
				numberOfTerritorySeaTiles.apply(t.random).intValue(),
				upperLeftCornerX.apply(t.random).intValue(),
				upperLeftCornerY.apply(t.random).intValue(),
				lowerRightCornerX.apply(t.random).intValue(),
				lowerRightCornerY.apply(t.random).intValue());
		return restrictions;
	}

}
