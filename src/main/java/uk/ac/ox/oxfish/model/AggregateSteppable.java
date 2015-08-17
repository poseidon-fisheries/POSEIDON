package uk.ac.ox.oxfish.model;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

import java.util.LinkedList;
import java.util.List;

/**
 * Aggregate steppable is a collection of steppables that are pulsed without randomization. It's a simple hack to speed up
 * model.schedule. <p>
 * The problem is simple: MASON schedule randomizes everything all the time. When there are a lot of steppables the
 * randomization becomes expensive and painfully slow. This is due to the fact that the heap is simply too crowded. <p>
 * But many StepOrder of the model do not need randomization and can happen in any order (data-collection for example).
 * Those steppables then are aggregated in one of these objects so that the MASON steppable only has to deal with it.
 * Created by carrknight on 8/14/15.
 */
public class AggregateSteppable implements Steppable{

    List<Steppable> steppableList = new LinkedList<>();


    @Override
    public void step(SimState simState) {
        for(Steppable steppable : steppableList)
            steppable.step(simState);
    }


    public Stoppable add(Steppable steppable) {
        steppableList.add(steppable);

        return new Stoppable() {
            @Override
            public void stop() {
                remove(steppable);
            }
        };
    }

    /**
     */
    public boolean remove(Steppable o) {
        return steppableList.remove(o);
    }
}
