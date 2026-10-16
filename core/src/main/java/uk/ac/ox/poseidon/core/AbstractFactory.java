/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * A {@link Factory} that builds its product once per owner: the same factory
 * object, asked for its product with scopes of the same owner (a simulation, a
 * vessel), always returns the same {@code P} instance. Products are kept in
 * {@link ProductStore}s keyed by factory identity, so sharing is declared by
 * referencing one factory object from several places: two distinct factory
 * objects never share a product, however equal their values. Subclasses say
 * which store holds the product (via {@link #productStoreFor}, usually
 * inherited from a {@code *ScopeFactory} base rather than overridden directly)
 * and supply the actual construction logic (via {@link #newInstance}). Extend
 * this rather than implementing {@link Factory} directly unless you have a
 * specific reason not to.
 *
 * @param <S> the scope this factory resolves against
 * @param <P> the type of the product
 */
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode()
public abstract class AbstractFactory<
  S extends Scope,
  P
> implements Factory<S, P> {

  // Transient, so that it is not a property: SnakeYAML doesn't write it,
  // equality ignores it, and, being final and initialised, it is not a
  // builder parameter, so a copy made through toBuilder() starts empty.
  private final transient ProductStore globalStore = new ProductStore();

  /**
   * Returns the product held for {@code scope}'s owner in the store that
   * {@link #productStoreFor} gives, building it with {@link #newInstance}
   * and storing it if it isn't there. A product kept in this factory's own
   * store is shared by every simulation reaching this factory, possibly at
   * once, so its build is locked by this factory.
   *
   * @param scope the scope to resolve against, must not be null
   * @return the stored or newly-built object
   */
  @Override
  public final P get(@NonNull final S scope) {
    final ProductStore store = productStoreFor(scope);
    if (store == globalStore) {
      synchronized (this) {
        return getFrom(store, scope);
      }
    }
    return getFrom(store, scope);
  }

  // Looked up, built and then stored, not filled in one call: the build asks
  // for its inputs, which may be stored in the same store.
  private P getFrom(final ProductStore store, final S scope) {
    if (store.contains(this)) {
      return store.get(this);
    }
    final P product = newInstance(scopeForBuild(scope));
    store.put(this, product);
    return product;
  }

  /**
   * @param scope the scope being resolved against
   * @return the store holding this factory's product for {@code scope}: its
   *         owner's (the simulation's, for a simulation-level factory), or
   *         {@link #globalStore()} for a global factory
   */
  protected abstract ProductStore productStoreFor(S scope);

  /**
   * @return this factory's own store, which holds its product when it is
   *         shared by every simulation; a copy of this factory starts with an
   *         empty one
   */
  protected final ProductStore globalStore() {
    return globalStore;
  }

  /**
   * @param scope the scope a product is asked for with
   * @return the scope that the build receives: {@code scope} itself, unless a
   *         level base gives its builds a scope of exactly its level
   */
  protected S scopeForBuild(final S scope) {
    return scope;
  }

  /**
   * @param scope the scope being resolved against
   * @return a freshly built {@code P}, called at most once per store
   */
  protected abstract P newInstance(S scope);

  /**
   * @return the concrete {@link Scope} subclass this factory resolves against
   */
  protected abstract Class<S> scopeClass();
}
