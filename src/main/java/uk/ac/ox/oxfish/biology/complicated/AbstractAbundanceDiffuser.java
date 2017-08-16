package uk.ac.ox.oxfish.biology.complicated;

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
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
     * how many cells distant can this species move in a day?
     */
    private final int diffusingRange;

    /**
     * we store here for each tile its neighbors. This way we ask the map only once
     */
    private final Map<SeaTile, List<SeaTile>> neighbors = new HashMap<>();


    public AbstractAbundanceDiffuser(
            int diffusingRange) {
        this.diffusingRange = diffusingRange;
    }


    @Override
    public void step(
            Species species,
            Map<SeaTile, AbundanceBasedLocalBiology> biologies,
            FishState model) {

        //turn it into a list and shuffle it
        List<Map.Entry<SeaTile, AbundanceBasedLocalBiology>> locals = Lists.newArrayList(biologies.entrySet());
        Collections.shuffle(locals, new Random(model.getRandom().nextLong()));


        for (Map.Entry<SeaTile, AbundanceBasedLocalBiology> here : locals) {
            neighbors.putIfAbsent(here.getKey(),
                                  getUsefulNeighbors(here.getKey(), model.getMap(),
                                                     biologies));
            List<SeaTile> potential = neighbors.get(here.getKey());
            if(potential.size()==0)
                continue;
            //shuffle neighbors
            Collections.shuffle(potential);
            for(SeaTile there : potential) {
                assert biologies.containsKey(there);
                AbundanceBasedLocalBiology thereBiology = biologies.get(there);
                int[] malesHere = here.getValue().getNumberOfMaleFishPerAge(species);
                int[] malesThere = thereBiology.getNumberOfMaleFishPerAge(species);

                int[] femaleHere = here.getValue().getNumberOfFemaleFishPerAge(species);
                int[] femaleThere = thereBiology.getNumberOfFemaleFishPerAge(species);


                //check for difference in abundance between each bin
                for (int bin = 0; bin < species.getMaxAge() + 1; bin++)
                {
                    //move male
                    int fishHere = malesHere[bin];
                    int fishThere = malesThere[bin];
                    int maleDelta = fishHere -
                            fishThere;
                    //move always get called, regardless of what the delta is!
                    move(species, here.getKey(),
                         here.getValue(), there, thereBiology, maleDelta,fishHere ,fishThere , bin, true, model.getRandom());

                    //move female
                    fishHere = femaleHere[bin];
                    fishThere = femaleThere[bin];
                    int femaleDelta = fishHere -
                            fishThere;
                    //move always get called, regardless of what the delta is!
                    move(species, here.getKey(),
                         here.getValue(), there, thereBiology, femaleDelta,fishHere ,fishThere , bin, false, model.getRandom());


                }

            }

        }



    }


    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     * @param species species moving
     * @param here departing point
     * @param biologyHere departing local biology
     * @param there arriving point
     * @param biologyThere arriving local biology
     * @param delta number of fish here - number of fish there (always positive or this isn't called)
     * @param fishHere
     * @param fishThere
     * @param bin bin/age studied
     * @param male whether it's male or female
     * @param random
     */
    public abstract void move(
            Species species,
            SeaTile here,
            AbundanceBasedLocalBiology biologyHere,
            SeaTile there,
            AbundanceBasedLocalBiology biologyThere,
            int delta,
            int fishHere, int fishThere, int bin,
            boolean male,
            MersenneTwisterFast random);



    /**
     * get all the neighbors of a given tile that have the right local biology and are above water
     * @param tile the tile we want the neighbors of
     * @param map the map object
     * @param biologies
     * @return a bag with all the neighbors
     */
    private List<SeaTile> getUsefulNeighbors(
            SeaTile tile,
            NauticalMap map,
            Map<SeaTile, AbundanceBasedLocalBiology> biologies)
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
