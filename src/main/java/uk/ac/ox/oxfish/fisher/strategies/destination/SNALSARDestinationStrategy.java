package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.erotetic.EroteticChooser;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureFilter;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * Filters sequentially looking for:
 * <ul>
 *     <li>Somewhere Safe</li>
 *     <li>Somewhere not known to fail to have acceptable profit in past.</li>
 *     <li>Somewhere legal for me.</li>
 *     <li>Somewhere socially appropriate</li>
 *     <li>Somewhere known to have acceptable profit in past.</li>
 *     <li>Somewhere randomly picked as a spot. </li>
 * </ul>
 * Created by carrknight on 5/26/16.
 */
public class SNALSARDestinationStrategy implements DestinationStrategy,
        TripListener
{




    /**
     * the utility object where we put all the filters and sequentially goes through them.
     */
    private final EroteticChooser<SeaTile> chooser;

    /**
     * delegate doing the actual navigation. This destination strategy just updates it after every trip.
     */
    private final FavoriteDestinationStrategy delegate;


    public SNALSARDestinationStrategy(
            FeatureFilter<SeaTile> safetyFilter,
            FeatureFilter<SeaTile> notKnownToFailProfitFilter,
            FeatureFilter<SeaTile> legalFilter,
            FeatureFilter<SeaTile> sociallyAppropriateFilter,
            FeatureFilter<SeaTile> knownToHaveAcceptableProfits,
            FavoriteDestinationStrategy delegate) {
        this.chooser = new EroteticChooser<>();
        this.chooser.add(safetyFilter);
        this.chooser.add(notKnownToFailProfitFilter);
        this.chooser.add(legalFilter);
        this.chooser.add(sociallyAppropriateFilter);
        this.chooser.add(knownToHaveAcceptableProfits);
        this.delegate = delegate;
    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {
        return delegate.chooseDestination(fisher,random,model,currentAction);
    }


    private FishState model ;

    private Fisher fisher;

    @Override
    public void start(FishState model, Fisher fisher) {
        this.model=model;
        fisher.addTripListener(this);
        delegate.start(model,fisher);
    }

    @Override
    public void turnOff() {
        delegate.turnOff();
    }

    @Override
    public void reactToFinishedTrip(TripRecord record) {
        //all choices
        List<SeaTile> options = model.getMap().getAllSeaTilesExcludingLandAsList();
        delegate.setFavoriteSpot(chooser.filterOptions(options,
                                                       fisher.getTileRepresentation(),
                                                       model, fisher
                                 )
        );
    }

}
