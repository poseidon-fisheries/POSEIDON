package uk.ac.ox.oxfish.geography.fads;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.List;
import java.util.Map;

/**
 * an exogenous generator of fad set events which are unconnected to both fishers and FadManagers
 */
public abstract class ExogenousFadSetter implements AdditionalStartable, Steppable {

    /**
     * when in the model order are fads set.
     */
    public static final StepOrder EXOGENOUS_FAD_SETTER_STEPORDER = StepOrder.FISHER_PHASE;


    /**
     * link to the map holding all fads. Will get it when starting
     */
    private FadMap fadMap;

    /**
     * link to your own stoppable to be stopped in case of being turned off
     */
    private Stoppable stoppable;

    abstract protected List<Fad> chooseWhichFadsToSetOnToday(
            FadMap fadMap,
            FishState model,
            int day
    );

    @Override
    public void step(SimState simState) {
        FishState model = ((FishState) simState);
        //get fads to set on
        List<Fad> allFadsToSetOn = chooseWhichFadsToSetOnToday(fadMap,
                model,
                model.getDay());
        //set on them
        for (Fad fad : allFadsToSetOn) {
            setOnFad(fad);
        }
        //done!
    }

    @Override
    public void start(FishState model) {
        Preconditions.checkState(stoppable==null, "already started!");
        fadMap = model.getFadMap();
        Preconditions.checkState(fadMap != null, "Exogenous Fad Setter cannot find reference to FadMap!");
        stoppable = model.scheduleEveryDay(this, EXOGENOUS_FAD_SETTER_STEPORDER);

    }

    /**
     * remove fad from circulation
     * @param fad
     */
    private void setOnFad(Fad fad){
        //remove it from the fadMap
        fadMap.remove(fad);
        //should have been removed automatically
        FadManager owner = fad.getOwner();
        if(owner!=null)
            assert !owner.getDeployedFads().contains(fad);
        //probably pointless turn off
        fad.getBiology().turnOff();



    }



    @Override
    public void turnOff() {
        if(stoppable!=null)
            stoppable.stop();
    }

    protected FadMap getFadMap() {
        return fadMap;
    }
}
