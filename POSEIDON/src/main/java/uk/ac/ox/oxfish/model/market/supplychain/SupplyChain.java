package uk.ac.ox.oxfish.model.market.supplychain;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SupplyChain {
    final int RAW=0, LOIN=1, PACKAGE=2;
    List<String> speciesNames = new ArrayList<>();
    List<String> locationNames = new ArrayList<>();
    int WCPOindex;

    MPSolver solver;
    double infinity = java.lang.Double.POSITIVE_INFINITY;

    //Inputs
    //Ports - each has landings
    GenericPort[] ports;

    //Processors
//    GenericProcessor[] processors;

    //Packagers
//    GenericProcessor[] packagers;

    //Combined facilities - act as processors and packagers
    GenericProcessor[] facilities;

    //Markets
    GenericMarket[] markets;

    int     nSpecies=3,
            nPort,
            nProcessor,
            nPackager,
            nMarkets;



    //TransportationCosts
//    double[][] costsPort2Processor;
//    double[][] costsProcessor2Packager;
//    double[][] costsProcessor2Market;
//    double[][] costsPackager2Market;

    double[][] transportCostMatrix;

//    double[] costsWCPO2Processor;
//    double[] costsWCPO2Packager;
//    double[] costsWCPO2Market;

    //External Trade Partners
//    double WCPOCostRaw, WCPOCostLoin, WCPOCostPackaged;


    double[][][] productCost;
    //Raw, localCostLoin, localCostPackaged;

    //Tarriffs
    List<GenericImportTariff> tariffs;



    // Linear Program Variables
    MPVariable[][][] portToProcessor; //PR_sij
    MPVariable[][][] processorToPackager; //RC_sjk
    MPVariable[][][] processorToMarket; //RM_sjl
    MPVariable[][] packagerToMarket; //CM_kl
    MPVariable[][] WCPOToProcessor; //WR_sj
    MPVariable[][] WCPOToPackager; //WC_sk
    MPVariable[][] WCPOToMarketLoin; //WlM_sl
    MPVariable[] WCPOToMarketPackaged; //WcM_l

    //Linear Program Constraints
    MPConstraint[][] landingsPerPort; //LPP_si
    MPConstraint[] processorCapacity; //PC_j
    MPConstraint[] packagerCapacity; //CC_k
    MPConstraint[][] loinProduction; //LP_sj
    MPConstraint[] packageProduction; //PP_k
    MPConstraint[][] marketDemandLoin; //MDL_sl
    MPConstraint[] marketDemandPackage; //MDC_l

    //Linear Program Objective
    MPObjective objective;
    MPSolver.ResultStatus resultStatus;

    public SupplyChain(){

    }

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
//        this.processors = processors;
//        this.packagers = packagers;
        this.markets = markets;
        this.nPort = ports.length;
        this.nProcessor = processors.length;
        this.nPackager = packagers.length;
        this.nMarkets = markets.length;

/*        this.costsPort2Processor = costsPort2Processor;
        this.costsProcessor2Packager = costsProcessor2Packager;
        this.costsProcessor2Market = costsProcessor2Market;
        this.costsPackager2Market = costsPackager2Market;
        this.costsWCPO2Processor = costsWCPO2Processor;
        this.costsWCPO2Packager = costsWCPO2Packager;
        this.costsWCPO2Market = costsWCPO2Market;*/
        setProductCosts(0,0,0);
//        this.WCPOCostLoin = WCPOCostLoin;
//        this.WCPOCostPackaged=WCPOCostPackaged;
//        this.WCPOCostRaw=WCPOCostRaw;
    }

    public SupplyChain(GenericPort[] ports,
                       GenericProcessor[] processors,
                       GenericProcessor[] packagers,
                       GenericMarket[] markets,
                       double[][] transportCostMatrix,
                       int WCPOindex,
                       double WCPOCostLoin,
                       double WCPOCostPackaged,
                       double WCPOCostRaw){
        this.ports = ports;
//        this.processors = processors;
//        this.packagers = packagers;
        this.markets = markets;

        this.transportCostMatrix = transportCostMatrix;
        setProductCosts(0,0,0);
        initializeWCPO(WCPOindex, WCPOCostRaw, WCPOCostLoin, WCPOCostPackaged);
    }

    public void initializeLP(){
        this.nSpecies = getnSpecies();
        this.nPort = ports.length;
        this.nProcessor = facilities.length;
        this.nPackager = facilities.length;
        this.nMarkets = markets.length;

        Loader.loadNativeLibraries();
        solver  = MPSolver.createSolver("GLOP");

        //Create Variables
        portToProcessor = new MPVariable[nSpecies][nPort][nProcessor];
        for(int s=0; s<nSpecies; s++){
            for(int i=0; i<nPort; i++){
                for(int j=0; j< nProcessor; j++){
                    portToProcessor[s][i][j] = solver.makeNumVar(0, MPSolver.infinity(), "PR"+s+"_"+i+"_"+j);
                }
            }
        }

        processorToPackager = new MPVariable[nSpecies][nProcessor][nPackager];
        processorToMarket = new MPVariable[nSpecies][nProcessor][nMarkets];
        WCPOToProcessor = new MPVariable[nSpecies][nProcessor];
        for(int s=0; s<nSpecies; s++){
            for(int j=0; j<nProcessor; j++){
                WCPOToProcessor[s][j] = solver.makeNumVar(0, MPSolver.infinity(), "WrR"+s+"_"+j);
                for(int k=0; k< nPackager; k++){
                    processorToPackager[s][j][k] = solver.makeNumVar(0, MPSolver.infinity(), "RC"+s+"_"+j+"_"+k);
                }
                for(int l=0; l< nMarkets; l++){
                    processorToMarket[s][j][l] = solver.makeNumVar(0, MPSolver.infinity(), "RM"+s+"_"+j+"_"+l);
                }
            }
        }

        WCPOToPackager = new MPVariable[nSpecies][nPackager];
        packagerToMarket = new MPVariable[nPackager][nMarkets];
        for(int k=0; k< nPackager; k++){
            for(int s=0; s<nSpecies; s++){
                WCPOToPackager[s][k] = solver.makeNumVar(0, MPSolver.infinity(), "WlC"+s+"_"+k);
            }
            for(int l=0; l< nMarkets; l++){
                packagerToMarket[k][l] = solver.makeNumVar(0, MPSolver.infinity(), "CM"+k+"_"+l);
            }
        }

        WCPOToMarketPackaged = new MPVariable[nMarkets];
        WCPOToMarketLoin = new MPVariable[nSpecies][nMarkets];
        for(int l=0; l< nMarkets; l++){
            for(int s=0; s<nSpecies; s++){
                WCPOToMarketLoin[s][l] = solver.makeNumVar(0, MPSolver.infinity(), "WlM"+s+"_"+l);
            }
            WCPOToMarketPackaged[l] = solver.makeNumVar(0, MPSolver.infinity(), "WcM"+l);
        }
    }

    public void establishConstraints(){
        //index gives the species index for which we want to set up the constraints.

        //Landings per port
        //Sum of port-to-processor must not exceed landings at port
        landingsPerPort = new MPConstraint[nSpecies][nPort];
        for(int s=0; s<nSpecies; s++){
            for(int i=0; i<nPort; i++){
                landingsPerPort[s][i] = solver.makeConstraint(0.0, ports[i].getLandings(s), "LPP"+s+"_"+i);
                for(int j=0; j<nProcessor; j++){
                    landingsPerPort[s][i].setCoefficient(portToProcessor[s][i][j],1.0);
                }
            }

        }

        //Processor Capacity
        //WrRj + sum_i PRij <= capacity_j
        processorCapacity = new MPConstraint[nProcessor];
        for(int j=0; j<nProcessor; j++){
            processorCapacity[j] = solver.makeConstraint(0.0, facilities[j].getMaxOutput(0), "PC"+j);
            for(int s=0; s<nSpecies; s++) {
                processorCapacity[j].setCoefficient(WCPOToProcessor[s][j], 1.0);
                for (int i = 0; i < nPort; i++) {
                    processorCapacity[j].setCoefficient(portToProcessor[s][i][j], 1.0);
                }
            }
        }

        //Packager (cannery) Capacity
        //WlCk + sum_j RCjk <= capacity_k
        packagerCapacity = new MPConstraint[nPackager];
        for(int k=0; k<nPackager; k++) {
            packagerCapacity[k] = solver.makeConstraint(0.0, facilities[k].getMaxOutput(1), "CC" + k);
            for (int s = 0; s < nSpecies; s++) {
                packagerCapacity[k].setCoefficient(WCPOToPackager[s][k], 1.0);
                for (int j = 0; j < nPackager; j++) {
                    packagerCapacity[k].setCoefficient(processorToPackager[s][j][k], 1.0);
                }
            }
        }
        //Loin Production
        loinProduction = new MPConstraint[nSpecies][nProcessor];
        //CTA_j*WrRj + CTA_j*sum_i(PRij) - sum_k RCjk - sum_l RMjl >= 0
        for(int s=0; s<nSpecies; s++){
            for(int j=0; j<nProcessor; j++) {
                loinProduction[s][j] = solver.makeConstraint(0.0, MPSolver.infinity(), "LP" +s+"_"+ j);
                loinProduction[s][j].setCoefficient(WCPOToProcessor[s][j], facilities[j].getTransformationAbility(s));
                for (int i = 0; i < nPort; i++) {
                    loinProduction[s][j].setCoefficient(portToProcessor[s][i][j], facilities[j].getTransformationAbility(s));
                }
                for (int k = 0; k < nPackager; k++) {
                    loinProduction[s][j].setCoefficient(processorToPackager[s][j][k], -1.0);
                }
                for (int l = 0; l < nMarkets; l++) {
                    loinProduction[s][j].setCoefficient(processorToMarket[s][j][l], -1.0);
                }
            }
       }

        //Package Production
        packageProduction = new MPConstraint[nPackager];
        //WlCk + sum_j RCjk -sum_l CMkl >=0
        for(int k=0; k<nPackager; k++){
            packageProduction[k] = solver.makeConstraint(0.0, MPSolver.infinity(), "PP"+k);
            for(int s=0; s<nSpecies;s++){
                packageProduction[k].setCoefficient(WCPOToPackager[s][k], 1.0);
                for(int j=0; j< nProcessor; j++){
                    packageProduction[k].setCoefficient(processorToPackager[s][j][k], 1.0);
                }
            }
            for(int l=0; l<nMarkets; l++){
                packageProduction[k].setCoefficient(packagerToMarket[k][l], -1.0);
            }
        }

        //Market Demands - Loins
        marketDemandLoin = new MPConstraint[nSpecies][nMarkets];
        //WlMl + sum_j RMjl = DLl
        for(int s=0; s<nSpecies; s++){
            for(int l=0; l< nMarkets; l++){
                marketDemandLoin[s][l] = solver.makeConstraint(markets[l].getDemandLoin(s),
                        markets[l].getDemandLoin(s),
                        "MDL"+s+"_"+l);
                marketDemandLoin[s][l].setCoefficient(WCPOToMarketLoin[s][l], 1.0);
                for(int j=0; j<nProcessor; j++){
                    marketDemandLoin[s][l].setCoefficient(processorToMarket[s][j][l], 1.0);
                }
            }
        }

        //Market Demands - Packaged
        marketDemandPackage = new MPConstraint[nMarkets];
        //WcMl + sum_k CMkl = DCl
        for(int l=0; l< nMarkets; l++){
            marketDemandPackage[l] = solver.makeConstraint(markets[l].getDemandPackaged(0),
                    markets[l].getDemandPackaged(0),
                    "MDC"+l);
            marketDemandPackage[l].setCoefficient(WCPOToMarketPackaged[l], 1.0);
            for(int k=0; k<nPackager; k++){
                marketDemandPackage[l].setCoefficient(packagerToMarket[k][l], 1.0);
            }
        }
    }

    public void setObjective(){

        objective = solver.objective();
        //Minimize total costs!!!
        //transportation costs ports -> processor
        //tc_ij * PR_ij
        for(int s=0; s<nSpecies; s++){
            for(int i=0; i<nPort; i++){
                for(int j=0; j<nProcessor; j++){
                    objective.setCoefficient(portToProcessor[s][i][j], getCostPort2Processor(s,i,j));
                }
            }
        }

        //transportation costs processor -> packager
        //tc_jk * RC_jk
        for(int s=0; s<nSpecies; s++){
            for(int j=0; j<nProcessor; j++){
                for(int k=0; k<nPackager; k++){
                    objective.setCoefficient(processorToPackager[s][j][k], getCostsProcessor2Packager(s,j,k));
                }
            }
        }

        //transportation costs processor -> market
        //tc_jl * RM_jl
        for(int s=0; s<nSpecies; s++){
            for(int j=0; j<nProcessor; j++){
                for(int l=0; l<nMarkets; l++){
                    objective.setCoefficient(processorToMarket[s][j][l], getCostsProcessor2Market(s,j,l));
                }
            }
        }

        //transportation costs packager -> market
        //tc_kl * CM_kl
        for(int k=0; k<nPackager; k++){
            for(int l=0; l<nMarkets; l++){
                objective.setCoefficient(packagerToMarket[k][l], getCostsPackager2Market(k,l));
            }
        }

        //processing costs loins
        //lc_j * (WrRj + PR_ij)
        for(int s=0; s<nSpecies; s++){
            for(int j=0; j< nProcessor; j++){
                objective.setCoefficient(WCPOToProcessor[s][j],facilities[j].getProcessingCost(0));
                for(int i=0; i<nPort; i++){
                    objective.setCoefficient(portToProcessor[s][i][j], facilities[j].getProcessingCost(0));
                }
            }
        }

        //processing costs packages
        //cc_k * (WlCk + RC_jk)
        for(int s=0; s<nSpecies; s++){
            for(int k=0; k<nPackager; k++){
                objective.setCoefficient(WCPOToPackager[s][k], facilities[k].getProcessingCost(1));
                for(int j=0; j< nProcessor; j++){
                    objective.setCoefficient(processorToPackager[s][j][k],facilities[k].getProcessingCost(1));
                }
            }
        }

        //import costs WCPO -> processor
        //WrCk * (cwr + tw_j)
        for(int s=0; s<nSpecies; s++){
            for(int j=0; j<nProcessor; j++){
                objective.setCoefficient(WCPOToProcessor[s][j], getCostsWCPO2Processor(s,j) + productCost[WCPOindex][s][RAW] );
            }
        }

        //import costs WCPO -> packager
        //WlCk * (cwl + tw_k)
        for(int s=0; s<nSpecies; s++){
            for(int k=0; k<nPackager; k++){
                objective.setCoefficient(WCPOToPackager[s][k], getCostsWCPO2Packager(s,k)+productCost[WCPOindex][s][LOIN] );
            }
        }

        //import costs WCPO -> market loins
        //WlMl * (cwl+twl_l)
        //import costs WCPO -> market packaged
        //WcMl * (cwc+twc_l)
        for(int l=0; l<nMarkets; l++){
            for(int s=0; s<nSpecies; s++){
                objective.setCoefficient(WCPOToMarketLoin[s][l], productCost[WCPOindex][s][LOIN]+getCostsWCPO2Market(s, LOIN, l));
            }
            objective.setCoefficient(WCPOToMarketPackaged[l], productCost[WCPOindex][0][PACKAGE]+getCostsWCPO2Market(0, PACKAGE, l));
        }
        objective.setMinimization();
    }

    private double getCostsWCPO2Packager(int s, int k) {
        int originIndex = WCPOindex;
        int destIndex = facilities[k].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, LOIN, s));
    }

    private double getCostsWCPO2Market(int s, int product, int l) {
        int originIndex = WCPOindex;
        int destIndex = markets[l].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, product, s));
    }

    private double getCostsWCPO2Processor(int s, int j) {
        int originIndex = WCPOindex;
        int destIndex = facilities[j].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, RAW, s));
    }

    private double getCostsPackager2Market(int k, int l) {
        int originIndex = facilities[k].getLocationIndex();
        int destIndex = markets[l].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, PACKAGE,0));
    }

    private double getCostsProcessor2Market(int s, int j, int l) {
        int originIndex = facilities[j].getLocationIndex();
        int destIndex = markets[l].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, LOIN,s));
    }

    private double getCostsProcessor2Packager(int s, int j, int k) {
        int originIndex = facilities[j].getLocationIndex();
        int destIndex = facilities[k].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, LOIN, s));
    }

    private double getCostPort2Processor(int s, int i, int j) {
        int originIndex = ports[i].getLocationIndex();
        int destIndex = facilities[j].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, RAW, s));
    }

    //calculate transportation costs per tonne, including tariff; does not include cost of tuna product itself
    public double calculateTransportCosts(int originIndex, int destIndex, int product, int species){
        double tc = transportCostMatrix[originIndex][destIndex];
        double tariff = getTariffRate(originIndex, destIndex, species, product);
        double cost_per_tonne = productCost[originIndex][species][product];
        return tc*1000+cost_per_tonne*tariff;
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
        for(int s=0; s<nSpecies; s++){
            for(int i=0; i<nPort; i++){
                System.out.println("Species "+s+", "+ports[i].getName()+":"+ -landingsPerPort[s][i].dualValue());

            }
        }
    }

    public double[] getExVesselPrices(int s) {
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            double[] prices = new double[nPort];
            for(int i=0; i<nPort; i++){
                prices[i]=-landingsPerPort[s][i].dualValue();
            }
            return prices;
        } else {
            return new double[]{};
        }
    }

    public double[][] getPortTransfers(int s){
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            double[][] transfers = new double[nPort][nProcessor];
            for(int i=0; i<nPort; i++){
                for(int j=0; j<nProcessor; j++){
                    transfers[i][j]=portToProcessor[s][i][j].solutionValue();
                }
            }
            return transfers;
        } else {
            return new double[][]{{}};
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

    public List<GenericTransportCost> readTransportCostsFromCSV(String path){
        List<GenericTransportCost> costs = new ArrayList<>();
        Path pathToFile = Paths.get(path);
        try(BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)){
            String line = br.readLine(); //First line is a header
            line = br.readLine();
            while(line != null){
                String[] attributes = line.split(",");
                GenericTransportCost cost = createTransportCost(attributes);
                costs.add(cost);
                line=br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return costs;
    }

    public List<GenericImportTariff> readTariffsFromCSV(String path){
        List<GenericImportTariff> tariffs = new ArrayList<>();
        Path pathToFile = Paths.get(path);
        try(BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)){
            String line = br.readLine(); //First line is a header
            line = br.readLine();
            while(line != null){
                String[] attributes = line.split(",");

                int tariffIndex = findTariff(tariffs, attributes[0],attributes[1]);
                if(tariffIndex==-1){
                    GenericImportTariff tariff = createTariff(attributes);
                    tariffs.add(tariff);
                } else {
                    String species = attributes[2];
                    int speciesIndex = getIndex(speciesNames, species);
                    double rateRaw = Double.parseDouble(attributes[3]);
                    double rateLoin =  Double.parseDouble(attributes[4]);
                    double ratePackaged  = Double.parseDouble(attributes[5]);
                    tariffs.get(tariffIndex).updateRates(speciesIndex,
                            rateRaw,
                            rateLoin,
                            ratePackaged);
                }

                line=br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return tariffs;

    }

    public int getLocationIndex(String locationName){
        return getIndex(locationNames, locationName);
    }

    private static int getIndex(List<String> list, String token){
        if(list.contains(token)){
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
        for(int i=0; i<(data.length-2)/2; i++){
            int speciesIndex = getIndex(speciesNames, data[i*2+2]);
            landings[speciesIndex] = Double.parseDouble(data[i*2+3]);
        }
        return new GenericPort(name, location, locationIndex, landings);
    }

    private  GenericProcessor createFacility(String[] data){
        String name = data[0];
        String location = data[1];
        int locationIndex = getIndex(locationNames, location);
        double coldStorage = Double.parseDouble(data[2]);
        double CTA = Double.parseDouble(data[3]);
        double maxLoining = Double.parseDouble(data[4]);
        double maxCanning = Double.parseDouble(data[5]);
        return new GenericProcessor(name, location, locationIndex, new double[]{maxLoining,maxCanning},new double[]{CTA}, new double[]{41.52, 87.80-41.52} );
    }

    private GenericTransportCost createTransportCost(String[] data){
        String origin = data[0];
        String destination = data[1];
        double cost = Double.parseDouble(data[2]);
        return new GenericTransportCost(getIndex(locationNames, origin),
                getIndex(locationNames, destination),
                cost);
        }

    private int findTariff(List<GenericImportTariff> tariffs, String origin, String destination){
        for(int i=0; i< tariffs.size(); i++){
            GenericImportTariff tariff = tariffs.get(i);
            if(tariff.getOrigin() == getLocationIndex(origin) &&
            tariff.getDestination() == getLocationIndex(destination)) {
                return i;
            }
        }
        return -1;
    }
    private int findTariff(List<GenericImportTariff> tariffs, int origin, int destination){
        for(int i=0; i< tariffs.size(); i++){
            GenericImportTariff tariff = tariffs.get(i);
            if(tariff.getOrigin() == origin &&
                    tariff.getDestination() == destination) {
                return i;
            }
        }
        return -1;
    }

    private GenericImportTariff createTariff(String[] data){
        String origin = data[0];
        String destination = data[1];
        String species = data[2];
        int speciesIndex = getIndex(speciesNames, species);
        int nSpecies = getnSpecies();
        double[] rateRaw = new double[nSpecies];
        rateRaw[speciesIndex] = Double.parseDouble(data[3]);
        double[] rateLoin = new double[nSpecies];
        rateLoin[speciesIndex] = Double.parseDouble(data[4]);
        double[] ratePackaged = new double[nSpecies];
        ratePackaged[speciesIndex] = Double.parseDouble(data[5]);
        return new GenericImportTariff(getIndex(locationNames, origin),
                getIndex(locationNames, destination),
                rateRaw, rateLoin, ratePackaged);
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
        return new GenericMarket(name, location, locationIndex, loinDemand, new double[]{canDemand} );
    }

    public int getnLocations(){
        return (locationNames.size());
    }
    public int getnSpecies(){
        return (speciesNames.size());
    }

    public void setPorts(List<GenericPort> ports){
        this.ports = new GenericPort[ports.size()];
        this.ports = ports.toArray(this.ports);
    }

    public void setFacilities(List<GenericProcessor> facilities){
        this.facilities = new GenericProcessor[facilities.size()];
        this.facilities = facilities.toArray(this.facilities);
    }

    public void setMarkets(List<GenericMarket> markets){
        this.markets = new GenericMarket[markets.size()];
        this.markets = markets.toArray(this.markets);
    }

    public void setTransportCosts(double[][] transportCostMatrix) {
        this.transportCostMatrix = transportCostMatrix;
    }

    public void initializeWCPO(int wcpoIndex, double wcpoCostRaw, double wcpoCostLoin, double wcpoCostPackaged) {
        this.WCPOindex = wcpoIndex;
        for(int s=0; s<getnSpecies(); s++){
            this.productCost[WCPOindex][s][RAW]=wcpoCostRaw;
            this.productCost[WCPOindex][s][LOIN]=wcpoCostLoin;
            this.productCost[WCPOindex][s][PACKAGE]=wcpoCostPackaged;
        }
    }

    public void setTariffs(List<GenericImportTariff> tariffs){
        this.tariffs =tariffs;
    }

    public double getTariffRate(int origin, int destination, int species, int product) {
        if(product==RAW) {
            return getTariffRate(origin, destination, species, "raw");
        }else if (product==LOIN){
            return getTariffRate(origin, destination, species, "loin");
        }else if(product==PACKAGE){
            return getTariffRate(origin, destination, species, "packaged");
        }
        else return 0;
    }
    public double getTariffRate(int origin, int destination, int species, String product){
        int index = findTariff(this.tariffs, origin, destination);
        if(index==-1){
            return 0;
        } else {
            double rate=0;
            switch(product){
                case "raw":
                    rate = tariffs.get(index).getRate_raw(species);
                    break;
                case "loin":
                    rate = tariffs.get(index).getRate_loin(species);
                    break;
                case "packaged":
                    rate =tariffs.get(index).getRate_packaged(species);
                    break;
            }
            return rate;
        }
    }

    public void setProductCosts(double costRaw, double costLoin, double costPackaged) {
        this.productCost = new double[getnLocations()][getnSpecies()][3];
        for(int l=0; l<getnLocations(); l++){
            for(int s=0; s<getnSpecies(); s++){
                this.productCost[l][s][RAW]=costRaw;
                this.productCost[l][s][LOIN]=costLoin;
                this.productCost[l][s][PACKAGE]=costPackaged;
            }
        }
    }
}

