package uk.ac.ox.oxfish.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Just a bunch of runs that will be knitted together into a dashboard of plots to visually study the health of the model
 * Created by carrknight on 10/30/15.
 */
public class Dashboard
{


    private final static Path DASHBOARD_OUTPUT_DIRECTORY = Paths.get("runs","dashboards");

    public final static Path DASHBOARD_INPUT_DIRECTORY = Paths.get("inputs","dashboard");


    private final static int RUNS_PER_SCENARIO = 10;

    public static void main(String[] args)
    {

        //get the directory to write in: probably with today's date
        String subDirectory = args[0];
        //turn it into a path
        Path containerPath = DASHBOARD_OUTPUT_DIRECTORY.resolve(subDirectory);


        /***
         *      _____                  ____        __   _         _            __   _
         *     / ___/___  ___ _ ____  / __ \ ___  / /_ (_)__ _   (_)___ ___ _ / /_ (_)___   ___
         *    / (_ // -_)/ _ `// __/ / /_/ // _ \/ __// //  ' \ / //_ // _ `// __// // _ \ / _ \
         *    \___/ \__/ \_,_//_/    \____// .__/\__//_//_/_/_//_/ /__/\_,_/ \__//_/ \___//_//_/
         *                                /_/
         */

        System.out.println("===============================================================");
        System.out.println("Gear Optimization");

        for(int i=0; i<RUNS_PER_SCENARIO; i++)
        {
            System.out.println("run " + i);


        }





    }



}
