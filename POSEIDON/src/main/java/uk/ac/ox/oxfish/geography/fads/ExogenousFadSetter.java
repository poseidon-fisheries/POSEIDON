package uk.ac.ox.oxfish.geography.fads;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.ObjectArrayMessage;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.List;

import static uk.ac.ox.oxfish.utility.CsvLogger.addCsvLogger;

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

    public static void initFadRemovalLog() {
        addCsvLogger(
            Level.DEBUG,
            "fad_removals",
            "step_deployed,lon_deployed,lat_deployed,step_removed,lon_removed,lat_removed"
        );
    }

    @Override
    public void step(final SimState simState) {
        final FishState model = ((FishState) simState);
        //get fads to set on
        final List<Fad> allFadsToSetOn = chooseWhichFadsToSetOnToday(
            fadMap,
            model,
            model.getDay()
        );
        //set on them
        for (final Fad fad : allFadsToSetOn) {
            logFadRemoval(fad, model);
            setOnFad(fad);
        }
        //done!
    }

    abstract protected List<Fad> chooseWhichFadsToSetOnToday(
        FadMap fadMap,
        FishState model,
        int day
    );

    private static void logFadRemoval(
        final Fad<? extends LocalBiology> fad,
        final FishState fishState
    ) {
        LogManager.getLogger("fad_removals").debug(() -> {
            final NauticalMap map = fishState.getMap();
            final Coordinate coordinatesDeployed =
                map.getCoordinates(map.getSeaTile(fad.getLocationDeployed()));
            final Coordinate coordinatesRemoved =
                map.getCoordinates(fad.getLocation());
            return new ObjectArrayMessage(
                fad.getStepDeployed(),
                coordinatesDeployed.x,
                coordinatesDeployed.y,
                fishState.getStep(),
                coordinatesRemoved.x,
                coordinatesRemoved.y
            );
        });
    }

    /**
     * remove fad from circulation
     *
     * @param fad
     */
    private void setOnFad(final Fad fad) {
        //remove it from the fadMap
        fadMap.remove(fad);
        //should have been removed automatically
        final FadManager owner = fad.getOwner();
        assert owner == null || !owner.getDeployedFads().contains(fad);
        //probably pointless turn off
        fad.getBiology().turnOff();


    }

    @Override
    public void start(final FishState model) {
        Preconditions.checkState(stoppable == null, "already started!");
        fadMap = model.getFadMap();
        Preconditions.checkState(fadMap != null, "Exogenous Fad Setter cannot find reference to FadMap!");
        stoppable = model.scheduleEveryDay(this, EXOGENOUS_FAD_SETTER_STEPORDER);

    }

    @Override
    public void turnOff() {
        if (stoppable != null)
            stoppable.stop();
    }

    protected FadMap getFadMap() {
        return fadMap;
    }
}
