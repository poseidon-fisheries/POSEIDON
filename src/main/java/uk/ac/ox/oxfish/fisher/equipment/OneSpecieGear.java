package uk.ac.ox.oxfish.fisher.equipment;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * A test gear that only catches one specie and always a fixed proportion of what is available where the fishing takes place!
 * Created by carrknight on 4/20/15.
 */
public class OneSpecieGear implements Gear {

    final Specie targetedSpecie;

    final double proportionCaught;

    public OneSpecieGear(Specie targetedSpecie, double proportionCaught)
    {
        this.targetedSpecie = targetedSpecie;
        this.proportionCaught = proportionCaught;
    }


    /**
     * catches a fixed proportion of the targeted specie and nothing of all the others
     * @param fisher the fisher
     * @param where where the fisher is fishing
     * @param hoursSpentFishing
     *@param modelBiology the biology (list of available species)  @return the catch
     */
    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, double hoursSpentFishing, GlobalBiology modelBiology) {
        double poundsCaught = hoursSpentFishing * proportionCaught * where.getBiomass(targetedSpecie);
        assert  poundsCaught >=0;
        if(poundsCaught> 0)
            where.reactToThisAmountOfBiomassBeingFished(targetedSpecie,poundsCaught);
        return new Catch(targetedSpecie, poundsCaught,modelBiology);
    }
}
