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

package uk.ac.ox.poseidon.io;

import com.google.common.collect.ImmutableList;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Node;
import uk.ac.ox.poseidon.core.ScenarioNode;
import uk.ac.ox.poseidon.core.utils.Builders;

/**
 * A SnakeYAML {@link Constructor} that builds objects through their Lombok
 * builders rather than through a no-argument constructor and setters, so that
 * it can load immutable classes.
 * <p>
 * Every class whose name starts with one of the given prefixes must have a
 * static {@code builder()} method; loading one that does not fails, naming
 * the class, instead of falling back on setters. Other classes (JDK
 * collections and values) are built as SnakeYAML builds them by default. A
 * class is registered the first time the loader meets it: through its tag, or
 * as the declared type of a property, for a value SnakeYAML wrote without a
 * tag because its class is the property's type.
 */
class BuilderConstructor extends Constructor {

  private final ImmutableList<String> builtClassPrefixes;
  private final PropertyUtils readOnlyProperties = new PropertyUtils();

  /**
   * @param rootType           the class of the document's root
   * @param builtClassPrefixes the prefixes of the classes to build through
   *                           their builders
   * @param loaderOptions      the options of the loader
   */
  BuilderConstructor(
    final Class<?> rootType,
    final Collection<String> builtClassPrefixes,
    final LoaderOptions loaderOptions
  ) {
    super(rootType, loaderOptions);
    this.builtClassPrefixes = ImmutableList.copyOf(builtClassPrefixes);
    readOnlyProperties.setAllowReadOnlyProperties(true);
    register(rootType);
  }

  @Override
  protected Class<?> getClassForName(final String name)
    throws ClassNotFoundException {
    final Class<?> clazz = super.getClassForName(name);
    register(clazz);
    return clazz;
  }

  private void register(final Class<?> clazz) {
    if (
      // The superclass registers a plain description for the root type
      !(typeDefinitions.get(clazz) instanceof BuilderTypeDescription) &&
      builtClassPrefixes.stream().anyMatch(clazz.getName()::startsWith)
    ) {
      if (Modifier.isAbstract(clazz.getModifiers())) {
        // Only a concrete class appears in a tag; an abstract one is only
        // ever a property's declared type, whose values carry their tags.
        return;
      }
      if (findBuilderMethod(clazz) == null) {
        throw new YAMLException(
          "Class " + clazz.getName() + " has no static builder() method"
        );
      }
      addTypeDescription(new BuilderTypeDescription(clazz));
    }
  }

  private static Method findBuilderMethod(final Class<?> clazz) {
    try {
      final Method method = clazz.getMethod("builder");
      return Modifier.isStatic(method.getModifiers()) ? method : null;
    } catch (final NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Describes a class built through its builder: SnakeYAML gets a builder as
   * its instance, sets each property through the builder's method of that
   * name, and gets the built object at the end.
   */
  private class BuilderTypeDescription extends TypeDescription {

    BuilderTypeDescription(final Class<?> clazz) {
      super(clazz);
    }

    @Override
    public Object newInstance(final Node node) {
      try {
        return findBuilderMethod(getType()).invoke(null);
      } catch (final IllegalAccessException | InvocationTargetException e) {
        throw new YAMLException(
          "Cannot make a builder for " + getType().getName(),
          e
        );
      }
    }

    /**
     * SnakeYAML checks that a property is writable before setting it, and
     * takes collection element types from it, so each property is the
     * class's read-only one, made writable through the builder.
     */
    @Override
    public Property getProperty(final String name) {
      final Property readable = readOnlyProperties.getProperty(getType(), name);
      register(readable.getType());
      // The elements of a collection are written without a tag too when their
      // class is the collection's element type; a scenario node among them is
      // built through its builder, and a plain value as SnakeYAML builds it
      final Class<?>[] typeArguments = readable.getActualTypeArguments();
      if (
        Collection.class.isAssignableFrom(readable.getType()) &&
        typeArguments != null
      ) {
        Arrays.stream(typeArguments)
          .filter(ScenarioNode.class::isAssignableFrom)
          .forEach(BuilderConstructor.this::register);
      }
      return new Property(name, readable.getType()) {
        @Override
        public Class<?>[] getActualTypeArguments() {
          return readable.getActualTypeArguments();
        }

        @Override
        public void set(final Object builder, final Object value) {
          Builders.invoke(builder, name, value);
        }

        @Override
        public Object get(final Object object) {
          return readable.get(object);
        }

        @Override
        public List<Annotation> getAnnotations() {
          return readable.getAnnotations();
        }

        @Override
        public <A extends Annotation> A getAnnotation(
          final Class<A> annotationType
        ) {
          return readable.getAnnotation(annotationType);
        }
      };
    }

    @Override
    public Object finalizeConstruction(final Object builder) {
      return Builders.invoke(builder, "build");
    }
  }
}
