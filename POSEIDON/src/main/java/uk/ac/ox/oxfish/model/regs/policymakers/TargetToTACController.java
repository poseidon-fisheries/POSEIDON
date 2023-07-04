package uk.ac.ox.oxfish.model.regs.policymakers;

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


    public static final Actuator<FishState, Double> POLICY_TO_ALLSPECIESTAC_ACTUATOR = (subject, tac, model) -> {
        if (!Double.isFinite(tac))
            return;

        final MonoQuotaRegulation quotaRegulation =
            new MonoQuotaRegulation(
                tac
            );
        for (final Fisher fisher : model.getFishers()) {
            fisher.setRegulation(quotaRegulation);
        }
    };
    private static final long serialVersionUID = 4033938011419507367L;

    public TargetToTACController(
        final ISlope islope
    ) {
        this(
            islope,
            islope.getMaxTimeLag() * 365
        );
    }

    public TargetToTACController(
        final Sensor<FishState, Double> target,
        final int intervalInDays
    ) {
        super(
            (Sensor<FishState, Double>) system -> -1d,
            target,
            POLICY_TO_ALLSPECIESTAC_ACTUATOR, intervalInDays
        );
    }

    public TargetToTACController(
        final ISlope islope,
        final String speciesAffectedByQuota
    ) {
        this(
            islope,
            islope.getMaxTimeLag() * 365,
            speciesAffectedByQuota
        );
    }

    public TargetToTACController(
        final Sensor<FishState, Double> target,
        final int intervalInDays,
        final String speciesAffectedByQuota
    ) {
        super(
            (Sensor<FishState, Double>) system -> -1d,
            target,
            POLICY_TO_ONESPECIES_TAC_ACTUATOR(speciesAffectedByQuota),
            intervalInDays
        );
    }

    public static final Actuator<FishState, Double> POLICY_TO_ONESPECIES_TAC_ACTUATOR(final String specificSpeciesRegulated) {
        return (subject, tac, model) -> {
            if (!Double.isFinite(tac))
                return;

            System.out.println("Building quota for " + specificSpeciesRegulated);
            final double[] realTac = new double[model.getSpecies().size()];
            Arrays.fill(realTac, Double.POSITIVE_INFINITY);
            final int importantSpecies = model.getSpecies(specificSpeciesRegulated).getIndex();
            realTac[importantSpecies] = tac;


            final MultiQuotaRegulation quotaRegulation =
                new MultiQuotaRegulation(
                    realTac, model
                );
            for (final Fisher fisher : model.getFishers()) {
                fisher.setRegulation(quotaRegulation);
            }
        };
    }

    public TargetToTACController(
        final ITarget itarget
    ) {
        this(
            itarget,
            itarget.getTimeInterval() * 365
        );
    }

    public TargetToTACController(
        final ITarget itarget,
        final String speciesAffectedByQuota
    ) {
        this(
            itarget,
            itarget.getTimeInterval() * 365,
            speciesAffectedByQuota
        );
    }


    @Override
    public double computePolicy(
        final double currentVariable,
        final double target,
        final FishState model,
        final double oldPolicy
    ) {
        assert currentVariable == -1;

        System.out.println("target TAC is: " + target);
        return target;
    }

    @Override
    public void start(final FishState model) {
        super.start(model);

        model.getYearlyDataSet().registerGatherer(

            "TAC from TARGET-TAC Controller",
            (Gatherer<FishState>) fishState -> getPolicy(),
            Double.NaN
        );
    }
}
