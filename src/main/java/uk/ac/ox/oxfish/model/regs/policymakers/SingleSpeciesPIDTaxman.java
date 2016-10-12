package uk.ac.ox.oxfish.model.regs.policymakers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * PID-driven taxman
 * Created by carrknight on 10/11/16.
 */
public class SingleSpeciesPIDTaxman implements Startable
{


    final private Species species;

    private double taxPreviouslyImposed;

    final private PIDController pid;


    public SingleSpeciesPIDTaxman(Species species,
                                  Sensor<FishState, Double> observed,
                                  Sensor<FishState, Double> target,
                                  int interval,
                                  double p, double i, double d) {
        this.species = species;
        this.pid = new PIDController(
                observed, target,
                //for each port and each market set the price by adding new tax and taking away the old one
                //this hopefully will mean that changes to price from other sources aren't lost
                new Actuator<FishState, Double>() {
                    @Override
                    public void apply(FishState subject, Double policy, FishState model) {
                        for (Port port : model.getPorts()) {
                            FixedPriceMarket market = (FixedPriceMarket) port.getDefaultMarketMap().getMarket(species);
                            market.setPrice(market.getPrice() - policy + taxPreviouslyImposed);
                        }
                        taxPreviouslyImposed = policy;
                    }
                },
                interval,
                p,i,d,0);

    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        pid.start(model);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        pid.turnOff();
    }


    /**
     * Getter for property 'taxPreviouslyImposed'.
     *
     * @return Value for property 'taxPreviouslyImposed'.
     */
    public double getTaxPreviouslyImposed() {
        return taxPreviouslyImposed;
    }

    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public Species getSpecies() {
        return species;
    }
}
