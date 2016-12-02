package uk.ac.ox.oxfish.utility;

/**
 * Just a marker for season
 * Created by carrknight on 12/2/16.
 */
public enum Season {

    SPRING,

    SUMMER,

    FALL,

    WINTER;

    /**
     * turns day of the year into season, approximately
     * @param dayOfTheYear the day of the year
     * @return
     */
    public static Season season(int dayOfTheYear)
    {

        int month = (int)(dayOfTheYear / 30.42) + 1; //plus one is there so that december is the 12 month
        int day = (int) (dayOfTheYear % 30.42);


        //stolen from here:
        // http://stackoverflow.com/a/9501252/975904
        if ((month == 3 & day >= 21) | (month == 4) | (month == 5) | (month == 6 & day < 21)) {
            return SPRING;
        } else if ((month == 6 & day >= 21) | (month == 7) | (month == 8) | (month == 9 & day < 21)) {
            return SUMMER;
        } else if ((month == 9 & day >= 21) | (month == 10) | (month == 11) | (month == 12 & day < 21)) {
            return FALL;
        } else {
            return WINTER;
        }

    }

}
