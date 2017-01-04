package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.behavior.functionapproximation.dense.NormalizedVariableFeatures;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.vardomain.VariableDomain;
import com.esotericsoftware.minlog.Log;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.ac.ox.oxfish.experiments.burlapspike.BurlapShodan.episodesToCSV;

/**
 * Created by carrknight on 12/25/16.
 */
public class BurlapQuota {


    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {


        Log.set(Log.LEVEL_INFO);

        Path containerPath = Paths.get("runs", "burlap_quota");
        containerPath.toFile().mkdirs();
        containerPath.resolve("data").toFile().mkdirs();
        containerPath.resolve("results").toFile().mkdirs();
        containerPath.resolve("saves").toFile().mkdirs();

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(300);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setPortPositionX(40);
        scenario.setPortPositionY(25);
        scenario.setMapInitializer(mapInitializer);



        //imagine a 10% yearly interest rate this means that yearly the discount rate is:
        // 1/(1+.1/12)
        //which is about 99%
        NormalizedVariableFeatures features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.BIOMASS, new VariableDomain(0, 10500000)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 365)).
                        variableDomain(ShodanStateOil.MONTHS_LEFT, new VariableDomain(0, 243));


        /*
        BurlapShodan.sarsaRunNormalized(.99,
                                        "99_sarsa_biomass2",
                                        1,
                                        .01,
                                        .3,
                                        features,
                                        scenario,
                                        containerPath, (Steppable) simState -> {},
                                        ShodanStateOil.BIOMASS,
                                        ShodanStateOil.MONTHS_LEFT);
*/

        /*
        BurlapShodan.sarsaRunNormalized(.99,
                                        "99_sarsa_biomass_highlambda3",
                                        1,
                                        .01,
                                        .85,
                                        features,
                                        scenario,
                                        containerPath, (Steppable) simState -> {},
                                        ShodanStateOil.BIOMASS,
                                        ShodanStateOil.DAY_OF_THE_YEAR,
                                        ShodanStateOil.MONTHS_LEFT);
*/

        //try 90% just to see the difference
        /*
        BurlapShodan.sarsaRunNormalized(.90,
                                        "90_sarsa_biomass2",
                                        1,
                                        .01,
                                        .3,
                                        features,
                                         scenario,
                                        containerPath, (Steppable) simState -> {},
                                        ShodanStateOil.BIOMASS,
                                        ShodanStateOil.MONTHS_LEFT);
*/





        BurlapShodan.sarsaRunFourier(.99,
                                        "99_sarsa_biomass_6lambda_fourier3",
                                        4,
                                        .005,
                                        .6,
                                        features,
                                         scenario,
                                        containerPath, (Steppable) simState -> {},
                                        ShodanStateOil.BIOMASS,
                                        ShodanStateOil.DAY_OF_THE_YEAR,
                                        ShodanStateOil.MONTHS_LEFT);






/*
        BurlapShodan.sarsaRunFourier(.99,
                                     "99_sarsa_biomass_6lambda_fourier3",
                                     4,
                                     .0001,
                                     .6,
                                     features,
                                     scenario,
                                     containerPath, (Steppable) simState -> {},
                                     ShodanStateOil.BIOMASS,
                                     ShodanStateOil.DAY_OF_THE_YEAR,
                                     ShodanStateOil.MONTHS_LEFT);

 */

/*
        BurlapShodan.sarsaRunFourier(1,
                                     "100_sarsa_biomass_6lambda_fourier2",
                                     4,
                                     .005,
                                     .6,
                                     features,
                                     scenario,
                                     containerPath, (Steppable) simState -> {},
                                     ShodanStateOil.BIOMASS,
                                     ShodanStateOil.DAY_OF_THE_YEAR,
                                     ShodanStateOil.MONTHS_LEFT);
*/
/*

        BurlapShodan.qRunFourier(1,
                                 "100_qrun_biomass_fourier4",
                                 4,
                                 .005,
                                 1000,
                                 5,
                                 scenario,
                                 containerPath,features,
                                 (Steppable) simState -> {},
                                 ShodanStateOil.BIOMASS,
                                 ShodanStateOil.DAY_OF_THE_YEAR,
                                 ShodanStateOil.MONTHS_LEFT);
*/


/*
        BurlapShodan.polynomialRun(.99,
                                   "99_lspi_poly3",
                                   5,
                                   scenario,
                                   containerPath,
                                   (Steppable) simState -> {},
                                   ShodanStateOil.BIOMASS,
                                   ShodanStateOil.DAY_OF_THE_YEAR,
                                   ShodanStateOil.MONTHS_LEFT);

*/

/*
        BurlapShodan.lspiPolynomialRunNormalized(.99,
                                                 "99_lspi_poly3_normalized",
                                                 3,
                                                 scenario,
                                                 containerPath,
                                                 (Steppable) simState -> {},
                                                 features,
                                                 ShodanStateOil.BIOMASS,
                                                 ShodanStateOil.DAY_OF_THE_YEAR,
                                                 ShodanStateOil.MONTHS_LEFT);
                                                 */

/*
                    BurlapShodan.lspiFourierRun(.99,
                                    "99_lspi_fourier2",
                                    2,
                                    features,
                                    scenario,
                                    containerPath,
                                    (Steppable) simState -> {},
                                    ShodanStateOil.BIOMASS,
                                    ShodanStateOil.DAY_OF_THE_YEAR,
                                    ShodanStateOil.MONTHS_LEFT);
                                    */


/*
        BurlapShodan.lspiPolynomialRunNormalized(100,
                                   "99_lspi_poly5_normalized",
                                   5,
                                   scenario,
                                   containerPath,
                                   (Steppable) simState -> {},
                                                 features,
                                   ShodanStateOil.BIOMASS,
                                   ShodanStateOil.DAY_OF_THE_YEAR,
                                   ShodanStateOil.MONTHS_LEFT);
*/


/*
        BurlapShodan.sarsaRunFourier(1,
                                     "100_sarsa_biomass_6lambda_fourier_decayexp2",
                                     4,
                                     .02,
                                     .6,
                                     features,
                                     scenario,
                                     containerPath, (Steppable) simState -> {},
                                     ShodanStateOil.BIOMASS,
                                     ShodanStateOil.DAY_OF_THE_YEAR,
                                     ShodanStateOil.MONTHS_LEFT);
                                     */
        //  quotaSweep();


        features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, new VariableDomain(0, 440)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 365)).
                        variableDomain(ShodanStateOil.MONTHS_LEFT, new VariableDomain(0, 243));




/*
        BurlapShodan.polynomialRun(.99,
                                   "99_lspi_poly3_distance",
                                   3,
                                   scenario,
                                   containerPath,
                                   (Steppable) simState -> {},
                                   ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                                   ShodanStateOil.DAY_OF_THE_YEAR,
                                   ShodanStateOil.MONTHS_LEFT);
*/
/*
        BurlapShodan.sarsaRunFourier(.99,
                                     "99_sarsa_biomass_6lambda_fourier_distance",
                                     4,
                                     .001,
                                     .6,
                                     features,
                                     scenario,
                                     containerPath, (Steppable) simState -> {},
                                     ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                                     ShodanStateOil.DAY_OF_THE_YEAR,
                                     ShodanStateOil.MONTHS_LEFT);
                                     */


        features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, new VariableDomain(0, 440)).
                        variableDomain(ShodanStateOil.AVERAGE_YEARLY_EFFORTS, new VariableDomain(0, 6215.0)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 365)).
                        variableDomain(ShodanStateOil.MONTHS_LEFT, new VariableDomain(0, 243));

/*
        BurlapShodan.sarsaRunFourier(1,
                                     "100_sarsa_effortDistance_6lambda_fourier",
                                     4,
                                     .05,
                                     .6,
                                     features,
                                     scenario,
                                     containerPath, (Steppable) simState -> {},
                                     ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                                     ShodanStateOil.AVERAGE_YEARLY_EFFORTS,
                                     ShodanStateOil.DAY_OF_THE_YEAR,
                                     ShodanStateOil.MONTHS_LEFT);

*/

        features =
                new NormalizedVariableFeatures().
                        variableDomain(ShodanStateOil.AVERAGE_DISTANCE_TO_PORT, new VariableDomain(0, 440)).
                        variableDomain(ShodanStateOil.AVERAGE_YEARLY_CASHFLOW, new VariableDomain(-5, 300)).
                        variableDomain(ShodanStateOil.DAY_OF_THE_YEAR, new VariableDomain(0, 365)).
                        variableDomain(ShodanStateOil.MONTHS_LEFT, new VariableDomain(0, 243));
/*
        BurlapShodan.sarsaRunFourier(1,
                                     "100_sarsa_cashDistance_6lambda_fourier2",
                                     4,
                                     .0025,
                                     .6,
                                     features,
                                     scenario,
                                     containerPath, (Steppable) simState -> {},
                                     ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                                     ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                                     ShodanStateOil.DAY_OF_THE_YEAR,
                                     ShodanStateOil.MONTHS_LEFT);

*/

/*
        BurlapShodan.sarsaRunFourier(1,
                                     "100_sarsa_cashDistance_6lambda_fourier3",
                                     4,
                                     .005,
                                     .6,
                                     features,
                                     scenario,
                                     containerPath, (Steppable) simState -> {},
                                     ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                                     ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                                     ShodanStateOil.DAY_OF_THE_YEAR,
                                     ShodanStateOil.MONTHS_LEFT);
*/
/*
        BurlapShodan.qRunFourier(1,
                                 "100_qrun_cashDistance_fourier4",
                                 4,
                                 .01,
                                 1000,
                                 100,
                                 scenario,
                                 containerPath,features,
                                 (Steppable) simState -> {},
                                 ShodanStateOil.AVERAGE_DISTANCE_TO_PORT,
                                 ShodanStateOil.AVERAGE_YEARLY_CASHFLOW,
                                 ShodanStateOil.DAY_OF_THE_YEAR,
                                 ShodanStateOil.MONTHS_LEFT);
*/
        episodesToCSV(containerPath);

    }


    public static  void quotaSweep(){

        for(double quota = 0; quota<2000000; quota+=100000){


            PrototypeScenario scenario = new PrototypeScenario();
            scenario.setFishers(300);
            SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
            mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0d));
            scenario.setPortPositionX(40);
            scenario.setPortPositionY(25);
            scenario.setMapInitializer(mapInitializer);
            ShodanEnvironment environment= new ShodanEnvironment(scenario, new Steppable() {
                @Override
                public void step(SimState simState) {

                }
            });

            Policy policy = new Policy() {
                @Override
                public Action action(State s) {
                    return new SimpleAction("open");
                }

                @Override
                public double actionProb(State s, Action a) {
                    return 1d;
                }

                @Override
                public boolean definedFor(State s) {
                    return true;
                }
            };


            TACMonoFactory factory = new TACMonoFactory();
            factory.setQuota(new FixedDoubleParameter(quota));
            environment.resetEnvironment(0);
            for (Fisher fisher : environment.getState().getFishers()) {
                fisher.setRegulation(factory.apply(environment.getState()));
            }
            PolicyUtils.rollout(policy,environment);
            System.out.println(quota + " = " + environment.totalReward());
        }



    }
}