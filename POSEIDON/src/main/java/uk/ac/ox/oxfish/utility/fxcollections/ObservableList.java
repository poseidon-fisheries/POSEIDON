

/*
 * Copyright (c) 2010, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package uk.ac.ox.oxfish.utility.fxcollections;/*

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;


/**
 * A list that allows listeners to track changes when they occur.
 *
 * @see ListChangeListener
 * @see ListChangeListener.Change
 * @param <E> the list element type
 * @since JavaFX 2.0
 */

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("overloads")
public interface ObservableList<E> extends List<E>, Observable {
    static <E> ObservableList<E> observableList(final List<E> list) {
        checkNotNull(list);
        if (list instanceof ArrayList)
            return new ObservableListWrapper<E>(list);
        else {
            //this ideally should be replaced by a constructor call that isn't stupid
            // but the new mockito seem to throw nullpointers when I do!
            final ArrayList<E> backer = new ArrayList<>();
            for (final E e : list) {
                backer.add(e);
            }
            return new ObservableListWrapper<E>(backer);
        }

    }

    @SuppressWarnings("unchecked")
    static <E> ObservableList<E> observableList(final E... items) {
        return new ObservableListWrapper<>(Lists.newArrayList(items));
    }

    /**
     * Add a listener to this observable list.
     *
     * @param listener the listener for listening to the list changes
     */
    void addListener(ListChangeListener<? super E> listener);

    /**
     * Tries to remove a listener from this observable list. If the listener is not
     * attached to this list, nothing happens.
     *
     * @param listener a listener to remove
     */
    void removeListener(ListChangeListener<? super E> listener);

    /**
     * A convenience method for var-arg addition of elements.
     *
     * @param elements the elements to add
     * @return true (as specified by Collection.add(E))
     */
    @SuppressWarnings("unchecked")
    boolean addAll(E... elements);

    /**
     * Clears the ObservableList and adds all the elements passed as var-args.
     *
     * @param elements the elements to set
     * @return true (as specified by Collection.add(E))
     * @throws NullPointerException if the specified arguments contain one or more null elements
     */
    @SuppressWarnings("unchecked")
    boolean setAll(E... elements);

    /**
     * Clears the ObservableList and adds all elements from the collection.
     *
     * @param col the collection with elements that will be added to this observableArrayList
     * @return true (as specified by Collection.add(E))
     * @throws NullPointerException if the specified collection contains one or more null elements
     */
    boolean setAll(Collection<? extends E> col);

    /**
     * A convenience method for var-arg usage of the {@link #removeAll(Collection) removeAll} method.
     *
     * @param elements the elements to be removed
     * @return true if list changed as a result of this call
     */
    @SuppressWarnings("unchecked")
    boolean removeAll(E... elements);

    /**
     * A convenience method for var-arg usage of the {@link #retainAll(Collection) retainAll} method.
     *
     * @param elements the elements to be retained
     * @return true if list changed as a result of this call
     */
    @SuppressWarnings("unchecked")
    boolean retainAll(E... elements);

    /**
     * A simplified way of calling {@code sublist(from, to).clear()}. As this is a common operation,
     * ObservableList has this method for convenient usage.
     *
     * @param from the start of the range to remove (inclusive)
     * @param to   the end of the range to remove (exclusive)
     * @throws IndexOutOfBoundsException if an illegal range is provided
     */
    void remove(int from, int to);


}