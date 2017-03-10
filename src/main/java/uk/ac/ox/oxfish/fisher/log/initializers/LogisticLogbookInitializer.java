package uk.ac.ox.oxfish.fisher.log.initializers;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.log.DiscretizedLocationMemory;
import uk.ac.ox.oxfish.fisher.log.LogisticLog;
import uk.ac.ox.oxfish.fisher.log.LogisticLogs;
import uk.ac.ox.oxfish.fisher.log.PseudoLogisticLogger;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
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
            String[] extractorNames) {
       this(discretization,commonExtractor,extractorNames,"");
    }



    public LogisticLogbookInitializer(
            MapDiscretization discretization,
            ObservationExtractor[] commonExtractor,
            String[] extractorNames,
            String identifier) {
        this.discretization = discretization;
        this.commonExtractor = commonExtractor;
        this.extractorNames = extractorNames;
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
        fisher.addTripListener(histogrammer);
        logger.add(log);

        //todo move this somewhere less unsavory
        if(!(fisher.getDestinationStrategy() instanceof LogitDestinationStrategy))
            fisher.setDiscretizedLocationMemory(
                    new DiscretizedLocationMemory(discretization));

    }
}
