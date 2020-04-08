package uk.ac.ox.oxfish.utility.adaptation;

import burlap.datastructures.BoltzmannDistribution;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.bandit.SoftmaxBanditAlgorithm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * compares your port with other ports. Pick one by softmax
 * @param <T>
 */
public class SimplePortAdaptation extends AbstractAdaptation<Port> {


    public static final double DEFAULT_INERTIA = .25;

    public static final double DEFAULT_TEMPERATURE = 1;
    private final double  inertia;

    public SimplePortAdaptation(double inertia) {


        super(new Sensor<Fisher, Port>() {
                  @Override
                  public Port scan(Fisher system) {
                      return system.getHomePort();
                  }
              }

                ,
              new Actuator<Fisher, Port>() {
                  @Override
                  public void apply(Fisher subject, Port policy, FishState model) {

                      //if you don't have to change, do nothing
                      if (subject.getHomePort().equals(policy))
                          return;
                      else {
                          //if you are at sea, return to new port
                          if (!subject.isAtPortAndDocked())
                              subject.setHomePort(policy);
                          else {
                              //otherwise teleport to new port
                              subject.getHomePort().depart(subject);
                              subject.setHomePort(policy);
                              subject.teleport(policy.getLocation());
                              policy.dock(subject);

                          }
                          //reset friendships
                          subject.getSocialNetwork().removeFisher(subject,model);
                          subject.getSocialNetwork().addFisher(subject,model);

                      }
                  }
              }

                ,
              new Predicate<Fisher>() {
                  @Override
                  public boolean test(Fisher fisher) {
                      return true;
                  }
              });

        this.inertia = inertia;
    }

    public SimplePortAdaptation() {
        this(DEFAULT_INERTIA);
    }

    @Override
    protected void onStart(FishState model, Fisher fisher) {

    }

    @Override
    public Port concreteAdaptation(Fisher toAdapt, FishState state, MersenneTwisterFast random) {

        ArrayList<Port> ports = new ArrayList<>(state.getPorts());

        if(state.getYear()<1 || random.nextDouble() < inertia)
            return null;

        Integer portSelected = SoftmaxBanditAlgorithm.drawFromSoftmax(
                new MersenneTwisterFast(),
                ports.size(),
                new Function<Integer, Double>() {
                    @Override
                    public Double apply(Integer integer) {

                        final Double latestProfitsAtPort = state.getYearlyDataSet().
                                getLatestObservation("Average Cash-Flow at " + ports.get(integer).getName()) /(365d);

                        return Double.isFinite(latestProfitsAtPort) ? latestProfitsAtPort : 0
                                ;
                    }
                },
                DEFAULT_TEMPERATURE

        );


        return ports.get(portSelected);


    }

    @Override
    public void turnOff(Fisher fisher) {

    }
}
