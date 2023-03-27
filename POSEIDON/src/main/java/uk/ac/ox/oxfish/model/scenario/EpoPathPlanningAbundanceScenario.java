package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceProcessesFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.FadZapper;
import uk.ac.ox.oxfish.geography.fads.LinearAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.YearlyMarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasFromFolderFactory;

import java.util.List;
import java.util.function.Predicate;

public class EpoPathPlanningAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity.csv"),
            getSpeciesCodesSupplier()
        );

    private PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> purseSeinerFleetFactory =
        new PurseSeinerFleetFactory<>(
            getInputFolder().path("boats.csv"),
            getInputFolder().path("costs.csv"),
            new AbundancePurseSeineGearFactory(
                new LinearAbundanceFadInitializerFactory(
                    getAbundanceFiltersFactory(),
                    getSpeciesCodesSupplier(),
                    "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
                )
            ),
            new FadRefillGearStrategyFactory(
                getInputFolder().path("max_deployments.csv")
            ),
            new EPOPlannedStrategyFlexibleFactory(
                new LocationValuesSupplier(
                    getInputFolder().path("location_values.csv")
                ),
                new AbundanceCatchSamplersFactory(
                    getSpeciesCodesSupplier(),
                    getAbundanceFiltersFactory(),
                    getInputFolder().path("set_samples.csv")
                ),
                getInputFolder().path("action_weights.csv"),
                getInputFolder().path("boats.csv")
            ),
            new DefaultToDestinationStrategyFishingStrategyFactory(),
            new StandardIattcRegulationsFactory(
                new ProtectedAreasFromFolderFactory(
                    getInputFolder().path("regions"),
                    "region_tags.csv"
                )
            ),
            new PurseSeinerDepartingStrategyFactory(),
            new YearlyMarketMapFromPriceFileFactory(
                getInputFolder().path("prices.csv"),
                getSpeciesCodesSupplier()
            ),
            new FromSimpleFilePortInitializer(
                TARGET_YEAR,
                getInputFolder().path("ports.csv")
            )
        );

    public PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> getPurseSeinerFleetFactory() {
        return purseSeinerFleetFactory;
    }

    @SuppressWarnings("unused")
    public void setPurseSeinerFleetFactory(final PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> purseSeinerFleetFactory) {
        this.purseSeinerFleetFactory = purseSeinerFleetFactory;
    }

    private boolean zapper = false;
    private boolean zapperAge = false;

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        purseSeinerFleetFactory.useDummyData(testFolder());
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {
        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);
        if (zapper) {
            final Predicate<AbstractFad> predicate = zapperAge ?
                fad -> fad.getLocation().getGridX() <= 20 :
                fad -> fad.getLocation().getGridX() <= 20 || fishState.getStep() - fad.getStepDeployed() > 150;
            fishState.registerStartable(new FadZapper(predicate));
        }
        return scenarioPopulation;
    }

    @Override
    List<Fisher> makeFishers(final FishState fishState, final int targetYear) {
        return purseSeinerFleetFactory.makeFishers(fishState, targetYear);
    }

    public boolean isZapper() {
        return zapper;
    }

    public void setZapper(final boolean zapper) {
        this.zapper = zapper;
    }

    public boolean isZapperAge() {
        return zapperAge;
    }

    public void setZapperAge(final boolean zapperAge) {
        this.zapperAge = zapperAge;
    }

}


