package uk.ac.ox.oxfish.model.regs;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.HashMap;
import java.util.Map;

/**
 * MPAs with a fine associated for each hour you spend in it
 * Created by carrknight on 2/13/17.
 */
public class FinedProtectedAreas extends ProtectedAreasOnly{



    private final MersenneTwisterFast random;

    private Map<MasonGeometry, Enforcement> enforcements = new HashMap<>();


    public FinedProtectedAreas(MersenneTwisterFast random) {
        this.random = random;
    }

    /**
     * no reaction
     *
     * @param where
     * @param who
     * @param fishCaught
     * @param hoursSpentFishing
     */
    @Override
    public void reactToFishing(
            SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing) {

        if(!where.isProtected())
            return;

        super.reactToFishing(where, who, fishCaught, hoursSpentFishing);
        assert who.isCheater(); //you must be willing to cheat to even be here!


        Enforcement enforcement = enforcements.get(where.grabMPA());
        Preconditions.checkState(enforcement!= null, "not a registered MPA!");

        for (int i = 0; i < hoursSpentFishing; i++)
            if(random.nextBoolean(enforcement.getHourlyProbabilityOfBeingCaught()))
                who.spendForTrip(enforcement.getFine());
    }


    public void registerMPA(MasonGeometry mpa, double hourlyProbabilityOfBeingCaught, double fine)
    {
        Preconditions.checkArgument(!enforcements.containsKey(mpa));
        enforcements.put(mpa, new Enforcement(hourlyProbabilityOfBeingCaught,fine));
    }

    private static class Enforcement{

        final private double hourlyProbabilityOfBeingCaught;

        final private double fine;


        public Enforcement(double hourlyProbabilityOfBeingCaught, double fine) {
            this.hourlyProbabilityOfBeingCaught = hourlyProbabilityOfBeingCaught;
            this.fine = fine;
        }


        public double getHourlyProbabilityOfBeingCaught() {
            return hourlyProbabilityOfBeingCaught;
        }

        public double getFine() {
            return fine;
        }
    }
}
