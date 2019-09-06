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

	@Override
	public ReputationalRestrictions apply(FishState t) {
		ReputationalRestrictions restrictions = new ReputationalRestrictions();
		restrictions.addTerritories(t.getMap(),t.getRandom(),numberOfTerritorySeaTiles.apply(t.getRandom()).intValue());
		return restrictions;
	}

}
