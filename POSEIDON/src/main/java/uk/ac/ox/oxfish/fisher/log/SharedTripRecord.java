package uk.ac.ox.oxfish.fisher.log;

import java.util.Collection;

import uk.ac.ox.oxfish.fisher.Fisher;

public class SharedTripRecord {
	
	private TripRecord trip;
	private boolean allFriends=true;
	private Collection<Fisher> sharedFriends=null;
	
	public SharedTripRecord(TripRecord trip, boolean allFriends, Collection<Fisher> sharedFriends){
		this.trip = trip;
		this.allFriends = allFriends;
		if(sharedFriends != null){
			shareWithMoreFriends(sharedFriends);
		}
	}
	public TripRecord getTrip(){
		return trip;
	}
	
	public void shareWithAllFriends(){
		allFriends = true;
	}
	
	public boolean sharedWithAll(){
		return this.allFriends;
	}
	
	public Collection<Fisher> getSharedFriends(){
		return sharedFriends;
	}
	
	public void shareWithMoreFriends(Collection<Fisher> newSharedFriends){
		for(Fisher newSharedFriend:newSharedFriends){
			boolean addHim = true;
			for(Fisher oldSharedFriend:sharedFriends){
				if(oldSharedFriend.equals(newSharedFriend)){
					addHim=false;
					break;
				}
			}
			if(addHim) sharedFriends.add(newSharedFriend);
		}
	}
	
}
