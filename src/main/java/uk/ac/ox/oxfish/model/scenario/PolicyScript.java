package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

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

    private Integer changeInNumberOfFishers = null;

    public PolicyScript() {
    }


    public void apply(FishState state)
    {

        //apply regulations
        if(regulation != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setRegulation(regulation.apply(state));
            }
            //new fishers will follow these new rules
            state.getFisherFactory().setRegulations(regulation);
        }
        //apply gear
        if(gear != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setGear(gear.apply(state));
            }
            //new fishers will use the new gear
            state.getFisherFactory().setGear(gear);
        }

        if(departingStrategy != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setDepartingStrategy(departingStrategy.apply(state));
            }
            //new fishers will use the new gear
            state.getFisherFactory().setGear(gear);
        }

        if(destinationStrategy != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setDestinationStrategy(destinationStrategy.apply(state));
            }
            //new fishers will use the new gear
            state.getFisherFactory().setGear(gear);
        }

        if(fishingStrategy != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setFishingStrategy(fishingStrategy.apply(state));
            }
            //new fishers will use the new gear
            state.getFisherFactory().setGear(gear);
        }

        if(weatherStrategy != null) {
            for (Fisher fisher : state.getFishers()) {
                fisher.setWeatherStrategy(weatherStrategy.apply(state));
            }
            //new fishers will use the new gear
            state.getFisherFactory().setGear(gear);
        }



        //create new fishers if needed
        if(changeInNumberOfFishers != null) {
            if(changeInNumberOfFishers>0)
                for (int i = 0; i < changeInNumberOfFishers; i++)
                    state.createFisher();
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
}
