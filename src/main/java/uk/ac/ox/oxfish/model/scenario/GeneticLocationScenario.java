package uk.ac.ox.oxfish.model.scenario;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.jenetics.Chromosome;
import org.jenetics.DoubleChromosome;
import org.jenetics.DoubleGene;
import org.jenetics.Genotype;
import org.jenetics.util.Factory;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.TACRegulation;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Basically a simple equal-efficiency equal chance to go out scenario where the only difference
 * is in the location chosen to go fishing
 * Created by carrknight on 5/5/15.
 */
public class GeneticLocationScenario extends PrototypeGeneticScenario {




    /**
     * if we need to change anything of the prototype scenario, do it here
     *
     * @param scenario the original scenario
     * @param model
     */
    @Override
    protected PrototypeScenario modifyPrototypeScenario(PrototypeScenario scenario, FishState model) {
        scenario.setFishers(100); //100 fishermen
        //fixed probability of going out
        scenario.setMaxDepartingProbability(1);
        scenario.setMinDepartingProbability(1);
        //fixed low efficiency
        scenario.setMaxFishingEfficiency(.01);
        scenario.setMinFishingEfficiency(.01);

        scenario.setRegulation(new FishingSeason(true,1000));
      //  scenario.setRegulation(new TACRegulation(10*scenario.getFishers(),model));


        scenario.setGridSizeInKm(5);

        return scenario;
    }


    /**
     * ignored
     */
    @Override
    protected ScenarioResult modifyScenarioResult(ScenarioResult result) {


        /*
        final NauticalMap map = result.getMap();
        for(int i=0; i<10; i++)
            for(int j=0;j<getHeight(); j++)
                map.getSeaTile(i,j).setMpa(new MasonGeometry());
*/

        return result;
    }

    /**
     * the factory that generates new random genotypes
     *
     * @param result the scenario result, if you need to focus on it
     * @return the factory
     */
    @Override
    protected Factory<Genotype<DoubleGene>> generateGenotypeFactory(
            ScenarioResult result)
    {
        //two chromosomes, one is the location proper, the other is just a random key
        //x,y as a proportion of the total width and height
        Chromosome<DoubleGene> location = new DoubleChromosome(0.0,.9999,2);
        Chromosome<DoubleGene> randomKey = new DoubleChromosome(0.0,.9999,1);

        return Genotype.of(location,randomKey);
    }

    /**
     * the function that transforms fishers to genotypes ready to be optimized
     *
     * @param result the scenario result
     * @return the transformer
     */
    @Override
    protected Function<Fisher, Genotype<DoubleGene>> generateFisherToGenotypeTransformer(
            ScenarioResult result) {



        //grabs the favorite location, encode it as a chromosome and returns it
        return new Function<Fisher,Genotype<DoubleGene>>(){

            @Override
            public Genotype<DoubleGene> apply(Fisher fisher) {
                SeaTile favoriteSpot =
                        ((FavoriteDestinationStrategy) fisher.getDestinationStrategy()).getFavoriteSpot();
                double x = (double)favoriteSpot.getGridX()/(double)getWidth();
                double y = (double)favoriteSpot.getGridY()/(double)getHeight();
                Chromosome<DoubleGene> location = DoubleChromosome.of(
                        DoubleGene.of(x,0d,.9999),DoubleGene.of(y,0d,.9999)
                );
                Chromosome<DoubleGene> randomKey = new DoubleChromosome(0.0,.9999,1);
                return Genotype.of(location,randomKey);

            }
        };
    }

    /**
     * the function that modifies fishers with their new genotype
     *
     * @param result the scenario result
     * @return the adapter
     */
    @Override
    protected Consumer<Pair<Fisher, Genotype<DoubleGene>>> generateFisherAdapterToNewGenotype(
            ScenarioResult result) {

        NauticalMap map = result.getMap();


        return new Consumer<Pair<Fisher, Genotype<DoubleGene>>>() {
            @Override
            public void accept(
                    Pair<Fisher, Genotype<DoubleGene>> fisherGenotypePair) {
                Chromosome<DoubleGene> chromosome = fisherGenotypePair.getSecond().getChromosome(0);
                //grab new spot
                int x = (int) Math.floor(chromosome.getGene(0).floatValue() * getWidth());
                int y = (int) Math.floor(chromosome.getGene(1).floatValue() * getHeight());
                //System.out.println(x + " --- " + y);
                SeaTile seaTile = map.getSeaTile(x, y);
                final Fisher fisher = fisherGenotypePair.getFirst();

                if(seaTile.getAltitude() >0) //if you are on land, go at random{
                    seaTile = map.getRandomBelowWaterLineSeaTile(fisher.getRandom());

                fisher.setDestinationStrategy(new FavoriteDestinationStrategy(seaTile));

            }
        };
    }
}
