package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.geography.SeaTile;

public class Territory {
	private SeaTile location;
	private int  timesFished;
	
	public Territory(SeaTile location){
		this.location=location;
		this.timesFished=0;
	}
	
	public SeaTile getLocation(){
		return location;
	}
	
	public int getTimesFished(){
		return timesFished;
	}
	
	public void incrementTimesFished(){
		this.timesFished++;
	}
	
	public void setLocation(SeaTile location){
		this.location=location;
	}
	
}
