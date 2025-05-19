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

package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.*;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * this is basically the IT5/IT10 controller form the DLM toolkit, using
 * the target formula from ITarget for reference
 */
public class IndexTargetController extends Controller {


    public static final Actuator<FishState, Double> RATIO_TO_SEASONAL_CLOSURE = (subject, effortRatio, model) -> {
        if (!Double.isFinite(effortRatio))
            return;

        assert effortRatio <= 1 : "i assume it's never above 1!";
        assert effortRatio >= 0 : "i assume it's always positive!";

        System.out.println("season length " + (int) (365 * effortRatio));


        final FishingSeason season =
            new FishingSeason(
                true,

                (int) (365 * effortRatio)
            );

        for (Fisher fisher : model.getFishers()) {
            fisher.setRegulation(season);
        }
    };
    public static final Actuator<FishState, Double> RATIO_TO_PERSONAL_SEASONAL_CLOSURE = (subject, effortRatio, model) -> {
        if (!Double.isFinite(effortRatio))
            return;

        assert effortRatio <= 1 : "i assume it's never above 1!";
        assert effortRatio >= 0 : "i assume it's always positive!";

        System.out.println("season length " + (int) (365 * effortRatio));


        final MaxHoursOutRegulation season =
            new MaxHoursOutRegulation(
                new ProtectedAreasOnly(),

                (int) (365 * 24 * effortRatio)
            );

        for (Fisher fisher : model.getFishers()) {
            fisher.setRegulation(season);
        }
    };
    public static final Actuator<FishState, Double> RATIO_TO_FLEET_SIZE =
        (subject, effortRatio, model) -> {
            if (!Double.isFinite(effortRatio))
                return;

            assert effortRatio <= 1 : "i assume it's never above 1!";
            assert effortRatio >= 0 : "i assume it's always positive!";

            System.out.println("fleet size " + effortRatio);


            for (Fisher fisher : model.getFishers()) {
                if (model.getRandom().nextDouble() > effortRatio)
                    fisher.setRegulation(new FishingSeason(true, 0));
                else
                    fisher.setRegulation(new FishingSeason(true, 365));

            }
        };
    public static final Actuator<FishState, Double> RATIO_TO_DAYSATSEA =
        (subject, effortRatio, model) -> {
            if (!Double.isFinite(effortRatio))
                return;

            assert effortRatio <= 1 : "i assume it's never above 1!";
            assert effortRatio >= 0 : "i assume it's always positive!";


            for (Fisher fisher : model.getFishers()) {
                Preconditions.checkArgument(
                    fisher.getFishingStrategy() instanceof MaximumDaysDecorator,
                    "Fisher doesn't have a max days at sea already set up; use other actuator"
                );
                final MaximumDaysDecorator strategy = (MaximumDaysDecorator) fisher.getFishingStrategy();
                final MaximumTripLengthRegulation newGearToUse =
                    new MaximumTripLengthRegulation(
                        strategy.getDaysBeforeGoingHome() * 24 * effortRatio
                    );
                fisher.setRegulation(newGearToUse);
            }
        };
    private final static double MINIMUM_POLICY_ALLOWED = 0;
    private static final long serialVersionUID = -1591012118969203672L;
    private final double maxPercentageChange;
    /**
     * lower is better, which means that the ratio is not I/ITarget but ITarget/I
     */
    private final boolean inverse;
    /**
     * is the ratio capped at 1? true means not capped
     */
    private final boolean canGoAboveZero;


    //public static final Actuator<FishState, Double> RATIO_TO_CATCHABILITY(final double originalCatchability)
    private double lastPolicy = Double.NaN;


    public IndexTargetController(
        final Sensor<FishState, Double> observed,
        final Sensor<FishState, Double> target,
        final Actuator<FishState, Double> actuator,
        final int intervalInDays,
        final double maxPercentageChange, final boolean inverse, final boolean canGoAboveZero
    ) {
        super(observed, target, actuator, intervalInDays);
        this.maxPercentageChange = maxPercentageChange;
        this.inverse = inverse;
        this.canGoAboveZero = canGoAboveZero;
    }

    public static final Actuator<FishState, Double> RATIO_TO_TAC(
        final Sensor<FishState, Double> lastCatchesSensor,
        final double minimumTAC,
        final double maxPercentageChange
    ) {
        return (subject, effortRatio, model) -> {
            if (!Double.isFinite(effortRatio))
                return;

            assert effortRatio >= 0 : "i assume it's always positive!";

            //with TAC IT we need to cap TAC changes rather than the multiplier
            if (effortRatio < 1d - maxPercentageChange)
                effortRatio = 1d - maxPercentageChange;
            else if (effortRatio > 1d + maxPercentageChange)
                effortRatio = 1d + maxPercentageChange;

            final Double lastCatches =
                Math.max(
                    lastCatchesSensor.scan(subject),
                    minimumTAC
                );
            System.out.println("lastCatches " + lastCatches);
            System.out.println("effortratio " + effortRatio);

            final double tac = Math.max(
                effortRatio * lastCatches,
                minimumTAC
            );

            System.out.println("tac " + tac);


            final MonoQuotaRegulation season =
                new MonoQuotaRegulation(tac);

            for (final Fisher fisher : model.getFishers()) {
                fisher.setRegulation(season);
            }
        };
    }

    public static final Actuator<FishState, Double> RATIO_TO_CATCHABILITY(
        final double originalCatchability
    ) {
        return (subject, effortRatio, model) -> {
            if (!Double.isFinite(effortRatio))
                return;

            assert effortRatio <= 1 : "i assume it's never above 1!";
            assert effortRatio >= 0 : "i assume it's always positive!";

            System.out.println("trip length " + originalCatchability * effortRatio);

            final RandomCatchabilityTrawl newGearToUse = new RandomCatchabilityTrawl(
                new double[]{originalCatchability * effortRatio},
                new double[]{0},
                5
            );

            for (final Fisher fisher : model.getFishers()) {

                fisher.setGear(
                    newGearToUse
                );


            }
        };
    }

    public static final Actuator<FishState, Double> RATIO_TO_DAYSATSEA(final double originalDaysAtSea) {
        return (subject, effortRatio, model) -> {
            if (!Double.isFinite(effortRatio))
                return;

            assert effortRatio <= 1 : "i assume it's never above 1!";
            assert effortRatio >= 0 : "i assume it's always positive!";

            System.out.println("trip length " + originalDaysAtSea * 24 * effortRatio);
            final MaximumTripLengthRegulation newGearToUse =
                new MaximumTripLengthRegulation(
                    originalDaysAtSea * 24 * effortRatio
                );
            for (final Fisher fisher : model.getFishers()) {
                fisher.setRegulation(newGearToUse);
            }
        };
    }

    @Override
    public double computePolicy(final double currentVariable, final double target, final FishState model, final double oldPolicy) {

        if (Double.isNaN(lastPolicy))
            lastPolicy = 1d;

        System.out.println("target " + target);
        System.out.println("currentVariable " + currentVariable);

        //new ratio
        final double newRatio = inverse ? target / currentVariable : currentVariable / target;


        if (!Double.isFinite(newRatio))
            return lastPolicy;

        double newPolicy = lastPolicy * newRatio;

        //keep it between 0 and 1
        if (newPolicy < MINIMUM_POLICY_ALLOWED)
            newPolicy = MINIMUM_POLICY_ALLOWED;
        if (newPolicy > 1 && !canGoAboveZero)
            newPolicy = 1;


        //do not let it go past the maximum percentage change
        if (newPolicy > lastPolicy * (1 + maxPercentageChange))
            newPolicy = lastPolicy * (1 + maxPercentageChange);
        if (newPolicy < lastPolicy * (1 - maxPercentageChange))
            newPolicy = lastPolicy * (1 - maxPercentageChange);

        lastPolicy = newPolicy;
        return lastPolicy;

    }


    public double getLastPolicy() {
        return lastPolicy;
    }

    public void setLastPolicy(final double lastPolicy) {
        this.lastPolicy = lastPolicy;
    }
}
