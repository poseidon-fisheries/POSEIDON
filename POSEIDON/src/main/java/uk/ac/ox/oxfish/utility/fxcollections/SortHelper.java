package uk.ac.ox.oxfish.utility.fxcollections;/*
 * Copyright (c) 2010, 2016, Oracle and/or its affiliates. All rights reserved.
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


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * Helper class that contains algorithms taken from JDK that additionally
 * tracks the permutation that's created thorough the process
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class SortHelper {
    private static final int INSERTIONSORT_THRESHOLD = 7;
    private int[] permutation;
    private int[] reversePermutation;

    private static void rangeCheck(final int arrayLen, final int fromIndex, final int toIndex) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                ") > toIndex(" + toIndex + ")");
        if (fromIndex < 0)
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex > arrayLen)
            throw new ArrayIndexOutOfBoundsException(toIndex);
    }

    private static int[] copyOfRange(final int[] original, final int from, final int to) {
        final int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        final int[] copy = new int[newLength];
        System.arraycopy(original, from, copy, 0,
            Math.min(original.length - from, newLength)
        );
        return copy;
    }

    private static <T> T[] copyOfRange(final T[] original, final int from, final int to) {
        return copyOfRange(original, from, to, (Class<T[]>) original.getClass());
    }

    private static <T, U> T[] copyOfRange(
        final U[] original,
        final int from,
        final int to,
        final Class<? extends T[]> newType
    ) {
        final int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        @SuppressWarnings("RedundantCast") final T[] copy = ((Object) newType == (Object) Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, from, copy, 0,
            Math.min(original.length - from, newLength)
        );
        return copy;
    }

    public <T extends Comparable<? super T>> int[] sort(final List<T> list) {
        T[] a = (T[]) Array.newInstance(Comparable.class, list.size());
        try {
            a = list.toArray(a);
        } catch (final ArrayStoreException e) {
            // this means this is not comparable (used without generics)
            throw new ClassCastException();
        }
        final int[] result = sort(a);
        final ListIterator<T> i = list.listIterator();
        for (int j = 0; j < a.length; j++) {
            i.next();
            i.set(a[j]);
        }
        return result;
    }

    public <T> int[] sort(final List<T> list, final Comparator<? super T> c) {
        final Object[] a = list.toArray();
        final int[] result = sort(a, (Comparator) c);
        final ListIterator i = list.listIterator();
        for (int j = 0; j < a.length; j++) {
            i.next();
            i.set(a[j]);
        }
        return result;
    }

    public <T extends Comparable<? super T>> int[] sort(final T[] a) {
        return sort(a, null);
    }

    public <T> int[] sort(final T[] a, final Comparator<? super T> c) {
        final T[] aux = a.clone();
        final int[] result = initPermutation(a.length);
        if (c == null)
            mergeSort(aux, a, 0, a.length, 0);
        else
            mergeSort(aux, a, 0, a.length, 0, c);
        reversePermutation = null;
        permutation = null;
        return result;
    }

    public <T> int[] sort(
        final T[] a, final int fromIndex, final int toIndex,
        final Comparator<? super T> c
    ) {
        rangeCheck(a.length, fromIndex, toIndex);
        final T[] aux = copyOfRange(a, fromIndex, toIndex);
        final int[] result = initPermutation(a.length);
        if (c == null)
            mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
        else
            mergeSort(aux, a, fromIndex, toIndex, -fromIndex, c);
        reversePermutation = null;
        permutation = null;
        return Arrays.copyOfRange(result, fromIndex, toIndex);
    }

    public int[] sort(final int[] a, final int fromIndex, final int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        final int[] aux = copyOfRange(a, fromIndex, toIndex);
        final int[] result = initPermutation(a.length);
        mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
        reversePermutation = null;
        permutation = null;
        return Arrays.copyOfRange(result, fromIndex, toIndex);
    }

    /**
     * Merge sort from Oracle JDK 6
     */
    private void mergeSort(
        final int[] src,
        final int[] dest,
        int low,
        int high,
        final int off
    ) {
        final int length = high - low;

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low &&
                    ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // Recursively sort halves of dest into src
        final int destLow = low;
        final int destHigh = high;
        low += off;
        high += off;
        final int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && ((Comparable) src[p]).compareTo(src[q]) <= 0) {
                dest[i] = src[p];
                permutation[reversePermutation[p++]] = i;
            } else {
                dest[i] = src[q];
                permutation[reversePermutation[q++]] = i;
            }
        }

        for (int i = destLow; i < destHigh; ++i) {
            reversePermutation[permutation[i]] = i;
        }
    }

    /**
     * Merge sort from Oracle JDK 6
     */
    private void mergeSort(
        final Object[] src,
        final Object[] dest,
        int low,
        int high,
        final int off
    ) {
        final int length = high - low;

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low &&
                    ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // Recursively sort halves of dest into src
        final int destLow = low;
        final int destHigh = high;
        low += off;
        high += off;
        final int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && ((Comparable) src[p]).compareTo(src[q]) <= 0) {
                dest[i] = src[p];
                permutation[reversePermutation[p++]] = i;
            } else {
                dest[i] = src[q];
                permutation[reversePermutation[q++]] = i;
            }
        }

        for (int i = destLow; i < destHigh; ++i) {
            reversePermutation[permutation[i]] = i;
        }
    }

    private void mergeSort(
        final Object[] src,
        final Object[] dest,
        int low, int high, final int off,
        final Comparator c
    ) {
        final int length = high - low;

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // Recursively sort halves of dest into src
        final int destLow = low;
        final int destHigh = high;
        low += off;
        high += off;
        final int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off, c);
        mergeSort(dest, src, mid, high, -off, c);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0) {
                dest[i] = src[p];
                permutation[reversePermutation[p++]] = i;
            } else {
                dest[i] = src[q];
                permutation[reversePermutation[q++]] = i;
            }
        }

        for (int i = destLow; i < destHigh; ++i) {
            reversePermutation[permutation[i]] = i;
        }
    }

    private void swap(final int[] x, final int a, final int b) {
        final int t = x[a];
        x[a] = x[b];
        x[b] = t;
        permutation[reversePermutation[a]] = b;
        permutation[reversePermutation[b]] = a;
        final int tp = reversePermutation[a];
        reversePermutation[a] = reversePermutation[b];
        reversePermutation[b] = tp;
    }

    private void swap(final Object[] x, final int a, final int b) {
        final Object t = x[a];
        x[a] = x[b];
        x[b] = t;
        permutation[reversePermutation[a]] = b;
        permutation[reversePermutation[b]] = a;
        final int tp = reversePermutation[a];
        reversePermutation[a] = reversePermutation[b];
        reversePermutation[b] = tp;
    }

    private int[] initPermutation(final int length) {
        permutation = new int[length];
        reversePermutation = new int[length];
        for (int i = 0; i < length; ++i) {
            permutation[i] = reversePermutation[i] = i;
        }
        return permutation;
    }
}