package uk.ac.ox.oxfish.biology.complicated;

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;

/**
 * Deals with the basics that are common for all diffusers
 * Created by carrknight on 7/6/17.
 */
public abstract class AbstractAbundanceDiffuser implements AbundanceDiffuser {


    /**
     * the species managed by this diffuser
     */
    private final Species species;

    /**
     * list of all abundanceBasedLocalBiology (this way we don't need to check the map every time for them)
     */
    private final Map<SeaTile,AbundanceBasedLocalBiology> biologies;

    /**
     * how many cells distant can this species move in a day?
     */
    private final int diffusingRange;

    /**
     * we store here for each tile its neighbors. This way we ask the map only once
     */
    private final Map<SeaTile, List<SeaTile>> neighbors = new HashMap<>();


    public AbstractAbundanceDiffuser(
            Species species,
            Map<SeaTile, AbundanceBasedLocalBiology> biologies, int diffusingRange) {
        this.species = species;
        this.biologies = biologies;
        this.diffusingRange = diffusingRange;
    }



    @Override
    public void step(SimState simState) {

        FishState model = (FishState) simState;
        //turn it into a list and shuffle it
        List<Map.Entry<SeaTile, AbundanceBasedLocalBiology>> locals = Lists.newArrayList(biologies.entrySet());
        Collections.shuffle(locals, new Random(model.getRandom().nextLong()));


        for (Map.Entry<SeaTile, AbundanceBasedLocalBiology> here : locals) {
            neighbors.putIfAbsent(here.getKey(),
                                  getUsefulNeighbors(here.getKey(),model.getMap()));
            List<SeaTile> potential = neighbors.get(here.getKey());
            if(potential.size()==0)
                continue;
            //shuffle neighbors
            Collections.shuffle(potential);
            for(SeaTile there : potential) {
                assert biologies.containsKey(there);
                //check for difference in abundance between each bin
                for (int bin = 0; bin < species.getMaxAge() + 1; bin++)
                {
                    AbundanceBasedLocalBiology thereBiology = biologies.get(there);
                    //move male
                    int maleDelta = here.getValue().getNumberOfMaleFishPerAge(species)[bin] -
                            thereBiology.getNumberOfMaleFishPerAge(species)[bin];
                    if(maleDelta > 0) //move only in one direction
                        move(model.getRandom(), here.getKey(),
                               here.getValue(), there, thereBiology, maleDelta, bin, true);

                    //move female
                    int femaleDelta = here.getValue().getNumberOfFemaleFishPerAge(species)[bin] -
                            thereBiology.getNumberOfFemaleFishPerAge(species)[bin];
                    if(femaleDelta > 0) //move only in one direction
                        move(model.getRandom(), here.getKey(),
                               here.getValue(), there, thereBiology, femaleDelta, bin, false);


                }

            }

        }



    }


    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     * @param random
     * @param here departing point
     * @param biologyHere departing local biology
     * @param there arriving point
     * @param biologyThere arriving local biology
     * @param delta number of fish here - number of fish there (always positive or this isn't called)
     * @param bin bin/age studied
     * @param male whether it's male or female
     */
    public abstract void move(
            MersenneTwisterFast random, SeaTile here, AbundanceBasedLocalBiology biologyHere,
            SeaTile there, AbundanceBasedLocalBiology biologyThere,
            int delta, int bin, boolean male);

    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public Species getSpecies() {
        return species;
    }


    /**
     * get all the neighbors of a given tile that have the right local biology and are above water
     * @param tile the tile we want the neighbors of
     * @param map the map object
     * @return a bag with all the neighbors
     */
    private List<SeaTile> getUsefulNeighbors(SeaTile tile, NauticalMap map)
    {
        final Bag mooreNeighbors = map.getMooreNeighbors(tile, diffusingRange);
        List<SeaTile> toKeep = new LinkedList<>();
        for(Object inBag : mooreNeighbors)
        {
            SeaTile newTile = (SeaTile) inBag;
            if (biologies.containsKey(newTile))
            {
                assert newTile.getAltitude() <= 0;
                assert newTile.getBiology() instanceof AbundanceBasedLocalBiology;
                toKeep.add(newTile);
            }
        }
        return toKeep;
    }
}
