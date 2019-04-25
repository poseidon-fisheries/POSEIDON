/*
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.destination;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.log.SharedTripRecord;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.MarketMap;

/** 
 * Generalized Cognitive Model Destination Strategy
 * Created by Brian Powers on Pi Day 2019 (3/14/2019)
 */
public class GeneralizedCognitiveStrategy implements DestinationStrategy {

	static List<PubliclySharedProfit> publicProfit=null;
	double minAbsoluteSatisfactoryProfit,
			minRelativeSatisfactoryProfit,
			weightProfit,
			weightLaw,
			weightCommunal,
			weightReputation;

	double inverseDistanceExponent = 16.0; //
	
	int timeScalarFunction = 0; //Default
	double timeScalarParameter1,timeScalarParameter2;

	double kExplore;
	int nExplore=1;
	double profitBest;
	int numberOfSpecies = -1;
	
	boolean thisTripWasExploration=false;
	boolean needToUpdateN = false;
	TripSharer tripSharer;
	
	class TripSharer implements Steppable{
		Stoppable dailyShare;
		Fisher fisher;
		
		TripSharer(Fisher fisher){
			this.fisher = fisher;
		}
		
		void startSharing(FishState model){
			dailyShare = model.scheduleEveryDay(this, StepOrder.FISHER_PHASE);
		}
		
		public void step(SimState simState){
			//pick from among the 10 most profitable trips, or fewer if fewer trips have been logged
			List<TripRecord> finishedTrips = fisher.getFinishedTrips();
			int nChoices = Math.min(10, finishedTrips.size());
			if (nChoices>0){
				TripRecord[]bestTrips = new TripRecord[nChoices];
				//Go through the trips, and put them into this array starting with index 0.
				//bump them down the list if there is a better one.
				for(TripRecord finishedTrip : finishedTrips){
					for(int i=0; i<nChoices;i++){
						if(bestTrips[i]==null){
							bestTrips[i]=finishedTrip;
							break;
						} else if (finishedTrip.getTotalTripProfit() > bestTrips[i].getTotalTripProfit()){
							//bump them down, starting with the end of the list going up
							for(int j=nChoices-1; j>i; j--){
								bestTrips[j]=bestTrips[j-1];
							}
							bestTrips[i]=finishedTrip;
							break;
						}
						
					}
				}
				//now pick one of these trips at random and share it with my social network:
				MersenneTwisterFast random = new MersenneTwisterFast();
				fisher.shareTrip(bestTrips[random.nextInt(nChoices)], true, null);
			}
		}
		
	}
	
	class ViableDestination{
		boolean observed = false;
		double[] expectedCatch;
		double scalarTotal;
		double expectedProfit;
		SeaTile destination;
		double attractiveness;
		double probability; // used if exploring
		
		void setObserved(){
			this.observed=true;
		}
	}
	
	class PubliclySharedProfit{
		double profit;
		Fisher fisher;
		
		public PubliclySharedProfit(double profit, Fisher fisher){
			this.profit= profit;
			this.fisher = fisher;
		}
		
		double getProfit(){
			return profit;
		}
		
		Fisher getFisher(){
			return fisher;
		}
		
		void setProfit(double profit){
			this.profit = profit;
		}
		
	}
	
	void setPublicProfit(Fisher fisher, double profit){
		boolean inList=false;
		for(PubliclySharedProfit publicFisherProfit: publicProfit){
			if(publicFisherProfit.getFisher()==fisher){
				publicFisherProfit.setProfit(profit);
				inList = true;
				break;
			}
		}
		if(!inList){
			publicProfit.add(new PubliclySharedProfit(profit, fisher));
		}
	}
	
	double getPublicProfit(Fisher fisher){
		double publicProfitValue = 0;
		if(publicProfit!=null){
			for(PubliclySharedProfit publicFisherProfit: publicProfit){
				if(publicFisherProfit.getFisher()==fisher){
					publicProfitValue = publicFisherProfit.getProfit();
					break;
				}
			}
		}
		return publicProfitValue;
	}
	
	@Override
	public void start(FishState model, Fisher fisher) {
		numberOfSpecies=model.getSpecies().size();
		tripSharer = new TripSharer(fisher);
	}

	@Override
	public void turnOff(Fisher fisher) {
	}

	@Override
	public SeaTile chooseDestination(
			Fisher fisher, 
			MersenneTwisterFast random, 
			FishState model, 
			Action currentAction) {
		
		SeaTile finalDestionation=fisher.getHomePort().getLocation();
		
		//If the fisher is at port, choose a destination
        if(fisher.isAtPort())
        {
            //they are probably docked
            assert currentAction instanceof AtPort;
            assert fisher.isGoingToPort(); //I assume at port your destination is still the port

            //Start with all water tiles
            List<SeaTile> allSeaTiles = model.getMap().getAllSeaTilesExcludingLandAsList();
            List<ViableDestination> viableDestinations = new ArrayList<>();
            for(SeaTile destination: allSeaTiles){
            	ViableDestination viableDestination = new ViableDestination();
            	viableDestination.destination=destination;
            	viableDestination.expectedCatch = new double[numberOfSpecies];
            	viableDestinations.add(viableDestination);
            }
            
            boolean noObservations = true;
            List<ViableDestination> observedDestinations = new ArrayList<>();
            
            //Now go through the finished trips and calculate expected catch at all locations 
            for(TripRecord trip: fisher.getFinishedTrips()){
            	//Calculate the time scalar for this trip based on the time
            	int t=model.getDay()-trip.getTripDate();
            	double scalar=timeScalar(t);
            	SeaTile tripDestination = trip.getMostFishedTileInTrip();
            	for(ViableDestination viableDestination : viableDestinations){
            		if(viableDestination.destination.equals(tripDestination)){
            			viableDestination.setObserved();
            			if (!observedDestinations.contains(viableDestination))
            				observedDestinations.add(viableDestination);
            			noObservations = false;
            			for(int i=0; i<numberOfSpecies; i++){
            				viableDestination.expectedCatch[i] += scalar*trip.getSoldCatch()[i];
            				viableDestination.scalarTotal+=scalar;
            			}
            			break;
            		}
            	}
            }
            
            //Now go through trips shared by friends and add them to the mix
            Collection<Fisher> myFriends = fisher.getSocialNetwork().getDirectedNeighbors(fisher);
        	for(Fisher friend : myFriends){
        		List<SharedTripRecord> friendSharedTrips = friend.getTripsSharedWith(fisher);
        		for(SharedTripRecord friendSharedTrip: friendSharedTrips){
        			TripRecord trip = friendSharedTrip.getTrip();
                	int t=model.getDay()-trip.getTripDate();
                	double scalar=timeScalar(t);
                	SeaTile tripDestination = trip.getMostFishedTileInTrip();
                	for(ViableDestination viableDestination : viableDestinations){
                		if(viableDestination.destination.equals(tripDestination)){
                			viableDestination.setObserved();
                			if (!observedDestinations.contains(viableDestination))
                				observedDestinations.add(viableDestination);
                			noObservations = false;
                			for(int i=0; i<numberOfSpecies; i++){
                				viableDestination.expectedCatch[i] += scalar*trip.getSoldCatch()[i];
                				viableDestination.scalarTotal+=scalar;
                			}
                			break;
                		}
                	}
        		}
        	}      
            
            for(ViableDestination viableDestination : viableDestinations){
            	for(int i=0; i<numberOfSpecies; i++){
            		//if there have been no trips there, just zero out the expected catch
            		viableDestination.expectedCatch[i] *= (viableDestination.scalarTotal>0?(1.0/viableDestination.scalarTotal):0);
            	}
        		//Now that we have the expected sold catch per species at all the viable
	            //locations, we calculate the expected cost of fishing at
	            //that location
	            
	            //figure out the expected price per species
            	Port homePort = fisher.getHomePort();
            	MarketMap marketMap =homePort.getMarketMap(fisher);
            	
            	for(int i=0; i<numberOfSpecies; i++){
            		double speciesPrice = marketMap.getSpeciesPrice(i);
            		viableDestination.expectedProfit+= viableDestination.expectedCatch[i]*speciesPrice;
            	}
	            //figure out the expected operational cost
                //figure out the expected trip cost
        		double expectedCost = estimateTripCost(viableDestination.destination,fisher, model);
        		viableDestination.expectedProfit += -expectedCost;
            }
            
            //If there is no history, then set the expected profit of EVERY location to be equal so 
            //they have a chance to fish anywhere
            if(noObservations){
            	for(ViableDestination viableDestination : viableDestinations){
            		viableDestination.expectedProfit = this.minAbsoluteSatisfactoryProfit;
            	}
            } else {
            //Otherwise go through all viable locations with no observations we estimate them using inverse distance weighting
            //This is pretty flexible and can be tuned by the exponent
            	for(ViableDestination destination : viableDestinations){
            		if(!observedDestinations.contains(destination)){
            			double sumScalars = 0.0;
            			destination.expectedProfit = 0.0;
            			for(ViableDestination observedDestination : observedDestinations){
            				double distance = model.getMap().distance(observedDestination.destination, destination.destination);
            				if(distance>0){
            					double scalar = 1.0 / Math.pow(distance,inverseDistanceExponent);
            					destination.expectedProfit += scalar*observedDestination.expectedProfit;
            					sumScalars+=scalar;
            				} else { //If for some reason there is another SeaTile on top of an observed seatile...
            					destination.expectedProfit = observedDestination.expectedProfit;
            					sumScalars=1.0;
            					break;
            				}
            			}
            			destination.expectedProfit *= 1.0 / sumScalars;
            		}
            	}
            }
            
            double minSocialSatisfactoryProfit=0;
            int nToBeat = (int)Math.floor(myFriends.size() * minRelativeSatisfactoryProfit);
            double[] friendProfits = new double[myFriends.size()];
            if(nToBeat>0){
            	int i=0;
            	for(Fisher friend : myFriends){
            		friendProfits[i]=getPublicProfit(friend);
            		i++;
            	}
            	for(i=0; i<friendProfits.length-1; i++){
            		for(int j=i+1; j<friendProfits.length; j++){
            			if(friendProfits[j]<friendProfits[i]){
            				double tempProfit = friendProfits[j];
            				friendProfits[j]=friendProfits[i];
            				friendProfits[i]=tempProfit;
            			}
            		}
            	}
            	minSocialSatisfactoryProfit = friendProfits[nToBeat-1];
            }
            
            double profitCutoff = Math.max(minAbsoluteSatisfactoryProfit, minSocialSatisfactoryProfit);
            
            //Remove any destinations that don't offer satisfactory profit
            {
            	Iterator<ViableDestination> d = viableDestinations.iterator();
	            while(d.hasNext()){
	            	ViableDestination s=d.next();
	            	if(s.expectedProfit<profitCutoff)
	            		d.remove();
	            }
            }
            
            double highestProfit=0.0;
            for(ViableDestination viableDestination: viableDestinations){
            	highestProfit = Math.max(highestProfit,viableDestination.expectedProfit);
            }
            
            for(ViableDestination viableDestination: viableDestinations){
            	viableDestination.attractiveness = weightProfit * viableDestination.expectedProfit -
            				weightLaw * (fisher.isAllowedToFishHere(viableDestination.destination, model)?0:1) -
            				weightCommunal * (fisher.isBadByCommunityStandardsToFishHere(viableDestination.destination, model)?0:1)-
            				weightReputation * (fisher.isBadReputationToFishHere(viableDestination.destination, model)?0:1);
            }
            //Remove any destinations with negative attractiveness
            {
            	Iterator<ViableDestination> d = viableDestinations.iterator();
	            while(d.hasNext()){
	            	ViableDestination s=d.next();
	            	if(s.attractiveness<0)
	            		d.remove();
	            }
            }
            
            //determine the most attractive site
            SeaTile mostAttractiveDestination = null;
            double bestAttraction=0;
            double totalProfits = 0;
            for(ViableDestination viableDestination: viableDestinations){
            	totalProfits+=viableDestination.expectedProfit;
            	if (viableDestination.attractiveness>bestAttraction){
            		mostAttractiveDestination = viableDestination.destination;
            		bestAttraction = viableDestination.attractiveness;
            		profitBest = viableDestination.expectedProfit; 
            		setPublicProfit(fisher,profitBest);
            	}
            }            
            
            //See if they had a previous trip and we need to update 'n'
            //The number of explorations that have not paid off:
            if(needToUpdateN){
            	double lastProfit = fisher.getLastFinishedTrip().getTotalTripProfit();
            	if(thisTripWasExploration && lastProfit > profitBest){
            		//Exploration paid off, reset N
            		nExplore = 1;
            	} else {
            		//Exploration was a bust (or exploited), increase N
            		nExplore++;
            	}
            }
            
            needToUpdateN = true;
            boolean goExploring = (viableDestinations.size()>1)?shouldIExplore(random):false;
            if(goExploring){
            	totalProfits -= profitBest;
            	thisTripWasExploration=true;
                {
                	Iterator<ViableDestination> d = viableDestinations.iterator();
    	            while(d.hasNext()){
    	            	ViableDestination s=d.next();
    	            	if(s.destination == mostAttractiveDestination)
    	            		d.remove();
    	            	else
    	            		s.probability = s.expectedProfit/totalProfits;
    	            }
                }
                double randDouble=random.nextDouble();
                for(ViableDestination viableDestination: viableDestinations){
                	randDouble -= viableDestination.probability;
                	if(randDouble<=0){
                		finalDestionation= viableDestination.destination;
                		break;
                	}
                }
            	
            } else {
            	thisTripWasExploration=false;
            	finalDestionation= mostAttractiveDestination;
            }
        }
        else
        {
            //we are not at port
            assert ! (currentAction instanceof AtPort);
            //are we there yet?
            if(fisher.getLocation() == fisher.getDestination())
            	finalDestionation= fisher.getHomePort().getLocation(); //return home
            else
            	finalDestionation= fisher.getDestination(); //stay the course!
        }
        return finalDestionation;
	}

	boolean shouldIExplore(MersenneTwisterFast random){
		return (random.nextBoolean(1/Math.pow(nExplore,kExplore)));
	}
	
	double timeScalar(double t){
		if (timeScalarFunction == 1){
			return 1/Math.pow(t+1,timeScalarParameter1);
		} else if (timeScalarFunction == 2) {
			return Math.exp(-timeScalarParameter1 * t);
		} else if (timeScalarFunction == 3) {
			return (1+Math.exp(-timeScalarParameter1))/(1+Math.exp(-timeScalarParameter1 + timeScalarParameter2 * t));	
		} else {
			return 1;
		}
	}
	
	double estimateTripCost(SeaTile destination, Fisher fisher, FishState model){
		double estimatedCost = 0;
		
		List<Cost> additionalCosts = fisher.getAdditionalTripCosts();
		SeaTile homePort = fisher.getHomePort().getLocation();
		double expectedFuel = fisher.getExpectedFuelConsumption(model.getMap().distance(destination,homePort));
		double fuelPrice = fisher.getHomePort().getGasPricePerLiter();
		for(Cost realCosts : additionalCosts){
			//We want to account for the additional costs of the trip - but since it is the same for all destinations, perhaps it doesn't matter.
		}
		estimatedCost += fuelPrice*expectedFuel;
		return estimatedCost;
	}
}
