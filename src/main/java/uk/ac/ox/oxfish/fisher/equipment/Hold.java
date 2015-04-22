package uk.ac.ox.oxfish.fisher.equipment;

import uk.ac.ox.oxfish.biology.Specie;

/**
 * The inventory object of the ship. It has a maximum capacity. Any fish caught after reaching capacity is thrown overboard
 * Created by carrknight on 4/21/15.
 */
public class Hold {


    /**
     * pounds of fish currently transported
     */
    private double poundsCarried = 0;

    /**
     * maximum pounds that can be held
     */
    private double maximumLoad = 0;

    private double[] hold;


    public Hold(double maximumLoadInPounds, int numberOfSpecies) {
        this.maximumLoad = maximumLoadInPounds;
        hold = new double[numberOfSpecies];
    }

    /**
     * store the catch
     * @param caught the catch
     */
    public void load(Catch caught)
    {
        //start loading up
        for(int i=0; i<hold.length; i++)
        {
            double poundsCaught = caught.getPoundsCaught(i);
            hold[i] += poundsCaught;
            poundsCarried+= poundsCaught;
            assert  poundsCaught >=0;
        }

        assert poundsCarried >=0;
        assert consistencyCheck();
        if(poundsCarried > maximumLoad)
            throwOverboard();
        assert poundsCarried <=maximumLoad;
    }

    /**
     * call this if you have loaded more than what you can carry. throws overboard catch proportionally
     */
    private void throwOverboard() {

        assert poundsCarried > maximumLoad;
        double proportionToKeep = 1.0 / getPercentageFilled();
        assert proportionToKeep < 1 && proportionToKeep > 0;
        poundsCarried = 0;
        for(int i=0;i<hold.length; i++)
        {
            hold[i] *= proportionToKeep;
            poundsCarried += hold[i];
        }
    }

    public double getPoundsCarried() {
        return poundsCarried;
    }

    public double getPoundsCarried(Specie specie) {
        return hold[specie.getIndex()];
    }


    public double getMaximumLoad() {
        return maximumLoad;
    }

    public double getPercentageFilled()
    {
        return poundsCarried/maximumLoad;
    }

   private boolean consistencyCheck()
   {
       double sum = 0;
       for(double pounds : hold)
           sum+=pounds;
       return  sum == poundsCarried;
   }

    public Catch  unload()
    {
        Catch toReturn = new Catch(hold);
        hold = new double[hold.length];
        poundsCarried = 0;
        assert consistencyCheck();
        return toReturn;
    }
}
