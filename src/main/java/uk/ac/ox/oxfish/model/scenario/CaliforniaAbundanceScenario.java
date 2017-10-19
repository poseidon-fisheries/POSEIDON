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

package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.NoiseMaker;
import uk.ac.ox.oxfish.biology.complicated.WeightedAbundanceDiffuser;
import uk.ac.ox.oxfish.biology.initializer.AllocatedBiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.*;
import uk.ac.ox.oxfish.fisher.selfanalysis.MovingAveragePredictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.FixedGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.CartesianUTMDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.AStarPathfinder;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.sampling.SampledMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.event.AbundanceDrivenFixedExogenousCatches;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.NetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.MultiQuotaMapFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.PortReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Reads the bathymetry file of california and for now not much else.
 * Created by carrknight on 5/7/15.
 */
public class CaliforniaAbundanceScenario extends CaliforniaAbstractScenario {

    private String countFileName = "count.csv";
    private boolean mortalityAt100PercentForOldestFish = true;
    /**
     * how much should the model biomass/abundance be given the data we read in?
     */
    private double biomassScaling = 1.0;



    private double sablefishDiffusingRate = 0;


    //old thinking:
    //1104.39389 liters of gasoline consumed each day
    //385.5864 kilometers a day if you cruise the whole time
    // = about 2.86 liter per kilometer
    //291.75 gallons consumed each day
    //10 miles per hour, 240 miles a day
    // 1.21 gallon per mile
    //These numbers however are higher than they should be because I am assuming fishers cruise
    //the whole time so I am just going to assume 1 gallon a day
    // 3.78541 liters / 1.60934 km
    // 2.352150571 liters per km


    //average diesel retail 2010
    //new FixedDoubleParameter(0.694094345);
    // from https://www.eia.gov/dnav/pet/hist/LeafHandler.ashx?n=PET&s=EMD_EPD2D_PTE_SCA_DPG&f=M


    {
        //numbers all come from stock assessment
        ((GarbageGearFactory) gear).setDelegate(
                new HeterogeneousGearFactory(
                        new Pair<>("Dover Sole",
                                   new DoubleNormalGearFactory(38.953, -1.483, 3.967,
                                                               -0.764, Double.NaN, -2.259,
                                                               0d, 50d, 1d, 26.962, 1.065, 0.869,
                                                               LITERS_OF_GAS_CONSUMED_PER_HOUR, DEFAULT_CATCHABILITY)),
                        new Pair<>("Longspine Thornyhead",
                                   new LogisticSelectivityGearFactory(23.5035,
                                                                      9.03702,
                                                                      21.8035,
                                                                      1.7773,
                                                                      0.992661,
                                                                      LITERS_OF_GAS_CONSUMED_PER_HOUR,
                                                                      DEFAULT_CATCHABILITY)),
                        //todo change this
                        new Pair<>("Sablefish",

                                   new SablefishGearFactory(DEFAULT_CATCHABILITY,
                                                            45.5128, 3.12457, 0.910947,
                                                            LITERS_OF_GAS_CONSUMED_PER_HOUR)
                        )
                        ,
                        new Pair<>("Shortspine Thornyhead",
                                   new DoubleNormalGearFactory(28.05,-0.3,4.25,
                                                               4.85,Double.NaN,Double.NaN,
                                                               0d,75d,1d,23.74,2.42,1d,
                                                               LITERS_OF_GAS_CONSUMED_PER_HOUR,
                                                               DEFAULT_CATCHABILITY)),
                        new Pair<>("Yelloweye Rockfish",
                                   new LogisticSelectivityGearFactory(36.364, 14.009,
                                                                      LITERS_OF_GAS_CONSUMED_PER_HOUR,
                                                                      DEFAULT_CATCHABILITY)
                        )

                )
        );
        //the proportion of garbage comes from DTS data from the catcher vesesl report
        ((GarbageGearFactory) gear).setGarbageSpeciesName(
                MultipleSpeciesAbundanceInitializer.FAKE_SPECIES_NAME
        );
        ((GarbageGearFactory) gear).setProportionSimulatedToGarbage(
                new FixedDoubleParameter(0.3221743)
        );
    }

    private MultipleSpeciesAbundanceInitializer initializer;


    private boolean fixedRecruitmentDistribution = false;

    /**
     * the multiplicative error to recruitment in a year. For now it applies to all species
     */
    private DoubleParameter recruitmentNoise = new FixedDoubleParameter(0);






    public CaliforniaAbundanceScenario() {


    }

    @Override
    protected GlobalBiology buildBiology(FishState model, LinkedHashMap<String, Path> folderMap) {
        GlobalBiology biology;
        initializer = new MultipleSpeciesAbundanceInitializer(folderMap,
                                                              biomassScaling,
                                                              fixedRecruitmentDistribution,
                                                              !mortalityAt100PercentForOldestFish,
                                                              true);
        initializer.setCountFileName(countFileName);

        biology = initializer.generateGlobal(model.getRandom(),
                                             model);


        model.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                for(Species thisSpecies : biology.getSpecies())
                {
                    DoubleParameter noise = recruitmentNoise.makeCopy();
                    initializer.getNaturalProcesses(thisSpecies).addNoise(
                            new NoiseMaker() {
                                @Override
                                public Double get() {
                                    return noise.apply(model.getRandom());
                                }
                            }

                    );
                }
            }

            @Override
            public void turnOff() {

            }
        });


        //diffusing
        if(sablefishDiffusingRate > 0)
        {

            model.registerStartable(new Startable() {
                @Override
                public void start(FishState model) {

                    Species sablefish = model.getBiology().getSpecie("Sablefish");
                    WeightedAbundanceDiffuser diffuser = new WeightedAbundanceDiffuser(
                            1,
                            sablefishDiffusingRate,
                            initializer.getInitialWeights(sablefish)
                    );
                    model.scheduleEveryDay(new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            diffuser.step(sablefish,initializer.getLocals(),model);
                        }
                    }, StepOrder.BIOLOGY_PHASE);
                }

                @Override
                public void turnOff() {

                }
            });


        }

        return biology;
    }


    /**
     * Getter for property 'biomassScaling'.
     *
     * @return Value for property 'biomassScaling'.
     */
    public double getBiomassScaling() {
        return biomassScaling;
    }

    /**
     * Setter for property 'biomassScaling'.
     *
     * @param biomassScaling Value to set for property 'biomassScaling'.
     */
    public void setBiomassScaling(double biomassScaling) {
        this.biomassScaling = biomassScaling;
    }


    public boolean isFixedRecruitmentDistribution() {
        return fixedRecruitmentDistribution;
    }

    public void setFixedRecruitmentDistribution(boolean fixedRecruitmentDistribution) {
        this.fixedRecruitmentDistribution = fixedRecruitmentDistribution;
    }

    /**
     * Getter for property 'recruitmentNoise'.
     *
     * @return Value for property 'recruitmentNoise'.
     */
    public DoubleParameter getRecruitmentNoise() {
        return recruitmentNoise;
    }

    /**
     * Setter for property 'recruitmentNoise'.
     *
     * @param recruitmentNoise Value to set for property 'recruitmentNoise'.
     */
    public void setRecruitmentNoise(DoubleParameter recruitmentNoise) {
        this.recruitmentNoise = recruitmentNoise;
    }


    @Override
    public AllocatedBiologyInitializer getBiologyInitializer() {
        return initializer;
    }

    @NotNull
    @Override
    protected ExogenousCatches turnIntoExogenousCatchesObject(
            HashMap<Species, Double> catchesPerSpecies) {
        return new AbundanceDrivenFixedExogenousCatches(catchesPerSpecies);
    }

    /**
     * Getter for property 'mortalityAt100PercentForOldestFish'.
     *
     * @return Value for property 'mortalityAt100PercentForOldestFish'.
     */
    public boolean isMortalityAt100PercentForOldestFish() {
        return mortalityAt100PercentForOldestFish;
    }

    /**
     * Setter for property 'mortalityAt100PercentForOldestFish'.
     *
     * @param mortalityAt100PercentForOldestFish Value to set for property 'mortalityAt100PercentForOldestFish'.
     */
    public void setMortalityAt100PercentForOldestFish(boolean mortalityAt100PercentForOldestFish) {
        this.mortalityAt100PercentForOldestFish = mortalityAt100PercentForOldestFish;
    }

    /**
     * Getter for property 'sablefishDiffusingRate'.
     *
     * @return Value for property 'sablefishDiffusingRate'.
     */
    public double getSablefishDiffusingRate() {
        return sablefishDiffusingRate;
    }

    /**
     * Setter for property 'sablefishDiffusingRate'.
     *
     * @param sablefishDiffusingRate Value to set for property 'sablefishDiffusingRate'.
     */
    public void setSablefishDiffusingRate(double sablefishDiffusingRate) {
        this.sablefishDiffusingRate = sablefishDiffusingRate;
    }

    /**
     * Getter for property 'countFileName'.
     *
     * @return Value for property 'countFileName'.
     */
    public String getCountFileName() {
        return countFileName;
    }

    /**
     * Setter for property 'countFileName'.
     *
     * @param countFileName Value to set for property 'countFileName'.
     */
    public void setCountFileName(String countFileName) {
        this.countFileName = countFileName;
    }



}



