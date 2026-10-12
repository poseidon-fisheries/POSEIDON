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

package uk.ac.ox.poseidon.agents;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.tasks.InactiveBehaviour;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.NONE;

/**
 * A single simulated agent (typically a vessel) that repeatedly drives its current
 * {@link Behaviour} forward, rescheduling itself either after a fixed
 * {@link #taskDuration} (while the behaviour reports itself running) or on the very next tick
 * (once it doesn't).
 * <p>
 * Changes from outside the behaviour (swapping it, or changing the agent's characteristics, as a
 * fleet register does) take effect between runs of the behaviour, never during one: they go
 * through {@link #mutate}, which queues them while the behaviour is running. A run is never
 * disturbed, so, for example, a vessel isn't deactivated at sea or given a new hold with its catch
 * on board. Changes the behaviour makes itself, such as moving the agent, take effect
 * immediately.
 * <p>
 * An agent has at most one step pending on the schedule. The agent isn't itself a
 * {@link Steppable}: what goes on the schedule is its private {@link #stepper}, so nothing outside
 * the agent can schedule it, and every step is scheduled through {@link #scheduleNextStep}, which
 * refuses to schedule a second one. Outside a step, an active agent always has a step pending
 * ({@link #mutate} restores this after each mutation). Becoming inactive doesn't cancel a pending
 * step; the step still runs, finds the agent inactive and schedules nothing, so an agent can be
 * inactive with a step pending, and reactivating it then needs no new step.
 */
@Getter
public class Agent {

    private static final int AGENT_BEHAVIOUR_ORDERING = 1;

    private final @NonNull TemporalSchedule schedule;
    private final @NonNull EventManager eventManager;

    /** Starts as {@link InactiveBehaviour}, so a new agent is inactive; see {@link #setBehaviour}. */
    private @NonNull Behaviour behaviour = InactiveBehaviour.INACTIVE_BEHAVIOUR;

    @Getter(NONE)
    private final Queue<Runnable> mutationQueue = new LinkedList<>();

    /**
     * How long until the running behaviour's task is executed again: set by that task when it
     * reports {@code RUNNING} (see {@link uk.ac.ox.poseidon.agents.tasks.AgentTask#runFor}), and
     * cleared once this agent's next step is scheduled, so each running step must set its own.
     */
    @Setter private Duration taskDuration;

    /** What this agent puts on the schedule: runs {@link #step()}. Private, so only it can. */
    @Getter(NONE)
    private final Steppable stepper = _ -> step();

    /** Whether this agent has a step on the schedule that hasn't run yet. */
    @Getter(NONE)
    private boolean stepPending;

    /**
     * Creates an inactive agent, with no behaviour yet. It starts stepping once it becomes active,
     * through {@link #mutate}: an agent never starts active, so an active agent always has a step
     * pending.
     *
     * @param schedule     the schedule this agent reschedules itself on after each step
     * @param eventManager the event manager this agent's behaviours report through
     */
    @SuppressFBWarnings(
        value = "EI2",
        justification = "Agent keeps references to mutable schedule/event manager by design."
    )
    public Agent(
        @NonNull final TemporalSchedule schedule,
        @NonNull final EventManager eventManager
    ) {
        this.schedule = schedule;
        this.eventManager = eventManager;
    }

    /**
     * Drains any queued mutations (only once the behaviour isn't running), then, if still active,
     * steps the behaviour and reschedules — after {@link #taskDuration} if it's now running, or on
     * the next tick otherwise.
     */
    private void step() {
        stepPending = false;
        if (!behaviour.isRunning()) {
            while (!mutationQueue.isEmpty()) {
                mutationQueue.poll().run();
            }
        }
        if (isActive()) {
            behaviour.step();
            if (behaviour.isRunning()) {
                final Duration delay = checkNotNull(
                    taskDuration,
                    "Agent %s's behaviour is running, but no task set a duration this step.",
                    this
                );
                taskDuration = null;
                scheduleNextStep(delay);
            } else {
                scheduleNextStep();
            }
        }
    }

    /** Schedules this agent's next step on the next tick. */
    private void scheduleNextStep() {
        checkState(!stepPending, "Agent %s already has a step pending.", this);
        stepPending = schedule.scheduleOnce(stepper, AGENT_BEHAVIOUR_ORDERING);
    }

    /** Schedules this agent's next step after {@code delay}. */
    private void scheduleNextStep(final Duration delay) {
        checkState(!stepPending, "Agent %s already has a step pending.", this);
        stepPending = schedule.scheduleOnceIn(delay, stepper, AGENT_BEHAVIOUR_ORDERING);
    }

    /** @return whether the current behaviour is active */
    public boolean isActive() {
        return behaviour.isActive();
    }

    /**
     * Applies a change from outside the agent's behaviour, between runs of the behaviour. If the
     * behaviour is running, we queue the mutation, and it will be applied the next time the agent
     * is stepped and the behaviour is not running.
     * <p>
     * Otherwise, we apply the mutation immediately and then restore the invariant that, outside a
     * step, an active agent has a step pending: if the agent is active and has none, we schedule
     * one. This depends only on the agent's state after the mutation, not on what it was before, so
     * a sequence of mutations that briefly activates and deactivates the agent (as a fleet event's
     * setters can) never leaves it with more than one step pending.
     */
    protected void mutate(final Runnable mutation) {
        if (behaviour.isRunning()) {
            mutationQueue.add(mutation);
        } else {
            mutation.run();
            if (isActive() && !stepPending) {
                scheduleNextStep();
            }
        }
    }

    /** Replaces the current behaviour, via {@link #mutate}. */
    public void setBehaviour(final Behaviour behaviour) {
        mutate(() -> this.behaviour = behaviour);
    }
}
