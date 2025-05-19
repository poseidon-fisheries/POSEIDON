/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.restrictions;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class RestrictedRectangularRegion {

    /**
     * This describes a rectangular region of the map where it is not good to fish.
     * This could be because of community standards or because of damage
     * to personal reputation
     * It has the coordinates as well as the start and end day of the regulation as well as repetition values
     * <p>
     * Currently it can be eternal, onetime (on/off on set model days) or it can repeat annually
     * Repeating annually is set by month/day on and month/day off OR day of year on/day of year off
     */
    public static final int ETERNAL = 1, ONETIME = 2, ANNUAL = 3;
    boolean eternal = false;
    boolean oneTime = false;
    //west < east, north>south, regardless of the actual grid being used. Just keeps it simpler and it
    //won't matter anyway
    int startDay, endDay; //Method 2 - starts and ends on specified days of the model run
    boolean repeatAnnually = false;
    int startMonth, startWeekOfMonth, startWeekOfYear, startDayOfYear, startDayOfMonth, startDayOfWeek;
    int endMonth, endWeekOfMonth, endWeekOfYear, endDayOfYear, endDayOfMonth, endDayOfWeek;
    private boolean active = false;
    private int westGridX, northGridY, eastGridX, southGridY;

    /**
     * Method 1: Eternal
     * The rectangular region is always active
     */
    RestrictedRectangularRegion(SeaTile cornerNW, SeaTile cornerSE, int method) {
        if (method == 1) { //Eternal
            setCorners(cornerNW, cornerSE);
            setEternal();
        }
    }

    void setCorners(SeaTile cornerNW, SeaTile cornerSE) {
        this.westGridX = Math.min(cornerNW.getGridX(), cornerSE.getGridX());
        this.northGridY = Math.min(cornerNW.getGridY(), cornerSE.getGridY());
        this.eastGridX = Math.max(cornerNW.getGridX(), cornerSE.getGridX());
        this.southGridY = Math.max(cornerNW.getGridY(), cornerSE.getGridY());
//		System.out.println("Created Communal Region: "+westGridX + " "+northGridY + " "+ eastGridX + " " + southGridY);
    }

    void setEternal() {
        this.eternal = true;
        this.oneTime = false;
        this.repeatAnnually = false;
    }

    /**
     * Method 2: One Time
     * The rectangular region turns 'on' on a specified day and turns off on a specified day
     */

    public RestrictedRectangularRegion(SeaTile cornerNW, SeaTile cornerSE, int method, int startDay, int endDay) {
        setCorners(cornerNW, cornerSE);
        if (method == ONETIME) { //One time
            setOneTime(startDay, endDay);
        } else if (method == ANNUAL) {//repeat Annually
            setRepeatAnnually((startDay - 1) % 365 + 1, (endDay - 1) % 365 + 1);
        }
    }
    /**
     * Method 3: Repeat annually
     * The rectangular region repeats annually. Set by the month & date. We pretend leap years don't happen.
     * At the moment we cannot accommodate for lunar calendars, or weekly repeats
     */

    public RestrictedRectangularRegion(
        SeaTile cornerNW,
        SeaTile cornerSE,
        int method,
        int startMonth,
        int startDay,
        int endMonth,
        int endDay
    ) {
        if (method == 3) { //Repeat Annually
            setCorners(cornerNW, cornerSE);
            setRepeatAnnually(startMonth, startDay, endMonth, endDay);
        }
    }

    public boolean canIFishHere(FishState model, SeaTile tile) {
        return !(isBadToFishHere(model, tile));
    }

    public boolean isBadToFishHere(FishState model, SeaTile tile) {
        return (isActive(model) && isInRegion(tile));
//		return isActive(model);
    }

    private boolean isActive(FishState model) {
        refreshActive(model);
        return this.active;
    }

    private boolean isInRegion(SeaTile tile) {
//		System.out.println("testing " + tile.getGridX() +" "+ tile.getGridY());
        return (tile.getGridX() >= this.westGridX &&
            tile.getGridX() <= this.eastGridX &&
            tile.getGridY() >= this.northGridY &&
            tile.getGridY() <= this.southGridY);
    }

    private void refreshActive(FishState model) {
        if (this.eternal) {
            this.active = true;
        } else if (this.oneTime) {
            this.active = (model.getDay() >= this.startDay && model.getDay() < this.endDay);
        } else if (this.repeatAnnually) {
            this.active = (model.getDayOfTheYear() >= this.startDayOfYear && model.getDayOfTheYear() < this.endDayOfYear);
        }
    }

    void setOneTime(int startDay, int endDay) {
        if (startDay < endDay) {
            setStartDay(startDay);
            setEndDay(endDay);
            this.oneTime = true;
            this.eternal = false;
            this.repeatAnnually = false;
        }
    }

    void setStartDay(int day) {
        this.startDay = day;
    }

    void setEndDay(int day) {
        this.endDay = day;
    }

    void setStartDate(int startMonth, int startDay) {
        this.startMonth = startMonth;
        this.startDayOfMonth = startDay;
        this.startDayOfYear = dayOfYear(startMonth, startDay);
    }

    void setEndDate(int endMonth, int endDay) {
        this.endMonth = endMonth;
        this.endDayOfMonth = endDay;
        this.endDayOfYear = dayOfYear(endMonth, endDay);
    }

    void setRepeatAnnually(int startMonth, int startDay, int endMonth, int endDay) {
        if (dayOfYear(startMonth, startDay) < dayOfYear(endMonth, endDay)) {
            setStartDate(startMonth, startDay);
            setEndDate(endMonth, endDay);
            this.repeatAnnually = true;
            this.eternal = false;
            this.oneTime = false;
        }
    }

    /*
     * These methods are handy internal tools
     */

    void setRepeatAnnually(int startDayOfYear, int endDayOfYear) {
        int startMonth = 1, endMonth = 1, startDay = startDayOfYear, endDay = endDayOfYear;
        while (startDay - daysInMonth(startMonth) > 0) {
            startDay -= daysInMonth(startMonth);
            startMonth++;
        }
        while (endDay - daysInMonth(endMonth) > 0) {
            endDay -= daysInMonth(endMonth);
            endMonth++;
        }
        setRepeatAnnually(startMonth, startDay, endMonth, endDay);
    }

    private int daysInMonth(int month) {
        if (month == 2) {
            return 28;
        } else {
            int days = 31;
            if (month == 9 || month == 4 || month == 6 || month == 11)
                days = 30; //Thirty days hath September, April June and November
            return days;
        }
    }

    private int dayOfYear(int month, int day) {
        int dayOfTheYear = day;
        for (int i = 1; i < month; i++) {
            dayOfTheYear += daysInMonth(i);
        }
        return dayOfTheYear;
    }


}
