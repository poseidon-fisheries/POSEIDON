/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ISlope;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

public class IslopeToTacController extends Controller {
    private static final long serialVersionUID = 7992488101173826992L;

    public IslopeToTacController(
        final ISlope islope
    ) {
        super(
            //this is a simplified controller where target to policy requires no adjustment
            (Sensor<FishState, Double>) system -> -1d,
            islope,
            //actuator just take the quota and sets it as a single TAC
            //for everybody!
            (subject, tac, model) -> {
                if (!Double.isFinite(tac))
                    return;

                final MonoQuotaRegulation quotaRegulation =
                    new MonoQuotaRegulation(
                        tac
                    );
                for (final Fisher fisher : model.getFishers()) {
                    fisher.setRegulation(quotaRegulation);
                }
            },
            islope.getMaxTimeLag() * 365
        );
    }


    @Override
    public void start(final FishState model) {
        super.start(model);

        model.getYearlyDataSet().registerGatherer(
            "TAC from ISLOPE-TAC Controller",
            (Gatherer<FishState>) fishState -> getPolicy(),
            Double.NaN
        );
    }

    @Override
    public double computePolicy(final double currentVariable, final double target, final FishState model, final double oldPolicy) {
        assert currentVariable == -1;

        System.out.println("target TAC is: " + target);
        return target;
    }
//
//    public static void  main(String[] args){
//        PrototypeScenario scenario = new PrototypeScenario();
//        scenario.setFishers(200);
//        FishState state = new FishState(System.currentTimeMillis());
//        scenario.getPlugins().add(
//                new AlgorithmFactory<AdditionalStartable>() {
//                    @Override
//                    public AdditionalStartable apply(FishState fishState) {
//                        return new AdditionalStartable() {
//                            @Override
//                            public void start(FishState model) {
//                                fishState.scheduleOnceInXDays(
//                                        new Steppable() {
//                                            @Override
//                                            public void step(SimState simState) {
//                                                IslopeToTacController controller = new IslopeToTacController(
//                                                        new ISlope(
//                                                                "Species 0 Landings",
//                                                                "Species 0 CPUE",
//                                                                0.4,
//                                                                0.8,
//                                                                5
//                                                        )
//                                                );
//                                                controller.start(state);
//                                                controller.step(state);
//                                            }
//                                        },
//                                        StepOrder.DAWN,
//                                        365*10
//                                );
//                            }
//                        };
//
//                    }
//                }
//        );
//        state.setScenario(scenario);
//        FishGUI gui = new FishGUI(state);
//        Console c = new Console(gui);
//        c.setVisible(true);
//
//    }
}
