package uk.ac.ox.oxfish.biology.tuna;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.IntStream.range;

public class AbundanceReporterProcess implements BiologicalProcess<AbundanceLocalBiology>{
    private String label;


    public AbundanceReporterProcess(String label) {
        this.label = label;
    }

    @Override
    public Collection<AbundanceLocalBiology> process(FishState fishState, Collection<AbundanceLocalBiology> biologies) {


        //Create an abundance shapshot for each species
        //get the abundance for each agebin/sex

        AbundanceLocalBiology aggregatedBio = new AbundanceAggregator().apply(fishState.getBiology(), biologies);

        //Breakpoint
        for(Species s : fishState.getSpecies()){
            StructuredAbundance abundance = aggregatedBio.getAbundance(s);
            double[][] abundMatrix = abundance.asMatrix();
            for(int i=0; i<abundMatrix.length; i++){
                String output = fishState.getStep()+","+label + "," + s.getCode() + ","+i+"," + StringUtils.join(ArrayUtils.toObject(abundMatrix[i]), ",");;
                System.out.println(output);
            }
        }


 /*       biologies.forEach(biology ->
                fishState.getSpecies().forEach(species -> {

                    final StructuredAbundance abundance = biology.getAbundance(species);


                    //Breakpoint
                    System.out.println("report it here");
                }));
*/

        return biologies;
    }
}
