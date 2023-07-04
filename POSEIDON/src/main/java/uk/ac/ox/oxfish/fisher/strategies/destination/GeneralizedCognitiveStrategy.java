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

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.log.SharedTripRecord;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.TimeScalarFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generalized Cognitive Model Destination Strategy
 * Created by Brian Powers on Pi Day 2019 (3/14/2019)
 */
public class GeneralizedCognitiveStrategy implements DestinationStrategy {

    static List<PubliclySharedProfit> publicProfit = new ArrayList<PubliclySharedProfit>();
    double minAbsoluteSatisfactoryProfit,
        minRelativeSatisfactoryProfit,
        weightProfit,
        weightLaw,
        weightCommunal,
        weightReputation;
//	int numberOfTerritorySites;

    double inverseDistanceExponent = 16.0; //

    //	int timeScalarFunction = 0; //Default
//	double timeScalarParameter1,timeScalarParameter2;
    TimeScalarFunction timeScalarFunction;

    double kExplore;
    int nExplore = 1;
    double profitBest;
    int numberOfSpecies = -1;

    boolean thisTripWasExploration = false;
    boolean needToUpdateN = false;
    TripSharer tripSharer;
    SeaTile chosenFishingSite;
    boolean pickNewSite = true;

    public GeneralizedCognitiveStrategy(
        final double minAbsoluteSatisfactoryProfit,
        final double minRelativeSatisfactoryProfit,
        final double weightProfit,
        final double weightLaw,
        final double weightCommunal,
        final double weightReputation,
        final TimeScalarFunction timeScalarFunction,
//			double timeScalarParameter1,
//			double timeScalarParameter2,
        final double kExplore/*,
			double numberOfTerritorySites*/
    ) {
        this.minAbsoluteSatisfactoryProfit = minAbsoluteSatisfactoryProfit;
        this.minRelativeSatisfactoryProfit = minRelativeSatisfactoryProfit;
        this.weightProfit = weightProfit;
        this.weightLaw = weightLaw;
        this.weightCommunal = weightCommunal;
        this.weightReputation = weightReputation;
        this.timeScalarFunction = timeScalarFunction;
//		this.timeScalarParameter1=timeScalarParameter1;
//		this.timeScalarParameter2=timeScalarParameter2;
        this.kExplore = kExplore;
//		this.numberOfTerritorySites=(int)numberOfTerritorySites;

    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        numberOfSpecies = model.getSpecies().size();
        tripSharer = new TripSharer(fisher);
//		addTerritories(model.getMap(), model.random, (int)numberOfTerritorySites);
    }

    @Override
    public void turnOff(final Fisher fisher) {
    }

    @Override
    public SeaTile chooseDestination(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState model,
        final Action currentAction
    ) {
        //if we have arrived
        if (fisher.getLocation().equals(chosenFishingSite)) {
            //and we are able to fish here, fish here
            if (fisher.canAndWantToFishHere()) {
                pickNewSite = true;
                return fisher.getLocation();
            }
            //otherwise go back home
            return fisher.getHomePort().getLocation();
        } else if (fisher.getLocation().equals(fisher.getHomePort().getLocation()) && pickNewSite) {
            //You are at port and you need a new site
            chosenFishingSite = pickNewSite(fisher, random, model, currentAction);
            pickNewSite = false;
            return chosenFishingSite;
        } else {
            //we haven't arrived
            //if we are going to port, keep going
            if (!fisher.isAtDestination() && fisher.isGoingToPort())
                return fisher.getHomePort().getLocation();

            //otherwise go/keep going to chosen fishing Site
            return chosenFishingSite;
        }
    }

    private SeaTile pickNewSite(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState model,
        final Action currentAction
    ) {

        SeaTile finalDestination = fisher.getHomePort().getLocation();

        //Start with all water tiles
        final List<SeaTile> allSeaTiles = model.getMap().getAllSeaTilesExcludingLandAsList();

        //if (true) return allSeaTiles.get(model.getRandom().nextInt(allSeaTiles.size()));

        final List<ViableDestination> viableDestinations = new ArrayList<>();
        final List<ViableDestination> unviableDestinations = new ArrayList<>();

        for (final SeaTile destination : allSeaTiles) {
            final ViableDestination viableDestination = new ViableDestination();
            viableDestination.destination = destination;
            viableDestination.expectedCatch = new double[numberOfSpecies];
            viableDestinations.add(viableDestination);
        }

        boolean noObservations = true;
        final List<ViableDestination> observedDestinations = new ArrayList<>();

        //Now go through the finished trips and calculate expected catch at all locations
        for (final TripRecord trip : fisher.getFinishedTrips()) {
            //Calculate the time scalar for this trip based on the time
            final int t = model.getDay() - trip.getTripDay();
            final double scalar = timeScalar(t);
            final SeaTile tripDestination = trip.getMostFishedTileInTrip();
            for (final ViableDestination viableDestination : viableDestinations) {
                if (viableDestination.destination.equals(tripDestination)) {
                    viableDestination.setObserved();
                    if (!observedDestinations.contains(viableDestination))
                        observedDestinations.add(viableDestination);
                    noObservations = false;
                    for (int i = 0; i < numberOfSpecies; i++) {
                        viableDestination.expectedCatch[i] += scalar * trip.getSoldCatch()[i];
                        viableDestination.scalarTotal += scalar;
                    }
                    break;
                }
            }
        }

        //Now go through trips shared by friends and add them to the mix
        final Collection<Fisher> myFriends = fisher.getSocialNetwork().getDirectedNeighbors(fisher);

        if (myFriends != null && !myFriends.isEmpty()) {
            for (final Fisher friend : myFriends) {
                final List<SharedTripRecord> friendSharedTrips = friend.getTripsSharedWith(fisher);
                if (!friendSharedTrips.isEmpty()) {
                    for (final SharedTripRecord friendSharedTrip : friendSharedTrips) {
                        final TripRecord trip = friendSharedTrip.getTrip();
                        final int t = model.getDay() - trip.getTripDay();
                        final double scalar = timeScalar(t);
                        final SeaTile tripDestination = trip.getMostFishedTileInTrip();
                        for (final ViableDestination viableDestination : viableDestinations) {
                            if (viableDestination.destination.equals(tripDestination)) {
                                viableDestination.setObserved();
                                if (!observedDestinations.contains(viableDestination))
                                    observedDestinations.add(viableDestination);
                                noObservations = false;
                                for (int i = 0; i < numberOfSpecies; i++) {
                                    viableDestination.expectedCatch[i] += scalar * trip.getSoldCatch()[i];
                                    viableDestination.scalarTotal += scalar;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        for (final ViableDestination viableDestination : viableDestinations) {
            for (int i = 0; i < numberOfSpecies; i++) {
                //if there have been no trips there, just zero out the expected catch
                viableDestination.expectedCatch[i] *= (viableDestination.scalarTotal > 0 ? (1.0 / viableDestination.scalarTotal) : 0);
            }
            //Now that we have the expected sold catch per species at all the viable
            //locations, we calculate the expected cost of fishing at
            //that location

            //figure out the expected price per species
            final Port homePort = fisher.getHomePort();
            final MarketMap marketMap = homePort.getMarketMap(fisher);

            for (int i = 0; i < numberOfSpecies; i++) {
                final double speciesPrice = marketMap.getSpeciesPrice(i);
                viableDestination.expectedProfit += viableDestination.expectedCatch[i] * speciesPrice;
            }
            //figure out the expected operational cost
            //figure out the expected trip cost
            final double expectedCost = estimateTripCost(viableDestination.destination, fisher, model);
            viableDestination.expectedProfit += -expectedCost;

        }

        //If there is no history, then set the expected profit of EVERY location to be equal so
        //they have a chance to fish anywhere
        if (noObservations) {
            for (final ViableDestination viableDestination : viableDestinations) {
                viableDestination.expectedProfit = this.minAbsoluteSatisfactoryProfit;

                //This will enforce that the fisher will give preference to territorial sites
                //in the absence of any other information
                if (fisher.isTerritory(viableDestination.destination)) viableDestination.expectedProfit *= 1.50;
            }
        } else {
            //Otherwise go through all viable locations with no observations we estimate them using inverse distance weighting
            //This is pretty flexible and can be tuned by the exponent
            for (final ViableDestination destination : viableDestinations) {
                if (!observedDestinations.contains(destination)) {
                    double sumScalars = 0.0;
                    destination.expectedProfit = 0.0;
                    for (final ViableDestination observedDestination : observedDestinations) {
                        final double distance = model.getMap()
                            .distance(observedDestination.destination, destination.destination);
                        if (distance > 0) {
                            final double scalar = 1.0 / Math.pow(distance, inverseDistanceExponent);
                            destination.expectedProfit += scalar * observedDestination.expectedProfit;
                            sumScalars += scalar;
                        } else { //If for some reason there is another SeaTile on top of an observed seatile...
                            destination.expectedProfit = observedDestination.expectedProfit;
                            sumScalars = 1.0;
                            break;
                        }
                    }
                    destination.expectedProfit *= 1.0 / sumScalars;
                    //This will enforce that the fisher will give preference to territorial sites
                    //in the absence of any observations
                    if (fisher.isTerritory(destination.destination)) destination.expectedProfit *= 1.50;
                }
            }
        }

        double minSocialSatisfactoryProfit = 0;
        if (myFriends != null) {
            final int nToBeat = (int) Math.floor(myFriends.size() * minRelativeSatisfactoryProfit);
            final double[] friendProfits = new double[myFriends.size()];
            if (nToBeat > 0) {
                int i = 0;
                for (final Fisher friend : myFriends) {
                    friendProfits[i] = getPublicProfit(friend);
                    i++;
                }
                for (i = 0; i < friendProfits.length - 1; i++) {
                    for (int j = i + 1; j < friendProfits.length; j++) {
                        if (friendProfits[j] < friendProfits[i]) {
                            final double tempProfit = friendProfits[j];
                            friendProfits[j] = friendProfits[i];
                            friendProfits[i] = tempProfit;
                        }
                    }
                }
                minSocialSatisfactoryProfit = friendProfits[nToBeat - 1];
            }
        }

        final double profitCutoff = Math.max(minAbsoluteSatisfactoryProfit, minSocialSatisfactoryProfit);

        //Remove any destinations that don't offer satisfactory profit
        for (final ViableDestination d : viableDestinations)
            if (d.expectedProfit < profitCutoff) unviableDestinations.add(d);
        viableDestinations.removeAll(unviableDestinations);
        unviableDestinations.clear();


        double highestProfit = 0.0;
        for (final ViableDestination viableDestination : viableDestinations) {
            highestProfit = Math.max(highestProfit, viableDestination.expectedProfit);
        }


        for (final ViableDestination viableDestination : viableDestinations) {
            //Now we scale the expected profit to be a number maxed out at 1
            viableDestination.attractiveness = weightProfit * viableDestination.expectedProfit / highestProfit -
                weightLaw * (fisher.isAllowedToFishHere(viableDestination.destination, model) ? 0 : 1) -
                weightCommunal * (fisher.isAllowedByCommunityStandardsToFishHere(
                    viableDestination.destination,
                    model
                ) ? 0 : 1) -
                weightReputation * (fisher.isAllowedReputationToFishHere(viableDestination.destination, model) ? 0 : 1);
        }
        //Remove any destinations with negative attractiveness
        for (final ViableDestination d : viableDestinations)
            if (d.attractiveness < 0) unviableDestinations.add(d);
        viableDestinations.removeAll(unviableDestinations);
        unviableDestinations.clear();


        //If the collection of viable destinations is now empty, then there simply isn't a good place to fish.
        //Return with a null and don't go exploring
        if (viableDestinations.isEmpty()) {
            //	System.out.println("No viable destinations");
            return fisher.getHomePort().getLocation();
        }


        //determine the most attractive site
        SeaTile mostAttractiveDestination = null;
        double bestAttraction = -10000000;
        double totalProfits = 0;
        for (final ViableDestination viableDestination : viableDestinations) {
            totalProfits += viableDestination.expectedProfit;
            if (viableDestination.attractiveness > bestAttraction) {
                mostAttractiveDestination = viableDestination.destination;
                bestAttraction = viableDestination.attractiveness;
                profitBest = viableDestination.expectedProfit;
                setPublicProfit(fisher, profitBest);
            }
        }
        if (mostAttractiveDestination == null) {
//        	System.out.println("Most attractive destination is null.");
            return (fisher.getHomePort().getLocation());
        }

        //See if they had a previous trip and we need to update 'n'
        //The number of explorations that have not paid off:
        if (needToUpdateN) {
            final double lastProfit = (fisher.getLastFinishedTrip() != null) ? fisher.getLastFinishedTrip()
                .getTotalTripProfit() : 0;
            if (thisTripWasExploration && lastProfit > profitBest) {
                //Exploration paid off, reset N
                nExplore = 1;
            } else {
                //Exploration was a bust (or exploited), increase N
                nExplore++;
            }
        }

        needToUpdateN = true;
//        System.out.println("Number of viable destinations: "+viableDestinations.size());
        final boolean goExploring = viableDestinations.size() > 1 && shouldIExplore(random);
        if (goExploring) {
            totalProfits -= profitBest;
            thisTripWasExploration = true;
            {
//            	double checkSum=0;
                for (final ViableDestination d : viableDestinations) {
                    if (d.destination == mostAttractiveDestination) {
                        d.probability = 0;
                    } else {
                        d.probability = d.expectedProfit / totalProfits;
//            			checkSum+=d.probability;
                    }
                }
//            	System.out.println("ProbSum ="+checkSum);
/*                	Iterator<ViableDestination> d = viableDestinations.iterator();
    	            while(d.hasNext()){
    	            	ViableDestination s=d.next();
    	            	if(s.destination == mostAttractiveDestination)
    	            		d.remove();
    	            	else
    	            		s.probability = s.expectedProfit/totalProfits;
    	            }*/
            }
            double randDouble = random.nextDouble();
            for (final ViableDestination viableDestination : viableDestinations) {
                randDouble -= viableDestination.probability;
                if (randDouble <= 0) {
                    finalDestination = viableDestination.destination;
//            		System.out.println("chose "+finalDestination.getGridX()+","+finalDestination.getGridY()+" with prob "+viableDestination.probability);
                    break;
                }
            }

        } else {
            thisTripWasExploration = false;
            finalDestination = mostAttractiveDestination;
        }
        return finalDestination;
    }

    double timeScalar(final double t) {
        return timeScalarFunction.timeScalar(t);
    }

    double estimateTripCost(final SeaTile destination, final Fisher fisher, final FishState model) {
        double estimatedCost = 0;

        final List<Cost> additionalCosts = fisher.getAdditionalTripCosts();
        final SeaTile homePort = fisher.getHomePort().getLocation();
        final double expectedFuel = fisher.getExpectedFuelConsumption(model.getMap().distance(destination, homePort));
        final double fuelPrice = fisher.getHomePort().getGasPricePerLiter();
        for (final Cost realCosts : additionalCosts) {
            //We want to account for the additional costs of the trip - but since it is the same for all destinations, perhaps it doesn't matter.
        }
        estimatedCost += fuelPrice * expectedFuel;
        return estimatedCost;
    }

    double getPublicProfit(final Fisher fisher) {
        double publicProfitValue = 0;
        if (publicProfit != null) {
            for (final PubliclySharedProfit publicFisherProfit : publicProfit) {
                if (publicFisherProfit.getFisher() == fisher) {
                    publicProfitValue = publicFisherProfit.getProfit();
                    break;
                }
            }
        }
        return publicProfitValue;
    }

    void setPublicProfit(final Fisher fisher, final double profit) {
        boolean inList = false;
        for (final PubliclySharedProfit publicFisherProfit : publicProfit) {
            if (publicFisherProfit.getFisher() == fisher) {
                publicFisherProfit.setProfit(profit);
                inList = true;
                break;
            }
        }
        if (!inList) {
            publicProfit.add(new PubliclySharedProfit(profit, fisher));
        }
    }

    boolean shouldIExplore(final MersenneTwisterFast random) {
//		System.out.println("n="+nExplore+", k="+kExplore+", Prob of explore: "+1/Math.pow(nExplore,kExplore));
        return (random.nextBoolean(1 / Math.pow(nExplore, kExplore)));
    }

    class TripSharer implements Steppable {
        private static final long serialVersionUID = 6634055835870014205L;
        Stoppable dailyShare;
        Fisher fisher;

        TripSharer(final Fisher fisher) {
            this.fisher = fisher;
        }

        void startSharing(final FishState model) {
            dailyShare = model.scheduleEveryDay(this, StepOrder.FISHER_PHASE);
        }

        public void step(final SimState simState) {
            //pick from among the 10 most profitable trips, or fewer if fewer trips have been logged
            final List<TripRecord> finishedTrips = fisher.getFinishedTrips();
            final int nChoices = Math.min(10, finishedTrips.size());
            if (nChoices > 0) {
                final TripRecord[] bestTrips = new TripRecord[nChoices];
                //Go through the trips, and put them into this array starting with index 0.
                //bump them down the list if there is a better one.
                for (final TripRecord finishedTrip : finishedTrips) {
                    for (int i = 0; i < nChoices; i++) {
                        if (bestTrips[i] == null) {
                            bestTrips[i] = finishedTrip;
                            break;
                        } else if (finishedTrip.getTotalTripProfit() > bestTrips[i].getTotalTripProfit()) {
                            //bump them down, starting with the end of the list going up
                            for (int j = nChoices - 1; j > i; j--) {
                                bestTrips[j] = bestTrips[j - 1];
                            }
                            bestTrips[i] = finishedTrip;
                            break;
                        }

                    }
                }
                //now pick one of these trips at random and share it with my social network:
                final MersenneTwisterFast random = new MersenneTwisterFast();
                fisher.shareTrip(bestTrips[random.nextInt(nChoices)], true, null);
            }
        }
    }

    class ViableDestination {
        boolean observed = false;
        double[] expectedCatch;
        double scalarTotal;
        double expectedProfit;
        SeaTile destination;
        double attractiveness;
        double probability; // used if exploring

        void setObserved() {
            this.observed = true;
        }
    }

    class PubliclySharedProfit {
        double profit;
        Fisher fisher;

        public PubliclySharedProfit(final double profit, final Fisher fisher) {
            this.profit = profit;
            this.fisher = fisher;
        }

        double getProfit() {
            return profit;
        }

        void setProfit(final double profit) {
            this.profit = profit;
        }

        Fisher getFisher() {
            return fisher;
        }

    }
}
