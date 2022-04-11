package uk.ac.ox.oxfish.fisher.actions;

import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.Delaying;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.MasonUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FadSearchAction implements Action {

    private final double hoursDelayIfNoFadFound;

    private final double fadSetDurationTime;

    private final double minimumValueOfFad;


    public FadSearchAction(double hoursDelayIfNoFadFound,
                           double fadSetDurationTime,
                           double minimumValueOfFad) {
        this.hoursDelayIfNoFadFound = hoursDelayIfNoFadFound;
        this.fadSetDurationTime = fadSetDurationTime;
        this.minimumValueOfFad = minimumValueOfFad;
    }

    @Override
    public ActionResult act(FishState model,
                            Fisher agent,
                            Regulation regulation,
                            double hoursLeft) {
        //get all the fads at this area

        Bag fadsHere = model.getFadMap().fadsAt(agent.getLocation());
        //if there is no fads here, spent some hours wasting time
        if(fadsHere.isEmpty())
            return new ActionResult(new Delaying(hoursDelayIfNoFadFound),hoursLeft);


        //grab a random, non owned fad
        Optional<Fad> randomFad = MasonUtils.<Fad>bagToStream(fadsHere).
                filter(
                        fad -> fad.getOwner().getFisher() != agent
                ).
                filter(fad -> fad.valueOfFishFor(agent) >= minimumValueOfFad).
                skip(model.getRandom().nextInt(fadsHere.size())).
                findAny();

        //if there are no fads,
        return randomFad.map(fad -> new ActionResult(
                new OpportunisticFadSetAction(
                        fad,
                        agent,
                        fadSetDurationTime),
                hoursLeft
        )).orElseGet(
                () -> new ActionResult(new Delaying(hoursDelayIfNoFadFound),
                        hoursLeft));

    }
}
