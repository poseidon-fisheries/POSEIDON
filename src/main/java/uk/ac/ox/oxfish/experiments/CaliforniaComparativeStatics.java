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

package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.gas.FixedGasFactory;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * THIS IS MOTHBALLED BECAUSE WE STOPPED DOING RESETS AT YEARS DIFFERENT FROM 1 (otherwise there would be subtle errors in the natural processes)
 * Created by carrknight on 6/10/17.
 */
public class CaliforniaComparativeStatics {


    ///home/carrknight/code/oxfish/docs/20170606 cali_catchability_2/policymaking/gas/
    private final static Path outputMainPath =
            Paths.get("docs", "20170606 cali_catchability_2", "policymaking");

    private final static Path yamlInput =
            Paths.get("docs", "20170606 cali_catchability_2", "results", "calicatch_2011_ignoring_narrow_4b.yaml");
    public static final int NUMBER_OF_RUNS = 30;

    /*
    public static void mainGas(String[] args) throws IOException {

        List<Double> gasPricesAllowed = Lists.newArrayList(1.78d, 0.89d);

        for(double gasPrice : gasPricesAllowed)
        {
            for(long seed = 0; seed< NUMBER_OF_RUNS; seed++) {
                gasPrice = FishStateUtilities.round5(gasPrice);
                FishState state = runGas(12, seed, yamlInput,
                                         gasPrice, null, 2);
                String printOut = FishStateUtilities.printTablePerPort(state, FisherYearlyTimeSeries.CASH_FLOW_COLUMN,
                                                                       2);

                FileWriter writer = new FileWriter(
                        outputMainPath.resolve("gas").resolve("itq_" + gasPrice + "_" + seed +".csv").toFile());
                writer.write(printOut);
                writer.flush();
                writer.close();
                System.out.println(printOut);
            }
        }

    }


    public static void main(String[] args) throws IOException {
 //       mainGas(args);
        mainSablefish(args);
    }

    public static void mainSablefish(String[] args) throws  IOException{

        List<Double> sableFishPrices = Lists.newArrayList(4.3235295*2d, 4.3235295d);


        for(double sablefish : sableFishPrices)
        {
            for(long seed =0; seed<30; seed++) {
                FishState state = runSablefish(12, seed, yamlInput, sablefish,
                                               null, 2);
                String printOut = FishStateUtilities.
                        printTablePerPort(state, FisherYearlyTimeSeries.CASH_FLOW_COLUMN,
                                                                       2);

                FileWriter writer = new FileWriter(
                        outputMainPath.resolve("sablefish").
                                resolve("itq_" + ((int)sablefish) + "_" + seed +".csv").toFile());
                writer.write(printOut);
                writer.flush();
                writer.close();
                System.out.println(printOut);
            }
        }
    }


    public static FishState runGas(
            int numberOfYears, long seed,
            Path yamlFile,
            double gasPrice,
            AlgorithmFactory<? extends Regulation> regulation, final int resetBiologyAtYear) throws FileNotFoundException {



        FishState state =  new FishState(seed);

        FishYAML yaml = new FishYAML();
        CaliforniaAbundanceScenario scenario = yaml.loadAs(new FileReader(yamlFile.toFile()), CaliforniaAbundanceScenario.class);
        scenario.setGasPriceMaker(new FixedGasFactory(gasPrice));
        scenario.setResetBiologyAtYear(resetBiologyAtYear);
        state.setScenario(scenario);

        if(regulation!=null)
            scenario.setRegulationPreReset(regulation);

        state.start();
        while(state.getYear()<numberOfYears)
            state.schedule.step(state);

        return state;


    }

    public static FishState runSablefish(
            int numberOfYears, long seed,
            Path yamlFile,
            double sablefishPrice,
            AlgorithmFactory<? extends Regulation> regulation,
            final int resetBiologyAtYear) throws FileNotFoundException {



        FishState state =  new FishState(seed);

        FishYAML yaml = new FishYAML();
        CaliforniaAbundanceScenario scenario = yaml.loadAs(new FileReader(yamlFile.toFile()), CaliforniaAbundanceScenario.class);
        String priceMap = "Dover Sole:0.6698922,Sablefish:" + sablefishPrice + ",Shortspine Thornyhead:1.0428510,Longspine Thornyhead:1.0428510,Yelloweye Rockfish:1.0754502"
                +"," + MultipleSpeciesAbundanceInitializer.FAKE_SPECIES_NAME+":1.7646181";
        scenario.setPriceMap(
                priceMap);
        scenario.setResetBiologyAtYear(resetBiologyAtYear);
        state.setScenario(scenario);

        if(regulation!=null)
            scenario.setRegulationPreReset(regulation);

        state.start();
        while(state.getYear()<numberOfYears)
            state.schedule.step(state);

        return state;


    }
*/
}
