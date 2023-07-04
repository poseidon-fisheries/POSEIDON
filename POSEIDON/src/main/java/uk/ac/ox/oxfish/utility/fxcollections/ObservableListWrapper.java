package uk.ac.ox.oxfish.utility.fxcollections;/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
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


import java.util.*;


/**
 * A List wrapper class that implements observability.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ObservableListWrapper<E> extends ModifiableObservableListBase<E> implements
    ObservableList<E>, RandomAccess {

    private final List<E> backingList;

    private final ElementObserver elementObserver;
    private SortHelper helper;

    public ObservableListWrapper(final List<E> list) {
        backingList = list;
        elementObserver = null;
    }


    public ObservableListWrapper(final List<E> list, final Callback<E, Observable[]> extractor) {
        backingList = list;
        this.elementObserver = new ElementObserver(extractor, (Callback<E, InvalidationListener>) e -> observable -> {
            beginChange();
            int i = 0;
            final int size = size();
            for (; i < size; ++i) {
                if (get(i) == e) {
                    nextUpdate(i);
                }
            }
            endChange();
        }, this);
        final int sz = backingList.size();
        for (int i = 0; i < sz; ++i) {
            elementObserver.attachListener(backingList.get(i));
        }
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public E get(final int index) {
        return backingList.get(index);
    }

    @Override
    protected void doAdd(final int index, final E element) {
        if (elementObserver != null)
            elementObserver.attachListener(element);
        backingList.add(index, element);
    }

    @Override
    protected E doSet(final int index, final E element) {
        final E removed = backingList.set(index, element);
        if (elementObserver != null) {
            elementObserver.detachListener(removed);
            elementObserver.attachListener(element);
        }
        return removed;
    }

    @Override
    protected E doRemove(final int index) {
        final E removed = backingList.remove(index);
        if (elementObserver != null)
            elementObserver.detachListener(removed);
        return removed;
    }

    @Override
    public int indexOf(final Object o) {
        return backingList.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return backingList.lastIndexOf(o);
    }

    @Override
    public boolean contains(final Object o) {
        return backingList.contains(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return backingList.containsAll(c);
    }

    @Override
    public void clear() {
        if (elementObserver != null) {
            final int sz = size();
            for (int i = 0; i < sz; ++i) {
                elementObserver.detachListener(get(i));
            }
        }
        if (hasListeners()) {
            beginChange();
            nextRemove(0, this);
        }
        backingList.clear();
        ++modCount;
        if (hasListeners()) {
            endChange();
        }
    }

    @Override
    public void remove(final int fromIndex, final int toIndex) {
        beginChange();
        for (int i = fromIndex; i < toIndex; ++i) {
            remove(fromIndex);
        }
        endChange();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        beginChange();
        final BitSet bs = new BitSet(c.size());
        for (int i = 0; i < size(); ++i) {
            if (c.contains(get(i))) {
                bs.set(i);
            }
        }
        if (!bs.isEmpty()) {
            int cur = size();
            while ((cur = bs.previousSetBit(cur - 1)) >= 0) {
                remove(cur);
            }
        }
        endChange();
        return !bs.isEmpty();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        beginChange();
        final BitSet bs = new BitSet(c.size());
        for (int i = 0; i < size(); ++i) {
            if (!c.contains(get(i))) {
                bs.set(i);
            }
        }
        if (!bs.isEmpty()) {
            int cur = size();
            while ((cur = bs.previousSetBit(cur - 1)) >= 0) {
                remove(cur);
            }
        }
        endChange();
        return !bs.isEmpty();
    }

    @Override
    public void sort(final Comparator<? super E> comparator) {
        if (backingList.isEmpty()) {
            return;
        }
        final int[] perm = getSortHelper().sort(backingList, comparator);
        fireChange(new NonIterableChange.SimplePermutationChange<E>(0, size(), perm, this));
    }

    private SortHelper getSortHelper() {
        if (helper == null) {
            helper = new SortHelper();
        }
        return helper;
    }

}