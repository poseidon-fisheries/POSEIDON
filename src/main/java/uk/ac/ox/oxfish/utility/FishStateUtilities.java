package uk.ac.ox.oxfish.utility;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private static final String JAR_NAME = "oxfish_executable.jar";



    public static double round(double value) {

        return (double)Math.round(value*100)/100;
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

    public static <T> T imitateFriendAtRandom(
            MersenneTwisterFast random, double fitness, T current, Collection<Fisher> friends,
            ObjectiveFunction<Fisher> objectiveFunction, Sensor<T> sensor) {
        //get random friend
        assert friends.size() >0;
        int i = random.nextInt(friends.size());
        Fisher friend = null;
        for(Fisher fisher : friends)
        {
            friend = fisher;
            if(i==0)
                break;
            i--;
        }
        double friendFitness = objectiveFunction.computeCurrentFitness(friend);

        if(friendFitness > fitness && Double.isFinite(friendFitness) && Double.isFinite(fitness)) {
            return sensor.scan(friend);
        }
        else
            return current;
    }


    public static <T> T imitateBestFriend(MersenneTwisterFast random, double fitness,
                                          T current, Collection<Fisher> friends,
                                          ObjectiveFunction<Fisher> objectiveFunction,
                                          Sensor<T> sensor)
    {
        final Optional<Map.Entry<Fisher, Double>> bestFriend = friends.stream().collect(
                Collectors.toMap((friend) -> friend, new Function<Fisher, Double>() {
                    @Override
                    public Double apply(Fisher fisher) {
                        return objectiveFunction.computeCurrentFitness(fisher);
                    }
                })).entrySet().stream().max(
                Map.Entry.comparingByValue());

        if(bestFriend.isPresent()) //if the best friend knows what he's doing
        {
            Double friendFitness = bestFriend.get().getValue();
            if(friendFitness > fitness) {
                T bestFriendDecision = sensor.scan(bestFriend.get().getKey());
                if (bestFriendDecision != null)
                    return bestFriendDecision;

            }
            return current;

        }
        else
            return current;

    }


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


    public static void pollHistogramToFile(Sensor<Double> poller, Collection<Fisher> fishers,
                                           File file)
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
     public static <T> Function<T, Double> generateYearlySum(final DataColumn column) {

        return new Function<T, Double>() {
            @Override
            public Double apply(T state) {
                //get the iterator
                final Iterator<Double> iterator = column.descendingIterator();
                if(!iterator.hasNext()) //not ready/year 1
                    return Double.NaN;
                double sum = 0;
                for(int i=0; i<365; i++) {
                    assert iterator.hasNext() : column.getName() + " " + i;
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
        double specieCatch = where.getBiomass(species) * q * hoursSpentFishing;
        //tell biomass
        if(specieCatch> 0)
            where.reactToThisAmountOfBiomassBeingFished(species, specieCatch);

        return specieCatch;

    }


}
