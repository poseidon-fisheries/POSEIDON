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

package uk.ac.ox.oxfish.model.scenario;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.YellowBycatchWithHistoryFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HoldLimitingDecoratorFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomTrawlStringFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.AlwaysDiscardTheseSpeciesFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.ports.PortListFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.event.BiomassDrivenFixedExogenousCatches;
import uk.ac.ox.oxfish.model.market.factory.ArrayFixedPriceMarket;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.*;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import static uk.ac.ox.oxfish.model.scenario.CaliforniaAbstractScenario.LITERS_OF_GAS_CONSUMED_PER_HOUR;

/**
 * The Phil Levin two populations model. In reality just a facade on the usual two population scenarios
 * (with custom port - suppliers)
 * Created by carrknight on 3/13/17.
 */
public class SimpleCaliforniaScenario extends TwoPopulationsScenario {




    /**
     * comes from stock assessment inputs prepared by Kristin (sablecathts.csv) to which I remove
     * the 2000 tonnes of sablefish that are represented in this model
     */
    private double exogenousSablefishCatches = 4187 * 1000;

    /**
     * if we assume all commercial landings are from this fishery and all recreational landings are exogenous
     * we get these numbers (from the YelloeyeCatchesfromAssessmentState.xlsx) report
     */
    private double exogenousYelloweyeCatches = 10.317*1000d;

    private AlgorithmFactory<? extends Regulation> regulationsToImposeAtStartYear = new MultipleRegulationsFactory();

    {
        //start with anarchy
        super.setRegulationLarge(new AnarchyFactory());
        super.setRegulationSmall(new AnarchyFactory());

        regulationsToImposeAtStartYear = new MultipleRegulationsFactory();
        MultipleRegulationsFactory local = ((MultipleRegulationsFactory) regulationsToImposeAtStartYear);
        local.getTags().clear();
        local.getFactories().clear();
        //there is a season of 213 days
        local.getTags().add("all");
        local.getFactories().add(new FishingSeasonFactory(213,false));
        //there is a set of individual quotas
        //the average landings were 1828.92828364222
        //that would mean (ignoring tiers) 20.321425374 tonnes per boat
        //with an allocation of 2,000 tonnes a year:
        IQMonoFactory iq = new IQMonoFactory();
        iq.setIndividualQuota(new FixedDoubleParameter((2000*1000)/90));
        local.getFactories().add(iq);
        local.getTags().add("all");

        //RCA
        local.getTags().add("all");
        ProtectedAreasOnlyFactory factory = new ProtectedAreasOnlyFactory();
        factory.getStartingMPAs().add(new StartingMPA(6,0,3,100));
        local.getFactories().add(factory);

    }


    {
        YellowBycatchWithHistoryFactory biologyInitializer = new YellowBycatchWithHistoryFactory();

        this.setAllowTwoPopulationFriendships(true);


        //2001 start, if needed!
        biologyInitializer.setHistoricalTargetBiomass(Lists.newArrayList(
                527154000d,527105779d,527033675d,526937145d,526819309d,526679414d,526563028d,526467416d,526392560d,526337557d,526243592d,526110372d,525941820d,525738694d,525502749d,
                525235725d,524940279d,523750897d,521807115d,519028892d,518183379d,517707156d,516809380d,516662376d,515626553d,514582427d,513397054d,512674141d,511423759d,510525199d,
                509507192d,508278705d,508102873d,507775981d,507491398d,506569660d,505125433d,504494266d,503975133d,503628931d,502963312d,502927457d,502782061d,501560771d,499724770d,
                497712445d,495422136d,493526237d,493495066d,492918991d,492646142d,492560761d,491204268d,491212648d,491585614d,491276018d,491098354d,489641827d,489181485d,489750949d,
                489615770d,488813834d,489120929d,488538202d,488842424d,488890144d,489039154d,489251476d,487090459d,486562540d,483366313d,481078276d,479618827d,475399328d,472254781d,
                466454189d,455081889d,433774184d,429505494d,421974007d,402971360d,399900476d,394650983d,381593314d,373460329d,366503129d,359845028d,354395185d,349711490d,347253956d,
                345399509d,344891059d,343993698d,343317882d,344025143d,345403773d,346390724d,346833531d,347657176d,352224351d,354280712d,356442219d
        ));
        biologyInitializer.setHistoricalTargetSurvival(Lists.newArrayList(0.906926410977892,0.90533206481522));
        /*
        "65" 1980 5884.75390821775 7570.39464071883 80.6977521701088 0.911643504584384 267.4
        "66" 1981 5684.81400583123 7539.11477741686 80.5797120779755 0.893149367230115 368.4
        "67" 1982 5388.82436349019 7501.04165097297 80.4351798236472 0.872913453252916 463.4
        "68" 1983 5003.39439583719 7461.07666659014 80.2824444569843 0.891801980573978 331.3
        "69" 1984 4755.7256703365 7354.80311652805 79.871124821994 0.899453305071633 276.8
        "70" 1985 4568.16526592639 7287.65567320182 79.6072914957053 0.886236958581081 329.1
        "71" 1986 4333.32840621945 7192.95479081678 79.2298782779113 0.906751653402573 219.1
        "72" 1987 4213.46491242035 7068.73430289459 78.7251436373954 0.898079385164327 251.3
        "73" 1988 4065.55744324838 6952.29316016972 78.2417453197403 0.889382661805629 279.5
        "74" 1989 3892.9123125654 6819.62353737464 77.678446943645 0.869935751715077 346.9
        "75" 1990 3655.63133792404 6699.58495536759 77.1568870268711 0.8930188400302 237.4
        "76" 1991 3531.3892303894 6555.81482839977 76.5167956590274 0.855516923649324 368
        "77" 1992 3278.22685605126 6409.65515493355 75.8481677369505 0.846432838984651 372.8
        "78" 1993 3021.32461787125 6144.85255271959 74.5883473973764 0.854299005073592 318.7
        "79" 1994 2820.02768880825 5884.75390821775 73.2860592436526 0.879382609525879 223.4
        "80" 1995 2716.72810126806 5684.81400583123 72.2380652253549 0.86553778449065 254.6
        "81" 1996 2582.87302878858 5388.82436349019 70.6058058792285 0.874543511909947 217.7
        "82" 1997 2485.78587079967 5003.39439583719 68.3217713609866 0.861256675081106 244.1
        "83" 1998 2361.38936282188 4755.7256703365 66.749550624956 0.911764934223519 107
        "84" 1999 2376.07942409806 4568.16526592639 65.4993369992089 0.892417837419521 155.8
        "85" 2000 2341.24361654434 4333.32840621945 63.8558847024621 0.938356073678119 40.9
        "86" 2001 2423.36370656817 4213.46491242035 62.9813932752828 0.932931166788219 56.1
         */
        biologyInitializer.setHistoricalBycatchBiomass(Lists.newArrayList(
                8883000d,8879174d,8873953d,8868155d,8864445d,8860530d,8856710d,8853086d,8849360d,
                8845047d,8838688d,8831406d,8823224d,8813870d,8800921d,8787570d,8776476d,8765928d,
                8757796d,8747619d,8735145d,8718657d,8702657d,8687139d,8676014d,8657811d,8635120d,
                8606655d,8534448d,8463244d,8344813d,8248938d,8216305d,8177412d,8154640d,8129782d,
                8097658d,8071355d,8053628d,8028939d,7995605d,7953450d,7903732d,7849827d,7802497d,
                7760358d,7727444d,7686846d,7657372d,7639984d,7543438d,7510307d,7470365d,7428501d,
                7320928d,7252466d,7156663d,7031742d,6914782d,6781853d,6661574d,6517739d,6371633d,
                6108281d,5850281d,5652339d,5359377d,4978877d,4735490d,4550998d,4318958d,4199800d,
                4051556d,3878241d,3641386d,3516316d,3264065d,3009226d,2809955d,2706916d,2572423d,
                2472791d,2345994d,2355185d,2313621d,2385708d

        ));
        biologyInitializer.setHistoricalBycatchSurvival(Lists.newArrayList(
                0.932582202042579,
                0.938156881691037

        ));

        biologyInitializer.setVerticalSeparator(new FixedDoubleParameter(5));
        this.setBiologyInitializer(biologyInitializer);




        //assuming all agents are just long-liners a la WFS
        this.setSmallHoldSize(new FixedDoubleParameter(6500));
        this.setLargeHoldSize(new FixedDoubleParameter(6500));
        this.setSmallSpeed(new FixedDoubleParameter(16.0661));
        this.setLargeSpeed(new FixedDoubleParameter(16.0661));
        //this is the original 1.21 gallons a mile in California we transformed in liters per km
        this.setSmallLitersPerKilometer(new FixedDoubleParameter(3.418019));
        this.setLargeLitersPerKilometer(new FixedDoubleParameter(3.418019));
        //infinite fuel size
        this.setSmallFuelTankSize(new FixedDoubleParameter(100000000));
        this.setLargeFuelTankSize(new FixedDoubleParameter(100000000));
        //1.67700 $/gallon in 2001 to 0.3688 $/litre
        this.setGasPricePerLiter(new FixedDoubleParameter(0.3689));
        //set prices correctly!
        ArrayFixedPriceMarket market = new ArrayFixedPriceMarket();
        //according to the report sablefish sold for 2$/lbs in 2001 (in 2013 money);
        //this means 1.52$ in 2001 money.
        //which translates into 3.35102639 $/kg
        market.setPrices("3.35102639,0");
        this.setMarket(market);

        //ratio width/height comes from the original california bathymetry
        //size of the cell is assuming max 120km distance to fish
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(100));
        mapInitializer.setWidth(new FixedDoubleParameter(11));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        mapInitializer.setCellSizeInKilometers(new FixedDoubleParameter(1547d/100d)); //1547 km from los angeles to seattle
        this.setMapInitializer(mapInitializer);


        HoldLimitingDecoratorFactory gear = new HoldLimitingDecoratorFactory();
        RandomTrawlStringFactory delegate = new RandomTrawlStringFactory();
        delegate.setCatchabilityMap("0:0.001,1:0.001");
        delegate.setTrawlSpeed(new FixedDoubleParameter(LITERS_OF_GAS_CONSUMED_PER_HOUR));
        gear.setDelegate(delegate);
        this.setGearLarge(gear);
        gear = new HoldLimitingDecoratorFactory();
        delegate = new RandomTrawlStringFactory();
        delegate.setCatchabilityMap("0:0.001,1:0.001");
        delegate.setTrawlSpeed(new FixedDoubleParameter(LITERS_OF_GAS_CONSUMED_PER_HOUR));
        gear.setDelegate(delegate);
        this.setGearSmall(gear);


        PortListFactory ports = new PortListFactory();
        ports.getPorts().clear();
        ports.getPorts().put("Washington","10,0");
        ports.getPorts().put("Oregon","10,49");
        ports.getPorts().put("California","10,99");
        this.setPorts(ports);

        //these numbers are from the
        /*
        PACIFIC COAST GROUNDFISH
    LIMITED
    ENTRY FIXED GEAR
    SABLEFISH PERMIT
    STACKING
    (CATCH SHARES)
    PROGRAM
    REVIEW
         */
        this.setSmallFishers(26); //washington
        this.setLargeFishers(90-26); //oregon + california
        this.setSeparateRegulations(false);


        /*
         * 2645.6$ dollars a day of costs according to fish-eye
         * of which 15% is approximately spent on fuel,
         * that leaves 2248.675$ a day or 93.69$ an hour
         */
        double hourlyVariableCosts=93.69;
        this.setHourlyTravellingCostLarge(new FixedDoubleParameter(hourlyVariableCosts));
        this.setHourlyTravellingCostSmall(new FixedDoubleParameter(hourlyVariableCosts));



        this.setRegulationSmall(new AnarchyFactory());


        AlwaysDiscardTheseSpeciesFactory discardingStrategyLarge = new AlwaysDiscardTheseSpeciesFactory();
        discardingStrategyLarge.setIndices("1");
        this.setDiscardingStrategyLarge(discardingStrategyLarge);
        AlwaysDiscardTheseSpeciesFactory discardStrategySmall = new AlwaysDiscardTheseSpeciesFactory();
        discardStrategySmall.setIndices("1");
        this.setDiscardingStrategySmall(discardStrategySmall);



    }


    @Override
    protected Supplier<Port> getLargePortSupplier(
            MersenneTwisterFast random, Port[] ports) {
        //choose between california and oregon
        assert ports[0].getName().equals("Washington");
        assert ports[1].getName().equals("Oregon");
        assert ports[2].getName().equals("California");
        return () -> {
            if(ports[1].getFishersHere().size()<=40)
                return ports[1];
            else
                return ports[2];
        };
    }

    @Override
    protected Supplier<Port> getSmallPortSupplier(
            MersenneTwisterFast random, Port[] ports) {
        assert ports[0].getName().equals("Washington");
        assert ports[1].getName().equals("Oregon");
        assert ports[2].getName().equals("California");
        //always the first!
        return () -> ports[0];
    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {
        ScenarioPopulation population = super.populateModel(model);

        //prepare a "real" start of the model
        model.scheduleOnceInXDays(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        //reset local biology
                        for(Species species : model.getSpecies())
                            ((YellowBycatchWithHistoryFactory) getBiologyInitializer()).retrieveLastMade().resetLocalBiology(species);

                        //put real regulations in!
                        for(Fisher fisher : model.getFishers())
                        {
                            fisher.setRegulation(regulationsToImposeAtStartYear.apply(model));
                        }
                    }
                },
                StepOrder.DAWN,
                364
        );

        //change all the tags!
        for(Fisher fisher : population.getPopulation())
        {
            fisher.getTags().clear();
            switch (fisher.getHomePort().getName())
            {
                case "Washington":
                    fisher.getTags().add("ship");
                    fisher.getTags().add("Washington");
                    fisher.getTags().add("blue");
                    break;
                case "Oregon":
                    fisher.getTags().add("ship");
                    fisher.getTags().add("Oregon");
                    fisher.getTags().add("red");
                    break;
                default:
                case "California":
                    fisher.getTags().add("ship");
                    fisher.getTags().add("California");
                    fisher.getTags().add("black");
                    break;
            }


        }




        //add exogenous mortality
        Preconditions.checkArgument(model.getSpecies().get(0).getName().equals("Sablefish"));
        LinkedHashMap<Species, Double> exogenousMortality = new LinkedHashMap<>();
        exogenousMortality.put(model.getSpecies().get(0),exogenousSablefishCatches);
        exogenousMortality.put(model.getSpecies().get(1),exogenousYelloweyeCatches);
        BiomassDrivenFixedExogenousCatches mortality = new BiomassDrivenFixedExogenousCatches(
                exogenousMortality,
                false);
        model.registerStartable(mortality);


        return population;
    }


    /**
     * Getter for property 'regulationsToImposeAtStartYear'.
     *
     * @return Value for property 'regulationsToImposeAtStartYear'.
     */
    public AlgorithmFactory<? extends Regulation> getRegulationsToImposeAtStartYear() {
        return regulationsToImposeAtStartYear;
    }

    /**
     * Setter for property 'regulationsToImposeAtStartYear'.
     *
     * @param regulationsToImposeAtStartYear Value to set for property 'regulationsToImposeAtStartYear'.
     */
    public void setRegulationsToImposeAtStartYear(
            AlgorithmFactory<? extends Regulation> regulationsToImposeAtStartYear) {
        this.regulationsToImposeAtStartYear = regulationsToImposeAtStartYear;
    }

    /**
     * Getter for property 'exogenousSablefishCatches'.
     *
     * @return Value for property 'exogenousSablefishCatches'.
     */
    public double getExogenousSablefishCatches() {
        return exogenousSablefishCatches;
    }

    /**
     * Setter for property 'exogenousSablefishCatches'.
     *
     * @param exogenousSablefishCatches Value to set for property 'exogenousSablefishCatches'.
     */
    public void setExogenousSablefishCatches(double exogenousSablefishCatches) {
        this.exogenousSablefishCatches = exogenousSablefishCatches;
    }

    /**
     * Getter for property 'exogenousYelloweyeCatches'.
     *
     * @return Value for property 'exogenousYelloweyeCatches'.
     */
    public double getExogenousYelloweyeCatches() {
        return exogenousYelloweyeCatches;
    }

    /**
     * Setter for property 'exogenousYelloweyeCatches'.
     *
     * @param exogenousYelloweyeCatches Value to set for property 'exogenousYelloweyeCatches'.
     */
    public void setExogenousYelloweyeCatches(double exogenousYelloweyeCatches) {
        this.exogenousYelloweyeCatches = exogenousYelloweyeCatches;
    }
}
