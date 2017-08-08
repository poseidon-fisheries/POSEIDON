package uk.ac.ox.oxfish.fisher.strategies.discarding;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.LinkedList;

/**
 * Created by carrknight on 6/23/17.
 */
public class AlwaysDiscardTheseSpecies implements DiscardingStrategy {


    final private LinkedList<Integer> indicesOfSpeciesToThrowOverboard;


    public AlwaysDiscardTheseSpecies(int... speciesToDiscard) {

        indicesOfSpeciesToThrowOverboard = new LinkedList<>();
        for(int index : speciesToDiscard)
            indicesOfSpeciesToThrowOverboard.add(index);

    }

    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * This strategy decides the new "catch" object, that is how much of the fish we are actually going to store
     * given how much we caught!
     *
     * @param where             where did we do the fishing
     * @param who               who did the fishing
     * @param fishCaught        the catch before any discard
     * @param hoursSpentFishing how many hours have we spent fishing
     * @param regulation        the regulation the fisher is subject to
     * @param model
     * @param random
     * @return a catch object holding how much we are actually going to load in the boat. The difference between
     * what is returned and the 'fishCaught' variable is the implicit discard
     */
    @Override
    public Catch chooseWhatToKeep(
            SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing, Regulation regulation, FishState model,
            MersenneTwisterFast random) {



        if(!fishCaught.hasAbundanceInformation())
        {
            double[] biomassArray = fishCaught.getBiomassArray();
            for (Integer index : indicesOfSpeciesToThrowOverboard)
                biomassArray[index] = 0;
            return new Catch(biomassArray);
        }
        else
        {
            StructuredAbundance[] abundance = new StructuredAbundance[fishCaught.numberOfSpecies()];
            for (int species=0; species< fishCaught.numberOfSpecies(); species++)
            {
                if(indicesOfSpeciesToThrowOverboard.contains(species))
                    abundance[species] = new StructuredAbundance(fishCaught.getAbundance(species).getSubdivisions(),
                                                                 fishCaught.getAbundance(species).getBins());
                else
                    abundance[species] = new StructuredAbundance(fishCaught.getAbundance(species));
            }
            return new Catch(abundance,model.getBiology());
        }

    }
}
