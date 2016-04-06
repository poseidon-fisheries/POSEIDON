package uk.ac.ox.oxfish.fisher.equipment;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;

import java.util.Arrays;

/**
 * Right now this is just a map specie--->pounds caught. It might in the future deal with age and other factors which is
 * why I create the object catch rather than just using a map
 * Created by carrknight on 4/20/15.
 */
public class Catch {


    private final double[] catchMap;

    /**
     * single species catch
     * @param species the species caught
     * @param poundsCaught the pounds that have been caugh
     */
    public Catch(Species species, double poundsCaught, GlobalBiology biology) {
        Preconditions.checkState(poundsCaught >=0);
        catchMap = new double[biology.getSize()];
        catchMap[species.getIndex()] = poundsCaught;
    }

    public Catch(double[] catches)
    {

        this.catchMap = catches;
    }

    public double getPoundsCaught(Species species)
    {
        return catchMap[species.getIndex()];
    }

    public double getPoundsCaught(int index)
    {
        return catchMap[index];
    }



    public double totalCatchWeight()
    {
        return Arrays.stream(catchMap).sum();
    }

    @Override
    public String toString() {
        return Arrays.toString(catchMap);

    }
}
