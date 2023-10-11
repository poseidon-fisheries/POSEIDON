package uk.ac.ox.oxfish.model.market.supplychain;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

import uk.ac.ox.oxfish.biology.Species;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SupplyChain {
    List<String> speciesNames = new ArrayList<>();
    List<String> locationNames = new ArrayList<>();

    MPSolver solver;
    double infinity = java.lang.Double.POSITIVE_INFINITY;

    //Inputs
    //Ports - each has landings
    GenericPort[] ports;

    //Processors
    GenericProcessor[] processors;

    //Packagers
    GenericProcessor[] packagers;

    //Markets
    GenericMarket[] markets;

    int     nPort,
            nProcessor,
            nPackager,
            nMarkets;



    //TransportationCosts
    double[][] costsPort2Processor;
    double[][] costsProcessor2Packager;
    double[][] costsProcessor2Market;
    double[][] costsPackager2Market;
    double[] costsWCPO2Processor;
    double[] costsWCPO2Packager;
    double[] costsWCPO2Market;

    //External Trade Partners
    double WCPOCostRaw, WCPOCostLoin, WCPOCostPackaged;



    // Linear Program Variables
    MPVariable[][] portToProcessor;
    MPVariable[][] processorToPackager;
    MPVariable[][] processorToMarket;
    MPVariable[][] packagerToMarket;
    MPVariable[] WCPOToProcessor;
    MPVariable[] WCPOToPackager;
    MPVariable[] WCPOToMarketLoin;
    MPVariable[] WCPOToMarketPackaged;

    //Linear Program Constraints
    MPConstraint[] landingsPerPort;
    MPConstraint[] processorCapacity;
    MPConstraint[] packagerCapacity;
    MPConstraint[] loinProduction;
    MPConstraint[] packageProduction;
    MPConstraint[] marketDemandLoin;
    MPConstraint[] marketDemandPackage;

    //Linear Program Objective
    MPObjective objective;
    MPSolver.ResultStatus resultStatus;

    public SupplyChain(GenericPort[] ports,
                       GenericProcessor[] processors,
                       GenericProcessor[] packagers,
                       GenericMarket[] markets,
                       double[][] costsPort2Processor,
                       double[][] costsProcessor2Packager,
                       double[][] costsProcessor2Market,
                       double[][] costsPackager2Market,
                       double[] costsWCPO2Processor,
                       double[] costsWCPO2Packager,
                       double[] costsWCPO2Market,
                       double WCPOCostLoin,
                       double WCPOCostPackaged,
                       double WCPOCostRaw){
        this.ports = ports;
        this.processors = processors;
        this.packagers = packagers;
        this.markets = markets;
        this.nPort = ports.length;
        this.nProcessor = processors.length;
        this.nPackager = packagers.length;
        this.nMarkets = markets.length;

        this.costsPort2Processor = costsPort2Processor;
        this.costsProcessor2Packager = costsProcessor2Packager;
        this.costsProcessor2Market = costsProcessor2Market;
        this.costsPackager2Market = costsPackager2Market;
        this.costsWCPO2Processor = costsWCPO2Processor;
        this.costsWCPO2Packager = costsWCPO2Packager;
        this.costsWCPO2Market = costsWCPO2Market;
        this.WCPOCostLoin = WCPOCostLoin;
        this.WCPOCostPackaged=WCPOCostPackaged;
        this.WCPOCostRaw=WCPOCostRaw;
    }

    public void initializeLP(){

        Loader.loadNativeLibraries();
        solver  = MPSolver.createSolver("GLOP");

        //Create Variables
        portToProcessor = new MPVariable[nPort][nProcessor];
        for(int i=0; i<nPort; i++){
            for(int j=0; j< nProcessor; j++){
                portToProcessor[i][j] = solver.makeNumVar(0, MPSolver.infinity(), "PR"+i+"_"+j);
            }
        }

        processorToPackager = new MPVariable[nProcessor][nPackager];
        processorToMarket = new MPVariable[nProcessor][nMarkets];
        WCPOToProcessor = new MPVariable[nProcessor];
        for(int j=0; j<nProcessor; j++){
            WCPOToProcessor[j] = solver.makeNumVar(0, MPSolver.infinity(), "WrR"+j);
            for(int k=0; k< nPackager; k++){
                processorToPackager[j][k] = solver.makeNumVar(0, MPSolver.infinity(), "RC"+j+"_"+k);
            }
            for(int l=0; l< nMarkets; l++){
                processorToMarket[j][l] = solver.makeNumVar(0, MPSolver.infinity(), "RM"+j+"_"+l);
            }
        }

        WCPOToPackager = new MPVariable[nPackager];
        packagerToMarket = new MPVariable[nPackager][nMarkets];
        for(int k=0; k< nPackager; k++){
            WCPOToPackager[k] = solver.makeNumVar(0, MPSolver.infinity(), "WlC"+k);
            for(int l=0; l< nMarkets; l++){
                packagerToMarket[k][l] = solver.makeNumVar(0, MPSolver.infinity(), "CM"+k+"_"+l);
            }
        }

        WCPOToMarketPackaged = new MPVariable[nMarkets];
        WCPOToMarketLoin = new MPVariable[nMarkets];
        for(int l=0; l< nMarkets; l++){
            WCPOToMarketLoin[l] = solver.makeNumVar(0, MPSolver.infinity(), "WlM"+l);
            WCPOToMarketPackaged[l] = solver.makeNumVar(0, MPSolver.infinity(), "WcM"+l);
        }
    }

    public void establishConstraints(int index){
        //index gives the species index for which we want to set up the constraints.
        int nPort = ports.length,
                nProcessor = processors.length,
                nPackager = packagers.length,
                nMarkets = markets.length;

        //Landings per port
        //Sum of port-to-processor must not exceed landings at port
        landingsPerPort = new MPConstraint[nPort];
        for(int i=0; i<nPort; i++){
            landingsPerPort[i] = solver.makeConstraint(0.0, ports[i].getLandings(index), "LPP"+i);
             for(int j=0; j<nProcessor; j++){
                 landingsPerPort[i].setCoefficient(portToProcessor[i][j],1.0);
             }
        }

        //Processor Capacity
        //WrRj + sum_i PRij <= capacity_j
        processorCapacity = new MPConstraint[nProcessor];
        for(int j=0; j<nProcessor; j++){
            processorCapacity[j] = solver.makeConstraint(0.0, processors[j].getMaxOutput(index), "PC"+j);
            processorCapacity[j].setCoefficient(WCPOToProcessor[j], 1.0);
            for(int i=0; i<nPort; i++){
                processorCapacity[j].setCoefficient(portToProcessor[i][j],1.0);
            }
        }

        //Packager (cannery) Capacity
        //WlCk + sum_j RCjk <= capacity_k
        packagerCapacity = new MPConstraint[nPackager];
        for(int k=0; k<nPackager; k++){
            packagerCapacity[k] = solver.makeConstraint(0.0, packagers[k].getMaxOutput(index), "CC"+k);
            packagerCapacity[k].setCoefficient(WCPOToPackager[k], 1.0);
            for(int j=0; j<nPackager; j++){
                packagerCapacity[k].setCoefficient(processorToPackager[j][k], 1.0);
            }
        }

        //Loin Production
        loinProduction = new MPConstraint[nProcessor];
        //CTA_j*WrRj + CTA_j*sum_i(PRij) - sum_k RCjk - sum_l RMjl >= 0
        for(int j=0; j<nProcessor; j++){
            loinProduction[j] = solver.makeConstraint(0.0, MPSolver.infinity(), "LP"+j);
            loinProduction[j].setCoefficient(WCPOToProcessor[j], processors[j].getTransformationAbility(index));
            for(int i=0; i<nPort; i++){
                loinProduction[j].setCoefficient(portToProcessor[i][j], processors[j].getTransformationAbility(index));
            }
            for(int k=0; k<nPackager; k++){
                loinProduction[j].setCoefficient(processorToPackager[j][k], -1.0);
            }
            for(int l=0; l<nMarkets; l++){
                loinProduction[j].setCoefficient(processorToMarket[j][l], -1.0);
            }
       }

        //Package Production
        packageProduction = new MPConstraint[nPackager];
        //WlCk + sum_j RCjk -sum_l CMkl >=0
        for(int k=0; k<nPackager; k++){
            packageProduction[k] = solver.makeConstraint(0.0, MPSolver.infinity(), "PP"+k);
            packageProduction[k].setCoefficient(WCPOToPackager[k], 1.0);
            for(int l=0; l<nMarkets; l++){
                packageProduction[k].setCoefficient(packagerToMarket[k][l], -1.0);
            }
            for(int j=0; j< nProcessor; j++){
                packageProduction[k].setCoefficient(processorToPackager[j][k], 1.0);
            }
        }

        //Market Demands - Loins
        marketDemandLoin = new MPConstraint[nMarkets];
        //WlMl + sum_j RMjl = DLl
        for(int l=0; l< nMarkets; l++){
            marketDemandLoin[l] = solver.makeConstraint(markets[l].getDemandLoin(index),
                    markets[l].getDemandLoin(index),
                    "MDL"+l);
            marketDemandLoin[l].setCoefficient(WCPOToMarketLoin[l], 1.0);
            for(int j=0; j<nProcessor; j++){
                marketDemandLoin[l].setCoefficient(processorToMarket[j][l], 1.0);
            }
        }

        //Market Demands - Packaged
        marketDemandPackage = new MPConstraint[nMarkets];
        //WcMl + sum_k CMkl = DCl
        for(int l=0; l< nMarkets; l++){
            marketDemandPackage[l] = solver.makeConstraint(markets[l].getDemandPackaged(index),
                    markets[l].getDemandPackaged(index),
                    "MDC"+l);
            marketDemandPackage[l].setCoefficient(WCPOToMarketPackaged[l], 1.0);
            for(int k=0; k<nPackager; k++){
                marketDemandPackage[l].setCoefficient(packagerToMarket[k][l], 1.0);
            }
        }
    }

    public void setObjective(int index){

        objective = solver.objective();
        //Minimize total costs!!!
        //transportation costs ports -> processor
        //tc_ij * PR_ij
        for(int i=0; i<nPort; i++){
            for(int j=0; j<nProcessor; j++){
                objective.setCoefficient(portToProcessor[i][j], costsPort2Processor[i][j]);
            }
        }

        //transportation costs processor -> packager
        //tc_jk * RC_jk
        for(int j=0; j<nProcessor; j++){
            for(int k=0; k<nPackager; k++){
                objective.setCoefficient(processorToPackager[j][k], costsProcessor2Packager[j][k]);
            }
        }

        //transportation costs processor -> market
        //tc_jl * RM_jl
        for(int j=0; j<nProcessor; j++){
            for(int l=0; l<nMarkets; l++){
                objective.setCoefficient(processorToMarket[j][l], costsProcessor2Market[j][l]);
            }
        }

        //transportation costs packager -> market
        //tc_kl * CM_kl
        for(int k=0; k<nPackager; k++){
            for(int l=0; l<nMarkets; l++){
                objective.setCoefficient(packagerToMarket[k][l], costsPackager2Market[k][l]);
            }
        }

        //processing costs loins
        //lc_j * (WrRj + PR_ij)
        for(int j=0; j< nProcessor; j++){
            objective.setCoefficient(WCPOToProcessor[j],processors[j].getProcessingCost(index));
            for(int i=0; i<nPort; i++){
                objective.setCoefficient(portToProcessor[i][j], processors[j].getProcessingCost(index));
            }
        }

        //processing costs packages
        //cc_k * (WlCk + RC_jk)
        for(int k=0; k<nPackager; k++){
            objective.setCoefficient(WCPOToPackager[k], packagers[k].getProcessingCost(index));
            for(int j=0; j< nProcessor; j++){
                objective.setCoefficient(processorToPackager[j][k],packagers[k].getProcessingCost(index));
            }
        }

        //import costs WCPO -> processor
        //WrCk * (cwr + tw_j)
        for(int j=0; j<nProcessor; j++){
            objective.setCoefficient(WCPOToProcessor[j], costsWCPO2Processor[j]+WCPOCostRaw );
        }

        //import costs WCPO -> packager
        //WlCk * (cwl + tw_k)
        for(int k=0; k<nPackager; k++){
            objective.setCoefficient(WCPOToPackager[k], costsWCPO2Packager[k]+WCPOCostLoin );
        }

        //import costs WCPO -> market loins
        //WlMl * (cwl+twl_l)
        //import costs WCPO -> market packaged
        //WcMl * (cwc+twc_l)
        for(int l=0; l<nMarkets; l++){
            objective.setCoefficient(WCPOToMarketLoin[l], WCPOCostLoin+costsWCPO2Market[l]);
            objective.setCoefficient(WCPOToMarketPackaged[l], WCPOCostPackaged+costsWCPO2Market[l]);
        }
        objective.setMinimization();
    }

    public void solveLP(){
        MPSolverParameters params = new MPSolverParameters();
        params.setIntegerParam(MPSolverParameters.IntegerParam.LP_ALGORITHM, MPSolverParameters.LpAlgorithmValues.PRIMAL.swigValue());
        solver.solve(params);
        resultStatus = solver.solve();
    }

    public void solveDual(){
        MPSolverParameters params = new MPSolverParameters();
        params.setIntegerParam(MPSolverParameters.IntegerParam.LP_ALGORITHM, MPSolverParameters.LpAlgorithmValues.DUAL.swigValue());
        solver.solve(params);
        resultStatus = solver.solve();
    }

    public double getObjectiveValue(){
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            return objective.value();
        } else {
            return 0.0;
        }
    }

    public void printLandingsDual(){
        for(int i=0; i<nPort; i++){
            System.out.println(landingsPerPort[i].dualValue());

        }
    }

    public double[] getExVesselPrices(int index) {
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            double[] prices = new double[nPort];
            for(int i=0; i<nPort; i++){
                prices[i]=-landingsPerPort[i].dualValue();
            }
            return prices;
        } else {
            return new double[]{};
        }

    }

    /*
    public double[] getPortPrices(int index){
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            return ;
        } else {
            return new double[]{};
        }
    }*/


    public List<GenericPort> readPortsFromCSV(String path){
        List<GenericPort> ports = new ArrayList<>();
        Path pathToFile = Paths.get(path);

        try(BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)){
            String line = br.readLine(); //First line is a header
            line = br.readLine();
            while(line != null){
                String[] attributes = line.split(",");
                GenericPort port = createPort(attributes);
                ports.add(port);
                line=br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return ports;
    }

    public List<GenericProcessor> readFacilitiesFromCSV(String path){
        List<GenericProcessor> facilities = new ArrayList<>();
        Path pathToFile = Paths.get(path);
        try(BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)){
            String line = br.readLine(); //First line is a header
            line = br.readLine();
            while(line != null){
                String[] attributes = line.split(",");
                GenericProcessor facility = createFacility(attributes);
                facilities.add(facility);
                line=br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return facilities;

    }

    public List<GenericMarket> readMarketsFromCSV(String path){
        List<GenericMarket> markets = new ArrayList<>();
        Path pathToFile = Paths.get(path);
        try(BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)){
            String line = br.readLine(); //First line is a header
            line = br.readLine();
            while(line != null){
                String[] attributes = line.split(",");
                GenericMarket market = createMarket(attributes);
                markets.add(market);
                line=br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return markets;
    }

    public List<TransportCost> readTransportCostsFromCSV(String path){
        List<TransportCost> costs = new ArrayList<>();
        Path pathToFile = Paths.get(path);
        try(BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)){
            String line = br.readLine(); //First line is a header
            line = br.readLine();
            while(line != null){
                String[] attributes = line.split(",");
                TransportCost cost = createTransportCost(attributes);
                costs.add(cost);
                line=br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return costs;
    }




    private static int getIndex(List<String> list, String token){
        if(list.indexOf(token) > -1){
            return (list.indexOf(token));
        } else {
            list.add(token);
            return (list.indexOf(token));
        }
    }

    private  GenericPort createPort(String[] data){
        String name = data[0];
        String location = data[1];
        int locationIndex = getIndex(locationNames,location);
        double[] landings = new double[(data.length-2)/2];
        //System.out.println(data.length);
        for(int i=0; i<(data.length-2)/2; i++){
            int speciesIndex = getIndex(speciesNames, data[i*2+2]);
           // System.out.println(speciesIndex);
            landings[speciesIndex] = Double.parseDouble(data[i*2+3]);
        }
        return new GenericPort(name, location, landings);
    }

    private  GenericProcessor createFacility(String[] data){
        String name = data[0];
        String location = data[1];
        int locationIndex = getIndex(locationNames, location);
        double coldStorage = Double.parseDouble(data[2]);
        double CTA = Double.parseDouble(data[3]);
        double maxLoining = Double.parseDouble(data[4]);
        double maxCanning = Double.parseDouble(data[5]);
        return new GenericProcessor(name, location, new double[]{maxLoining,maxCanning},new double[]{CTA}, new double[]{41.52, 87.80-41.52} );
    }

    private TransportCost createTransportCost(String[] data){
        String origin = data[0];
        String destination = data[1];
        double cost = Double.parseDouble(data[2]);
        return new TransportCost(getIndex(locationNames, origin),
                getIndex(locationNames, destination),
                cost);
        }


    private GenericMarket createMarket(String[] data){
        String name = data[0];
        String location = data[1];
        int locationIndex = getIndex(locationNames, location);
        double canDemand = Double.parseDouble(data[2]);
        //then
        //loin demand per species
        double[] loinDemand = new double[(data.length-3)/2];
        //System.out.println(data.length);
        for(int i=0; i<(data.length-3)/2; i++){
            int speciesIndex = getIndex(speciesNames, data[i*2+3]);
            // System.out.println(speciesIndex);
            loinDemand[speciesIndex] = Double.parseDouble(data[i*2+4]);
        }
        return new GenericMarket(name, location,loinDemand, new double[]{canDemand} );
    }

}

