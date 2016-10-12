package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.policymakers.SingleSpeciesPIDTaxman;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.WeakHashMap;

/**
 * monthly check of taxation, targeting landings. The P and I values are reversed since we are imposing a tax (decreasing the price)
 * Created by carrknight on 10/11/16.
 */
public class SingleSpeciesPIDTaxationOnLandingsFactory implements AlgorithmFactory<ProtectedAreasOnly> {


    private int speciesIndex = 0;

    //how often does it act in # of days!
    private DoubleParameter actionInterval = new FixedDoubleParameter(7);

    private DoubleParameter landingTarget = new FixedDoubleParameter(5000);


    private DoubleParameter p = new FixedDoubleParameter(.05);

    private DoubleParameter i = new FixedDoubleParameter(.1);


    private final WeakHashMap<FishState,SingleSpeciesPIDTaxman> taxes = new WeakHashMap<>();


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ProtectedAreasOnly apply(FishState fishState) {
        ProtectedAreasOnly regulations = new ProtectedAreasOnly();

        if(!taxes.containsKey(fishState)) {
            double target = landingTarget.apply(fishState.getRandom());

            Species species = fishState.getSpecies().get(speciesIndex);
            int days = actionInterval.apply(fishState.getRandom()).intValue();
            //creates the pid taxman, targeting landings
            SingleSpeciesPIDTaxman pid = new SingleSpeciesPIDTaxman(
                    species,
                    new Sensor<FishState, Double>() {
                        @Override
                        public Double scan(FishState fisher) {
                            DataColumn landings = fisher.getDailyDataSet().getColumn(
                                    species + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                            double totalLandings = 0;
                            int toCycle = Math.min(days, landings.size());
                            for (int i = 0; i < toCycle; i++)
                                totalLandings += landings.getDatumXDaysAgo(i);

                            return totalLandings;
                        }
                    },
                    new Sensor<FishState, Double>() {
                        @Override
                        public Double scan(FishState system) {
                            return target;
                        }
                    },
                    days,
                    -p.apply(fishState.getRandom()),
                    -i.apply(fishState.getRandom()),
                    0d
            );

            fishState.registerStartable(pid);
            taxes.put(fishState,pid);
        }

        return regulations;

    }



    /**
     * Getter for property 'speciesIndex'.
     *
     * @return Value for property 'speciesIndex'.
     */
    public int getSpeciesIndex() {
        return speciesIndex;
    }

    /**
     * Setter for property 'speciesIndex'.
     *
     * @param speciesIndex Value to set for property 'speciesIndex'.
     */
    public void setSpeciesIndex(int speciesIndex) {
        this.speciesIndex = speciesIndex;
    }

    /**
     * Getter for property 'landingTarget'.
     *
     * @return Value for property 'landingTarget'.
     */
    public DoubleParameter getLandingTarget() {
        return landingTarget;
    }

    /**
     * Setter for property 'landingTarget'.
     *
     * @param landingTarget Value to set for property 'landingTarget'.
     */
    public void setLandingTarget(DoubleParameter landingTarget) {
        this.landingTarget = landingTarget;
    }

    /**
     * Getter for property 'p'.
     *
     * @return Value for property 'p'.
     */
    public DoubleParameter getP() {
        return p;
    }

    /**
     * Setter for property 'p'.
     *
     * @param p Value to set for property 'p'.
     */
    public void setP(DoubleParameter p) {
        this.p = p;
    }

    /**
     * Getter for property 'i'.
     *
     * @return Value for property 'i'.
     */
    public DoubleParameter getI() {
        return i;
    }

    /**
     * Setter for property 'i'.
     *
     * @param i Value to set for property 'i'.
     */
    public void setI(DoubleParameter i) {
        this.i = i;
    }

    /**
     * Getter for property 'actionInterval'.
     *
     * @return Value for property 'actionInterval'.
     */
    public DoubleParameter getActionInterval() {
        return actionInterval;
    }

    /**
     * Setter for property 'actionInterval'.
     *
     * @param actionInterval Value to set for property 'actionInterval'.
     */
    public void setActionInterval(DoubleParameter actionInterval) {
        this.actionInterval = actionInterval;
    }
}
