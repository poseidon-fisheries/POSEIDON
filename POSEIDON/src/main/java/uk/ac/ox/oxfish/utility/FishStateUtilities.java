/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Fishing;
import uk.ac.ox.oxfish.fisher.selfanalysis.MovingAveragePredictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.OutputPlugin;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.TowHeatmapGatherer;
import uk.ac.ox.oxfish.model.scenario.PolicyScripts;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.oxfish.utility.yaml.ModelResults;

import java.awt.geom.Point2D;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static com.google.common.collect.Streams.zip;
import static java.util.stream.Collectors.collectingAndThen;

/**
 * Just a collector of all the utilities function i need
 * Created by carrknight on 6/19/15.
 */
public class FishStateUtilities {

    public static final double EPSILON = .01;
    public final static int MALE = 0;
    public final static int FEMALE = 1;

    private static final String JAR_NAME = "yamler.jar";


    private FishStateUtilities() {
    }

    /**
     * round to 5th decimal place
     *
     * @param value
     * @return
     */
    public static double round5(final double value) {

        return (double) Math.round(value * 100000) / 100000;
    }

    public static int quickRounding(final double value) {
        return (int) (value + 0.5d);
    }

    /**
     * looks for a file first in the current directory, otherwise in the directory the jar is stored
     * otherwise returns back the relative osmoseWFSPath
     *
     * @param relativePath something like dir/file.txt
     * @return the absolute osmoseWFSPath
     */
    public static String getAbsolutePath(String relativePath) {

        relativePath = relativePath.replaceAll("\"", "");

        //first try user dir (where java is called)
        final String classPath = System.getProperty("user.dir");
        File file = new File(classPath + File.separator + relativePath);
        if (file.exists())
            return file.getAbsolutePath();

        //otherwise try "."
        file = new File(new File(".").getAbsolutePath() + File.separator + relativePath);
        if (file.exists())
            return file.getAbsolutePath();

        //if there is no protection, you can try to "get the source code"
        try {
            final CodeSource src = FishState.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                final URL url = new URL(src.getLocation(), relativePath);
                file = new File(url.toURI().getPath());
                if (file.exists())
                    return file.getAbsolutePath();
            }
        } catch (final MalformedURLException | SecurityException | URISyntaxException ignored) {
        }

        //finally you can just try to look for the jar file
        //see here : http://stackoverflow.com/questions/775389/accessing-properties-files-outside-the-jar/775565
        final String classpath = System.getProperty("java.class.path");
        System.out.println("classpath " + classpath);
        if (classpath.length() > 0) {
            final int jarPos = classpath.indexOf(JAR_NAME);
            final int jarPathPos = classpath.lastIndexOf(File.pathSeparatorChar, jarPos) + 1;
            System.out.println(jarPos);
            System.out.println(jarPathPos);
            final String path = classpath.substring(jarPathPos, jarPos);
            file = new File(path + File.separator + relativePath);
            System.out.println(file.getAbsolutePath());
            if (file.exists())
                return file.getAbsolutePath();
        }
        System.err.println("failed to find the absolute path of the default config file");
        return relativePath;


    }

    //stolen from: http://stackoverflow.com/a/19136617/975904
    public static String removeParentheses(String toClean) {
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

    public static <T> Pair<T, Fisher> imitateFriendAtRandom(
        final MersenneTwisterFast random, final double fitness, final T current, final Collection<Fisher> friends,
        final ObjectiveFunction<Fisher> objectiveFunction, final Sensor<Fisher, T> sensor,
        final Fisher fisherThinkingOfImitating
    ) {
        //get random friend
        final List<Fisher> friendList = friends.stream().
            //remove friends we can't imitate
                filter(fisher -> sensor.scan(fisher) != null).
            //sort by id (remove hashing randomness)
                sorted((o1, o2) -> Integer.compare(o1.getID(), o2.getID())).collect(Collectors.toList());

        assert friends.size() > 0;
        if (friendList.isEmpty())
            return new Pair<>(current, null);
        else {
            final Fisher friend = friendList.get(random.nextInt(friendList.size()));
            final double friendFitness = objectiveFunction.computeCurrentFitness(
                fisherThinkingOfImitating,
                friend
            );
            if (friendFitness > fitness && Double.isFinite(friendFitness) && Double.isFinite(fitness)) {
                return new Pair<>(sensor.scan(friend), friend);
            } else
                return new Pair<>(current, null);
        }

    }

    public static <T> Pair<T, Fisher> imitateBestFriend(
        final MersenneTwisterFast random, final Fisher fisherDoingTheImitation, final double fitness,
        final T current, final Collection<Fisher> friends,
        final ObjectiveFunction<Fisher> objectiveFunction,
        final Sensor<Fisher, T> sensor
    ) {

        //if you have no friends, keep doing what you currently are doing
        if (friends.isEmpty())
            return new Pair<>(current, null);

        //associate a fitness to each friend and compute the maxFitness
        final double[] maxFitness = {fitness};
        final Set<Map.Entry<Fisher, Double>> friendsFitnesses = friends.stream().
            //ignore friends who we can't imitate
                filter(fisher -> sensor.scan(fisher) != null).
            //ignore fishers who aren't allowed out anyway
                collect(
                Collectors.toMap((friend) -> friend, new Function<Fisher, Double>() {
                    @Override
                    public Double apply(final Fisher fisher) {
                        //get your friend fitness
                        final double friendFitness = objectiveFunction.computeCurrentFitness(
                            fisherDoingTheImitation, fisher);
                        //if it is finite check if it is better than what we already have
                        if (Double.isFinite(friendFitness))
                            maxFitness[0] = Math.max(maxFitness[0], friendFitness);
                        //return it
                        return friendFitness;
                    }
                })).entrySet();

        //make sure it's finite and at least as good as our current fitness
        if (Double.isNaN(fitness) && Double.isNaN(maxFitness[0]))
            return new Pair<>(current, null);

        assert Double.isFinite(maxFitness[0]);
        assert maxFitness[0] >= fitness;

        //if you are doing better than your friends, keep doing what you are doing
        if (Math.abs(maxFitness[0] - fitness) < EPSILON)
            return new Pair<>(current, null);

        //prepare to store the possible imitation options
        final List<Fisher> bestFriends = new LinkedList<>();
        //take all your friends
        friendsFitnesses.stream().
            //choose only the ones with the highest fitness
                filter(fisherDoubleEntry -> Math.abs(maxFitness[0] - fisherDoubleEntry.getValue()) < EPSILON).
            // sort them by id (we need to kill the hashing randomization which we can't control)
                sorted((o1, o2) -> Integer.compare(o1.getKey().getID(), o2.getKey().getID())).
            //now put in the best option list by scanning
                forEachOrdered(fisherDoubleEntry -> bestFriends.add(fisherDoubleEntry.getKey()));


        //return a random best option

        final Fisher bestFriend = bestFriends.size() == 1 ?
            bestFriends.get(0) :
            bestFriends.get(random.nextInt(bestFriends.size()));
        assert bestFriend != null;
        return new Pair<>(sensor.scan(bestFriend), bestFriend);


    }

    /**
     * stolen from here:
     * http://stackoverflow.com/questions/442758/which-java-library-computes-the-cumulative-standard-normal-distribution-function
     * this is a quick and dirty way of computing the STANDARD normal CDF function
     *
     * @param x the value you want to compute the CDF of
     * @return the probability that a standard normal draw is below x
     */
    public static double CNDF(double x) {
        final int neg = (x < 0d) ? 1 : 0;
        if (neg == 1)
            x *= -1d;

        final double k = (1d / (1d + 0.2316419 * x));
        double y = ((((1.330274429 * k - 1.821255978) * k + 1.781477937) *
            k - 0.356563782) * k + 0.319381530) * k;
        y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

        return (1d - neg) * y + neg * (1d - y);
    }

    public static void printCSVColumnToFile(final File file, final DataColumn column) {
        try {
            final FileWriter writer = new FileWriter(file);
            for (final Double aColumn : column) {
                writer.write(aColumn.toString());
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public static void printCSVColumnsToFile(final File file, final DataColumn... columns) {
        try {
            final FileWriter writer = new FileWriter(file);
            //write header
            for (int i = 0; i < columns.length; i++) {
                if (i != 0)
                    writer.write(",");
                writer.write(columns[i].getName());
            }
            writer.write("\n");

            //write columns
            for (int row = 0; row < columns[0].size(); row++) {
                for (int i = 0; i < columns.length; i++) {
                    if (i != 0)
                        writer.write(",");
                    writer.write(String.valueOf(columns[i].get(row)));
                }
                writer.write("\n");

            }
            writer.flush();
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public static void pollFishersToFile(
        final Collection<Fisher> fishers,
        final File file, final Sensor<Fisher, Double>... pollers
    ) {
        // File histogramFile = Paths.get("runs", "lambda", "hist100.csv").toFile();
        final ArrayList<String> histogram = new ArrayList<>(fishers.size());
        for (final Fisher fisher : fishers) {

            final StringBuilder row = new StringBuilder();
            row.append(fisher.getID());
            for (final Sensor<Fisher, Double> poller : pollers) {
                row.append(",");
                row.append(poller.scan(fisher));
            }
            histogram.add(
                row.toString());


        }

        final String csvColumn = histogram.stream().reduce((t, u) -> t + "\n" + u).get();

        try {
            final FileWriter writer = new FileWriter(file);
            writer.write(csvColumn);
            writer.flush();
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void pollHistogramToFile(
        final Collection<Fisher> fishers, final File file, final Sensor<Fisher, Double> poller
    ) {
        // File histogramFile = Paths.get("runs", "lambda", "hist100.csv").toFile();
        final ArrayList<String> histogram = new ArrayList<>(fishers.size());
        for (final Fisher fisher : fishers) {

            histogram.add(
                Double.toString(
                    poller.scan(fisher)
                )
            );
        }

        final String csvColumn = histogram.stream().reduce((t, u) -> t + "\n" + u).get();

        try {
            final FileWriter writer = new FileWriter(file);
            writer.write(csvColumn);
            writer.flush();
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static String getComputerName() {
        try {
            final InetAddress addr;
            addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (final UnknownHostException ex) {
            final Map<String, String> env = System.getenv();
            if (env.containsKey("COMPUTERNAME"))
                return env.get("COMPUTERNAME");
            else return env.getOrDefault(
                "HOSTNAME",
                "UNKNOWN"
            );
        }
    }

    /**
     * takes a column of daily observations and sum them up to generate a yearly observation
     *
     * @param column colun to sum over
     * @return a sum or NAN if the column is empty
     */
    public static <T> Gatherer<T> generateYearlySum(final DataColumn column) {
        return generateYearlySum(column, x -> x);
    }

    /**
     * takes a column of daily observations and sum them up to generate a yearly observation
     *
     * @param column         colun to sum over
     * @param sumTransformer takes the sum and applies an arbitrary function to it
     * @return a sum or NAN if the column is empty
     */
    public static <T> Gatherer<T> generateYearlySum(
        final DataColumn column,
        final SerializableFunction<Double, Double> sumTransformer
    ) {
        return state -> {
            //get the iterator
            final Iterator<Double> iterator = column.descendingIterator();
            if (!iterator.hasNext()) //not ready/year 1
                return Double.NaN;
            double sum = 0;
            for (int i = 0; i < 365; i++) {
                //it should be step 365 times at most, but it's possible that this agent was added halfway through
                //and only has a partially filled collection
                if (iterator.hasNext())
                    sum += iterator.next();
            }

            return sumTransformer.apply(sum);
        };
    }

    /**
     * takes a column of daily observations and sum them up to generate a yearly observation
     *
     * @param column colun to sum over
     * @return a sum or NAN if the column is empty
     */
    public static <T> Gatherer<T> generateYearlyAverage(final DataColumn column) {

        return new Gatherer<T>() {
            @Override
            public Double apply(final T state) {
                //get the iterator
                final Iterator<Double> iterator = column.descendingIterator();
                if (!iterator.hasNext()) //not ready/year 1
                    return Double.NaN;
                final DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
                for (int i = 0; i < 365; i++) {
                    //it should be step 365 times at most, but it's possible that this agent was added halfway through
                    //and only has a partially filled collection
                    if (iterator.hasNext()) {
                        final Double next = iterator.next();
                        if (Double.isFinite(next))
                            statistics.accept(next);
                    }
                }

                return statistics.getAverage();
            }
        };
    }

    /**
     * taken from the c++ version here: http://www.codeproject.com/Articles/49723/Linear-correlation-and-statistical-functions
     * which explains the minimalistic naming convention
     */
    public static double computeCorrelation(final double[] x, final double[] y) {

        //will regularize the unusual case of complete correlation
        final double TINY = 1.0e-20;
        int j;
        final int n = x.length;
        Double yt, xt, t, df;
        Double syy = 0.0, sxy = 0.0, sxx = 0.0, ay = 0.0, ax = 0.0;
        for (j = 0; j < n; j++) {
            //finds the mean
            ax += x[j];
            ay += y[j];
        }
        ax /= n;
        ay /= n;
        for (j = 0; j < n; j++) {
            // compute correlation coefficient
            xt = x[j] - ax;
            yt = y[j] - ay;
            sxx += xt * xt;
            syy += yt * yt;
            sxy += xt * yt;
        }
        return sxy / (Math.sqrt(sxx * syy) + TINY);
    }

    /**
     * returns L *  (1-(1/(Math.exp(-k*(x-x0)))));
     */
    public static double logisticProbability(final double L, final double k, final double x0, final double x) {


        return L * (1 - (1 / (1 + Math.exp(-k * (x - x0)))));


    }

    /**
     * catch with fixed proportion
     *
     * @param where             location fished
     * @param hoursSpentFishing hours spent fishing
     * @param species           species targeted
     * @param q                 catchability
     * @return
     */
    public static double catchSpecieGivenCatchability(
        final LocalBiology where, final int hoursSpentFishing, final Species species, final double q
    ) {
        Preconditions.checkState(q >= 0);
        Preconditions.checkArgument(hoursSpentFishing == Fishing.MINIMUM_HOURS_TO_PRODUCE_A_CATCH);
        //catch
        final double specieCatch = Math.min(
            FishStateUtilities.round(where.getBiomass(species) *
                q),
            where.getBiomass(species)
        );


        return specieCatch;

    }

    /**
     * round to 2nd decimal value
     *
     * @param value
     * @return
     */
    public static double round(final double value) {

        return (double) Math.round(value * 100) / 100;
    }

    /**
     * converts easting-northing UTM. taken from:
     * http://stackoverflow.com/a/28224544/975904
     *
     * @param UTM
     * @param easting
     * @param northing
     * @return
     */
    public static Point2D.Double utmToLatLong(final String UTM, final double easting, final double northing) {
        double latitude;
        double longitude;
        final String[] parts = UTM.split(" ");
        final int zone = Integer.parseInt(parts[0]);
        final char Letter = parts[1].toUpperCase(Locale.ENGLISH).charAt(0);
        final double Hem;
        if (Letter > 'M')
            Hem = 'N';
        else
            Hem = 'S';
        final double north;
        if (Hem == 'S')
            north = northing - 10000000;
        else
            north = northing;

        latitude = (north / 6366197.724 / 0.9996 +
            (1 + 0.006739496742 * Math.pow
                (Math.cos
                    (north / 6366197.724 / 0.9996), 2)
                - 0.006739496742 * Math.sin(north / 6366197.724 / 0.9996) *
                Math.cos(north / 6366197.724 / 0.9996) * (Math.atan(Math.cos
                (Math.atan((Math.exp((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt
                    ((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) *
                    (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt
                        ((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2)
                        / 2 * Math.pow(
                        Math.cos(north / 6366197.724 / 0.9996),
                        2
                    ) / 3)) - Math.exp(-(easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )))), 2) / 2 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                ) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(
                    2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(
                    0.006739496742 * 3 / 4,
                    2
                ) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
                    2 * north / 6366197.724 / 0.9996) * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )) / 4 - Math.pow(
                    0.006739496742 * 3 / 4,
                    3
                ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
                    2 * north / 6366197.724 / 0.9996) * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                ) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt(
                    (1 + 0.006739496742 * Math.pow(
                        Math.cos(north / 6366197.724 / 0.9996),
                        2
                    )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )))), 2) / 2 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )) + north / 6366197.724 / 0.9996))) * Math.tan((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(
                2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(
                0.006739496742 * 3 / 4,
                2
            ) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
                2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(
                0.006739496742 * 3 / 4,
                3
            ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
                2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(
                2 * north / 6366197.724 / 0.9996) * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            ) * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            )) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            )))), 2) / 2 * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            )) + north / 6366197.724 / 0.9996)) - north / 6366197.724 / 0.9996) * 3 / 2) * (Math.atan(Math.cos(Math.atan(
                (Math.exp((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                    Math.cos(
                        north / 6366197.724 / 0.9996),
                    2
                )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )))), 2) / 2 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                ) / 3)) - Math.exp(-(easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )))), 2) / 2 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                ) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(
                    2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(
                    0.006739496742 * 3 / 4,
                    2
                ) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
                    2 * north / 6366197.724 / 0.9996) * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )) / 4 - Math.pow(
                    0.006739496742 * 3 / 4,
                    3
                ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
                    2 * north / 6366197.724 / 0.9996) * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                ) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt(
                    (1 + 0.006739496742 * Math.pow(
                        Math.cos(north / 6366197.724 / 0.9996),
                        2
                    )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )))), 2) / 2 * Math.pow(
                    Math.cos(north / 6366197.724 / 0.9996),
                    2
                )) + north / 6366197.724 / 0.9996))) * Math.tan((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(
                2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(
                0.006739496742 * 3 / 4,
                2
            ) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
                2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(
                0.006739496742 * 3 / 4,
                3
            ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
                2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(
                2 * north / 6366197.724 / 0.9996) * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            ) * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            )) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            )))), 2) / 2 * Math.pow(
                Math.cos(north / 6366197.724 / 0.9996),
                2
            )) + north / 6366197.724 / 0.9996)) - north / 6366197.724 / 0.9996)) * 180 / Math.PI;
        latitude = Math.round(latitude * 10000000);
        latitude = latitude / 10000000;
        longitude = Math.atan((Math.exp((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )))), 2) / 2 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        ) / 3)) - Math.exp(-(easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )))), 2) / 2 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        ) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(
            2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(
            0.006739496742 * 3 / 4,
            2
        ) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
            2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(
            0.006739496742 * 3 / 4,
            3
        ) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(
            2 * north / 6366197.724 / 0.9996) * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        ) * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )))) * (1 - 0.006739496742 * Math.pow((easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )))), 2) / 2 * Math.pow(
            Math.cos(north / 6366197.724 / 0.9996),
            2
        )) + north / 6366197.724 / 0.9996)) * 180 / Math.PI + zone * 6 - 183;
        longitude = Math.round(longitude * 10000000);
        longitude = longitude / 10000000;
        return new Point2D.Double(latitude, longitude);
    }

    /**
     * weights this number of fish (split into age cohorts) into the total amount of biomass
     *
     * @param male
     * @param female
     * @param meristics
     * @return
     */
    public static double weigh(final double[] male, final double[] female, final Meristics meristics) {

        double totalWeight = 0;
        //go through all the fish and sum up their weight at given age
        for (int age = 0; age < meristics.getNumberOfBins(); age++) {
            totalWeight += meristics.getWeight(MALE, age) * male[age];
            totalWeight += meristics.getWeight(FEMALE, age) * female[age];
        }

        return totalWeight;


    }

    /**
     * weights this number of fish  assuming they are all male
     *
     * @param abundance number of fish per size
     * @param meristics species object containig the details
     * @return the weight of hte fish
     */
    public static double weigh(final StructuredAbundance abundance, final Meristics meristics) {
        return weigh(abundance.asMatrix(), meristics);


    }

    /**
     * weights this number of fish  assuming they are all male
     *
     * @param abundance number of fish per size
     * @param meristics species object containig the details
     * @return the weight of hte fish
     */
    public static double weigh(final double[][] abundance, final Meristics meristics) {
        //no female-male split
        double totalWeight = 0;
        //go through all the fish and sum up their weight at given age
        for (int subdivision = 0; subdivision < meristics.getNumberOfSubdivisions(); subdivision++)
            for (int bin = 0; bin < meristics.getNumberOfBins(); bin++)
                totalWeight += abundance[subdivision][bin] * meristics.getWeight(subdivision, bin);

        return totalWeight;


    }

    /**
     * used to weigh only one bin of the structured abundance catch
     *
     * @param abundance
     * @param meristics
     * @param binIndex
     * @return
     */
    public static double weigh(
        final StructuredAbundance abundance,
        final Meristics meristics, final int binIndex
    ) {
        //no female-male split
        double totalWeight = 0;
        //go through all the fish and sum up their weight at given age
        for (int subdivision = 0; subdivision < meristics.getNumberOfSubdivisions(); subdivision++) {
            totalWeight += abundance.getAbundance(subdivision, binIndex) * meristics.getWeight(subdivision, binIndex);
        }

        return totalWeight;


    }

    /**
     * used to weigh only one bin for one subdivision of the structured abundance catch
     *
     * @param abundance
     * @param meristics
     * @param binIndex
     * @return
     */
    public static double weigh(
        final StructuredAbundance abundance,
        final Meristics meristics, final int subdivisionIndex,
        final int binIndex
    ) {
        //no female-male split
        double totalWeight = 0;
        //go through all the fish and sum up their weight at given age

        totalWeight += abundance.getAbundance(subdivisionIndex, binIndex) * meristics.getWeight(
            subdivisionIndex,
            binIndex
        );


        return totalWeight;


    }

    /**
     * weights this number of fish  assuming they are all male
     *
     * @param ageStructure number of fish per size
     * @param species      species object containig the details
     * @return the weight of hte fish
     */
    private static double weigh(final double[] ageStructure, final Meristics species) {
        double totalWeight = 0;
        //go through all the fish and sum up their weight at given age
        for (int age = 0; age < species.getNumberOfBins(); age++) {
            totalWeight += species.getWeight(0, age) * ageStructure[age];
        }

        return totalWeight;


    }

    /**
     * this is a slight modification of ArrayUtils.toString(.) to deal with nested arrays
     *
     * @param array           the (possibly array, possibly nested) object to display
     * @param rowSeparator    how should row elements (within same array) be seperated
     * @param columnSeparator how should each array start and close
     * @return
     */
    public static String deepToStringArray(
        final Object array,
        final String rowSeparator,
        final String columnSeparator
    ) {

        if (array == null) {
            return "";
        }

        if (!array.getClass().isArray()) {
            return String.valueOf(array);
        }

        final StringBuilder builder = new StringBuilder();

        builder.append(columnSeparator);
        for (int i = 0, length = Array.getLength(array); i < length; i++) {
            final String value;
            final Object toRepresent = Array.get(array, i);
            if (!toRepresent.getClass().isArray())
                value = String.valueOf(toRepresent);
            else
                value = deepToStringArray(toRepresent, rowSeparator, columnSeparator);
            // Concatenate the separator
            if (i > 0)
                builder.append(rowSeparator);

            // Build the string
            builder.append(value);
        }
        builder.append(columnSeparator);


        return builder.toString();
    }

    public static String getFilenameExtension(final File file) {

        final String fileName = file.getName();
        String extension = "";

        final int i = fileName.lastIndexOf('.');
        final int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    public static FishState readModelFromFile(final File file) {
        Logger.getGlobal().info("Reading from File");
        final XStream xstream = new XStream(new StaxDriver());
        String xml = null;
        try {
            xml = new String(Files.readAllBytes(file.toPath()));
            return (FishState) xstream.fromXML(xml);
        } catch (final IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("Failed to read file " + file);
            return null;
        }
    }

    public static void deleteRecursively(final File f) throws IOException {
        if (f.isDirectory()) {
            for (final File c : f.listFiles())
                deleteRecursively(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    public static Function<Double, Double> normalPDF(final double mean, final double standardDeviation) {
        return new Function<Double, Double>() {


            @Override
            public Double apply(final Double x) {
                return Math.exp(-Math.pow(x - mean, 2) / (2 * standardDeviation * standardDeviation)) /
                    Math.sqrt(2 * standardDeviation * standardDeviation * Math.PI);

            }
        };
    }

    public static String printTablePerPort(
        final FishState model,
        final String fisherYearlyColumn,
        final int firstValidYear
    ) {
        final HashMap<String, DataColumn> portColumns = new HashMap<>();
        final LinkedList<String> columns = new LinkedList<>();

        for (final Port port : model.getPorts()) {
            portColumns.put(port.getName(), new DataColumn(port.getName() + " " + fisherYearlyColumn));
            columns.add(port.getName());
        }
        assert columns.size() > 0;
        assert model.getYear() > 0;

        for (int year = firstValidYear; year < model.getYear(); year++) {
            final HashMap<String, DoubleSummaryStatistics> averages = new HashMap<>();
            for (final String portName : portColumns.keySet())
                averages.put(portName, new DoubleSummaryStatistics());
            for (final Fisher fisher : model.getFishers()) {
                averages.get(fisher.getHomePort().getName()).accept(
                    fisher.getYearlyData().getColumn(fisherYearlyColumn).get(year));
            }

            for (final Map.Entry<String, DoubleSummaryStatistics> average : averages.entrySet())
                portColumns.get(average.getKey()).add(average.getValue().getAverage());


        }


        final StringBuilder builder = new StringBuilder();

        //write header
        for (int i = 0; i < columns.size(); i++) {
            if (i != 0)
                builder.append(",");
            builder.append(columns.get(i));
        }
        builder.append("\n");

        //write columns
        for (int row = 0; row < model.getYear() - firstValidYear; row++) {
            for (int i = 0; i < columns.size(); i++) {
                if (i != 0)
                    builder.append(",");
                builder.append(portColumns.get(columns.get(i)).get(row));
            }
            builder.append("\n");

        }

        return builder.toString();

    }

    //from here: http://stackoverflow.com/questions/80476/how-can-i-concatenate-two-arrays-in-java
    public static double[] concatenateArray(final double[] a, final double[] b) {
        final int aLen = a.length;
        final int bLen = b.length;
        final double[] c = new double[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    //from here: https://gist.github.com/gubatron/c4c816fcec5a74b752ea
    public static List<double[]> splitArray(final double[] items, final int maxSubArraySize) {
        final List<double[]> result = new ArrayList<>();
        if (items == null || items.length == 0) {
            return result;
        }

        int from = 0;
        int to = 0;
        int slicedItems = 0;
        while (slicedItems < items.length) {
            to = from + Math.min(maxSubArraySize, items.length - to);
            final double[] slice = Arrays.copyOfRange(items, from, to);
            result.add(slice);
            slicedItems += slice.length;
            from = to;
        }
        return result;
    }

    public static FishState run(
        final String simulationName, final Path scenarioYaml,
        @Nullable final Path outputFolder,
        final Long seed, final String logLevel, final boolean additionalData,
        final String policyScript, final int yearsToRun,
        final boolean saveOnExit, final Integer heatmapGathererYear,
        @Nullable final Consumer<Scenario> scenarioSetup,
        @Nullable final Consumer<FishState> preStartSetup,
        //if any of returns true, it stops the simulation before it is over!
        @Nullable final LinkedList<Pair<Integer,
            AlgorithmFactory<? extends AdditionalStartable>>> outsidePlugins,
        @Nullable final List<Predicate<FishState>> circuitBreakers
    ) throws IOException {


        System.out.println("seed " + seed);
        //create scenario and files
        final String fullScenario = String.join("\n", Files.readAllLines(scenarioYaml));

        final FishYAML yaml = new FishYAML();
        final Scenario scenario = yaml.loadAs(fullScenario, Scenario.class);


        if (outputFolder != null) {
            outputFolder.toFile().mkdirs();

            final FileWriter io = new FileWriter(outputFolder.resolve("scenario.yaml").toFile());
            yaml.dump(scenario, io);
            io.close();

        }

        if (scenarioSetup != null)
            scenarioSetup.accept(scenario);

        final FishState model = new FishState(seed);

        final Logger logger = outputFolder != null
            ? new FishStateLogger(model, outputFolder.resolve(simulationName + "_log.txt"))
            : Logger.getGlobal();
        logger.setLevel(Level.parse(logLevel));


        model.setScenario(scenario);

        final TowHeatmapGatherer gatherer;
        if (heatmapGathererYear != null && heatmapGathererYear >= 0) {
            gatherer = new TowHeatmapGatherer(heatmapGathererYear);
            model.registerStartable(gatherer);
        } else
            gatherer = null;

        if (preStartSetup != null)
            preStartSetup.accept(model);
        model.start();

        if (additionalData) {
            logger.info("adding additional data");
            model.attachAdditionalGatherers();
        }

        //if you have a policy script, then follow it
        if (policyScript != null && !policyScript.isEmpty()) {
            final String policyScriptString = new String(Files.readAllBytes(Paths.get(policyScript)));
            final PolicyScripts scripts = yaml.loadAs(policyScriptString, PolicyScripts.class);
            model.registerStartable(scripts);
            Files.write(
                outputFolder.resolve("policy_script.yaml"),
                yaml.dump(scripts.getScripts()).getBytes()
            );
        }

        //     System.out.println("random " + model.random.nextDouble());

        mainloop:
        while (model.getYear() < yearsToRun) {
            model.schedule.step(model);
            //           System.out.println("random_day" + model.getDay() +"  ---- " + model.random.nextDouble());

            if (model.getDayOfTheYear() == 1) {

                //if you fail any of the
                if (circuitBreakers != null)
                    for (final Predicate<FishState> circuitBreaker : circuitBreakers) {
                        if (circuitBreaker.test(model))
                            break mainloop;
                    }


                if (outsidePlugins != null)
                    for (final Pair<Integer,
                        AlgorithmFactory<? extends AdditionalStartable>> outsidePlugin : outsidePlugins) {

                        if (outsidePlugin.getFirst() == model.getYear()) {
                            System.out.println("started new plugin");
                            model.registerStartable(outsidePlugin.getSecond().apply(model));
                        }

                    }

                logger.fine("Year " + model.getYear() + " starting");
            }


        }

        if (outputFolder != null) {

            FileWriter writer = new FileWriter(outputFolder.resolve("result.yaml").toFile());
            final ModelResults results = new ModelResults(model);
            yaml.dump(results, writer);

            writer = new FileWriter(outputFolder.resolve("seed.txt").toFile());
            writer.write(Long.toString(seed));
            writer.close();

            if (gatherer != null) {
                writer = new FileWriter(outputFolder.resolve("tow_heatmap.txt").toFile());
                writer.write(FishStateUtilities.gridToCSV(gatherer.getTowHeatmap()));
                writer.close();

            }
            writeAdditionalOutputsToFolder(outputFolder, model);
            if (saveOnExit)
                writeModelToFile(
                    outputFolder.resolve(simulationName + ".checkpoint").toFile(),
                    model
                );
        }


        return model;
    }

    public static String gridToCSV(final double[][] grid) {

        final StringBuilder buffer = new StringBuilder();
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                buffer.append(grid[x][y]);
                if (y < grid[x].length - 1)
                    buffer.append(",");
            }
            buffer.append("\n");
        }

        return buffer.toString();

    }

    public static void writeAdditionalOutputsToFolder(
        final Path outputFolder,
        final FishState model
    ) throws IOException {
        FileWriter writer;//add additional outputs
        for (final OutputPlugin plugin : model.getOutputPlugins()) {
            plugin.reactToEndOfSimulation(model);
            writer = new FileWriter(outputFolder.resolve(plugin.getFileName()).toFile());
            writer.write(plugin.composeFileContents());
            writer.close();

        }
    }

    public static void writeModelToFile(final File file, final FishState state) {
        final XStream xstream = new XStream(new StaxDriver());
        Logger.getGlobal().info("Writing to file!");
        final String xml = xstream.toXML(state);

        try {
            Files.write(file.toPath(), xml.getBytes());
            Logger.getGlobal().info("State saved at " + file);
        } catch (final IOException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("Failed to write file " + file + "with error: " + e.getMessage());
        }
    }

    /**
     * function that produces a predictor setup. Basically if the "usePredictor" flag is true this function
     * will return a consumer that, when called, will add catch predictors to the fisher; the predictors are necessary
     * to build ITQ reservation prices
     *
     * @param usePredictors if false it returns an empty consumer
     * @param biology       link to the model being initialize
     * @return consumer we can use to setup agents
     */
    public static final Consumer<Fisher> predictorSetup(
        final boolean usePredictors,
        final GlobalBiology biology
    ) {

        if (!usePredictors)
            return new Consumer<Fisher>() {
                @Override
                public void accept(final Fisher fisher) {

                }
            };
        else
            return new Consumer<Fisher>() {
                @Override
                public void accept(final Fisher fisher) {

                    for (final Species species : biology.getSpecies()) {

                        //create the predictors

                        fisher.setDailyCatchesPredictor(
                            species.getIndex(),
                            MovingAveragePredictor.dailyMAPredictor(
                                "Predicted Daily Catches of " + species,
                                fisher1 ->
                                    //check the daily counter but do not input new values
                                    //if you were not allowed at sea
                                    fisher1.getDailyCounter().getLandingsPerSpecie(
                                        species.getIndex())

                                ,
                                365
                            )
                        );


                        fisher.setProfitPerUnitPredictor(species.getIndex(), MovingAveragePredictor.perTripMAPredictor(
                            "Predicted Unit Profit " + species,
                            fisher1 -> fisher1.getLastFinishedTrip().getUnitProfitPerSpecie(species.getIndex()),
                            30
                        ));


                    }


                    //daily profits predictor
                    fisher.assignDailyProfitsPredictor(
                        MovingAveragePredictor.dailyMAPredictor("Predicted Daily Profits",
                            fisher1 ->
                                //check the daily counter but do not input new values
                                //if you were not allowed at sea
                                fisher1.isAllowedAtSea() ?
                                    fisher1.getDailyCounter().
                                        getColumn(
                                            FisherYearlyTimeSeries.CASH_FLOW_COLUMN)
                                    :
                                    Double.NaN
                            ,

                            7
                        ));

                }

            };
    }

    /**
     * 8.123 is rounded to 9 with probability of 12.3%
     *
     * @param x      number to round
     * @param random randomiser
     * @return x either ceiled or floored
     */
    public static int randomRounding(double x, final MersenneTwisterFast random) {
        final double signum = Math.signum(x);
        x = Math.abs(x);
        final boolean ceiling = random.nextDouble() < x - (int) x;
        final int toReturn = (int) (ceiling ? Math.ceil(x) : Math.floor(x));

        return signum > 0 ? toReturn : -toReturn;
    }

    public static SeaTile getValidSeatileFromGroup(
        final MersenneTwisterFast random,
        final List<SeaTile> mapGroup,
        final boolean respectMPA,
        final Fisher fisher,
        final FishState model,
        final boolean ignoreWastelands,
        final int maxAttempts
    ) {
        int attempts = 0;
        SeaTile tile;
        do {
            tile =
                mapGroup.get(random.nextInt(mapGroup.size()));

            attempts++;

            if (attempts > maxAttempts)
                break;

        } while (
            (respectMPA && !fisher.isAllowedToFishHere(tile, model)) ||
                (ignoreWastelands && !tile.isFishingEvenPossibleHere()));
        if (attempts > maxAttempts) {
            return null;
        }
        assert !respectMPA || fisher.isAllowedToFishHere(tile, model);
        assert !ignoreWastelands || tile.isFishingEvenPossibleHere();
        return tile;
    }

    public static double getAverage(final DataColumn column, final int startAtIndex) {
        final Iterator<Double> iterator = column.iterator();
        for (int i = 0; i < startAtIndex; i++)
            iterator.next();
        final DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
        while (iterator.hasNext())
            statistics.accept(iterator.next());

        return statistics.getAverage();


    }

    public static double getWeightedAverage(final double[] observations, final double[] weight) {
        Preconditions.checkArgument(observations.length == weight.length);
        double sum = 0;
        double denominator = 0;
        for (int i = 0; i < observations.length; i++) {
            sum += observations[i] * weight[i];
            denominator += weight[i];
        }
        return sum / denominator;
    }

    public static double timeSeriesDistance(
        final DataColumn data,
        final Path csvFilePath, final double exponent
    ) throws IOException {
        return timeSeriesDistance(
            data,
            Files.readAllLines(csvFilePath).stream().mapToDouble(
                value -> Double.parseDouble(value.trim())
            ).boxed().collect(Collectors.toList()
            ),
            exponent,

            false
        );
    }

    public static double timeSeriesDistance(
        final Iterable<Double> timeSeriesOne,
        final Iterable<Double> timeSeriesTwo,
        final double exponent, final boolean cumulativeError
    ) {
        Preconditions.checkArgument(exponent > 0);
        final Iterator<Double> firstIterator = timeSeriesOne.iterator();
        final Iterator<Double> secondIterator = timeSeriesTwo.iterator();


        double error = 0;
        while (firstIterator.hasNext()) {
            Preconditions.checkArgument(
                secondIterator.hasNext(),
                "Time series are of different length"
            );

            double raw = firstIterator.next() - secondIterator.next();
            if (!cumulativeError) {
                raw = Math.pow(Math.abs(raw), exponent);
            }

            error += raw;
        }
        Preconditions.checkArgument(
            !secondIterator.hasNext(),
            "Time series are of different length"
        );

        return Math.abs(error);
    }

    /**
     * same as java.util.stream.Collectors::throwingMerger (which is annoyingly private)
     * Useful for collecting a stream to a non-java.util.HashMap Map implementation.
     * I think Java 9 has a toMap method that doesn't require this and that we could use once we upgrade.
     */
    public static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    /**
     * Just makes it nicer to create AbstractMap.SimpleImmutableEntry objects.
     */
    public static <K, V> AbstractMap.SimpleImmutableEntry<K, V> entry(final K k, final V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    /**
     * Builds an immutable map where each element of {@code keys}
     * is associated with the corresponding element of {@code values}.
     */
    public static <K, V> ImmutableMap<K, V> zipToMap(final Iterable<K> keys, final Iterable<V> values) {
        //noinspection UnstableApiUsage
        return zip(stream(keys), stream(values), AbstractMap.SimpleImmutableEntry::new)
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * if global biology is made up only of species whose meristics is the default fake one, i can quite safely assume
     * that there is no abundance to be worried of
     *
     * @return
     */
    public static boolean guessIfBiologyIsBiomassOnly(final GlobalBiology biology) {
        for (final Species species : biology.getSpecies()) {
            if (species.getMeristics() != StockAssessmentCaliforniaMeristics.FAKE_MERISTICS)
                return false;
        }
        return true;
    }

    public static <T> Collector<T, ?, List<T>> shufflingCollector(
        final MersenneTwisterFast rng
    ) {
        return shufflingCollector(ArrayList::new, rng);
    }

    public static <T, C extends List<T>> Collector<T, ?, C> shufflingCollector(
        final Supplier<C> collectionSupplier,
        final MersenneTwisterFast rng
    ) {
        return collectingAndThen(
            Collectors.toCollection(collectionSupplier),
            collection -> {
                Collections.shuffle(collection, new Random(rng.nextLong()));
                return collection;
            }
        );
    }

    /**
     * A functional interface that can be used to ensure that a function is serializable.
     */
    @FunctionalInterface
    interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
    }

}

