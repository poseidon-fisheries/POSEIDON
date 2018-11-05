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

package uk.ac.ox.oxfish.utility.yaml;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.maximization.generic.CommaMapOptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.factory.Regulations;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.SelectDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class FishYAMLTest {


    @Test
    public void canReadAScenario() throws Exception
    {

        String scenarioFile =
                "Abstract:\n" +
                        "  biologyInitializer:\n" +
                        "    Diffusing Logistic:\n" +
                        "      carryingCapacity: '14.0'\n" +
                        "      differentialPercentageToMove: '0.001'\n" +
                        "      percentageLimitOnDailyMovement: select 0.2 0.5 \n" +
                        "  departingStrategy:\n" +
                        "    Fixed Rest:\n" +
                        "      hoursBetweenEachDeparture: '12.0'\n" +
                        "  destinationStrategy:\n" +
                        "    Imitator-Explorator:\n" +
                        "      ignoreEdgeDirection: true\n" +
                        "      probability:\n" +
                        "        Adaptive Probability:\n" +
                        "          explorationProbability: '0.8'\n" +
                        "          explorationProbabilityMinimum: '0.01'\n" +
                        "          imitationProbability: '1.0'\n" +
                        "          incrementMultiplier: '0.02'\n" +
                        "      stepSize: uniform 1.0 10.0\n" +
                        "  enginePower: normal 100.0 10.0\n" +
                        "  fishers: 100\n" +
                        "  fishingStrategy:\n" +
                        "    Until Full With Day Limit:\n" +
                        "      daysAtSea: '5.0'\n" +
                        "  fuelTankSize: '100000.0'\n" +
                        "  gasPricePerLiter: '0.01'\n" +
                        "  gear:\n" +
                        "    Random Catchability:\n" +
                        "      meanCatchabilityFirstSpecies: '0.01'\n" +
                        "      meanCatchabilityOtherSpecies: '0.01'\n" +
                        "      standardDeviationCatchabilityFirstSpecies: '0.0'\n" +
                        "      standardDeviationCatchabilityOtherSpecies: '0.0'\n" +
                        "      gasPerHourFished: '5.0'\n" +
                        "  habitatInitializer: All Sand\n" +
                        "  holdSize: '100.0'\n" +
                        "  literPerKilometer: '10.0'\n" +
                        "  mapInitializer: !!uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory\n" +
                        "    cellSizeInKilometers: '10.0'\n" +
                        "    coastalRoughness: '4.0'\n" +
                        "    depthSmoothing: '1000000.0'\n" +
                        "    height: '50.0'\n" +
                        "    width: '50.0'\n" +
                        "  mapMakerDedicatedRandomSeed: null\n" +
                        "  market:\n" +
                        "    Fixed Price Market:\n" +
                        "      marketPrice: '10.0'\n" +
                        "  networkBuilder:\n" +
                        "    Equal Out Degree:\n" +
                        "      degree: 2\n" +
                        "  ports: 1\n" +
                        "  regulation: Anarchy\n" +
                        "  speedInKmh: '5.0'\n" +
                        "  usePredictors: false\n" +
                        "  weatherInitializer:\n" +
                        "    Constant Weather:\n" +
                        "      temperature: '30.0'\n" +
                        "      windOrientation: '0.0'\n" +
                        "      windSpeed: '0.0'\n" +
                        "  weatherStrategy: Ignore Weather";

        FishYAML yaml = new FishYAML();
        final Object loaded = yaml.loadAs(scenarioFile, Scenario.class);
        //read prototype scenario correctly
        Assert.assertTrue(loaded instanceof PrototypeScenario);
        PrototypeScenario scenario = (PrototypeScenario) loaded;
        //read initializer correctly
        Assert.assertTrue(scenario.getBiologyInitializer() instanceof DiffusingLogisticFactory);
        DiffusingLogisticFactory factory = (DiffusingLogisticFactory) scenario.getBiologyInitializer();
        //reads double parameters correctly
        Assert.assertTrue(factory.getCarryingCapacity() instanceof FixedDoubleParameter);
        Assert.assertEquals(((FixedDoubleParameter) factory.getCarryingCapacity()).getFixedValue(),14.0,.001);
        //reads normal doubles correctly
        double[] possibleValues = ((SelectDoubleParameter) factory.getPercentageLimitOnDailyMovement()).getPossibleValues();
        Assert.assertEquals(possibleValues[0], .2, .0001);
        Assert.assertEquals(possibleValues[1], .5, .0001);
        //reads anarchy factory just as well (it's a scalar algorithmFactory which is tricky)
        Assert.assertTrue(scenario.getRegulation() instanceof AnarchyFactory);

    }


    @Test
    public void writePrettilyAndReadBack() throws Exception {

        DiffusingLogisticFactory factory = new DiffusingLogisticFactory();
        factory.setCarryingCapacity(new NormalDoubleParameter(10000, 10));
        factory.setPercentageLimitOnDailyMovement(new UniformDoubleParameter(0, 10));
        factory.setDifferentialPercentageToMove(new FixedDoubleParameter(.001));
        FishYAML yaml = new FishYAML();
        final String dumped = yaml.dump(factory);
        System.out.println(dumped);

        //test pretty printing
        Assert.assertTrue(dumped.contains("percentageLimitOnDailyMovement: uniform 0.0 10.0"));
        Assert.assertTrue(dumped.contains("carryingCapacity: normal 10000.0 10.0"));

        //now read it back! (notice that I need to do "loadAs" because when writing prettily the factory gets written
        //as a map; that's not an issue in scenarios because the constructor knows where factories ought to be but when
        //the factory is written without any warning that it's going to be an AlgorithmFactory then things go badly
        DiffusingLogisticFactory factory2 = yaml.loadAs(dumped, DiffusingLogisticFactory.class);
        assertEquals(((NormalDoubleParameter) factory2.getCarryingCapacity()).getMean(), 10000, .001);
        assertEquals(((NormalDoubleParameter) factory2.getCarryingCapacity()).getStandardDeviation(), 10, .001);

    }

    @Test
    public void writePrettilyAllSortsOfScenarios()
    {
        PrototypeScenario scenario = new PrototypeScenario();
        ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setDifferentialPercentageToMove(new FixedDoubleParameter(.9));
        scenario.setRegulation(Regulations.CONSTRUCTORS.get("MPA Only").get());
        FishYAML yaml = new FishYAML();
        final String dumped = yaml.dump(scenario);
        //load back! Notice that because it's made "pretty" I still have to call loadAs
        Scenario scenario2 = yaml.loadAs(dumped, Scenario.class);
        Assert.assertTrue(scenario2 instanceof PrototypeScenario);

        //make sure it remembers that the regulations have changed
        Assert.assertTrue(((PrototypeScenario) scenario2).getRegulation() instanceof ProtectedAreasOnlyFactory);
        //make sure three recursions in this is still correct.
        Assert.assertEquals(
                ((FixedDoubleParameter) ((DiffusingLogisticFactory) ((PrototypeScenario) scenario2).getBiologyInitializer()).getDifferentialPercentageToMove()).getFixedValue(),.9,.0001);


        //final test, if I redump you, it'll be exactly like before
        String dump2 = yaml.dump(scenario2);
        Assert.assertEquals(dumped,dump2);
    }



    @Test
    public void canNavigateThroughObject()
    {
        Yaml yaml = new Yaml();
        Map<String,Object> read = (Map<String, Object>)
                yaml.load("Flexible:\n" +
                                                    "  allowFriendshipsAcrossPorts: true\n" +
                                                    "  biologyInitializer:\n" +
                                                    "    Multiple Species Biomass:\n" +
                                                    "      constantBiomass: false\n" +
                                                    "      factories:\n" +
                                                    "      - Single Species Biomass Normalized:\n" +
                                                    "          biomassSuppliedPerCell: false\n" +
                                                    "          carryingCapacity: '159793986'\n" +
                                                    "          differentialPercentageToMove: '0.0'\n" +
                                                    "          grower:\n" +
                                                    "            Common Logistic Grower:\n" +
                                                    "              steepness: '0.269'\n" +
                                                    "          initialBiomassAllocator:\n" +
                                                    "            Random Allocator:\n" +
                                                    "              maximum: '0.1546'\n" +
                                                    "              minimum: '0.1546'\n" +
                                                    "          initialCapacityAllocator:\n" +
                                                    "            Shape File Allocator:\n" +
                                                    "              delegate:\n" +
                                                    "                From File Allocator:\n" +
                                                    "                  biomassPath: ./docs/indonesia_hub/runs/712/malabaricus_pixellated_map.csv\n" +
                                                    "                  inputFileHasHeader: true\n" +
                                                    "              insidePoligon: true\n" +
                                                    "              shapeFile: ./docs/indonesia_hub/runs/712/shape/WPP_boundary.shp   \n" +
                                                    "          percentageLimitOnDailyMovement: '0.0'\n" +
                                                    "          speciesName: Lutjanus malabaricus\n" +
                                                    "          unfishable: false\n" +
                                                    "      - Single Species Biomass Normalized:\n" +
                                                    "          biomassSuppliedPerCell: false\n" +
                                                    "          carryingCapacity: '30869208'\n" +
                                                    "          differentialPercentageToMove: '0.0'\n" +
                                                    "          grower:\n" +
                                                    "            Common Logistic Grower:\n" +
                                                    "              steepness: '0.6170723'\n" +
                                                    "          initialBiomassAllocator:\n" +
                                                    "            Random Allocator:\n" +
                                                    "              maximum: '0.2766'\n" +
                                                    "              minimum: '0.2766'\n" +
                                                    "          initialCapacityAllocator:\n" +
                                                    "            Shape File Allocator:\n" +
                                                    "              delegate:\n" +
                                                    "                From File Allocator:\n" +
                                                    "                  biomassPath: ./docs/indonesia_hub/runs/712/multidens_pixellated_map.csv\n" +
                                                    "                  inputFileHasHeader: true\n" +
                                                    "              insidePoligon: true\n" +
                                                    "              shapeFile: ./docs/indonesia_hub/runs/712/shape/WPP_boundary.shp   \n" +
                                                    "          percentageLimitOnDailyMovement: '0.0'\n" +
                                                    "          speciesName: Pristipomoides multidens\n" +
                                                    "          unfishable: false    \n" +
                                                    "      - Single Species Biomass Normalized:\n" +
                                                    "          biomassSuppliedPerCell: false\n" +
                                                    "          carryingCapacity: '5738469'\n" +
                                                    "          differentialPercentageToMove: '0.0'\n" +
                                                    "          grower:\n" +
                                                    "            Common Logistic Grower:\n" +
                                                    "              steepness: '0.3010377'\n" +
                                                    "          initialBiomassAllocator:\n" +
                                                    "            Random Allocator:\n" +
                                                    "              maximum: '0.787981777'\n" +
                                                    "              minimum: '0.787981777'\n" +
                                                    "          initialCapacityAllocator:\n" +
                                                    "            Shape File Allocator:\n" +
                                                    "              delegate:\n" +
                                                    "                From File Allocator:\n" +
                                                    "                  biomassPath: ./docs/indonesia_hub/runs/712/areolatus_pixellated_map.csv\n" +
                                                    "                  inputFileHasHeader: true\n" +
                                                    "              insidePoligon: true\n" +
                                                    "              shapeFile: ./docs/indonesia_hub/runs/712/shape/WPP_boundary.shp   \n" +
                                                    "          percentageLimitOnDailyMovement: '0.0'\n" +
                                                    "          speciesName: Epinephelus areolatus\n" +
                                                    "          unfishable: false   \n" +
                                                    "      - Single Species Biomass Normalized:\n" +
                                                    "          biomassSuppliedPerCell: false\n" +
                                                    "          carryingCapacity: '3670001'\n" +
                                                    "          differentialPercentageToMove: '0.0'\n" +
                                                    "          grower:\n" +
                                                    "            Common Logistic Grower:\n" +
                                                    "              steepness: '0.6831402'\n" +
                                                    "          initialBiomassAllocator:\n" +
                                                    "            Random Allocator:\n" +
                                                    "              maximum: '0.787981777'\n" +
                                                    "              minimum: '0.787981777'\n" +
                                                    "          initialCapacityAllocator:\n" +
                                                    "            Shape File Allocator:\n" +
                                                    "              delegate:\n" +
                                                    "                From File Allocator:\n" +
                                                    "                  biomassPath: ./docs/indonesia_hub/runs/712/erythropterus_pixellated_map.csv\n" +
                                                    "                  inputFileHasHeader: true\n" +
                                                    "              insidePoligon: true\n" +
                                                    "              shapeFile: ./docs/indonesia_hub/runs/712/shape/WPP_boundary.shp   \n" +
                                                    "          percentageLimitOnDailyMovement: '0.0'\n" +
                                                    "          speciesName: Lutjanus erythropterus\n" +
                                                    "          unfishable: false            \n" +
                                                    "  cheaters: false\n" +
                                                    "  exogenousCatches:\n" +
                                                    "    Lutjanus erythropterus: 98348.40\n" +
                                                    "    Lutjanus malabaricus: 501163.14\n" +
                                                    "    Pristipomoides multidens: 18686.26\n" +
                                                    "    Epinephelus areolatus: 41136.48\n" +
                                                    "  fisherDefinitions:\n" +
                                                    "  - departingStrategy:\n" +
                                                    "      Max Hours Per Year:\n" +
                                                    "        maxHoursOut: '4800.0'\n" +
                                                    "    destinationStrategy:\n" +
                                                    "      Imitator-Explorator:\n" +
                                                    "        alwaysCopyBest: false\n" +
                                                    "        automaticallyIgnoreAreasWhereFishNeverGrows: true\n" +
                                                    "        automaticallyIgnoreMPAs: true\n" +
                                                    "        backtracksOnBadExploration: true\n" +
                                                    "        dropInUtilityNeededForUnfriend: '-1.0'\n" +
                                                    "        ignoreEdgeDirection: true\n" +
                                                    "        ignoreFailedTrips: false\n" +
                                                    "        maxInitialDistance: 378.0\n" +
                                                    "        objectiveFunction:\n" +
                                                    "          Simulated Profit Objective:\n" +
                                                    "            tripLength: '288.0'\n" +
                                                    "        probability:\n" +
                                                    "          Fixed Probability:\n" +
                                                    "            explorationProbability: uniform 0.05 0.5\n" +
                                                    "            imitationProbability: '1.0'\n" +
                                                    "        stepSize: uniform 1.0 10.0\n" +
                                                    "    discardingStrategy: No Discarding\n" +
                                                    "    fishingStrategy:\n" +
                                                    "      Until Full With Day Limit:\n" +
                                                    "        daysAtSea: '12.0'\n" +
                                                    "    fuelTankSize: '100000.0'\n" +
                                                    "    gear:\n" +
                                                    "      Hold Upper Limit:\n" +
                                                    "        delegate:\n" +
                                                    "          Garbage Gear:\n" +
                                                    "            delegate:\n" +
                                                    "              Random Catchability By List:\n" +
                                                    "                catchabilityMap: '0:0.0016,1:0.0016,2:0.0016,3:0.0016'\n" +
                                                    "                standardDeviationMap: ''\n" +
                                                    "                trawlSpeed: '5.0'\n" +
                                                    "            garbageSpeciesName: Others\n" +
                                                    "            proportionSimulatedToGarbage: '0.3'\n" +
                                                    "            rounding: false\n" +
                                                    "    gearStrategy: Never Change Gear\n" +
                                                    "    holdSize: '1940.0'\n" +
                                                    "    hourlyVariableCost: '46381.0'\n" +
                                                    "    initialFishersPerPort:\n" +
                                                    "      Sumenep: 454\n" +
                                                    "      Gili Iyang: 2\n" +
                                                    "      Brondong: 163\n" +
                                                    "      Tanjung Pandan: 16\n" +
                                                    "    literPerKilometer: '1.2669'\n" +
                                                    "    logbook: No Logbook\n" +
                                                    "    regulation: Anarchy\n" +
                                                    "    speedInKmh: '13.0'\n" +
                                                    "    tags: small,dropline,yellow,canoe\n" +
                                                    "    usePredictors: false\n" +
                                                    "    weatherStrategy: Ignore Weather\n" +
                                                    "  - departingStrategy:\n" +
                                                    "      Max Hours Per Year:\n" +
                                                    "        maxHoursOut: '4800.0'\n" +
                                                    "    destinationStrategy:\n" +
                                                    "      Imitator-Explorator:\n" +
                                                    "        alwaysCopyBest: false\n" +
                                                    "        automaticallyIgnoreAreasWhereFishNeverGrows: true\n" +
                                                    "        automaticallyIgnoreMPAs: true\n" +
                                                    "        backtracksOnBadExploration: true\n" +
                                                    "        dropInUtilityNeededForUnfriend: '-1.0'\n" +
                                                    "        ignoreEdgeDirection: true\n" +
                                                    "        ignoreFailedTrips: false\n" +
                                                    "        maxInitialDistance: 551.0\n" +
                                                    "        objectiveFunction:\n" +
                                                    "          Simulated Profit Objective:\n" +
                                                    "            tripLength: '288.0'\n" +
                                                    "        probability:\n" +
                                                    "          Fixed Probability:\n" +
                                                    "            explorationProbability: uniform 0.05 0.5\n" +
                                                    "            imitationProbability: '1.0'\n" +
                                                    "        stepSize: uniform 1.0 10.0\n" +
                                                    "    discardingStrategy: No Discarding\n" +
                                                    "    fishingStrategy:\n" +
                                                    "      Until Full With Day Limit:\n" +
                                                    "        daysAtSea: '12.0'\n" +
                                                    "    fuelTankSize: '100000.0'\n" +
                                                    "    gear:\n" +
                                                    "      Hold Upper Limit:\n" +
                                                    "        delegate:\n" +
                                                    "          Garbage Gear:\n" +
                                                    "            delegate:\n" +
                                                    "              Random Catchability By List:\n" +
                                                    "                catchabilityMap: '0:0.0016,1:0.0016,2:0.0016,3:0.0016'\n" +
                                                    "                standardDeviationMap: ''\n" +
                                                    "                trawlSpeed: '5.0'\n" +
                                                    "            garbageSpeciesName: Others\n" +
                                                    "            proportionSimulatedToGarbage: '0.3'\n" +
                                                    "            rounding: false\n" +
                                                    "    gearStrategy: Never Change Gear\n" +
                                                    "    holdSize: '6900.0'\n" +
                                                    "    hourlyVariableCost: '106929.0'\n" +
                                                    "    initialFishersPerPort:\n" +
                                                    "      Bajomulyo: 54\n" +
                                                    "      Karangsong: 5\n" +
                                                    "      Probolinggo: 9\n" +
                                                    "    literPerKilometer: '1.44'\n" +
                                                    "    logbook: No Logbook\n" +
                                                    "    regulation: Anarchy\n" +
                                                    "    speedInKmh: '13.0'\n" +
                                                    "    tags: blue,longline,boat,medium\n" +
                                                    "    usePredictors: false\n" +
                                                    "    weatherStrategy: Ignore Weather\n" +
                                                    "  - departingStrategy:\n" +
                                                    "      Max Hours Per Year:\n" +
                                                    "        maxHoursOut: '4800.0'\n" +
                                                    "    destinationStrategy:\n" +
                                                    "      Perfect RPUE Logit:\n" +
                                                    "        automaticallyAvoidMPA: true\n" +
                                                    "        automaticallyAvoidWastelands: true\n" +
                                                    "        discretizer:\n" +
                                                    "          Squared Discretization:\n" +
                                                    "            horizontalSplits: '10.0'\n" +
                                                    "            verticalSplits: '10.0'\n" +
                                                    "        hoursOut: '1440.0'\n" +
                                                    "        profitBeta: '1.0'\n" +
                                                    "    discardingStrategy: No Discarding\n" +
                                                    "    fishingStrategy:\n" +
                                                    "      Until Full With Day Limit:\n" +
                                                    "        daysAtSea: '60.0'\n" +
                                                    "    fuelTankSize: '100000.0'\n" +
                                                    "    gear:\n" +
                                                    "      Hold Upper Limit:\n" +
                                                    "        delegate:\n" +
                                                    "          Garbage Gear:\n" +
                                                    "            delegate:\n" +
                                                    "              Random Catchability By List:\n" +
                                                    "                catchabilityMap: '0:0.0016,1:0.0016,2:0.0016,3:0.0016'\n" +
                                                    "                standardDeviationMap: ''\n" +
                                                    "                trawlSpeed: '5.0'\n" +
                                                    "            garbageSpeciesName: Others\n" +
                                                    "            proportionSimulatedToGarbage: '0.3'\n" +
                                                    "            rounding: false\n" +
                                                    "    gearStrategy: Never Change Gear\n" +
                                                    "    holdSize: '13500.0'\n" +
                                                    "    hourlyVariableCost: '162288.0'\n" +
                                                    "    initialFishersPerPort:\n" +
                                                    "      Bajomulyo: 21\n" +
                                                    "      Probolinggo: 3\n" +
                                                    "    literPerKilometer: '6.0'\n" +
                                                    "    logbook: No Logbook\n" +
                                                    "    regulation: Anarchy\n" +
                                                    "    speedInKmh: '13.0'\n" +
                                                    "    tags: black,longline,ship,big\n" +
                                                    "    usePredictors: false\n" +
                                                    "    weatherStrategy: Ignore Weather\n" +
                                                    "  gasPricePerLiter: '10000.0'\n" +
                                                    "  habitatInitializer: All Sand\n" +
                                                    "  mapInitializer:\n" +
                                                    "    From File Map:\n" +
                                                    "      gridWidthInCell: '100.0'\n" +
                                                    "      header: true\n" +
                                                    "      latLong: true\n" +
                                                    "      mapFile: /home/carrknight/code/oxfish/docs/indonesia_hub/runs/712/712_map.csv\n" +
                                                    "  mapMakerDedicatedRandomSeed: null\n" +
                                                    "  market:\n" +
                                                    "    Fixed Price Market:\n" +
                                                    "      marketPrice: '40000.0'\n" +
                                                    "  networkBuilder:\n" +
                                                    "    Equal Out Degree:\n" +
                                                    "      allowMutualFriendships: true\n" +
                                                    "      degree: '2.0'\n" +
                                                    "      equalOutDegree: true\n" +
                                                    "  plugins: [\n" +
                                                    "    ]\n" +
                                                    "  portInitializer:\n" +
                                                    "    List of Ports:\n" +
                                                    "      ports:\n" +
                                                    "        Sumenep: 113.972926,-7.05225\n" +
                                                    "        Gili Iyang: 114.190362,-6.974766\n" +
                                                    "        Bajomulyo: 111.186606,-6.658267\n" +
                                                    "        Brondong: 112.267658,-6.867022\n" +
                                                    "        Karangsong: 108.372488,-6.30807\n" +
                                                    "        Tanjung Pandan: 107.62312,-2.733054\n" +
                                                    "        Probolinggo: 113.20387,-7.728979\n" +
                                                    "      usingGridCoordinates: false\n" +
                                                    "  portSwitching: false\n" +
                                                    "  tagsToTrackSeparately: ''\n" +
                                                    "  weatherInitializer:\n" +
                                                    "    Constant Weather:\n" +
                                                    "      temperature: '30.0'\n" +
                                                    "      windOrientation: '0.0'\n" +
                                                    "      windSpeed: '0.0'\n"
                                                    );

       // System.out.println(read);
       //System.out.println(read.get("Flexible"));
        Object flexible = read.get("Flexible");
        System.out.println(flexible.getClass());
        Object biologyInitializer = ((Map<String, Object>) flexible).get("biologyInitializer");
        System.out.println(biologyInitializer);
        Object multiple = ((Map<String, Object>) biologyInitializer).get("Multiple Species Biomass");
        System.out.println(multiple);
        System.out.println(multiple.getClass());
        Object factories = ((Map<String, Object>) multiple).get("factories");
        System.out.println(factories);
        System.out.println(factories.getClass());

    }




    @Test
    public void optimizationParameters() {

        List<OptimizationParameter> parameters = new LinkedList<>();
        //gear
        //catchabilities
        parameters.add(new CommaMapOptimizationParameter(
                4, "fisherDefinitions$"+0+".gear.delegate.delegate.catchabilityMap",
                0,
                1
        ));
        //garbage collectors
        parameters.add(new SimpleOptimizationParameter(
                "fisherDefinitions$"+1+".gear.delegate.proportionSimulatedToGarbage",
                .10,
                .80
        ));
        parameters.add(
                new SimpleOptimizationParameter(
                        "biologyInitializer.factories$"+3+".grower.distributionalWeight",
                        .5,
                        10

                )
        );


        FishYAML yaml = new FishYAML();
        String output = yaml.dump(parameters);
        System.out.println(output);

        parameters = null;
        parameters = yaml.loadAs(output,List.class);
        System.out.println(parameters);
        System.out.println(parameters.get(0).getClass());
        System.out.println(parameters.get(1).getClass());
        System.out.println(parameters.get(2).getClass());
    }
}