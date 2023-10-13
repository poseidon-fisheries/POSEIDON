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
    int     nSpecies=3,
            nPort,
            nProcessor,
            nPackager,
            nMarkets;

    MPSolver solver;

    //Inputs
    //Ports - each has landings
    GenericPort[] ports;

    //Combined facilities - act as processors and packagers
    GenericProcessor[] facilities;

    //Markets
    GenericMarket[] markets;

    //TransportationCosts
    double[][] transportCostMatrix;

    //External Trade Partners
    double[][][] productCost;
    boolean wcpoRaw=false, wcpoLoinProcessor=true, wcpoLoinMarket=true, wcpoPackageMarket=true;
    //Raw, localCostLoin, localCostPackaged;
    double WCPORetailLoin;
    double WCPORetailPackage;

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

    public void initializeModel(String portsFile,
                                String facilitiesFile,
                                String marketsFile,
                                String transportationCostsFile,
                                String tariffsFile,
                                double[] productCosts,
                                double[] wcpoCosts){
        setPorts(readPortsFromCSV(portsFile));
        setFacilities(readFacilitiesFromCSV(facilitiesFile));
        setMarkets(readMarketsFromCSV(marketsFile));
        setTransportCosts(createTransportCostMatrixFromCSV(transportationCostsFile));
        setTariffs(readTariffsFromCSV(tariffsFile));
        setProductCosts(productCosts[0],productCosts[1],productCosts[2]);
        initializeWCPO(getLocationIndex("Thailand"), wcpoCosts[0], wcpoCosts[1], wcpoCosts[1], wcpoCosts[2]);
    }

    //-----------------------------------------------------------
    // Linear Program Methods
    //-----------------------------------------------------------

    public void initializeLP(){
        this.nSpecies = getnSpecies();
        this.nPort = ports.length;
        this.nProcessor = facilities.length;
        this.nPackager = facilities.length;
        this.nMarkets = markets.length;

        Loader.loadNativeLibraries();
        solver  = MPSolver.createSolver("GLOP");

        //Create Variables
        portToProcessor = new MPVariable[nSpecies][nPort][nProcessor];          //PR_sij
        processorToPackager = new MPVariable[nSpecies][nProcessor][nPackager];  //RC_sjk
        processorToMarket = new MPVariable[nSpecies][nProcessor][nMarkets];     //RM_sjl
        WCPOToProcessor = new MPVariable[nSpecies][nProcessor];                 //WrR_sj
        WCPOToPackager = new MPVariable[nSpecies][nPackager];                   //WlC_sk
        packagerToMarket = new MPVariable[nPackager][nMarkets];                 //CM_kl
        WCPOToMarketPackaged = new MPVariable[nMarkets];                        //WcM_l
        WCPOToMarketLoin = new MPVariable[nSpecies][nMarkets];                  //WlM_sl

        for(int s=0; s<nSpecies; s++){
            for(int i=0; i<nPort; i++){
                for(int j=0; j< nProcessor; j++){
                    portToProcessor[s][i][j] = solver.makeNumVar(0, MPSolver.infinity(), "PR"+s+"_"+i+"_"+j);
                }
            }
            for(int j=0; j<nProcessor; j++){
                WCPOToProcessor[s][j] = solver.makeNumVar(0, (wcpoRaw?MPSolver.infinity():0), "WrR"+s+"_"+j);
                for(int k=0; k< nPackager; k++){
                    processorToPackager[s][j][k] = solver.makeNumVar(0, MPSolver.infinity(), "RC"+s+"_"+j+"_"+k);
                }
                for(int l=0; l< nMarkets; l++){
                    processorToMarket[s][j][l] = solver.makeNumVar(0, MPSolver.infinity(), "RM"+s+"_"+j+"_"+l);
                }
            }
        }

        for(int k=0; k< nPackager; k++){
            for(int s=0; s<nSpecies; s++){
                WCPOToPackager[s][k] = solver.makeNumVar(0, (wcpoLoinProcessor?MPSolver.infinity():0), "WlC"+s+"_"+k);
            }
            for(int l=0; l< nMarkets; l++){
                packagerToMarket[k][l] = solver.makeNumVar(0, MPSolver.infinity(), "CM"+k+"_"+l);
            }
        }

        for(int l=0; l< nMarkets; l++){
            for(int s=0; s<nSpecies; s++){
                WCPOToMarketLoin[s][l] = solver.makeNumVar(0, (wcpoLoinMarket?MPSolver.infinity():0), "WlM"+s+"_"+l);
            }
            WCPOToMarketPackaged[l] = solver.makeNumVar(0, (wcpoPackageMarket?MPSolver.infinity():0), "WcM"+l);
        }
    }

    public void establishConstraints(){
        //Landings per port
        //Sum of port-to-processor must not exceed landings at port
        landingsPerPort = new MPConstraint[nSpecies][nPort];
        //LPP_si: sum_j PR_sij <= Landings_si
        for(int s=0; s<nSpecies; s++){
            for(int i=0; i<nPort; i++){
                landingsPerPort[s][i] = solver.makeConstraint(0.0, ports[i].getLandings(s), "LPP"+s+"_"+i);
                for(int j=0; j<nProcessor; j++){
                    landingsPerPort[s][i].setCoefficient(portToProcessor[s][i][j],1.0);
                }
//                System.out.println(stringifyConstraint(landingsPerPort[s][i]));
            }
        }



        //Processor Capacity
        //WrRj + sum_i PRij <= capacity_j

        //Actually I think the left hand side should be scaled by CTA
        processorCapacity = new MPConstraint[nProcessor];
        for(int j=0; j<nProcessor; j++){
            processorCapacity[j] = solver.makeConstraint(0.0, facilities[j].getMaxOutput(0), "PC"+j);
            for(int s=0; s<nSpecies; s++) {
//                processorCapacity[j].setCoefficient(WCPOToProcessor[s][j], 1.0);
                processorCapacity[j].setCoefficient(WCPOToProcessor[s][j], facilities[j].getTransformationAbility(s));
                for (int i = 0; i < nPort; i++) {
//                    processorCapacity[j].setCoefficient(portToProcessor[s][i][j], 1.0);
                    processorCapacity[j].setCoefficient(portToProcessor[s][i][j], facilities[j].getTransformationAbility(s));
                }
            }
//            System.out.println(stringifyConstraint(processorCapacity[j]));
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
//            System.out.println(stringifyConstraint(packagerCapacity[k]));
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
//                System.out.println(stringifyConstraint(loinProduction[s][j]));
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
//            System.out.println(stringifyConstraint(packageProduction[k]));
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
//                System.out.println(stringifyConstraint(marketDemandLoin[s][l]));
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
//            System.out.println(stringifyConstraint(marketDemandPackage[l]));
        }
    }

    public void setObjective(){
        int nSpecies = getnSpecies();
        int nPort = ports.length;
        int nProcessor = facilities.length;
        int nPackager = facilities.length;
        int nMarkets = markets.length;

        objective = solver.objective();
        //Minimize total costs!!!

        //transportation costs ports -> processor
        // plus processing costs for all raw tuna arriving
        //tc_ij * PR_ij
        //import costs WCPO -> processor
        // plus processing costs
        //WrCk * (cwr + tw_j + lc_j)
        for(int s=0; s<nSpecies; s++){
            for(int j=0; j<nProcessor; j++){
                for(int i=0; i<nPort; i++){
                    objective.setCoefficient(portToProcessor[s][i][j], getCostPort2Processor(s,i,j)+facilities[j].getProcessingCost(0));
                }
                objective.setCoefficient(WCPOToProcessor[s][j], getCostsWCPO2Processor(s,j) + productCost[WCPOindex][s][RAW] +facilities[j].getProcessingCost(0));
            }
        }

        //transportation costs processor -> packager
        // plus processing costs for loins arriving
        //tc_jk * RC_jk
        //WlCk * (cwl + tw_k + cc_k)
        for(int s=0; s<nSpecies; s++){
            for(int k=0; k<nPackager; k++){
                for(int j=0; j<nProcessor; j++){
                    objective.setCoefficient(processorToPackager[s][j][k], getCostsProcessor2Packager(s,j,k)+facilities[k].getProcessingCost(1));
                }
                objective.setCoefficient(WCPOToPackager[s][k], getCostsWCPO2Packager(s,k)+productCost[WCPOindex][s][LOIN] +facilities[k].getProcessingCost(1));
            }
        }


        //transportation costs processor -> market
        //tc_jl * RM_jl
        //import costs WCPO -> market loins
        //WlMl * (cwl+twl_l)
        for(int s=0; s<nSpecies; s++){
            for(int l=0; l<nMarkets; l++){
                for(int j=0; j<nProcessor; j++){
                    objective.setCoefficient(processorToMarket[s][j][l], getCostsProcessor2Market(s,j,l));
                }
                objective.setCoefficient(WCPOToMarketLoin[s][l], getCostsWCPO2Market(s, LOIN, l) + WCPORetailLoin);
            }
        }

        //transportation costs packager -> market
        //tc_kl * CM_kl
        //import costs WCPO -> market packaged
        //WcMl * (cwc+twc_l)
        for(int l=0; l<nMarkets; l++){
            for(int k=0; k<nPackager; k++){
                objective.setCoefficient(packagerToMarket[k][l], getCostsPackager2Market(k,l));
            }
            objective.setCoefficient(WCPOToMarketPackaged[l], getCostsWCPO2Market(0, PACKAGE, l)+WCPORetailPackage);
        }

        objective.setMinimization();
//        System.out.println(stringifyObjective());
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


    //-----------------------------------------------------------
    // Create Output Strings & Print Info
    //-----------------------------------------------------------

    public String stringifyConstraint(MPConstraint constraint){
        String output = constraint.name() + ": "+ constraint.lb() + "<=";
        boolean first=true;
        for(MPVariable var:solver.variables()){
            if(constraint.getCoefficient(var)!=0){
                if(first==false) output +=" + ";
                first=false;
                output += constraint.getCoefficient(var) + var.name();
            }
        }
        output += " <= " + constraint.ub();
        return output;
    }

    public String stringifyObjective(){
        String output =  "Cost: ";
        boolean first=true;
        for(MPVariable var:solver.variables()){
            if(objective.getCoefficient(var)!=0){
                if(first==false) output +=" + ";
                first=false;
                output += Math.round(objective.getCoefficient(var)) + var.name();
            }
        }
        return output;
    }

    public String generateSolutionNetwork(){
        String output = "from,to,species,weight,category\n";
        //Raw transfers
        for(int s=0; s<nSpecies; s++){
            for(int j=0; j<nProcessor; j++){
                for(int i=0; i<nPort; i++){
                    output += ports[i].getLocation() + "_PORT," + facilities[j].getLocation()+"_PROCESSOR,"+speciesNames.get(s)+","+Math.round(portToProcessor[s][i][j].solutionValue())+",raw\n";
                }
                output += locationNames.get(WCPOindex) + "_IMPORTRAW," + facilities[j].getLocation()+"_PROCESSOR,"+speciesNames.get(s)+","+Math.round(WCPOToProcessor[s][j].solutionValue())+",raw\n";
            }
        }
        //Loin transfers
        for(int s=0; s<nSpecies; s++){
            for(int k=0; k<nPackager; k++){
                for(int j=0; j<nProcessor; j++){
                    output += facilities[j].getLocation() + "_PROCESSOR," + facilities[k].getLocation()+"_CANNERY,"+speciesNames.get(s)+","+Math.round(processorToPackager[s][j][k].solutionValue())+",loin\n";
                }
                output += locationNames.get(WCPOindex) + "_IMPORT," + facilities[k].getLocation()+"_CANNERY,"+speciesNames.get(s)+","+Math.round(WCPOToPackager[s][k].solutionValue())+",loin\n";
            }
        }
        for(int s=0; s<nSpecies; s++){
            for(int l=0; l<nMarkets; l++){
                for(int j=0; j<nProcessor; j++){
                    output += facilities[j].getLocation() + "_PROCESSOR," + markets[l].getLocation()+"_LOINMARKET,"+speciesNames.get(s)+","+Math.round(processorToMarket[s][j][l].solutionValue())+",loin\n";
                }
                output += locationNames.get(WCPOindex) + "_IMPORT," + markets[l].getLocation()+"_LOINMARKET,"+speciesNames.get(s)+","+Math.round(WCPOToMarketLoin[s][l].solutionValue())+",loin\n";
            }
        }
        //Package Transfers
        for(int l=0; l<nMarkets; l++){
            for(int k=0; k<nPackager; k++){
                output += facilities[k].getLocation() + "_CANNERY," + markets[l].getLocation()+"_MARKET,mixed,"+Math.round(packagerToMarket[k][l].solutionValue())+",canned\n";
            }
            output += locationNames.get(WCPOindex) + "_IMPORTCANNED," + markets[l].getLocation()+"_MARKET,mixed,"+Math.round(WCPOToMarketPackaged[l].solutionValue())+",canned\n";
        }
        return output;
    }

    public void printPort2ProcessorTC(){
        int nPort = ports.length, nProcessor=facilities.length;
        System.out.println("We have "+nPort + " ports");
        System.out.println("We have "+nProcessor + " facilities");
        for(int i=0; i<nPort; i++){
            for(int j=0; j<nProcessor; j++){
                System.out.println(ports[i].getName()+ "("+ports[i].getLocation()+") to "+facilities[j].getLocation()+" " + getCostPort2Processor(0,i,j));
            }
        }
    }

    //Actually Prints Shadow Prices
    public void printLandingsDual(){
        for(int s=0; s<nSpecies; s++){
            for(int i=0; i<nPort; i++){
                System.out.println("Species "+s+", "+ports[i].getLocation()+": "+ - Math.round(landingsPerPort[s][i].dualValue()*100)/100.0 );

            }
        }
    }

    public void printPorts() {
        String output = "Ports: ";
        for(GenericPort port:ports){
            output += port.getLocation() + "|";
        }
        System.out.println(output);
    }
    public void printFacilities(){
        String output = "Facilities: ";
        for(GenericProcessor facility:facilities){
            output+=facility.getLocation()+"|";
        }
        System.out.println(output);
    }
    public void printMarkets(){
        String output = "Markets: ";
        for(GenericMarket market:markets){
            output += market.getLocation();
        }
        System.out.println(output);
    }
    //-----------------------------------------------------------
    // Retrieve Data
    //-----------------------------------------------------------

    private double getCostsWCPO2Packager(int s, int k) {
        int originIndex = WCPOindex;
        int destIndex = facilities[k].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, LOIN, s));
    }

    private double getCostsWCPO2Market(int s, int product, int l) {
        int originIndex = WCPOindex;
        int destIndex = markets[l].getLocationIndex();
        double tc = transportCostMatrix[originIndex][destIndex];
        double tariff = getTariffRate(originIndex, destIndex, s, product);
        double cost_per_tonne = 0;
        if(product==1) cost_per_tonne=WCPORetailLoin;
        if(product==2) cost_per_tonne=WCPORetailPackage;
        return tc*1000+cost_per_tonne*tariff;
    }

    private double getCostsWCPO2Processor(int s, int j) {
        int originIndex = WCPOindex;
        int destIndex = facilities[j].getLocationIndex();
        return(calculateTransportCosts(originIndex, destIndex, RAW, s));
    }

    private double getCostsPackager2Market(int k, int l) {
        int originIndex = facilities[k].getLocationIndex();
        int destIndex = markets[l].getLocationIndex();
        if(markets[l].getName().equals("Latin America")){
            return 0;
        } else {
            return(calculateTransportCosts(originIndex, destIndex, PACKAGE,0));
        }
    }

    private double getCostsProcessor2Market(int s, int j, int l) {
        int originIndex = facilities[j].getLocationIndex();
        int destIndex = markets[l].getLocationIndex();
        if(markets[l].getName().equals("Latin America")){
            return 0;
        } else {
            return(calculateTransportCosts(originIndex, destIndex, LOIN,s));
        }
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

    //----------------------------------------------------------------------
    // Get Methods
    //----------------------------------------------------------------------
    public double getObjectiveValue(){
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            return objective.value();
        } else {
            return 0.0;
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

    public double[][] getProcessorTransfers(int s){
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            double[][] transfers = new double[nProcessor][nPackager];
            for(int k=0; k<nPackager; k++){
                for(int j=0; j<nProcessor; j++){
                    transfers[j][k]=processorToPackager[s][j][k].solutionValue();
                }
            }
            return transfers;
        } else {
            return new double[][]{{}};
        }
    }

    public double[][] getMarketSupply(int s){
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            double[][] transfers = new double[nProcessor+1][nMarkets];
            for(int l=0; l<nMarkets; l++){
                for(int j=0; j<nProcessor; j++){
                    transfers[j][l]=processorToMarket[s][j][l].solutionValue();
                }
                transfers[nProcessor][l]=WCPOToMarketLoin[s][l].solutionValue();
            }
            return transfers;
        } else {
            return new double[][]{{}};
        }
    }

    public double[][] getMarketSupplyPackages(){
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            double[][] transfers = new double[nPackager+1][nMarkets];
            for(int l=0; l<nMarkets; l++){
                for(int k=0; k<nPackager; k++){
                    transfers[k][l]=packagerToMarket[k][l].solutionValue();
                }
                transfers[nPackager][l]=WCPOToMarketPackaged[l].solutionValue();
            }
            return transfers;
        } else {
            return new double[][]{{}};
        }
    }

    public double[] getWCPOImports(int s, int product){
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            double[] output={};
            if(product==RAW) {
                output = new double[nProcessor];
                for (int j = 0; j < nProcessor; j++) {
                    output[j]=WCPOToProcessor[s][j].solutionValue();
                }
            } else if(product==LOIN) {
                output = new double[nPackager];
                for (int j = 0; j < nPackager; j++) {
                    output[j]=WCPOToPackager[s][j].solutionValue();
                }
            }
            return output;
        } else {
            return new double[]{};
        }

    }

    public int getLocationIndex(String locationName){
        return getIndex(locationNames, locationName);
    }

    public String getLocationName(int locationIndex){
        return locationNames.get(locationIndex);
    }

    private static int getIndex(List<String> list, String token){
        if(list.contains(token)){
            return (list.indexOf(token));
        } else {
            list.add(token);
            return (list.indexOf(token));
        }
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

    public int getnLocations(){
        return (locationNames.size());
    }
    public int getnSpecies(){
        return (speciesNames.size());
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

    public int getPortLocationIndex(int i) {
        return ports[i].getLocationIndex();
    }

    public int getFacilityLocationIndex(int i) {
        return facilities[i].getLocationIndex();
    }

    //---------------------------------------------------------------------
    // Data File Read Methods
    //----------------------------------------------------------------------
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

    public double[][] createTransportCostMatrixFromCSV(String path){
        List<GenericTransportCost> transportCosts = readTransportCostsFromCSV(path);
        double[][] transportCostMatrix = new double[getnLocations()][getnLocations()];
        for(GenericTransportCost tc : transportCosts){
            transportCostMatrix[tc.getOrigin()][tc.getDestination()] = tc.getCost();
        }
        return transportCostMatrix;
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

    //--------------------------------------------------------------------
    // Component Creation Methods
    //--------------------------------------------------------------------
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

    //-------------------------------------------------------
    // Set Methods
    //--------------------------------------------------------
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

    public void initializeWCPO(int wcpoIndex, double wcpoCostRaw, double wcpoCostLoin, double wcpoRetailLoin, double wcpoRetailPackage) {
        this.WCPOindex = wcpoIndex;
        for(int s=0; s<getnSpecies(); s++){
            this.productCost[WCPOindex][s][RAW]=wcpoCostRaw;
            this.productCost[WCPOindex][s][LOIN]=wcpoCostLoin;
            this.productCost[WCPOindex][s][PACKAGE]=wcpoRetailPackage;
        }
        this.WCPORetailLoin = wcpoRetailLoin;
        this.WCPORetailPackage=wcpoRetailPackage;
    }

    public void setTariffs(List<GenericImportTariff> tariffs){
        this.tariffs =tariffs;
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

    public void enableWCPOImports(){
        wcpoRaw=true;
        wcpoLoinProcessor=true;
        wcpoLoinMarket=true;
        wcpoPackageMarket=true;
    }

}

