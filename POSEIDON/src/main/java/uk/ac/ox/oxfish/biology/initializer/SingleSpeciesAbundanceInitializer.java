/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.InitialAbundanceFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.MeristicsFileFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;

/**
 * A biology initializer that creates a one species model with abundance biology splitting the population
 * according to biomassAllocator
 * Created by carrknight on 3/11/16.
 */
public class SingleSpeciesAbundanceInitializer implements BiologyInitializer {


    /**
     * object containing the number of initial fish in the ocean!
     */
    final private InitialAbundance initialAbundance;

    /**
     * object allocating biomass around when simulation starts
     */
    final private BiomassAllocator intialAbundanceAllocator;


    final private AgingProcess aging;

    final private NaturalMortalityProcess mortality;


    /**
     * creates the weights/lengths of fish we are studying
     */
    final private Meristics meristics;

    /**
     * the name of the species
     */
    private final String speciesName;

    /**
     * scales down/up the number of fish present by multiplying it by scaling
     */
    private final double scaling;

    private final RecruitmentProcess recruitmentProcess;


    private final AbundanceDiffuser diffuser;

    /**
     * By default (if this is null) any area where there is no fish initially is marked as wasteland;
     * If this is not null, then even if there is no fish initially an area where this allocator returns > 0
     * will be livable
     */
    final private BiomassAllocator habitabilityAllocator;


    /**
     * possibly null allocator to choose where recruits go
     */
    private final BiomassAllocator recruitmentAllocator;
    private final boolean daily;
    private final boolean rounding;
    private SingleSpeciesNaturalProcesses processes;
    /**
     * weights to each location
     */
    private final Map<SeaTile, Double> initialWeights = new HashMap<>();

    public SingleSpeciesAbundanceInitializer(
        final String speciesName,
        final InitialAbundance initialAbundance,
        final BiomassAllocator intialAbundanceAllocator,
        final AgingProcess aging,
        final Meristics meristics,
        final double scaling,
        final RecruitmentProcess recruitmentProcess,
        final AbundanceDiffuser diffuser,
        final BiomassAllocator recruitmentAllocator,
        final BiomassAllocator habitabilityAllocator,
        final NaturalMortalityProcess mortality, final boolean daily, final boolean rounding
    ) {
        this.initialAbundance = initialAbundance;
        this.intialAbundanceAllocator = intialAbundanceAllocator;
        this.aging = aging;
        this.meristics = meristics;
        this.speciesName = speciesName;
        this.scaling = scaling;
        this.recruitmentProcess = recruitmentProcess;
        this.diffuser = diffuser;
        this.recruitmentAllocator = recruitmentAllocator;
        this.habitabilityAllocator = habitabilityAllocator;
        this.mortality = mortality;
        this.daily = daily;
        this.rounding = rounding;
    }


    /**
     * this is the default "read from files" california stock assessment constructor
     *
     * @param biologicalDirectory
     * @param speciesName
     * @param scaling
     * @param state
     */
    public SingleSpeciesAbundanceInitializer(
        final Path biologicalDirectory, final String speciesName, final double scaling, final FishState state
    ) {
        initialAbundance = new InitialAbundanceFromFileFactory(
            biologicalDirectory.resolve("count.csv")).apply(state);
        final StockAssessmentCaliforniaMeristics
            cali = new MeristicsFileFactory(
            biologicalDirectory.resolve("meristics.yaml")
        ).apply(state);
        meristics = cali;
        recruitmentProcess = new RecruitmentBySpawningBiomass(
            cali.getVirginRecruits(),
            cali.getSteepness(),
            cali.getCumulativePhi(),
            cali.isAddRelativeFecundityToSpawningBiomass(),
            cali.getMaturity(),
            cali.getRelativeFecundity(),
            FEMALE, false
        );
        aging = new StandardAgingProcess(false);

        this.daily = false;
        intialAbundanceAllocator = new ConstantBiomassAllocator();
        this.speciesName = speciesName;
        this.scaling = scaling;
        this.mortality = new ExponentialMortalityProcess(
            cali
        );
        this.diffuser = new NoAbundanceDiffusion();
        this.recruitmentAllocator = null;
        this.habitabilityAllocator = null;
        this.rounding = true;
    }


    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
        final GlobalBiology biology, final SeaTile seaTile, final MersenneTwisterFast random, final int mapHeightInCells,
        final int mapWidthInCells, final NauticalMap map
    ) {


        if (seaTile.isLand())
            return new EmptyLocalBiology();
        //weight we want to allocate to this area
        final double weight = intialAbundanceAllocator.allocate(
            seaTile,
            map,
            random
        );
        //weights of 0 or below are wastelands
        if (!Double.isFinite(weight) || weight <= 0 || (habitabilityAllocator != null && habitabilityAllocator.allocate(
            seaTile,
            map,
            random
        ) <= 0))
            return new EmptyLocalBiology();
        else {
            final AbundanceLocalBiology local = new AbundanceLocalBiology(biology);
            initialWeights.put(seaTile, weight);
            return local;
        }
    }


    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
        final GlobalBiology biology,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final FishState model
    ) {


        final Species species = biology.getSpeciesByCaseInsensitiveName(speciesName);

        //read in the total number of fish
        initialAbundance.initialize(species);
        final double[][] totalCount = initialAbundance.getInitialAbundance();
        assert totalCount.length == species.getNumberOfSubdivisions();
        Preconditions.checkArgument(
            totalCount[0].length == species.getNumberOfBins(),
            "mismatch between size of initial abundance and maxAge of species"
        );


        //now the count is complete, let's distribute these fish uniformly throughout the seatiles


        final double sum = initialWeights.values().stream().mapToDouble(
            value -> value
        ).sum();


        //create the natural process
        processes = new SingleSpeciesNaturalProcesses(
            recruitmentProcess,
            species,
            rounding, aging,
            diffuser,
            mortality, daily
        );
        if (recruitmentAllocator != null)
            processes.setRecruitsAllocator(recruitmentAllocator);


        final DataColumn recruitmentColumn = model.getDailyDataSet().registerGatherer(
            speciesName + " Recruits",
            (Gatherer<FishState>) fishState -> processes.getLastRecruits(),
            0d
        );

        model.getYearlyDataSet().registerGatherer(
            speciesName + " Recruits",
            FishStateUtilities.generateYearlySum(recruitmentColumn),
            0d
        );


        /**
         * this used to be collected when generating, but with other initializers it might not
         * be the case that what you generated was used; to be super-safe then we generate this year
         */
        for (final SeaTile tile : map.getAllSeaTilesExcludingLandAsList()) {
            if (!initialWeights.containsKey(tile))
                continue;
            final double ratio = initialWeights.get(tile) / sum;

            //cast is justified because we put it in ourselves!
            assert tile.getBiology() instanceof AbundanceLocalBiology;
            processes.add((AbundanceLocalBiology) tile.getBiology(), tile);
            final StructuredAbundance abundance = tile.getBiology().getAbundance(species);
            for (int bin = 0; bin < abundance.getBins(); bin++)

                for (int subdivision = 0; subdivision < abundance.getSubdivisions(); subdivision++) {
                    abundance.asMatrix()[subdivision][bin] =
                        scaling * totalCount[subdivision][bin] * ratio;
                }

        }


        //start it!
        model.registerStartable(processes);

    }


    /**
     * creates the global biology object for the model
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(
        final MersenneTwisterFast random, final FishState modelBeingInitialized
    ) {

        final Species species = new Species(speciesName, meristics);
        return new GlobalBiology(species);

    }


    /**
     * Getter for property 'mortality'.
     *
     * @return Value for property 'mortality'.
     */
    public NaturalMortalityProcess getMortality() {
        return mortality;
    }

    /**
     * Getter for property 'processes'.
     *
     * @return Value for property 'processes'.
     */
    @VisibleForTesting
    public SingleSpeciesNaturalProcesses getProcesses() {
        return processes;
    }


    public InitialAbundance getInitialAbundance() {
        return initialAbundance;
    }

    public BiomassAllocator getIntialAbundanceAllocator() {
        return intialAbundanceAllocator;
    }

    public AgingProcess getAging() {
        return aging;
    }

    public Meristics getMeristics() {
        return meristics;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public double getScaling() {
        return scaling;
    }

    public RecruitmentProcess getRecruitmentProcess() {
        return recruitmentProcess;
    }

    public AbundanceDiffuser getDiffuser() {
        return diffuser;
    }

    public BiomassAllocator getHabitabilityAllocator() {
        return habitabilityAllocator;
    }

    public BiomassAllocator getRecruitmentAllocator() {
        return recruitmentAllocator;
    }

    public boolean isDaily() {
        return daily;
    }

    public boolean isRounding() {
        return rounding;
    }

    public Map<SeaTile, Double> getInitialWeights() {
        return initialWeights;
    }
}
