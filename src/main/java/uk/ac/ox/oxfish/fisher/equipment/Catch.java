package uk.ac.ox.oxfish.fisher.equipment;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;

/**
 * Right now this is just a map specie--->pounds caught. It might in the future deal with age and other factors which is
 * why I create the object catch rather than just using a map
 * Created by carrknight on 4/20/15.
 */
public class Catch {


    private final double[] catchMap;

    /**
     * single specie catch
     * @param specie the specie caught
     * @param poundsCaught the pounds that have been caugh
     */
    public Catch(Specie specie, double poundsCaught, GlobalBiology biology) {
        Preconditions.checkState(poundsCaught >=0);
        catchMap = new double[biology.getSize()];
        catchMap[specie.getIndex()] = poundsCaught;
    }

    public Catch(double[] catches)
    {

        this.catchMap = catches;
    }

    public double getPoundsCaught(Specie specie)
    {
        return catchMap[specie.getIndex()];
    }

    public double getPoundsCaught(int index)
    {
        return catchMap[index];
    }




}
