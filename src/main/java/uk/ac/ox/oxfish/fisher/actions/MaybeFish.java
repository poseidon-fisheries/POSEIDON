package uk.ac.ox.oxfish.fisher.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

public class MaybeFish implements Action {
    @Override
    public ActionResult act(
        FishState model, Fisher agent, Regulation regulation, double hoursLeft
    ) {
        return agent.canAndWantToFishHere() ?
            new ActionResult(new Fishing(), hoursLeft) : // if we want to fish here, let's fish
            new ActionResult(new Arriving(), 0d); // otherwise, basically wait
    }
}
