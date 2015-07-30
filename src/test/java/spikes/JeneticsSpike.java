package spikes;

import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.util.CharSeq;
import org.jenetics.util.Factory;


public class JeneticsSpike {
    private static final String TARGET_STRING = "jenetics";

    private static Integer evaluate(final Genotype<CharacterGene> gt) {
        return TARGET_STRING.length() - levenshtein(
                TARGET_STRING, (CharacterChromosome)gt.getChromosome()
        );
    }

    public static void main(String[] args) throws Exception {
        final CharSeq chars = CharSeq.of("a-z");
        final Factory<Genotype<CharacterGene>> gtf = Genotype.of(
                new CharacterChromosome(chars, TARGET_STRING.length())
        );

        final Engine<CharacterGene, Integer> engine = Engine
                .builder(JeneticsSpike::evaluate, gtf)
                .populationSize(500)
                .survivorsSelector(new StochasticUniversalSelector<>())
                .offspringSelector(new TournamentSelector<>(5))
                .alterers(
                        new Mutator<>(0.1),
                        new SinglePointCrossover<>(0.5))
                .build();

     /*   final Phenotype<CharacterGene, Integer> result = engine.stream().limit(100)
                .collect(toBestPhenotype());
*/
        Population<CharacterGene,Integer> population = new Population<>();
        for(int i=0;i<500;i++)
            population.add( Phenotype.of(gtf.newInstance(),1,JeneticsSpike::evaluate,
                                         scale->scale));


        for(int i=1;i<100;i++)
        {
            EvolutionResult<CharacterGene, Integer> evolved = engine.evolve(population, i);
            population = evolved.getPopulation();

            System.out.println(evolved.getBestFitness());

        }

    }


    /**
     * Return Levenshtein distance of two character sequences.
     */
    private static int levenshtein(final CharSequence s, final CharSequence t) {
        //Step 1:
        final int n = s.length();
        final int m = t.length();
        if (n == 0 || m == 0) {
            return Math.max(n, m);
        }

        //Step 2:
        int d[][] = new int[n + 1][m +1];
        for (int i = 0; i <= n; ++i) {
            d[i][0] = i;
        }
        for (int j = 0; j <= m; ++j) {
            d[0][j] = j;
        }

        //Step 3:
        for (int i = 1; i <= n; ++i) {
            final char si = s.charAt(i - 1);

            //Step 4:
            for (int j = 1; j <= m; ++j) {
                final char tj = t.charAt(j - 1);

                //Step 5:
                int cost = 0;
                if (si == tj) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                //Step 6:
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
            }
        }

        //Step 7:
        return d[n][m];
    }

    private static int min(final int a, final int b, final int c) {
        int m = a;
        if (b < m) {
            m = b;
        }
        if (c < m) {
            m = c;
        }
        return m;
    }

}