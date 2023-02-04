package uk.ac.ox.oxfish.model.restrictions;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * 
 * A list of geographic restrictions - they could be of any type
 * These are generic rectangles.
 * @author Brian Powers
 *
 */
public class RegionalRestrictions implements Restriction{

	private List<RestrictedRectangularRegion> restrictions = new ArrayList<>();
	
	public boolean canFishHere(Fisher agent, SeaTile tile, FishState model){
		boolean isOK=true;
		for(RestrictedRectangularRegion restriction : restrictions){
			if(!restriction.canIFishHere(model, tile)){ 
				isOK=false;
				break;
			}
		}
		return isOK;
	}
	
	public void addEternalRestriction(SeaTile nwCorner, SeaTile seCorner){
		RestrictedRectangularRegion restriction = new RestrictedRectangularRegion(nwCorner, seCorner,1);
		this.restrictions.add(restriction);
	}
	
	public void addOneTimeRestriction(SeaTile nwCorner, SeaTile seCorner, int onDay, int offDay){
		RestrictedRectangularRegion restriction = new RestrictedRectangularRegion(nwCorner, seCorner,2,onDay,offDay);
		this.restrictions.add(restriction);
	}
	
	public void addAnnualRestriction(SeaTile nwCorner, SeaTile seCorner, int onDay, int offDay){
		RestrictedRectangularRegion restriction = new RestrictedRectangularRegion(nwCorner, seCorner,3,onDay,offDay);
		this.restrictions.add(restriction);
	}
	
	public void addAnnualRestriction(SeaTile nwCorner, SeaTile seCorner, int onMonth, int onDay, int offMonth, int offDay){
		RestrictedRectangularRegion restriction = new RestrictedRectangularRegion(nwCorner, seCorner,3,onMonth,onDay,offMonth,offDay);
		this.restrictions.add(restriction);
	}
	
	public void clearRestrictions(){
		this.restrictions.clear();
	}

	@Override
	public void start(FishState model, Fisher fisher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void turnOff(Fisher fisher) {
		// TODO Auto-generated method stub
		
	}
	
}
