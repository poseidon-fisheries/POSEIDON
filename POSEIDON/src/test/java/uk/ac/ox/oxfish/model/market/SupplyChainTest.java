package uk.ac.ox.oxfish.model.market;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.market.supplychain.GenericMarket;
import uk.ac.ox.oxfish.model.market.supplychain.GenericPort;
import uk.ac.ox.oxfish.model.market.supplychain.GenericProcessor;
import uk.ac.ox.oxfish.model.market.supplychain.SupplyChain;

import java.util.Arrays;

public class SupplyChainTest {

    @Test
    public void testSimpleSupplyChain(){
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
        testSupplyChain.initializeLP();
        testSupplyChain.establishConstraints(0);
        testSupplyChain.setObjective(0);

        testSupplyChain.solveLP();
        System.out.println(testSupplyChain.getObjectiveValue());

        testSupplyChain.solveDual();


        //breakpoint

        testSupplyChain.printLandingsDual();
        System.out.println(Arrays.toString(testSupplyChain.getExVesselPrices(0)));

    }
}
