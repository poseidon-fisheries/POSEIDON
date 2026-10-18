/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.core;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The products built for one owner (a factory, for its global product; a
 * simulation; or something a simulation holds, such as a vessel or one of its
 * components), keyed by the identity of the factory that built them: two
 * factory objects never share a product here, however equal they are. A store
 * ends with its owner, and an ended store refuses every later use. Ending it
 * first runs the teardowns of the registrations made for its products, last
 * made first.
 *
 * <p>A store is used by one thread at a time: the simulation that owns it, or
 * that holds its owner, is built and stepped by one thread. A factory's own
 * store, which holds its product when it is global, is locked by that factory.
 */
public final class ProductStore {

  private final Map<Factory<?, ?>, Object> products = new IdentityHashMap<>();
  private final Deque<Runnable> teardowns = new ArrayDeque<>();
  private boolean ended;

  /**
   * @param factory the factory whose product to look for
   * @return whether {@code factory}'s product is in this store
   * @throws IllegalStateException if this store has ended
   */
  boolean contains(final Factory<?, ?> factory) {
    checkNotEnded();
    return products.containsKey(factory);
  }

  /**
   * @param factory the factory whose product to return
   * @return {@code factory}'s product, which only {@code factory} can have put
   *         here
   * @throws IllegalStateException if this store has ended
   */
  @SuppressWarnings("unchecked")
  <P> P get(final Factory<?, P> factory) {
    checkNotEnded();
    return (P) products.get(factory);
  }

  /**
   * @param factory the factory that built {@code product}
   * @param product what {@code factory} built for this store's owner
   * @throws IllegalStateException if this store has ended
   */
  <P> void put(final Factory<?, P> factory, final P product) {
    checkNotEnded();
    products.put(factory, product);
  }

  /**
   * Public, since the scopes that make registrations are not all in this
   * package; a build never reaches a store.
   *
   * @param teardown takes back a registration made for one of this store's
   *                 products, when this store ends
   * @throws IllegalStateException if this store has ended
   */
  public void addTeardown(final Runnable teardown) {
    checkNotEnded();
    teardowns.push(teardown);
  }

  /**
   * Runs this store's teardowns, the last added first, then drops its
   * products. Every later use of this store throws.
   *
   * @throws IllegalStateException if this store has already ended
   */
  public void end() {
    checkNotEnded();
    while (!teardowns.isEmpty()) {
      teardowns.pop().run();
    }
    products.clear();
    ended = true;
  }

  private void checkNotEnded() {
    checkState(!ended, "This product store has ended.");
  }
}
