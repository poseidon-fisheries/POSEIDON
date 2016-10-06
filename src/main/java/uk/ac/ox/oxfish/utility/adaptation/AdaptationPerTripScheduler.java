package uk.ac.ox.oxfish.utility.adaptation;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

import java.util.LinkedList;
import java.util.List;

/**
 * A list of adaptations to fire each time the fisher finishes a trip
 * Created by carrknight on 8/10/15.
 */
public class AdaptationPerTripScheduler implements TripListener, FisherStartable
{

    private FishState model;

    private Fisher fisher;

    private final List<Adaptation> adaptations = new LinkedList<>();

    @Override
    public void start(FishState model, Fisher fisher) {
        this.model = model;
        this.fisher=fisher;
        fisher.addTripListener(this);

        for(Adaptation a : adaptations)
            a.start(model, fisher);

    }

    @Override
    public void turnOff(Fisher fisher) {
        if(this.fisher !=null)
            this.fisher.removeTripListener(this);
    }

    /**
     * add an adaptation algorithm to the list. Start it if we have already started
     * @param adaptation
     */
    public void registerAdaptation(Adaptation adaptation)
    {

        adaptations.add(adaptation);
       if(model != null)
           adaptation.start(model, fisher);


    }


    public void removeAdaptation(Adaptation adaptation)
    {
        adaptations.remove(adaptation);
    }

    @Override
    public void reactToFinishedTrip(TripRecord record)
    {
        for(Adaptation a : adaptations)
            a.adapt(fisher,model,fisher.grabRandomizer());
    }
}
