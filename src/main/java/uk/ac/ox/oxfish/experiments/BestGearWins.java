package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.biology.initializer.BiologyInitializers;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Testing that imitation gets the best gear
 * Created by carrknight on 8/5/15.
 */
public class BestGearWins {

    public static DataColumn efficiencyImitation(
            final double gasPrice, final int simulationYears,
            final String biologyInitializer, final long seed)
    {

        //without fuel cost:
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(BiologyInitializers.CONSTRUCTORS.get(biologyInitializer).get());
        scenario.setFishers(100);
        scenario.setGasPricePerLiter(new FixedDoubleParameter(gasPrice));
        RandomCatchabilityTrawlFactory gear = new RandomCatchabilityTrawlFactory();
        gear.setGasPerHourFished(new UniformDoubleParameter(0, 20));
        scenario.setGear(gear);
        //start everything
        FishState state = new FishState(seed);
        state.setScenario(scenario);
        state.start();
        //attach analysis
        GearImitationAnalysis.attachGearAnalysisToEachFisher(state.getFishers(), state, new ArrayList<>(),
                                                             new CashFlowObjective(60));

        //pre-run average efficiency
        double average = 0;
        for(Fisher fisher : state.getFishers())
        {
            average += ((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished();
        }
        average/=100;
  //      System.out.println(average);

        while(state.getYear() < simulationYears)
            state.schedule.step(state);

        //average now? Ought to be more or less the same
        average = 0;
        for(Fisher fisher : state.getFishers())
        {
            average += ((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished();
        }
        average/=100;
     //   System.out.println(average);
        return state.getDailyDataSet().getColumn("Thrawling Fuel Consumption");

    }

    public static void main(String[] args) throws IOException {


        Path root = Paths.get("runs", "ltr");

        root.toFile().mkdirs();
        for(int i=0; i<100;i++)
        {
            DataColumn column = efficiencyImitation(.05, 20, "From Left To Right Fixed", System.currentTimeMillis());
            File file = root.resolve("pricey" + i + ".csv").toFile();
            FileWriter writer = new FileWriter(file);
            for (Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }
        for(int i=0; i<100;i++)
        {
            DataColumn column = efficiencyImitation(0, 20, "From Left To Right Fixed", System.currentTimeMillis());
            File file = root.resolve("free" + i + ".csv").toFile();
            FileWriter writer = new FileWriter(file);
            for (Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }


        root = Paths.get("runs", "logistic");
        root.toFile().mkdirs();
        for(int i=0; i<100;i++)
        {
            DataColumn column = efficiencyImitation(.05, 20, "Independent Logistic", System.currentTimeMillis());
            File file = root.resolve("pricey" + i + ".csv").toFile();
            FileWriter writer = new FileWriter(file);
            for (Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }
        for(int i=0; i<100;i++)
        {
            DataColumn column = efficiencyImitation(0, 20, "Independent Logistic", System.currentTimeMillis());
            File file = root.resolve("free" + i + ".csv").toFile();
            FileWriter writer = new FileWriter(file);
            for (Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }

        root = Paths.get("runs", "marginal");
        root.toFile().mkdirs();
        for(int i=0; i<100;i++)
        {
            DataColumn column = efficiencyImitation(.2, 20, "Independent Logistic", System.currentTimeMillis());
            File file = root.resolve("pricey" + i + ".csv").toFile();
            FileWriter writer = new FileWriter(file);
            for (Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }
        for(int i=0; i<100;i++)
        {
            DataColumn column = efficiencyImitation(.01, 20, "Independent Logistic", System.currentTimeMillis());
            File file = root.resolve("free" + i + ".csv").toFile();
            FileWriter writer = new FileWriter(file);
            for (Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }


    }
}
