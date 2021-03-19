package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.*;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * this is basically the IT5/IT10 controller form the DLM toolkit, using
 * the target formula from ITarget for reference
 */
public class IndexTargetController extends Controller{



    private double lastPolicy = Double.NaN;

    private final double maxPercentageChange;

    private final static double MINIMUM_POLICY_ALLOWED = 0;

    /**
     * lower is better, which means that the ratio is not I/ITarget but ITarget/I
     */
    private final boolean inverse;


    /**
     * is the ratio capped at 1? true means not capped
     */
    private final boolean canGoAboveZero;


    public IndexTargetController(Sensor<FishState, Double> observed,
                                 Sensor<FishState, Double> target,
                                 Actuator<FishState, Double> actuator,
                                 int intervalInDays,
                                 double maxPercentageChange, boolean inverse, boolean canGoAboveZero) {
        super(observed, target, actuator, intervalInDays);
        this.maxPercentageChange = maxPercentageChange;
        this.inverse = inverse;
        this.canGoAboveZero = canGoAboveZero;
    }

    public static final Actuator<FishState, Double> RATIO_TO_SEASONAL_CLOSURE = new Actuator<FishState, Double>() {
        @Override
        public void apply(FishState subject, Double effortRatio, FishState model) {
            if (!Double.isFinite(effortRatio))
                return;

            assert  effortRatio<=1 : "i assume it's never above 1!";
            assert  effortRatio>=0 : "i assume it's always positive!";

            System.out.println("season length " + (int)(365*effortRatio));


            final FishingSeason season =
                    new FishingSeason(true,

                            (int)(365*effortRatio));

            for (Fisher fisher : model.getFishers()) {
                fisher.setRegulation(season);
            }
        }
    };



    public static final Actuator<FishState, Double> RATIO_TO_PERSONAL_SEASONAL_CLOSURE = new Actuator<FishState, Double>() {
        @Override
        public void apply(FishState subject, Double effortRatio, FishState model) {
            if (!Double.isFinite(effortRatio))
                return;

            assert  effortRatio<=1 : "i assume it's never above 1!";
            assert  effortRatio>=0 : "i assume it's always positive!";

            System.out.println("season length " + (int)(365*effortRatio));


            final MaxHoursOutRegulation season =
                    new MaxHoursOutRegulation(new ProtectedAreasOnly(),

                            (int)(365*24*effortRatio));

            for (Fisher fisher : model.getFishers()) {
                fisher.setRegulation(season);
            }
        }
    };




    //public static final Actuator<FishState, Double> RATIO_TO_CATCHABILITY(final double originalCatchability)

    public static final Actuator<FishState, Double> RATIO_TO_TAC(Sensor<FishState,Double> lastCatchesSensor,
                                                                 double minimumTAC,
                                                                 double maxPercentageChange) {
        return new Actuator<FishState, Double>() {
            public void apply(FishState subject, Double effortRatio, FishState model) {
                if (!Double.isFinite(effortRatio))
                    return;

                assert effortRatio >= 0 : "i assume it's always positive!";

                //with TAC IT we need to cap TAC changes rather than the multiplier
                if(effortRatio< 1d - maxPercentageChange)
                    effortRatio = 1d - maxPercentageChange;
                else
                if(effortRatio > 1d + maxPercentageChange)
                    effortRatio = 1d + maxPercentageChange;

                final Double lastCatches =
                        Math.max(lastCatchesSensor.scan(subject),
                                minimumTAC);
                System.out.println("lastCatches " + lastCatches);
                System.out.println("effortratio " + effortRatio);

                final double tac = Math.max(
                        effortRatio * lastCatches,
                        minimumTAC);

                System.out.println("tac " + tac);


                final MonoQuotaRegulation season =
                        new MonoQuotaRegulation(tac);

                for (Fisher fisher : model.getFishers()) {
                    fisher.setRegulation(season);
                }
            }
        };
    }


    public static final Actuator<FishState, Double> RATIO_TO_FLEET_SIZE =
            new Actuator<FishState, Double>() {
        @Override
        public void apply(FishState subject, Double effortRatio, FishState model) {
            if (!Double.isFinite(effortRatio))
                return;

            assert  effortRatio<=1 : "i assume it's never above 1!";
            assert  effortRatio>=0 : "i assume it's always positive!";

            System.out.println("fleet size " + effortRatio);


            for (Fisher fisher : model.getFishers()) {
                if(model.getRandom().nextDouble()>effortRatio)
                    fisher.setRegulation(new FishingSeason(true,0));
                else
                    fisher.setRegulation(new FishingSeason(true,365));

            }
        }
    };

    public static final Actuator<FishState, Double> RATIO_TO_CATCHABILITY(
            final double originalCatchability){
        return new Actuator<FishState, Double>() {
            @Override
            public void apply(FishState subject, Double effortRatio, FishState model) {
                if (!Double.isFinite(effortRatio))
                    return;

                assert  effortRatio<=1 : "i assume it's never above 1!";
                assert  effortRatio>=0 : "i assume it's always positive!";

                System.out.println("trip length " + originalCatchability * effortRatio);

                final RandomCatchabilityTrawl newGearToUse = new RandomCatchabilityTrawl(new double[]{originalCatchability * effortRatio},
                        new double[]{0},
                        5);

                for (Fisher fisher : model.getFishers()) {

                    fisher.setGear(
                            newGearToUse
                    );


                }
            }
        };
    }

    public static final Actuator<FishState, Double> RATIO_TO_DAYSATSEA(final double originalDaysAtSea){
        return new Actuator<FishState, Double>() {
            @Override
            public void apply(FishState subject, Double effortRatio, FishState model) {
                if (!Double.isFinite(effortRatio))
                    return;

                assert  effortRatio<=1 : "i assume it's never above 1!";
                assert  effortRatio>=0 : "i assume it's always positive!";

                System.out.println("trip length " + originalDaysAtSea * 24 * effortRatio);
                final MaximumTripLengthRegulation newGearToUse =
                        new MaximumTripLengthRegulation(
                                originalDaysAtSea * 24 * effortRatio
                        );
                for (Fisher fisher : model.getFishers()) {
                    fisher.setRegulation(newGearToUse);
                }
            }
        };
    }

    public static final Actuator<FishState, Double> RATIO_TO_DAYSATSEA =
            new Actuator<FishState, Double>() {
            @Override
            public void apply(FishState subject, Double effortRatio, FishState model) {
                if (!Double.isFinite(effortRatio))
                    return;

                assert  effortRatio<=1 : "i assume it's never above 1!";
                assert  effortRatio>=0 : "i assume it's always positive!";


                for (Fisher fisher : model.getFishers()) {
                    Preconditions.checkArgument(fisher.getFishingStrategy() instanceof MaximumDaysDecorator,
                            "Fisher doesn't have a max days at sea already set up; use other actuator");
                    final MaximumDaysDecorator strategy = (MaximumDaysDecorator) fisher.getFishingStrategy();
                    final MaximumTripLengthRegulation newGearToUse =
                            new MaximumTripLengthRegulation(
                                    strategy.getDaysBeforeGoingHome() * 24 * effortRatio
                            );
                    fisher.setRegulation(newGearToUse);
                }
            }
        };



    @Override
    public double computePolicy(double currentVariable, double target, FishState model, double oldPolicy) {

        if(Double.isNaN(lastPolicy))
            lastPolicy=1d;

        System.out.println("target " + target);
        System.out.println("currentVariable " + currentVariable);

        //new ratio
        double newRatio = inverse ? target/currentVariable : currentVariable / target;


        if(!Double.isFinite(newRatio))
            return lastPolicy;

        //keep it between 0 and 1
        if(newRatio<MINIMUM_POLICY_ALLOWED)
            newRatio=MINIMUM_POLICY_ALLOWED;
        if(newRatio>1 && !canGoAboveZero)
            newRatio=1;

        //do not let it go past the maximum percentage change
        if(newRatio> lastPolicy * (1+maxPercentageChange))
            newRatio = lastPolicy * (1+maxPercentageChange);
        if(newRatio< lastPolicy * (1-maxPercentageChange))
            newRatio = lastPolicy * (1-maxPercentageChange);

        lastPolicy = newRatio;
        return lastPolicy;

    }


    public double getLastPolicy() {
        return lastPolicy;
    }

    public void setLastPolicy(double lastPolicy) {
        this.lastPolicy = lastPolicy;
    }
}
