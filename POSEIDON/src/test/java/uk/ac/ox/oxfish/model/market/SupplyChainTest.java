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

        List<GenericPort> testPorts = testSupplyChain.readPortsFromCSV(Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "ports.csv").toAbsolutePath().toString());
        testSupplyChain.setPorts(testPorts);
        List<GenericProcessor> testFacilities = testSupplyChain.readFacilitiesFromCSV(Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "facilities.csv").toAbsolutePath().toString());
        testSupplyChain.setFacilities(testFacilities);
        List<GenericMarket> testMarkets = testSupplyChain.readMarketsFromCSV(Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "demands.csv").toAbsolutePath().toString());
        testSupplyChain.setMarkets(testMarkets);
        List<GenericTransportCost> transportCosts = testSupplyChain.readTransportCostsFromCSV(Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "transportation_costs.csv").toAbsolutePath().toString());
        double[][] transportCostMatrix = new double[testSupplyChain.getnLocations()][testSupplyChain.getnLocations()];
        for(GenericTransportCost tc : transportCosts){
            transportCostMatrix[tc.getOrigin()][tc.getDestination()] = tc.getCost();
        }
        testSupplyChain.setTransportCosts(transportCostMatrix);

        List<GenericImportTariff> importTarriffs = testSupplyChain.readTariffsFromCSV(Paths.get("inputs", "epo_inputs", "tests", "supply_chain", "tariffs.csv").toAbsolutePath().toString());
        testSupplyChain.setTariffs(importTarriffs);

        testSupplyChain.setProductCosts(1100,1500,2500);
        int WCPOindex = testSupplyChain.getLocationIndex("Thailand");
        double WCPOCostRaw = 1100;
        double WCPOCostLoin = 1500;
        double WCPOCostPackaged = 2500;
        testSupplyChain.initializeWCPO(WCPOindex, WCPOCostRaw, WCPOCostLoin, WCPOCostPackaged);

        //breakpoint

        System.out.println(testSupplyChain.calculateTransportCosts(4,7,1,0));

        testSupplyChain.initializeLP();

        //breakpoint

        testSupplyChain.establishConstraints();

        testSupplyChain.setObjective();

        testSupplyChain.solveLP();

//        System.out.println(testSupplyChain.getObjectiveValue());

        testSupplyChain.solveDual();


        //breakpoint

        testSupplyChain.printLandingsDual();
        System.out.println("Port to Facility Transfers");
        System.out.println(Arrays.deepToString(testSupplyChain.getPortTransfers(0)));

//        System.out.println("Ex vessel prices");
//        System.out.println(Arrays.toString(testSupplyChain.getExVesselPrices(0)));

    }
}
