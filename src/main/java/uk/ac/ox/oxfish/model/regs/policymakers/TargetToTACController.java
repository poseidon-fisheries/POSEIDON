package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * controller that calls a sensor to give it a number which it
 * then translates literally into a monoTAC
 */
public class TargetToTACController extends Controller {
    public TargetToTACController(
                                 Sensor<FishState, Double> target,
                                 int intervalInDays) {
        super(
                (Sensor<FishState, Double>) system -> -1d,
                target,
                new Actuator<FishState, Double>() {
                    @Override
                    public void apply(FishState subject, Double tac, FishState model) {
                        if(!Double.isFinite(tac))
                            return;

                        final MonoQuotaRegulation quotaRegulation =
                                new MonoQuotaRegulation(
                                tac
                        );
                        for (Fisher fisher : model.getFishers()) {
                            fisher.setRegulation(quotaRegulation);
                        }
                    }
                } , intervalInDays);
    }



    public TargetToTACController(
            ISlope islope
    ){
        this(islope,
                islope.getMaxTimeLag()*365);
    }


    public TargetToTACController(
            ITarget itarget
    ){
        this(itarget,
                itarget.getTimeInterval()*365);
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
