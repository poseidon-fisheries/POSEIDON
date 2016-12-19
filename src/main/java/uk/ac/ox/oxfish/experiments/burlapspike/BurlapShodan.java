package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.behavior.functionapproximation.dense.DenseCrossProductFeatures;
import burlap.behavior.functionapproximation.dense.NormalizedVariableFeatures;
import burlap.behavior.functionapproximation.dense.fourier.FourierBasis;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.lspi.LSPI;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.domain.singleagent.mountaincar.MCState;
import burlap.domain.singleagent.mountaincar.MountainCar;
import burlap.domain.singleagent.mountaincar.MountainCarVisualizer;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.vardomain.VariableDomain;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.visualizer.Visualizer;

import java.nio.file.Paths;

/**
 * Created by carrknight on 12/19/16.
 */
public class BurlapShodan {


    public static void main(String[] args){

        Paths.get("runs","burlap").toFile().mkdirs();



        //approximator. Really just takes a state and needs to turn it into a double[]
        //you will focus on x and v and then you write down the bounds
        NormalizedVariableFeatures inputFeatures = new NormalizedVariableFeatures()
                .variableDomain(ShodanStateOil.MONTHS_LEFT, new VariableDomain(0,240))
                .variableDomain(ShodanStateOil.CUMULATIVE_EFFORT, new VariableDomain(0,1000000))
                .variableDomain(ShodanStateOil.GAS_PRICE,new VariableDomain(0,1));

        //create a fourier basis (4th series) for this
        FourierBasis fb = new FourierBasis(inputFeatures, 4);



        SADomain domain = new SADomain();
        domain.setActionTypes(new UniversalActionType(ShodanEnvironment.ACTION_OPEN),
                              new UniversalActionType(ShodanEnvironment.ACTION_CLOSE));
        //create LSPI with discount factor of .99, keep running it until the policy iteration converges
        //densecrossproductfeatures is a way to approximate the Q-Value rather than the Value (like Fourier basis does)
        //3 is the number of actions there are
        LSPI lspi = new LSPI(domain, 0.99, new DenseCrossProductFeatures(fb, 2));

        ShodanEnvironment environment = new ShodanEnvironment();
        environment.resetEnvironment();

        lspi.setMaxNumPlanningIterations(5);
        lspi.setMinNewStepsForLearningPI(200000000);
        //run learning for 100 episodes
        for(int i = 0; i < 100; i++){
            ((EpsilonGreedy) lspi.getLearningPolicy()).setEpsilon(.4d*Math.pow(.98,i));
            Episode e = lspi.runLearningEpisode(environment, 20000);


            System.out.println(i + ": " + environment.totalReward());

            //reset environment for next learning episode
            environment.resetEnvironment();
            if(i%10==0 && i != 0)
            {
                System.out.println("force regression");
                lspi.runPolicyIteration(10,.001);


                environment.resetEnvironment();
                //final
                GreedyQPolicy policy = new GreedyQPolicy(lspi);
                PolicyUtils.rollout(policy,environment).write(Paths.get("runs","burlap","lspi_"+i).toAbsolutePath().toString());
                System.out.println("final_"+i + ": " + environment.totalReward());
                environment.resetEnvironment();

            }
        }





        System.out.println("Finished");
    }
}
