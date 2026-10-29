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

import static com.google.common.base.Preconditions.checkArgument;

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
 * objects never share a product, however equal their values. A factory's
 * level says which owner it builds for: declared by a level base class
 * ({@link GlobalScopeFactory}, {@link SimulationScopeFactory} and the vessel
 * one), or derived from its inputs by {@link RelativeScopeFactory}.
 * Subclasses supply the actual construction logic (via {@link #newInstance}).
 * Extend a level base class or {@link RelativeScopeFactory} rather than
 * implementing {@link Factory} directly unless you have a specific reason not
 * to.
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
   * Returns the product held for {@code scope}'s owner at this factory's
   * level, building it with {@link #newInstance} and storing it if it isn't
   * there. The scope is first widened to this factory's level: a scope of
   * exactly that class is constructed over the same owners, so that the build
   * receives nothing a narrower scope offers, and its registrations go to the
   * store that holds its product. The product of a global factory is kept in
   * this factory's own store, shared by every simulation reaching this
   * factory, possibly at once, so its build is locked by this factory.
   *
   * @param scope the scope to resolve against, must not be null
   * @return the stored or newly-built object
   * @throws IllegalArgumentException if {@code scope} is wider than this
   *                                  factory's level
   */
  @Override
  public final P get(@NonNull final S scope) {
    final Class<? extends Scope> level = levelFor(scope);
    checkArgument(
      level.isInstance(scope),
      "%s is at the level of %s, but was asked for its product with a " +
        "wider %s",
      getClass().getName(),
      level.getSimpleName(),
      scope.getClass().getSimpleName()
    );
    @SuppressWarnings("unchecked")
    final S widened = (S) scope.widenedTo(level);
    final P product;
    if (level == Scope.class) {
      synchronized (this) {
        product = getFrom(globalStore, widened);
      }
    } else {
      product = getFrom(((SimulationScope) widened).getProductStore(), widened);
    }
    return product;
  }

  // Looked up, built and then stored, not filled in one call: the build asks
  // for its inputs, which may be stored in the same store.
  private P getFrom(final ProductStore store, final S scope) {
    if (!store.contains(this)) {
      store.put(this, newInstance(scope));
    }
    return store.get(this);
  }

  /**
   * @param scope the scope a product is asked for with
   * @return this factory's level, the class of the scope its build receives:
   *         its declared level, unless it derives one
   */
  Class<? extends Scope> levelFor(final Scope scope) {
    return scopeClass();
  }

  /**
   * @param scope the scope being resolved against, of exactly this factory's
   *              level
   * @return a freshly built {@code P}, called at most once per store
   */
  protected abstract P newInstance(S scope);

  /**
   * @return the level this factory declares: the concrete {@link Scope}
   *         subclass it resolves against
   */
  protected abstract Class<S> scopeClass();
}
