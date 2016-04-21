package uk.ac.ox.oxfish.utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Just a collector of all the utilities function i need
 * Created by carrknight on 6/19/15.
 */
public class FishStateUtilities {

    public static final double EPSILON = .01;
    public final static int MALE = 0;
    public final static int FEMALE = 1;

    private static final String JAR_NAME = "oxfish_executable.jar";


    /**
     * round to 2nd decimal value
     * @param value
     * @return
     */
    public static double round(double value) {

        return (double)Math.round(value*100)/100;
    }

    /**
     * round to 5th decimal place
     * @param value
     * @return
     */
    public static double round5(double value) {

        return (double)Math.round(value*100000)/100000;
    }







    /**
     * looks for a file first in the current directory, otherwise in the directory the jar is stored
     * otherwise returns back the relative path
     *
     * @param relativePath something like dir/file.txt
     * @return the absolute path
     */
    public static String getAbsolutePath(String relativePath) {

        relativePath = relativePath.replaceAll("\"","");

        //first try user dir (where java is called)
        String classPath = System.getProperty("user.dir");
        File file = new File(classPath + File.separator + relativePath);
        if (file.exists())
            return file.getAbsolutePath();

        //otherwise try "."
        file = new File(new File(".").getAbsolutePath() + File.separator + relativePath);
        if (file.exists())
            return file.getAbsolutePath();

        //if there is no protection, you can try to "get the source code"
        try {
            CodeSource src = FishState.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL url = new URL(src.getLocation(), relativePath);
                file = new File(url.toURI().getPath());
                if (file.exists())
                    return file.getAbsolutePath();
            }
        } catch (MalformedURLException | SecurityException |  URISyntaxException ignored) {
        }

        //finally you can just try to look for the jar file
        //see here : http://stackoverflow.com/questions/775389/accessing-properties-files-outside-the-jar/775565
        String classpath = System.getProperty("java.class.path");
        int jarPos = classpath.indexOf(JAR_NAME);
        int jarPathPos = classpath.lastIndexOf(File.pathSeparatorChar, jarPos) + 1;
        String path = classpath.substring(jarPathPos, jarPos);
        file = new File(path + File.separator + relativePath);
        if (file.exists())
            return file.getAbsolutePath();

        System.err.println("failed to find the absolute path of the default config file");
        return relativePath;


    }

    //stolen from: http://stackoverflow.com/a/19136617/975904
    public static String removeParentheses(String toClean)
    {
        int open = 0;
        int closed = 0;
        boolean changed = true;
        int startIndex = 0, openIndex = -1, closeIndex = -1;

        while (changed) {
            changed = false;
            for (int a = startIndex; a < toClean.length(); a++) {
                if (toClean.charAt(a) == '<') {
                    open++;
                    if (open == 1) {
                        openIndex = a;
                    }
                } else if (toClean.charAt(a) == '>') {
                    closed++;
                    if (open == closed) {
                        closeIndex = a;
                        toClean = toClean.substring(0, openIndex)
                                + toClean.substring(closeIndex + 1);
                        changed = true;
                        break;
                    }
                } else {
                    if (open == 0)
                        startIndex++;
                }
            }
        }
        return toClean;
    }

    public static <T> Pair<T,Fisher> imitateFriendAtRandom(
            MersenneTwisterFast random, double fitness, T current, Collection<Fisher> friends,
            ObjectiveFunction<Fisher> objectiveFunction, Sensor<T> sensor) {
        //get random friend
        List<Fisher> friendList = friends.stream().
                //remove friends we can't imitate
                        filter(fisher -> sensor.scan(fisher) != null).
                //sort by id (remove hashing randomness)
                        sorted((o1, o2) -> Integer.compare(o1.getID(),o2.getID())).collect(Collectors.toList());

        assert friends.size() >0;
        if(friendList.isEmpty())
            return new Pair<>(current,null);
        else {
            Fisher friend = friendList.get(random.nextInt(friendList.size()));
            double friendFitness = objectiveFunction.computeCurrentFitness(friend);
            if(friendFitness > fitness && Double.isFinite(friendFitness) && Double.isFinite(fitness)) {
                return new Pair<>(sensor.scan(friend),friend);
            }
            else
                return new Pair<>(current,null);
        }

    }


    public static <T> Pair<T,Fisher> imitateBestFriend(MersenneTwisterFast random, double fitness,
                                                       T current, Collection<Fisher> friends,
                                                       ObjectiveFunction<Fisher> objectiveFunction,
                                                       Sensor<T> sensor)
    {

        //if you have no friends, keep doing what you currently are doing
        if(friends.isEmpty())
            return new Pair<>(current,null);

        //associate a fitness to each friend and compute the maxFitness
        final double[] maxFitness = {fitness};
        Set<Map.Entry<Fisher, Double>> friendsFitnesses = friends.stream().
                //ignore friends who we can't imitate
                        filter(fisher -> sensor.scan(fisher) != null).
                        collect(
                                Collectors.toMap((friend) -> friend, new Function<Fisher, Double>() {
                                    @Override
                                    public Double apply(Fisher fisher) {
                                        //get your friend fitness
                                        double friendFitness = objectiveFunction.computeCurrentFitness(fisher);
                                        //if it is finite check if it is better than what we already have
                                        if(Double.isFinite(friendFitness))
                                            maxFitness[0] = Math.max(maxFitness[0], friendFitness);
                                        //return it
                                        return friendFitness;
                                    }
                                })).entrySet();

        //make sure it's finite and at least as good as our current fitness
        if(Double.isNaN(fitness) && Double.isNaN(maxFitness[0]))
            return new Pair<>(current,null);

        assert Double.isFinite(maxFitness[0]);
        assert maxFitness[0] >= fitness;

        //if you are doing better than your friends, keep doing what you are doing
        if(Math.abs(maxFitness[0] -fitness)<EPSILON)
            return new Pair<>(current,null);

        //prepare to store the possible imitation options
        List<Fisher> bestFriends = new LinkedList<>();
        //take all your friends
        friendsFitnesses.stream().
                //choose only the ones with the highest fitness
                        filter(fisherDoubleEntry -> Math.abs(maxFitness[0] - fisherDoubleEntry.getValue()) < EPSILON).
                // sort them by id (we need to kill the hashing randomization which we can't control)
                        sorted((o1, o2) -> Integer.compare(o1.getKey().getID(), o2.getKey().getID())).
                //now put in the best option list by scanning
                        forEachOrdered(fisherDoubleEntry -> bestFriends.add(fisherDoubleEntry.getKey()));



        //return a random best option

        Fisher bestFriend = bestFriends.size() == 1 ?
                bestFriends.get(0) :
                bestFriends.get(random.nextInt(bestFriends.size()));
        assert bestFriend!=null;
        return new Pair<>(sensor.scan(bestFriend),bestFriend);


    }


    /*
     //if you have no friends, keep doing what you currently are doing
        if(friends.isEmpty())
            return current;

        //associate a fitness to each friend and compute the maxFitness
        final double[] maxFitness = {fitness};
        Set<Map.Entry<Fisher, Double>> friendsFitnesses = friends.stream().
                //ignore friends who we can't imitate
                        filter(fisher -> sensor.scan(fisher) != null).
                        collect(
                                Collectors.toMap((friend) -> friend, new Function<Fisher, Double>() {
                                    @Override
                                    public Double apply(Fisher fisher) {
                                        //get your friend fitness
                                        double friendFitness = objectiveFunction.computeCurrentFitness(fisher);
                                        //if it is finite check if it is better than what we already have
                                        if(Double.isFinite(friendFitness))
                                            maxFitness[0] = Math.max(maxFitness[0], friendFitness);
                                        //return it
                                        return friendFitness;
                                    }
                                })).entrySet();

        //make sure it's finite and at least as good as our current fitness
        assert Double.isFinite(maxFitness[0]);
        assert maxFitness[0] >= fitness;

        //if you are doing better than your friends, keep doing what you are doing
        if(Math.abs(maxFitness[0] -fitness)<EPSILON)
            return current;

        //prepare to store the possible imitation options
        List<T> bestOptions = new LinkedList<>();
        //take all your friends
        friendsFitnesses.stream().
                //choose only the ones with the highest fitness
                filter(fisherDoubleEntry -> Math.abs(maxFitness[0] - fisherDoubleEntry.getValue()) < EPSILON).
                // sort them by id (we need to kill the hashing randomization which we can't control)
                        sorted((o1, o2) -> Integer.compare(o1.getKey().getID(), o2.getKey().getID())).
                //now put in the best option list by scanning
                        forEachOrdered(fisherDoubleEntry -> bestOptions.add(sensor.scan(fisherDoubleEntry.getKey())));



        //return a random best option
        return bestOptions.size() == 1  ?
                bestOptions.get(0) :
                bestOptions.get(random.nextInt(bestOptions.size()));

     */

    /**
     * stolen from here:
     * http://stackoverflow.com/questions/442758/which-java-library-computes-the-cumulative-standard-normal-distribution-function
     * this is a quick and dirty way of computing the STANDARD normal CDF function
     * @param x the value you want to compute the CDF of
     * @return the probability that a standard normal draw is below x
     */
    public static double CNDF(double x)
    {
        int neg = (x < 0d) ? 1 : 0;
        if ( neg == 1)
            x *= -1d;

        double k = (1d / ( 1d + 0.2316419 * x));
        double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
                k - 0.356563782) * k + 0.319381530) * k;
        y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

        return (1d - neg) * y + neg * (1d - y);
    }


    public static void printCSVColumnToFile(File file, DataColumn column)
    {
        try {
            FileWriter writer = new FileWriter(file);
            for (Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void printCSVColumnsToFile(File file, DataColumn... columns)
    {
        try {
            FileWriter writer = new FileWriter(file);
            //write header
            for(int i=0; i<columns.length; i++)
            {
                if(i!=0)
                    writer.write(",");
                writer.write(columns[i].getName());
            }
            writer.write("\n");

            //write columns
            for(int row=0; row<columns[0].size(); row++)
            {
                for(int i=0; i<columns.length; i++)
                {
                    if(i!=0)
                        writer.write(",");
                    writer.write(String.valueOf(columns[i].get(row)));
                }
                writer.write("\n");

            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public static void pollFishersToFile(Collection<Fisher> fishers,
                                           File file,Sensor<Double>... pollers)
    {
        // File histogramFile = Paths.get("runs", "lambda", "hist100.csv").toFile();
        ArrayList<String> histogram = new ArrayList<>(fishers.size());
        for(Fisher fisher : fishers)
        {

            StringBuilder row = new StringBuilder();
            row.append(fisher.getID());
            for(Sensor<Double> poller : pollers) {
                row.append(",");
                row.append(poller.scan(fisher));
            }
            histogram.add(
                   row.toString());


        }

        String csvColumn = histogram.stream().reduce((t, u) -> t + "\n" + u).get();

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(csvColumn);
            writer.flush();
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void pollHistogramToFile(
            Collection<Fisher> fishers, File file, Sensor<Double> poller)
    {
        // File histogramFile = Paths.get("runs", "lambda", "hist100.csv").toFile();
        ArrayList<String> histogram = new ArrayList<>(fishers.size());
        for(Fisher fisher : fishers)
        {

            histogram.add(
                    Double.toString(
                            poller.scan(fisher)
                    )
            );
        }

        String csvColumn = histogram.stream().reduce((t, u) -> t + "\n" + u).get();

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(csvColumn);
            writer.flush();
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * takes a column of daily observations and sum them up to generate a yearly observation
     * @param column colun to sum over
     * @return a sum or NAN if the column is empty
     */
    public static <T> Gatherer<T> generateYearlySum(final DataColumn column) {

        return new Gatherer<T>() {
            @Override
            public Double apply(T state) {
                //get the iterator
                final Iterator<Double> iterator = column.descendingIterator();
                if(!iterator.hasNext()) //not ready/year 1
                    return Double.NaN;
                double sum = 0;
                for(int i=0; i<365; i++) {
                    //it should be step 365 times at most, but it's possible that this agent was added halfway through
                    //and only has a partially filled collection
                    if(iterator.hasNext())
                        sum += iterator.next();
                }

                return sum;
            }
        };
    }


    /**
     * taken from the c++ version here: http://www.codeproject.com/Articles/49723/Linear-correlation-and-statistical-functions
     * which explains the minimalistic naming convention
     */
    public static double computeCorrelation(double[] x, double[] y)
    {

        //will regularize the unusual case of complete correlation
        final double TINY=1.0e-20;
        int j,n=x.length;
        Double yt,xt,t,df;
        Double syy=0.0,sxy=0.0,sxx=0.0,ay=0.0,ax=0.0;
        for (j=0;j<n;j++) {
            //finds the mean
            ax += x[j];
            ay += y[j];
        }
        ax /= n;
        ay /= n;
        for (j=0;j<n;j++) {
            // compute correlation coefficient
            xt=x[j]-ax;
            yt=y[j]-ay;
            sxx += xt*xt;
            syy += yt*yt;
            sxy += xt*yt;
        }
        return sxy/(Math.sqrt(sxx*syy)+TINY);
    }


    /**
     * returns L *  (1-(1/(Math.exp(-k*(x-x0)))));
     */
    public static double logisticProbability(double L, double k, double x0, double x)
    {


        return L *  (1-(1/(1+Math.exp(-k*(x-x0)))));




    }

    public static double catchSpecieGivenCatchability(
            SeaTile where, double hoursSpentFishing, Species species, double q) {
        Preconditions.checkState(q >= 0);
        //catch
        double specieCatch = FishStateUtilities.round(where.getBiomass(species) *
                                                              q *
                                                              hoursSpentFishing);
        //tell biomass
        if(specieCatch> 0)
            where.reactToThisAmountOfBiomassBeingFished(species, specieCatch);

        return specieCatch;

    }


    /**
     * converts easting-northing UTM. taken from:
     * http://stackoverflow.com/a/28224544/975904
     * @param UTM
     * @param easting
     * @param northing
     * @return
     */
    public static Point2D.Double utmToLatLong(String UTM, double easting, double northing)
    {
        double latitude;
        double longitude;
        String[] parts=UTM.split(" ");
        int zone=Integer.parseInt(parts[0]);
        char Letter=parts[1].toUpperCase(Locale.ENGLISH).charAt(0);
        double Hem;
        if (Letter>'M')
            Hem='N';
        else
            Hem='S';
        double north;
        if (Hem == 'S')
            north = northing - 10000000;
        else
            north = northing;

        latitude = (north/6366197.724/0.9996+
                (1+0.006739496742*Math.pow
                        (Math.cos
                                (north/6366197.724/0.9996),2)
                        -0.006739496742*Math.sin(north/6366197.724/0.9996)*
                        Math.cos(north/6366197.724/0.9996)*(Math.atan(Math.cos
                        (Math.atan(( Math.exp((easting - 500000) / (0.9996*6399593.625/Math.sqrt
                                ((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*
                                                      (1-0.006739496742*Math.pow((easting - 500000) / (0.9996*6399593.625/Math.sqrt
                                                              ((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)
                                                              /2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*( 1 -  0.006739496742*Math.pow((easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996 )/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996)))*Math.tan((north-0.9996*6399593.625*(north/6366197.724/0.9996 - 0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996 )*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))-north/6366197.724/0.9996)*3/2)*(Math.atan(Math.cos(Math.atan((Math.exp((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996)))*Math.tan((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))-north/6366197.724/0.9996))*180/Math.PI;
        latitude=Math.round(latitude*10000000);
        latitude=latitude/10000000;
        longitude =Math.atan((Math.exp((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*( north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2* north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3)) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))*180/Math.PI+zone*6-183;
        longitude=Math.round(longitude*10000000);
        longitude=longitude/10000000;
        return new Point2D.Double(latitude,longitude);
    }


    /**
     * weights this number of fish (split into age cohorts) into the total amount of biomass
     * @param male
     * @param female
     * @param species
     * @return
     */
    public static double weigh(int[] male, int[] female, Species species)
    {
        final ImmutableList<Double> maleWeights = species.getWeightMaleInKg();
        final ImmutableList<Double> femaleWeights = species.getWeightFemaleInKg();
        double totalWeight = 0;
        //go through all the fish and sum up their weight at given age
        for(int age=0; age<species.getMaxAge()+1; age++)
        {
            totalWeight += maleWeights.get(age) * male[age];
            totalWeight += femaleWeights.get(age) * female[age];
        }

        return totalWeight;



    }


    /**
     * this is a slight modification of ArrayUtils.toString(.) to deal with nested arrays
     * @param array the (possibly array, possibly nested) object to display
     * @param rowSeparator how should row elements (within same array) be seperated
     * @param columnSeparator how should each array start and close
     * @return
     */
    public static String deepToStringArray( Object array,
                                            String rowSeparator,
                                            String columnSeparator) {

        if ( array == null ) {
            return "";
        }

        if ( !array.getClass().isArray() ) {
            return String.valueOf(array);
        }

        StringBuilder builder = new StringBuilder();

        builder.append(columnSeparator);
        for (int i = 0, length = Array.getLength(array ); i < length; i++ ) {
            String value = String.valueOf( Array.get( array, i ) );
            // Concatenate the separator
            if(i>0)
                builder.append(rowSeparator);

            // Build the string
            builder.append( value );
        }
        builder.append(columnSeparator);


        return builder.toString();
    }


    public static String getFilenameExtension(File file){

        String fileName = file.getName();
        String extension = "";

        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i+1);
        }
        return extension;
    }

}

