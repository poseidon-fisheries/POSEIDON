package uk.ac.ox.oxfish.fisher.actions;

import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.Delaying;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.MasonUtils;

import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadSearchAction implements Action {

    private final double hoursDelayIfNoFadFound;

    private final double fadSetDurationTime;

    private final double minimumValueOfFad;


    public FadSearchAction(
        final double hoursDelayIfNoFadFound,
        final double fadSetDurationTime,
        final double minimumValueOfFad
    ) {
        this.hoursDelayIfNoFadFound = hoursDelayIfNoFadFound;
        this.fadSetDurationTime = fadSetDurationTime;
        this.minimumValueOfFad = minimumValueOfFad;
    }

    @Override
    public ActionResult act(
        final FishState model,
        final Fisher fisher,
        final Regulation regulation,
        final double hoursLeft
    ) {
        //get all the fads at this area

        final Bag fadsHere = model.getFadMap().fadsAt(fisher.getLocation());
        //if there is no fads here, spent some hours wasting time
        if (fadsHere.isEmpty())
            if (hoursDelayIfNoFadFound > 0)
                return new ActionResult(new Delaying(hoursDelayIfNoFadFound), hoursLeft);
            else
                return new ActionResult(new Arriving(), hoursLeft);

        final FadManager<?, ?> fadManager = getFadManager(fisher);
        final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();

        //grab a random, non owned fad
        final List<AbstractFad> fadsThatICanSteal = MasonUtils.<AbstractFad>bagToStream(fadsHere).
            filter(fad -> fad.getOwner().getFisher() != fisher).
            filter(fad -> fadManager.getFishValueCalculator().valueOf(fad.getBiology(), prices) >= minimumValueOfFad).
            collect(Collectors.toList());

        if (fadsThatICanSteal.size() > 0)
            return new ActionResult(
                new OpportunisticFadSetAction(
                    fadsThatICanSteal.get(model.getRandom().nextInt(fadsThatICanSteal.size())),
                    fisher,
                    fadSetDurationTime
                ),
                hoursLeft
            );
        else if (hoursDelayIfNoFadFound > 0)
            return new ActionResult(
                new Delaying(hoursDelayIfNoFadFound),
                hoursLeft
            );
        else
            return new ActionResult(new Arriving(), hoursLeft);

    }
}
