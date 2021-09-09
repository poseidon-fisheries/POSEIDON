package uk.ac.ox.oxfish.model.regs.policymakers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ISlope;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ITarget;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.Arrays;

/**
 * controller that calls a sensor to give it a number which it
 * then translates literally into a monoTAC
 */
public class TargetToTACController extends Controller {



    public static final Actuator<FishState, Double> POLICY_TO_ALLSPECIESTAC_ACTUATOR = new Actuator<FishState, Double>() {
        @Override
        public void apply(FishState subject, Double tac, FishState model) {
            if (!Double.isFinite(tac))
                return;

            final MonoQuotaRegulation quotaRegulation =
                    new MonoQuotaRegulation(
                            tac
                    );
            for (Fisher fisher : model.getFishers()) {
                fisher.setRegulation(quotaRegulation);
            }
        }
    };

    public static final Actuator<FishState, Double> POLICY_TO_ONESPECIES_TAC_ACTUATOR(String specificSpeciesRegulated) {
        return new Actuator<FishState, Double>() {
            @Override
            public void apply(FishState subject, Double tac, FishState model) {
                if (!Double.isFinite(tac))
                    return;

                System.out.println("Building quota for " +specificSpeciesRegulated);
                double[] realTac = new double[model.getSpecies().size()];
                Arrays.fill(realTac,Double.POSITIVE_INFINITY);
                final int importantSpecies = model.getSpecies(specificSpeciesRegulated).getIndex();
                realTac[importantSpecies] = tac;


                final MultiQuotaRegulation quotaRegulation =
                        new MultiQuotaRegulation(
                                realTac,model
                        );
                for (Fisher fisher : model.getFishers()) {
                    fisher.setRegulation(quotaRegulation);
                }
            }
        };
    }

    public TargetToTACController(
                                 Sensor<FishState, Double> target,
                                 int intervalInDays) {
        super(
                (Sensor<FishState, Double>) system -> -1d,
                target,
                POLICY_TO_ALLSPECIESTAC_ACTUATOR, intervalInDays);
    }

    public TargetToTACController(
            Sensor<FishState, Double> target,
            int intervalInDays,
            @NotNull
            String speciesAffectedByQuota) {
        super(
                (Sensor<FishState, Double>) system -> -1d,
                target,
                POLICY_TO_ONESPECIES_TAC_ACTUATOR(speciesAffectedByQuota),
                intervalInDays);
    }

    public TargetToTACController(
            ISlope islope
    ){
        this(islope,
                islope.getMaxTimeLag()*365);
    }


    public TargetToTACController(
            ISlope islope,
            String speciesAffectedByQuota
    ){
        this(islope,
                islope.getMaxTimeLag()*365,
                speciesAffectedByQuota);
    }

    public TargetToTACController(
            ITarget itarget
    ){
        this(itarget,
                itarget.getTimeInterval()*365);
    }

    public TargetToTACController(
            ITarget itarget,
            String speciesAffectedByQuota
    ){
        this(itarget,
                itarget.getTimeInterval()*365,
                speciesAffectedByQuota);
    }


    @Override
    public double computePolicy(double currentVariable,
                                double target,
                                FishState model,
                                double oldPolicy) {
        assert currentVariable==-1;

        System.out.println("target TAC is: " + target);
        return target;
    }

    @Override
    public void start(FishState model) {
        super.start(model);

        model.getYearlyDataSet().registerGatherer(

                "TAC from TARGET-TAC Controller",
                new Gatherer<FishState>() {
                    @Override
                    public Double apply(FishState fishState) {
                        return getPolicy();
                    }
                },
                Double.NaN);
    }
}
