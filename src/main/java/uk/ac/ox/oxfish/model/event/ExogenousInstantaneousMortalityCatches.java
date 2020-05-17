package uk.ac.ox.oxfish.model.event;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.ExponentialMortalityFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExogenousInstantaneousMortalityCatches extends AbstractExogenousCatches {


    private final HashMap<String,Double> exponentialMortality;

    private final LinkedHashMap<Species,Double> lastExogenousCatches = new LinkedHashMap<>();

    private final boolean isAbundanceBased;


    public ExogenousInstantaneousMortalityCatches(String dataColumnName,
                                                  LinkedHashMap<String, Double> exponentialMortality,
                                                  boolean isAbundanceBased) {
        super(dataColumnName);
        this.exponentialMortality = exponentialMortality;
        this.isAbundanceBased = isAbundanceBased;
    }

    @Override
    public void step(SimState simState) {
        final FishState model = (FishState) simState;

        List<? extends LocalBiology> allTiles = getAllCatchableBiologies(model);

        for (Map.Entry<String, Double> mortality : exponentialMortality.entrySet()) {
            //total landed
            double totalLanded = 0;

            //get the species
            Species target = model.getSpecies(mortality.getKey());
            Preconditions.checkArgument(target!=null, "Couldn't find this species");


            //worry only about tiles that have this fish
            List<? extends LocalBiology> tiles =  allTiles.stream().filter(
                    seaTile -> getFishableBiomass(target, seaTile) >
                            FishStateUtilities.EPSILON).collect(Collectors.toList());

            final Double instantMortality = mortality.getValue();
            for (LocalBiology tile : tiles) {
                if(isAbundanceBased) {
                    totalLanded += abundanceCatch(instantMortality,
                            model, target, tile);
                }
                else{


                    totalLanded+= biomassCatch(model, target, instantMortality, tile);
                }

            }


            super.lastExogenousCatchesMade.put(target,totalLanded);

        }


    }

    private double biomassCatch(FishState fishstate, Species target, Double instantMortality, LocalBiology tile) {
        assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
        double caught = tile.getBiomass(target) * (1-Math.exp(-instantMortality));
        Catch fish = new Catch(target,
                caught,
                fishstate.getBiology());
        //round to be supersafe
        if(fish.totalCatchWeight()>tile.getBiomass(target)) {
            //should be by VERY little!
            assert tile.getBiomass(target) + FishStateUtilities.EPSILON > fish.getTotalWeight();
            //bound it to what is available
            fish = new Catch(target,tile.getBiomass(target),fishstate.getBiology());
            assert (fish.totalCatchWeight()<=tile.getBiomass(target));
        }
        tile.reactToThisAmountOfBiomassBeingFished(fish,fish,fishstate.getBiology());
        return fish.getTotalWeight();
    }

    private double abundanceCatch(double mortality,
                                  FishState fishstate, Species species, LocalBiology tile) {
        HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(
                0d,
                new ExponentialMortalityFilter(mortality)
        );
        final GlobalBiology biology = fishstate.getBiology();
        StructuredAbundance[] structuredAbundances = new StructuredAbundance[biology.getSize()];
        for(int i=0; i<structuredAbundances.length; i++)
            structuredAbundances[i] = new StructuredAbundance(biology.getSpecie(i).getNumberOfSubdivisions(),
                    biology.getSpecie(i).getNumberOfBins());
        structuredAbundances[species.getIndex()] =
                gear.catchesAsAbundanceForThisSpecies(tile, 1, species);
        Catch fish = new Catch(structuredAbundances,
                biology);
        tile.reactToThisAmountOfBiomassBeingFished(fish,fish, biology);
        return fish.getTotalWeight();
    }

}
