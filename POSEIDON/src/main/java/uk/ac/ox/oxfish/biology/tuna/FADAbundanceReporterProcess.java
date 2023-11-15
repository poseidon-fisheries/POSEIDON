package uk.ac.ox.oxfish.biology.tuna;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class FADAbundanceReporterProcess implements BiologicalProcess<AbundanceLocalBiology>{
    private String label;


    public FADAbundanceReporterProcess(String label) {
        this.label = label;
    }

    @Override
    public Collection<AbundanceLocalBiology> process(FishState fishState, Collection<AbundanceLocalBiology> biologies) {


        Stream<Fad> fads = fishState.getFadMap().allFads();

        
        Collection<AbundanceLocalBiology> fadBiologies = fishState.getFadMap().allFads().map(Fad::getBiology).map(AbundanceLocalBiology.class::cast).collect(toImmutableList());
        AbundanceAggregator abundanceAggregator = new AbundanceAggregator();

        AbundanceLocalBiology aggregatedFADAbundance = abundanceAggregator.apply(fishState.getBiology(), fadBiologies);

        //Breakpoint
        for(Species s : fishState.getSpecies()){
            StructuredAbundance abundance = aggregatedFADAbundance.getAbundance(s);
            double[][] abundMatrix = abundance.asMatrix();
            for(int i=0; i<abundMatrix.length; i++){
                String output = fishState.getStep()+","+ label + "," + s.getCode() + ","+i+"," + StringUtils.join(ArrayUtils.toObject(abundMatrix[i]), ",");;
                System.out.println(output);
            }
        }

        return biologies;
    }
}
