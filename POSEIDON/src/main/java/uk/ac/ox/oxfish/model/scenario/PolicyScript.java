/*
 *     POSEIDON, an agent-based model of fisheries
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

package uk.ac.ox.oxfish.model.scenario;

import com.google.common.base.Preconditions;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.function.Consumer;

/**
 * A bunch of factories to apply to a model, usually to change its policies or something similar
 * Created by carrknight on 5/3/16.
 */
public class PolicyScript
{

    private AlgorithmFactory<? extends Gear> gear;
    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy;


    private AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy;

    /**
     * this REPLACES all the other additional trip costs!
     */
    private DoubleParameter hourlyTravellingCosts;


    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy;
    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy;
    /**
     * factory to produce when agents do not want to deal with weather
     */
    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy;

    /**
     * factory to produce new regulations everybody ought to follow
     */
    private AlgorithmFactory<? extends Regulation> regulation;

    private Double gasPricePerLiter = null;

    private boolean removeAllMPAs = false;

    private Integer changeInNumberOfFishers = null;


    private String nameOfPopulation = FishState.DEFAULT_POPULATION_NAME;

    public PolicyScript() {
    }


    public void apply(FishState state)
    {

        System.out.println("Starting a polcy script!");
        //apply regulations
        if(regulation != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setRegulation(regulation.apply(state));
            }
            //new fishers will follow these new rules
            state.getFisherFactory(nameOfPopulation).setRegulations(regulation);
        }
        //apply gear
        if(gear != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setGear(gear.apply(state));
            }
            //new fishers will use the new gear
            state.getFisherFactory(nameOfPopulation).setGear(gear);
        }

        if(departingStrategy != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setDepartingStrategy(departingStrategy.apply(state));
            }
            //new fishers will use the new strategy
            state.getFisherFactory(nameOfPopulation).setDepartingStrategy(departingStrategy);
        }

        if(destinationStrategy != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setDestinationStrategy(destinationStrategy.apply(state));
            }
            //new fishers will use the new strategy
            state.getFisherFactory(nameOfPopulation).setDestinationStrategy(destinationStrategy);
        }

        if(discardingStrategy != null)
        {
            for (Fisher fisher : state.getFishers()) {
                fisher.setDiscardingStrategy(discardingStrategy.apply(state));
            }
            //new fishers will use the new strategy
            state.getFisherFactory(nameOfPopulation).setDiscardingStrategy(discardingStrategy);
        }

        if(hourlyTravellingCosts != null)
        {
            for (Fisher fisher : state.getFishers()) {
                Preconditions.checkArgument(fisher.getAdditionalTripCosts().size() <=1,
                                            "replacing more than one additional cost, this is probably not what you want");
                if(fisher.getAdditionalTripCosts().size()==1)
                    Log.warn("Replacing the previous additional trip cost object with the new one");

                fisher.getAdditionalTripCosts().clear();
                fisher.getAdditionalTripCosts().add(new HourlyCost(
                        hourlyTravellingCosts.apply(state.getRandom())
                ));
            }
            state.getFisherFactory(nameOfPopulation).getAdditionalSetups().add(
                    new Consumer<Fisher>() {
                        @Override
                        public void accept(Fisher fisher) {
                            fisher.getAdditionalTripCosts().clear();
                            fisher.getAdditionalTripCosts().add(new HourlyCost(
                                    hourlyTravellingCosts.apply(state.getRandom())
                            ));
                        }
                    }
            );
        }

        if(removeAllMPAs)
        {
            for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList()) {
                tile.assignMpa(null);
                assert !tile.isProtected();
            }
        }

        if(fishingStrategy != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setFishingStrategy(fishingStrategy.apply(state));
            }
            //new fishers will use the new strategy
            state.getFisherFactory(nameOfPopulation).setFishingStrategy(fishingStrategy);
        }

        if(weatherStrategy != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setWeatherStrategy(weatherStrategy.apply(state));
            }
            //new fishers will use the new strategy
            state.getFisherFactory(nameOfPopulation).setWeatherStrategy(weatherStrategy);
        }



        //create new fishers if needed
        if(changeInNumberOfFishers != null) {
            if(changeInNumberOfFishers>0)
                for (int i = 0; i < changeInNumberOfFishers; i++)
                    state.createFisher(nameOfPopulation);
            else
            {
                for (int i = 0; i < -changeInNumberOfFishers; i++)
                    state.killRandomFisher();
            }
        }

        if(gasPricePerLiter!=null)
        {
            for(Port port : state.getPorts())
                port.setGasPricePerLiter(gasPricePerLiter);
        }
    }

    public AlgorithmFactory<? extends Regulation> getRegulation() {
        return regulation;
    }

    public void setRegulation(
            AlgorithmFactory<? extends Regulation> regulation) {
        this.regulation = regulation;
    }

    public AlgorithmFactory<? extends Gear> getGear() {
        return gear;
    }

    public void setGear(AlgorithmFactory<? extends Gear> gear) {
        this.gear = gear;
    }

    public Integer getChangeInNumberOfFishers() {
        return changeInNumberOfFishers;
    }

    public void setChangeInNumberOfFishers(Integer changeInNumberOfFishers) {
        this.changeInNumberOfFishers = changeInNumberOfFishers;
    }


    /**
     * Getter for property 'departingStrategy'.
     *
     * @return Value for property 'departingStrategy'.
     */
    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return departingStrategy;
    }

    /**
     * Setter for property 'departingStrategy'.
     *
     * @param departingStrategy Value to set for property 'departingStrategy'.
     */
    public void setDepartingStrategy(
            AlgorithmFactory<? extends DepartingStrategy> departingStrategy) {
        this.departingStrategy = departingStrategy;
    }

    /**
     * Getter for property 'destinationStrategy'.
     *
     * @return Value for property 'destinationStrategy'.
     */
    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategy() {
        return destinationStrategy;
    }

    /**
     * Setter for property 'destinationStrategy'.
     *
     * @param destinationStrategy Value to set for property 'destinationStrategy'.
     */
    public void setDestinationStrategy(
            AlgorithmFactory<? extends DestinationStrategy> destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    /**
     * Getter for property 'fishingStrategy'.
     *
     * @return Value for property 'fishingStrategy'.
     */
    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategy() {
        return fishingStrategy;
    }

    /**
     * Setter for property 'fishingStrategy'.
     *
     * @param fishingStrategy Value to set for property 'fishingStrategy'.
     */
    public void setFishingStrategy(
            AlgorithmFactory<? extends FishingStrategy> fishingStrategy) {
        this.fishingStrategy = fishingStrategy;
    }

    /**
     * Getter for property 'weatherStrategy'.
     *
     * @return Value for property 'weatherStrategy'.
     */
    public AlgorithmFactory<? extends WeatherEmergencyStrategy> getWeatherStrategy() {
        return weatherStrategy;
    }

    /**
     * Setter for property 'weatherStrategy'.
     *
     * @param weatherStrategy Value to set for property 'weatherStrategy'.
     */
    public void setWeatherStrategy(
            AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy) {
        this.weatherStrategy = weatherStrategy;
    }

    /**
     * Getter for property 'gasPricePerLiter'.
     *
     * @return Value for property 'gasPricePerLiter'.
     */
    public Double getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    /**
     * Setter for property 'gasPricePerLiter'.
     *
     * @param gasPricePerLiter Value to set for property 'gasPricePerLiter'.
     */
    public void setGasPricePerLiter(Double gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }

    public AlgorithmFactory<? extends DiscardingStrategy> getDiscardingStrategy() {
        return discardingStrategy;
    }

    public void setDiscardingStrategy(
            AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy) {
        this.discardingStrategy = discardingStrategy;
    }

    /**
     * Getter for property 'hourlyTravellingCosts'.
     *
     * @return Value for property 'hourlyTravellingCosts'.
     */
    public DoubleParameter getHourlyTravellingCosts() {
        return hourlyTravellingCosts;
    }

    /**
     * Setter for property 'hourlyTravellingCosts'.
     *
     * @param hourlyTravellingCosts Value to set for property 'hourlyTravellingCosts'.
     */
    public void setHourlyTravellingCosts(DoubleParameter hourlyTravellingCosts) {
        this.hourlyTravellingCosts = hourlyTravellingCosts;
    }

    /**
     * Getter for property 'removeAllMPAs'.
     *
     * @return Value for property 'removeAllMPAs'.
     */
    public boolean isRemoveAllMPAs() {
        return removeAllMPAs;
    }

    /**
     * Setter for property 'removeAllMPAs'.
     *
     * @param removeAllMPAs Value to set for property 'removeAllMPAs'.
     */
    public void setRemoveAllMPAs(boolean removeAllMPAs) {
        this.removeAllMPAs = removeAllMPAs;
    }

    /**
     * Getter for property 'nameOfPopulation'.
     *
     * @return Value for property 'nameOfPopulation'.
     */
    public String getNameOfPopulation() {
        return nameOfPopulation;
    }

    /**
     * Setter for property 'nameOfPopulation'.
     *
     * @param nameOfPopulation Value to set for property 'nameOfPopulation'.
     */
    public void setNameOfPopulation(String nameOfPopulation) {
        this.nameOfPopulation = nameOfPopulation;
    }
}
