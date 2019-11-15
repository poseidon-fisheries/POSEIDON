package uk.ac.ox.oxfish.fisher.actions.fads;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.Optional;

import static java.lang.Math.min;
import static java.util.stream.IntStream.range;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class MakeUnassociatedSet implements SetAction {

    private static final Quantity<Mass> MAX_CATCH_PER_SPECIES = getQuantity(50, TONNE);

    @Override
    public Optional<SeaTile> getActionTile(Fisher fisher) {
        return Optional.of(fisher.getLocation());
    }

    @Override
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        final PurseSeineGear purseSeineGear = (PurseSeineGear) fisher.getGear();
        if (isAllowed(model, fisher) && isPossible(model, fisher)) {
            final int duration = toHours(getDuration(fisher, model.getRandom()));
            if (model.getRandom().nextDouble() < purseSeineGear.getSuccessfulSetProbability()) {
                final SeaTile seaTile = fisher.getLocation();
                final GlobalBiology globalBiology = model.getBiology();
                final double maxCatchInKg = asDouble(MAX_CATCH_PER_SPECIES, KILOGRAM);
                final double[] biomasses = range(0, globalBiology.getSize()).mapToDouble(i -> {
                    final double biomassInTile = seaTile.getBiology().getBiomass(globalBiology.getSpecie(i));
                    final double biomassCaught = model.random.nextDouble() * maxCatchInKg;
                    return min(biomassInTile, biomassCaught);
                }).toArray();
                final VariableBiomassBasedBiology schoolBiology = new BiomassLocalBiology(biomasses, biomasses);
                // Remove the catches from the underlying biology:
                final Catch catchObject = new Catch(schoolBiology.getCurrentBiomass());
                seaTile.getBiology().reactToThisAmountOfBiomassBeingFished(catchObject, catchObject, globalBiology);
                // Have the fisher fish the school biology
                fisher.fishHere(globalBiology, duration, model, schoolBiology);
                model.recordFishing(seaTile);
            }
            return new ActionResult(new Arriving(), hoursLeft - duration);
        } else {
            return new ActionResult(new Arriving(), hoursLeft);
        }
    }

}
