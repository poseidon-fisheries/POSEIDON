package uk.ac.ox.oxfish.model.market;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.market.supplychain.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SupplyChainTest {



    @Test
    public void testSimpleSupplyChain(){
/*
        //Create Ports
        GenericPort[] ports = new GenericPort[2];
        ports[0] = new GenericPort("PortA", new double[]{1000.0});
        ports[1] = new GenericPort("PortB", new double[]{1300.0});

        //Create Processors
        GenericProcessor[] processors = new GenericProcessor[3];
        processors[0] = new GenericProcessor(new double[]{1400},
                                             new double[]{.7},
                                             new double[]{1.5});
        processors[1] = new GenericProcessor(new double[]{1600},
                new double[]{.7},
                new double[]{1.4});
        processors[2] = new GenericProcessor(new double[]{2000},
                new double[]{.7},
                new double[]{1.6});

        //Create Packagers
        GenericProcessor[] packagers = new GenericProcessor[2];
        packagers[0] = new GenericProcessor(new double[]{2500},
                new double[]{1.0},
                new double[]{2});
        packagers[1] = new GenericProcessor(new double[]{2500},
                new double[]{1.0},
                new double[]{3});

        //Create Markets
        GenericMarket[] markets = new GenericMarket[2];
        markets[0] = new GenericMarket("Local",
                new double[]{1750},
                new double[]{2800});
        markets[1] = new GenericMarket("Spain",
                new double[]{1000.0},
                new double[]{1200});

        SupplyChain testSupplyChain = new SupplyChain(ports,
                processors,
                packagers,
                markets,
                new double[][]{{0,0,0},{0,0,0}},
                new double[][]{{0,0},{0,0},{0,0}},
                new double[][]{{0,0},{0,0},{0,0}},
                new double[][]{{0,0},{0,0}},
                new double[]{.85, .99, .75},
                new double[]{.85,.88},
                new double[]{.82,.83},
                1.5,
                2.5,
                1.1);
*/
        SupplyChain testSupplyChain = new SupplyChain();
        testSupplyChain.initializeModel(
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "ports2021.csv").toAbsolutePath().toString(),
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "facilities_2022.csv").toAbsolutePath().toString(),
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "demands2021.csv").toAbsolutePath().toString(),
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "transportation_costs_2022.csv").toAbsolutePath().toString(),
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "tariffs2021.csv").toAbsolutePath().toString(),
//                new double[]{1500, 3250, 3800},
//                new double[]{999999999, (35.2 * 1000000) / (16.4 * 1000) * 1.4 , (2274.0 * 1000000)/(576.4*1000)}
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "product_costs2021.csv").toAbsolutePath().toString());

        SupplyChain slice1b = new SupplyChain();
        slice1b.initializeModel(
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "ports.csv").toAbsolutePath().toString(),
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "facilities.csv").toAbsolutePath().toString(),
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "demands_s1.csv").toAbsolutePath().toString(),
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "transportation_costs_s1.csv").toAbsolutePath().toString(),
                Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "tariffs.csv").toAbsolutePath().toString(),
                new double[]{1000, 1200, 1500},
                new double[]{999999999, (35.2 * 1000000) / (16.4 * 1000) * 1.4 , (2274.0 * 1000000)/(576.4*1000)});
/*
        slice1b.run();
        slice1b.printConstraints();
        System.out.println("SOLUTION NETWORK ---------------------");
        System.out.printf(slice1b.generateSolutionNetwork());
        System.out.println("--------------------------------------");
        slice1b.printLandingsDual();
*/

        System.out.println("------tests----------");

        System.out.println("---------------------");

        testSupplyChain.run();
//        testSupplyChain.printConstraints();


//        System.out.println(testSupplyChain.getObjectiveValue());
//        testSupplyChain.printPorts();
//        testSupplyChain.printFacilities();
//        testSupplyChain.printMarkets();

        System.out.println("SOLUTION NETWORK ---------------------");
        System.out.printf(testSupplyChain.generateSolutionNetwork());
        System.out.println("--------------------------------------");

        testSupplyChain.printLandingsDual();

    }
}
