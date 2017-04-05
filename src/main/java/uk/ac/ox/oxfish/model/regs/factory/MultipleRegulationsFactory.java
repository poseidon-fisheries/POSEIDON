package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Factory to generate multiple algorithms map
 * Created by carrknight on 4/4/17.
 */
public class MultipleRegulationsFactory implements AlgorithmFactory<MultipleRegulations> {



    //maps of weird maps are really hard to read for SnakeYAML so I am going to use to separate list and
    //generate the map on the second list

    private LinkedList<String> tags = new LinkedList<>();

    private LinkedList<AlgorithmFactory<? extends Regulation>> factories = new LinkedList<>();


    /**
     * the factories provided
     */
    {
        tags.add("all");
        tags.add("all");
        factories.add(new ITQMonoFactory(2500));
        factories.add(new ProtectedAreasOnlyFactory());
    }


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MultipleRegulations apply(FishState fishState) {

        Preconditions.checkArgument(factories.size()>0);
        Preconditions.checkArgument(factories.size()==tags.size());
        Map<AlgorithmFactory<? extends Regulation>,String> regulations = new HashMap<>();

        for(int i=0; i<tags.size(); i++)
            regulations.put(factories.get(i),tags.get(i));

        return new MultipleRegulations(regulations);
    }

    /**
     * Getter for property 'tags'.
     *
     * @return Value for property 'tags'.
     */
    public LinkedList<String> getTags() {
        return tags;
    }

    /**
     * Setter for property 'tags'.
     *
     * @param tags Value to set for property 'tags'.
     */
    public void setTags(LinkedList<String> tags) {
        this.tags = tags;
    }

    /**
     * Getter for property 'factories'.
     *
     * @return Value for property 'factories'.
     */
    public LinkedList<AlgorithmFactory<? extends Regulation>> getFactories() {
        return factories;
    }

    /**
     * Setter for property 'factories'.
     *
     * @param factories Value to set for property 'factories'.
     */
    public void setFactories(
            LinkedList<AlgorithmFactory<? extends Regulation>> factories) {
        this.factories = factories;
    }
}
