package uk.ac.ox.oxfish.model.regs.factory;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Creates either a multi-itq or a multi-tac reading from file
 * Created by carrknight on 4/5/16.
 */
public class MultiQuotaFileFactory implements AlgorithmFactory<MultiQuotaRegulation>{


    private String pathToFile = "inputs/california/2015_quotas.csv";

    private boolean itq=true;


    private StringBuilder representer;

    private int quotaExchangedPerMatch = 100;

    private boolean multipleTradesAllowed = false;

    //even though we only one use delegate we keep both available
    private MultiITQStringFactory itqFactory = new MultiITQStringFactory();

    private MultiTACStringFactory tacFactory  = new MultiTACStringFactory();


    public MultiQuotaFileFactory() {
    }

    public MultiQuotaFileFactory(String pathToFile, boolean itq) {
        this.pathToFile = pathToFile;
        this.itq = itq;
    }

    /**
     * we parse the file into a string that can be fed to delegate factories
     */
    private void representFileAsString(List<Species> species) throws IOException {

        List<String> fileLines = Files.readAllLines(Paths.get(pathToFile));
        Iterator<String> iterator = fileLines.iterator();
        iterator.next(); //ignore header

        //we will use the string builder to build the string representation of the quota file
        representer = new StringBuilder();

        //check that there are some fish being protected or what's the point?
        Preconditions.checkArgument(fileLines.size()>1,"There are no species to protect in this file!");
        while(iterator.hasNext())
        {
            String[] line = iterator.next().split(",");
            assert line.length==2;
            String speciesName = line[0];
            Double totalQuota = Double.parseDouble(line[1]);


            //find which species it belongs to
            Optional<Species> matchingSpecies = species.stream().filter(species1 ->
                                                                                species1.getName().equalsIgnoreCase(
                                                                                        speciesName.trim())).findAny();
            //we didn't find a corresponding species, that's possibly a problem so warn
            if(!matchingSpecies.isPresent() && Log.WARN)
                Log.warn("Could not find " + speciesName + " in the list of model species, I will ignore its quota");
            else
            {
                int index = matchingSpecies.get().getIndex();
                if(representer.length()>0)
                    representer.append(",");
                representer.append(index).append(":").append(totalQuota);
            }

        }

        itqFactory.setYearlyQuotaMaps(representer.toString());
        tacFactory.setYearlyQuotaMaps(representer.toString());





    }


    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public boolean isItq() {
        return itq;
    }

    public void setItq(boolean itq) {
        this.itq = itq;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(FishState fishState) {
        if(representer == null)
            try {
                representFileAsString(fishState.getSpecies()
                );
            } catch (IOException e) {
                e.printStackTrace();
                Log.error("failed to read " + pathToFile + " and couldn't instantiate quotas!");
                throw new RuntimeException("Failed to read quota file, will abort");
            }
        assert representer!=null;


        if(itq) {
            itqFactory.setAllowMultipleTrades(multipleTradesAllowed);
            itqFactory.setMinimumQuotaTraded(quotaExchangedPerMatch);
            MultiQuotaRegulation regulation = itqFactory.apply(fishState);
            //set up a startable that divide it by the number of fishers
            fishState.registerStartable(new ITQScaler(regulation));
            return regulation;
        }
        else
            return tacFactory.apply(fishState);




    }

    public StringBuilder getRepresenter() {
        return representer;
    }

    static class ITQScaler implements Startable
    {

        private final MultiQuotaRegulation toScale;

        public ITQScaler(MultiQuotaRegulation toScale) {
            this.toScale = toScale;
        }

        @Override
        public void start(FishState model) {
            for (int i = 0; i < model.getSpecies().size(); i++) {
                double availableQuota = toScale.getQuotaRemaining(i);
                if (Double.isFinite(availableQuota))
                    toScale.setYearlyQuota(i,
                                              availableQuota /
                                                      model.getNumberOfFishers());
            }

        }

        @Override
        public void turnOff() {

        }

    }
}
