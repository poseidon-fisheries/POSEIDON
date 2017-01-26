package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticInputMaker;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticMultiClassifier;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.log.DiscretizedLocationMemory;
import uk.ac.ox.oxfish.fisher.log.LogisticLog;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.bandit.BanditSwitch;

import java.util.ArrayList;
import java.util.List;

/**
 * makes choices as if a big SOFTMAX where rewards are a linear combination of features
 * Created by carrknight on 12/5/16.
 */
public class LogitDestinationStrategy implements DestinationStrategy{


    /**
     * this object links the index choices of the logit with the map group of the discretizers
     */
    private final BanditSwitch switcher;

    /**
     * the map discretization
     */
    private final MapDiscretization discretization;

    /**
     * object actually making a choice
     */
    private final LogisticMultiClassifier classifier;


    /**
     * the object containing the extractors and building logisti inputs
     */
    private final LogisticInputMaker input;

    private final FavoriteDestinationStrategy delegate;

    private final DiscretizedLocationMemory memory;


    private LogisticLog log;


    /**
     *
     * @param betas table of all the betas (some might be ignored if the map doesn't cover them)
     * @param covariates table of all hte observation extractors (generate x on the spot)
     * @param rowNames column that assign to each row of betas the group it belongs to
     * @param discretization the discretization map
     */
    public LogitDestinationStrategy(double[][] betas, ObservationExtractor[][] covariates,
                                    List<Integer> rowNames,
                                    MapDiscretization discretization,
                                    FavoriteDestinationStrategy delegate,
                                    MersenneTwisterFast random
                                    )
    {

        Preconditions.checkArgument(rowNames.size()==betas.length,"Row names do not match number of betas");
        Preconditions.checkArgument(rowNames.size()==covariates.length,"Row names do not match number of covariates");

        this.delegate = delegate;
        this.discretization = discretization;

        //only model arms for which we have both at least a tile in the map AND is listed in the input file
        switcher = new BanditSwitch(discretization.getNumberOfGroups(),
                                    integer -> discretization.isValid(integer) && rowNames.contains(integer));

        //here we store only the betas which have a model arm associated to it
        ArrayList<double[]> effectiveBetas = new ArrayList<>();
        ArrayList<ObservationExtractor[]> effectiveCovariates = new ArrayList<>();
        for(int i=0; i<discretization.getNumberOfGroups(); i++)
        {
            int rowNumber = rowNames.indexOf(i);
            if(rowNumber<0)
                continue;
            Integer arm = switcher.getArm(i);
            if(arm==null) //there might not be anything in the map associated with this arm, ignore it then!
                continue;
            effectiveBetas.add(betas[rowNumber]);
            effectiveCovariates.add(covariates[rowNumber]);
        }
        //the numbers should all match
        assert effectiveBetas.size() == effectiveCovariates.size();
        assert effectiveBetas.size() == switcher.getNumberOfArms();


        this.input = new LogisticInputMaker(
                effectiveCovariates.toArray(new ObservationExtractor[effectiveCovariates.size()][]),
                arm -> {
                    List<SeaTile> group = discretization.getGroup(switcher.getGroup(arm));
                    return group.get(random.nextInt(group.size()));
                }
                );
        this.classifier = new LogisticMultiClassifier(
                effectiveBetas.toArray(new double[effectiveBetas.size()][]));


        this.memory = new DiscretizedLocationMemory(discretization);







    }

    /**
     * delegate object that re-routes adaptation back to this function
     */
    private final Adaptation adaptation = new Adaptation() {
        @Override
        public void adapt(Fisher toAdapt, FishState state, MersenneTwisterFast random) {

            LogitDestinationStrategy.this.adapt(state, random, toAdapt);
        }

        @Override
        public void start(FishState model, Fisher fisher) {
            //nothing
        }

        @Override
        public void turnOff(Fisher fisher) {
            //nothing
        }
    };


    boolean started = false;
    @Override
    public void start(FishState model, Fisher fisher) {
        Preconditions.checkState(!started,"Already started!");
        started = true;
        delegate.start(model, fisher);
        fisher.addPerTripAdaptation(adaptation);
        //keep in memory the last time the arm was chosen
        fisher.setDiscretizedLocationMemory(memory);
    }

    /**
     * calls the multi-logit regression to choose a new arm then pick at random from that group
     * @param state the model
     * @param random the randomizer
     * @param fisher the agent making the choie
     */
    public void adapt(FishState state, MersenneTwisterFast random, Fisher fisher) {
        double[][] input = this.input.getRegressionInput(fisher, state);
        if(log!=null)
            log.recordInput(input);
        int armChosen = classifier.choose(input, random);
        if(log!=null)
            log.recordChoice(armChosen);

        List<SeaTile> group = discretization.getGroup(switcher.getGroup(armChosen));
        SeaTile destination = group.get(random.nextInt(group.size()));
        delegate.setFavoriteSpot(destination);
    }

    @Override
    public void turnOff(Fisher fisher) {

        if(started)
            fisher.removePerTripAdaptation(adaptation);
        delegate.turnOff(fisher);
    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }


    public SeaTile getCurrentTarget() {
        return delegate.getFavoriteSpot();
    }


    /**
     * Getter for property 'switcher'.
     *
     * @return Value for property 'switcher'.
     */
    public BanditSwitch getSwitcher() {
        return switcher;
    }

    /**
     * Getter for property 'discretization'.
     *
     * @return Value for property 'discretization'.
     */
    public MapDiscretization getDiscretization() {
        return discretization;
    }

    /**
     * Getter for property 'classifier'.
     *
     * @return Value for property 'classifier'.
     */
    public LogisticMultiClassifier getClassifier() {
        return classifier;
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public FavoriteDestinationStrategy getDelegate() {
        return delegate;
    }

    /**
     * Getter for property 'adaptation'.
     *
     * @return Value for property 'adaptation'.
     */
    public Adaptation getAdaptation() {
        return adaptation;
    }

    /**
     * Getter for property 'started'.
     *
     * @return Value for property 'started'.
     */
    public boolean isStarted() {
        return started;
    }


    /**
     * Getter for property 'input'.
     *
     * @return Value for property 'input'.
     */
    public LogisticInputMaker getInput() {
        return input;
    }

    /**
     * Getter for property 'log'.
     *
     * @return Value for property 'log'.
     */
    public LogisticLog getLog() {
        return log;
    }

    /**
     * Setter for property 'log'.
     *
     * @param log Value to set for property 'log'.
     */
    public void setLog(LogisticLog log) {
        this.log = log;
    }
}
