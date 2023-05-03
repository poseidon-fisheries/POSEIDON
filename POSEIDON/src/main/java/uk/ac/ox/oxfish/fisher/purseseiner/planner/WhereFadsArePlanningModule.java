package uk.ac.ox.oxfish.fisher.purseseiner.planner;

/**
 * simply picks the best fad in the area where there are most fads, weighted by the age of the fad:
 * <p>
 * (best area has max SUM(FAD_AGE^AGE_WEIGHT)
 */
public class WhereFadsArePlanningModule extends PickBestPilePlanningModule {


    private final double ageWeight;


    public WhereFadsArePlanningModule(
        final OwnFadSetDiscretizedActionGenerator optionsGenerator,
        final double ageWeight
    ) {

        super(optionsGenerator);
        this.ageWeight = ageWeight;


    }


    @Override
    protected double weighFad(
        final int currentModelStep,
        final OwnFadSetDiscretizedActionGenerator.ValuedFad valuedFad
    ) {
        //get the age of the fad
        final int age = valuedFad.getKey().isActive() ?
            currentModelStep - valuedFad.getKey().getStepDeployed() :
            0;
        //age ^ weight
        return Math.pow(age, ageWeight);
    }


}
