package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.scenario.CaliforniaBathymetryScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by carrknight on 8/9/16.
 */
public class CaliforniaGasPrices {


    private final static double MIN_GAS_PRICE = 0;
    private final static double MAX_GAS_PRICE = 3;
    private final static double GAS_PRICE_INCREMENT = 0.01;


    public static void old(String[] args) throws IOException {

        for(double gasPrice = MIN_GAS_PRICE; gasPrice<= MAX_GAS_PRICE; gasPrice+=GAS_PRICE_INCREMENT)
        {
            gasPrice = FishStateUtilities.round5(gasPrice);
            FishState state = run(10,0,gasPrice,new AnarchyFactory());
            String printOut = FishStateUtilities.printTablePerPort(state, FisherYearlyTimeSeries.CASH_FLOW_COLUMN,0);

            FileWriter writer = new FileWriter(Paths.get("runs","caligas","simple_"+gasPrice + ".csv").toFile());
            writer.write(printOut);
            writer.flush();
            writer.close();
            System.out.println(printOut);

        }

    }


    public static void main_fish(String[] args) throws IOException {

        List<Double> sablefish = Lists.newArrayList(3.589,3.589*2);

        for(double price : sablefish)
        {
            for(long seed =0; seed<30; seed++) {
                price = FishStateUtilities.round5(price);
                FishState state = runSableFish(10, seed, price,null);
                String printOut = FishStateUtilities.printTablePerPort(state, FisherYearlyTimeSeries.CASH_FLOW_COLUMN,0);

                FileWriter writer = new FileWriter(
                        Paths.get("runs", "caligas","fishitq", "simple_" + price + "_" + seed +".csv").toFile());
                writer.write(printOut);
                writer.flush();
                writer.close();
                System.out.println(printOut);
            }
        }

    }

    public static void main(String[] args) throws IOException {

        List<Double> gasPricesAllowed = Lists.newArrayList(2.4d,0.8);

        for(double gasPrice : gasPricesAllowed)
        {
            for(long seed =30; seed<60; seed++) {
                gasPrice = FishStateUtilities.round5(gasPrice);
                FishState state = run(10, seed, gasPrice,new AnarchyFactory());
                String printOut = FishStateUtilities.printTablePerPort(state, FisherYearlyTimeSeries.CASH_FLOW_COLUMN,0);

                FileWriter writer = new FileWriter(
                        Paths.get("runs", "caligas","anarchy", "simple_" + gasPrice + "_" + seed +".csv").toFile());
                writer.write(printOut);
                writer.flush();
                writer.close();
                System.out.println(printOut);
            }
        }

    }

    public static FishState run(int numberOfYears, long seed, double gasPrice,
                                AlgorithmFactory<? extends Regulation> regulation)
    {



        FishState state =  new FishState(seed);

        CaliforniaBathymetryScenario scenario = new CaliforniaBathymetryScenario();
        scenario.setGasPricePerLiter(new FixedDoubleParameter(gasPrice));
        scenario.setResetBiologyAtYear(5);
        state.setScenario(scenario);

        if(regulation!=null)
            scenario.setRegulation(regulation);

        state.start();
        while(state.getYear()<numberOfYears)
            state.schedule.step(state);

        return state;


    }

    public static FishState runSableFish(int numberOfYears, long seed, double fishPrice,
                                AlgorithmFactory<? extends Regulation> regulation)
    {



        System.out.println(fishPrice);
        FishState state =  new FishState(seed);

        CaliforniaBathymetryScenario scenario = new CaliforniaBathymetryScenario();
        scenario.setPriceMap(
                "Dover Sole:1.208,Sablefish:"+fishPrice + ",Shortspine Thornyhead:3.292,Longspine Thornyhead:0.7187,Yelloweye Rockfish:1.587"
        );
        scenario.setResetBiologyAtYear(5);
        state.setScenario(scenario);

        if(regulation!=null)
            scenario.setRegulation(regulation);

        state.start();
        while(state.getYear()<numberOfYears)
            state.schedule.step(state);

        return state;


    }


}
