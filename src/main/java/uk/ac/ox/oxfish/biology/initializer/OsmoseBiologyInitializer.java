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

import ec.util.MersenneTwisterFast;
import fr.ird.osmose.OsmoseSimulation;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.osmose.LocalOsmoseWithoutRecruitmentBiology;
import uk.ac.ox.oxfish.geography.osmose.OsmoseStepper;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Creates an osmose biology (including the the simulation link and the stepper)
 * Created by carrknight on 11/5/15.
 */
public class OsmoseBiologyInitializer implements BiologyInitializer {

    /**
     * Path to the main configuration file in OSMOSE
     */
    private final String osmoseConfigurationFile;

    /**
     * if true, don't start a new simulation but grab a random pre-initialized configuration
     */
    private final boolean preInitializedConfiguration;

    /**
     * the configuration directory where there are pre-initialized configs
     */
    private final String preInitializedConfigurationDirectory;


    /**
     * years to burn-in if we start simulation from scratch
     */
    private final int burnInYears;

    /**
     * species osmose shouldn't fish because we are fishing ourselves!
     */
    private final Integer[] speciesToManageFromThisSide;


    private final HashMap<Integer,Integer> recruitmentAges;

    private final double scalingFactor;
    /**
     * mortality rates for discarded fish. When not specified, the discard rate is 100% (1d)
     */
    private final HashMap<Integer, Double> discardMortalityRate
            ;


    public OsmoseBiologyInitializer(
            String osmoseConfigurationFile, boolean preInitializedConfiguration,
            String preInitializedConfigurationDirectory, int burnInYears,
            double scalingFactor, HashMap<Integer,Integer> recruitmentAges,
            HashMap<Integer,Double> discardMortalityRate,
            Integer... speciesToManage) {
        this.osmoseConfigurationFile = osmoseConfigurationFile;
        this.preInitializedConfiguration = preInitializedConfiguration;
        this.preInitializedConfigurationDirectory = preInitializedConfigurationDirectory;
        this.burnInYears = burnInYears;
        this.recruitmentAges = recruitmentAges;
        this.speciesToManageFromThisSide = speciesToManage;
        this.scalingFactor = scalingFactor;
        this.discardMortalityRate = discardMortalityRate;

    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *  @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells, NauticalMap map) {
        OsmoseSimulation simulation = ((OsmoseGlobalBiology) biology).getSimulation();

        if(seaTile.getAltitude()>0)
            return new EmptyLocalBiology();



        final int height = simulation.getMap().get_ny(); //needs height because OSMOSE map is reversed
        final int x = seaTile.getGridX();
        final int y = seaTile.getGridY();

        int recruitments[] = new int[biology.getSpecies().size()];
        for(Map.Entry<Integer,Integer> pair : recruitmentAges.entrySet())
            recruitments[pair.getKey()] = pair.getValue();

        double discardMortality[] = new double[biology.getSize()];
        Arrays.fill(discardMortality,1d); //by default mortality is 100%
        for(Map.Entry<Integer,Double> pair : discardMortalityRate.entrySet())
            discardMortality[pair.getKey()] = pair.getValue();

        final LocalOsmoseWithoutRecruitmentBiology local =
                new LocalOsmoseWithoutRecruitmentBiology(simulation.getMortality(),
                                                         simulation.getCounter().getBiomass(x, height-y-1),
                                                         scalingFactor,
                                                         recruitments,
                                                         discardMortality);
        ((OsmoseGlobalBiology) biology).getStepper().getToReset().add(local);

        return local;

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
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {

        model.registerStartable(((OsmoseGlobalBiology) biology).getStepper());




    }

    /**
     * creates the osmose simulation and link
     *
     * @param random                the random number generator
     * @param model the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(MersenneTwisterFast random, FishState model) {

        OsmoseSimulation osmoseSimulation;

        //either read in a pre-made OSMOSE start or spinup
        try {
            if(!preInitializedConfiguration)
                osmoseSimulation = OsmoseSimulation.startUpOSMOSESimulationWithBurnIn(burnInYears,
                                                                                      osmoseConfigurationFile);
            else
            {
                ArrayList<Path> fileList = new ArrayList<>();
                Files.walk(Paths.get(preInitializedConfigurationDirectory), 1).filter(
                        path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".nc")
                ).forEach(fileList::add);

                osmoseSimulation = OsmoseSimulation.startupOSMOSEWithRestartFile(12,osmoseConfigurationFile,
                                                                                 fileList.get(model.getRandom().nextInt(fileList.size())).toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw  new IllegalArgumentException("Can't instantiate OSMOSE! \n" );
        }

        //activate exogenous (with respect to OSMOSE) mortality for the right species
        for(Integer speciesIndex : speciesToManageFromThisSide  )
            osmoseSimulation.getMortality().markThisSpeciesAsExogenous(speciesIndex);

        //grab all the species
        final Species[] species = new Species[osmoseSimulation.getNumberOfSpecies()];
        for(int i=0; i<species.length; i++)
            species[i] = new Species(osmoseSimulation.getSpecies(i).getName());
        System.out.println(Arrays.toString(species));

        //set up the stepper
        final OsmoseStepper stepper = new OsmoseStepper(model.getStepsPerDay() * 365, osmoseSimulation, model.random);



        //for species where there is a recruitment age, add data tracker on real biomass (since the default one ignores juveniles)
        if(!recruitmentAges.isEmpty())
        {
            model.registerStartable(new Startable() {
                @Override
                public void start(FishState model) {
                    for (Integer index : recruitmentAges.keySet()) {
                    //for (int index = 0; index<species.length; index++) {
                        //add daily gatherer
                        //notice that it grabs the list of local biology from the stepper (since that's one less cast)
                        int finalIndex = index;
                        //the datagatherer checks instantaneous biomass
                        Gatherer<FishState> dataGatherer = new Gatherer<FishState>() {
                            @Override
                            public Double apply(FishState state) {
                                ToDoubleFunction<LocalOsmoseWithoutRecruitmentBiology> getter =
                                        new ToDoubleFunction<LocalOsmoseWithoutRecruitmentBiology>() {
                                            @Override
                                            public double applyAsDouble(
                                                    LocalOsmoseWithoutRecruitmentBiology value) {
                                                return value.getBiomassIncludingJuveniles(species[finalIndex]);
                                            }
                                        };
                                return stepper.getToReset().stream().mapToDouble(
                                        getter).sum();
                            }
                        };
                        //add it daily
                        DataColumn dailyGatherer = model.getDailyDataSet().registerGatherer(
                                "Total Biomass " + species[index].getName(),
                                dataGatherer,
                                Double.NaN
                        );
                        //add it yearly
                        model.getYearlyDataSet().registerGatherer(
                                "Total Biomass " + species[index].getName(),
                                new Gatherer<FishState>() {
                                    @Override
                                    public Double apply(FishState state) {
                                        return dailyGatherer.getLatest();
                                    }
                                },
                                Double.NaN);
                    }
                }

                @Override
                public void turnOff() {

                }
            });
        }

        return new OsmoseGlobalBiology(osmoseSimulation,stepper,species);

    }

    /**
     * Getter for property 'discardMortalityRate'.
     *
     * @return Value for property 'discardMortalityRate'.
     */
    public HashMap<Integer, Double> getDiscardMortalityRate() {
        return discardMortalityRate;
    }
}
