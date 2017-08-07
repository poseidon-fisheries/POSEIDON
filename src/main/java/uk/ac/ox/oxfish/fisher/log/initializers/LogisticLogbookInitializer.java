package uk.ac.ox.oxfish.fisher.log.initializers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.log.DiscretizedLocationMemory;
import uk.ac.ox.oxfish.fisher.log.LogisticLog;
import uk.ac.ox.oxfish.fisher.log.LogisticLogs;
import uk.ac.ox.oxfish.fisher.log.PseudoLogisticLogger;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.DiscretizationHistogrammer;

/**
 * Creates a logistic logbook
 * Created by carrknight on 2/17/17.
 */
public class LogisticLogbookInitializer implements LogbookInitializer {


    private final MapDiscretization discretization;

    private final ObservationExtractor[] commonExtractor;

    private final String[] extractorNames;


    /**
     * the object doing the actual logbooking (actually a container for the individual logbook makers)
     */
    private LogisticLogs logger;

    private final int histogrammerStartYear;

    /**
     * an additional output of the simulation, a histogram of trips to each spot
     */
    private DiscretizationHistogrammer histogrammer;

    /**
     * useful to discriminate between multiple outputs
     */
    private final String identifier;


    public LogisticLogbookInitializer(
            MapDiscretization discretization,
            ObservationExtractor[] commonExtractor,
            String[] extractorNames, int histogrammerStartYear) {
       this(discretization, commonExtractor, extractorNames, histogrammerStartYear, "");
    }



    public LogisticLogbookInitializer(
            MapDiscretization discretization,
            ObservationExtractor[] commonExtractor,
            String[] extractorNames,
            int histogrammerStartYear, String identifier) {
        this.discretization = discretization;
        this.commonExtractor = commonExtractor;
        this.extractorNames = extractorNames;
        this.histogrammerStartYear = histogrammerStartYear;
        this.identifier = identifier;
    }
    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {


        logger = new LogisticLogs();
        logger.setFileName( identifier + logger.getFileName());
        //let it build, we won't start it until it's time though
        histogrammer = new DiscretizationHistogrammer(
                discretization,false);
        histogrammer.setFileName( identifier + histogrammer.getFileName());

        model.getOutputPlugins().add(logger);
        model.getOutputPlugins().add(histogrammer);





    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }

    @Override
    public void add(Fisher fisher, FishState state) {



        LogisticLog log = new LogisticLog(extractorNames, fisher.getID());

        PseudoLogisticLogger pseudoLogger = new PseudoLogisticLogger(
                discretization,
                commonExtractor,
                log,
                fisher,
                state,
                state.getRandom()
        );

        fisher.addTripListener(pseudoLogger);
        //add histogrammer now or when it is time!
        if(histogrammerStartYear>=0) { //don't do anything if the start year is negative!
            if (state.getYear() > histogrammerStartYear)
                fisher.addTripListener(histogrammer);
            else
                state.scheduleOnceAtTheBeginningOfYear(new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        fisher.addTripListener(histogrammer);
                    }
                }, StepOrder.DAWN, histogrammerStartYear);
        }
        logger.add(log);


        //todo move this somewhere less unsavory
        if(!(fisher.getDestinationStrategy() instanceof LogitDestinationStrategy))
            fisher.setDiscretizedLocationMemory(
                    new DiscretizedLocationMemory(discretization));

    }
}
