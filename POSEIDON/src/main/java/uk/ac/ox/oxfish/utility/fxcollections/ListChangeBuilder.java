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

package uk.ac.ox.oxfish.utility.fxcollections;/*
 * Copyright (c) 2011, 2016, Oracle and/or its affiliates. All rights reserved.
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
import java.util.Map.Entry;

@SuppressWarnings({"unchecked", "rawtypes"})
final class ListChangeBuilder<E> {

    private static final int[] EMPTY_PERM = new int[0];
    private final ObservableListBase<E> list;
    private int changeLock;
    private List<SubChange<E>> addRemoveChanges;
    private List<SubChange<E>> updateChanges;
    private SubChange<E> permutationChange;

    ListChangeBuilder(final ObservableListBase<E> list) {
        this.list = list;
    }

    public void nextPermutation(final int from, final int to, final int[] perm) {
        checkState();

        int prePermFrom = from;
        int prePermTo = to;
        int[] prePerm = perm;

        if ((addRemoveChanges != null && !addRemoveChanges.isEmpty())) {
            //Because there were already some changes to the list, we need
            // to "reconstruct" the original list and create a permutation
            // as-if there were no changes to the list. We can then
            // merge this with the permutation we already did

            // This maps elements from current list to the original list.
            // -1 means the map was not in the original list.
            // Note that for performance reasons, the map is permutated when created
            // by the permutation. So it basically contains the order in which the original
            // items were permutated by our new permutation.
            final int[] mapToOriginal = new int[list.size()];
            // Marks the original-list indexes that were removed
            final Set<Integer> removed = new TreeSet<Integer>();
            int last = 0;
            int offset = 0;
            for (int i = 0, sz = addRemoveChanges.size(); i < sz; ++i) {
                final SubChange<E> change = addRemoveChanges.get(i);
                for (int j = last; j < change.from; ++j) {
                    mapToOriginal[j < from || j >= to ? j : perm[j - from]] = j + offset;
                }
                for (int j = change.from; j < change.to; ++j) {
                    mapToOriginal[j < from || j >= to ? j : perm[j - from]] = -1;
                }
                last = change.to;
                final int removedSize = (change.removed != null ? change.removed.size() : 0);
                for (int j = change.from + offset, upTo = change.from + offset + removedSize;
                     j < upTo; ++j) {
                    removed.add(j);
                }
                offset += removedSize - (change.to - change.from);

            }
            // from the last add/remove change to the end of the list
            for (int i = last; i < mapToOriginal.length; ++i) {
                mapToOriginal[i < from || i >= to ? i : perm[i - from]] = i + offset;
            }

            final int[] newPerm = new int[list.size() + offset];
            int mapPtr = 0;
            for (int i = 0; i < newPerm.length; ++i) {
                if (removed.contains(i)) {
                    newPerm[i] = i;
                } else {
                    while (mapToOriginal[mapPtr] == -1) {
                        mapPtr++;
                    }
                    newPerm[mapToOriginal[mapPtr++]] = i;
                }
            }

            // We could theoretically find the first and last items such that
            // newPerm[i] != i and trim the permutation, but it is not necessary
            prePermFrom = 0;
            prePermTo = newPerm.length;
            prePerm = newPerm;
        }


        if (permutationChange != null) {
            if (prePermFrom == permutationChange.from && prePermTo == permutationChange.to) {
                for (int i = 0; i < prePerm.length; ++i) {
                    permutationChange.perm[i] = prePerm[permutationChange.perm[i] - prePermFrom];
                }
            } else {
                final int newTo = Math.max(permutationChange.to, prePermTo);
                final int newFrom = Math.min(permutationChange.from, prePermFrom);
                final int[] newPerm = new int[newTo - newFrom];

                for (int i = newFrom; i < newTo; ++i) {
                    if (i < permutationChange.from || i >= permutationChange.to) {
                        newPerm[i - newFrom] = prePerm[i - prePermFrom];
                    } else {
                        final int p = permutationChange.perm[i - permutationChange.from];
                        if (p < prePermFrom || p >= prePermTo) {
                            newPerm[i - newFrom] = p;
                        } else {
                            newPerm[i - newFrom] = prePerm[p - prePermFrom];
                        }
                    }
                }

                permutationChange.from = newFrom;
                permutationChange.to = newTo;
                permutationChange.perm = newPerm;
            }
        } else {
            permutationChange = new SubChange<E>(prePermFrom, prePermTo, null, prePerm, false);
        }

        if ((addRemoveChanges != null && !addRemoveChanges.isEmpty())) {
            final Set<Integer> newAdded = new TreeSet<Integer>();
            final Map<Integer, List<E>> newRemoved = new HashMap<Integer, List<E>>();
            for (int i = 0, sz = addRemoveChanges.size(); i < sz; ++i) {
                final SubChange<E> change = addRemoveChanges.get(i);
                for (int cIndex = change.from; cIndex < change.to; ++cIndex) {
                    if (cIndex < from || cIndex >= to) {
                        newAdded.add(cIndex);
                    } else {
                        newAdded.add(perm[cIndex - from]);
                    }
                }
                if (change.removed != null) {
                    if (change.from < from || change.from >= to) {
                        newRemoved.put(change.from, change.removed);
                    } else {
                        newRemoved.put(perm[change.from - from], change.removed);
                    }
                }
            }
            addRemoveChanges.clear();
            SubChange<E> lastChange = null;
            for (final Integer i : newAdded) {
                if (lastChange == null || lastChange.to != i) {
                    lastChange = new SubChange<E>(i, i + 1, null, EMPTY_PERM, false);
                    addRemoveChanges.add(lastChange);
                } else {
                    lastChange.to = i + 1;
                }
                final List<E> removed = newRemoved.remove(i);
                if (removed != null) {
                    if (lastChange.removed != null) {
                        lastChange.removed.addAll(removed);
                    } else {
                        lastChange.removed = removed;
                    }
                }
            }

            for (final Entry<Integer, List<E>> e : newRemoved.entrySet()) {
                final Integer at = e.getKey();
                final int idx = findSubChange(at, addRemoveChanges);
                assert (idx < 0);
                addRemoveChanges.add(~idx, new SubChange<E>(at, at, e.getValue(), new int[0], false));
            }
        }

        if (updateChanges != null && !updateChanges.isEmpty()) {
            final Set<Integer> newUpdated = new TreeSet<Integer>();
            for (int i = 0, sz = updateChanges.size(); i < sz; ++i) {
                final SubChange<E> change = updateChanges.get(i);
                for (int cIndex = change.from; cIndex < change.to; ++cIndex) {
                    if (cIndex < from || cIndex >= to) {
                        newUpdated.add(cIndex);
                    } else {
                        newUpdated.add(perm[cIndex - from]);
                    }
                }
            }
            updateChanges.clear();
            SubChange<E> lastUpdateChange = null;
            for (final Integer i : newUpdated) {
                if (lastUpdateChange == null || lastUpdateChange.to != i) {
                    lastUpdateChange = new SubChange<E>(i, i + 1, null, EMPTY_PERM, true);
                    updateChanges.add(lastUpdateChange);
                } else {
                    lastUpdateChange.to = i + 1;
                }
            }
        }
    }

    private void checkState() {
        if (changeLock == 0) {
            throw new IllegalStateException("beginChange was not called on this builder");
        }
    }

    private int findSubChange(final int idx, final List<SubChange<E>> list) {
        int from = 0;
        int to = list.size() - 1;

        while (from <= to) {
            final int changeIdx = (from + to) / 2;
            final SubChange<E> change = list.get(changeIdx);

            if (idx >= change.to) {
                from = changeIdx + 1;
            } else if (idx < change.from) {
                to = changeIdx - 1;
            } else {
                return changeIdx;
            }
        }
        return ~from;
    }

    public void nextReplace(final int from, final int to, final List<? extends E> removed) {
        nextRemove(from, removed);
        nextAdd(from, to);
    }

    public void nextRemove(final int idx, final List<? extends E> removed) {
        checkState();

        for (int i = 0; i < removed.size(); ++i) {
            nextRemove(idx, removed.get(i));
        }
    }

    public void nextAdd(final int from, final int to) {
        checkState();
        checkAddRemoveList();
        final SubChange<E> last = addRemoveChanges.isEmpty() ? null :
            addRemoveChanges.get(addRemoveChanges.size() - 1);
        final int numberOfAdded = to - from;

        if (last != null && last.to == from) {
            last.to = to;
        } else if (last != null && from >= last.from && from < last.to) { // Adding to the middle
            last.to += numberOfAdded;
        } else {
            insertAdd(from, to);
        }

        if (updateChanges != null && !updateChanges.isEmpty()) {
            int uPos = findSubChange(from, updateChanges);
            if (uPos < 0) {
                uPos = ~uPos;
            } else {
                // We have to split the change into 2
                final SubChange<E> change = updateChanges.get(uPos);
                updateChanges.add(uPos + 1, new SubChange<E>(to, change.to + to - from, null, EMPTY_PERM, true));
                change.to = from;
                uPos += 2; // skip those 2 for the update
            }
            for (int i = uPos; i < updateChanges.size(); ++i) {
                updateChanges.get(i).from += numberOfAdded;
                updateChanges.get(i).to += numberOfAdded;
            }
        }

    }

    public void nextRemove(final int idx, final E removed) {
        checkState();
        checkAddRemoveList();

        final SubChange<E> last = addRemoveChanges.isEmpty() ? null
            : addRemoveChanges.get(addRemoveChanges.size() - 1);

        if (last != null && last.to == idx) {
            last.removed.add(removed);
        } else if (last != null && last.from == idx + 1) {
            last.from--;
            last.to--;
            last.removed.add(0, removed);
        } else {
            insertRemoved(idx, removed);
        }

        if (updateChanges != null && !updateChanges.isEmpty()) {
            int uPos = findSubChange(idx, updateChanges);
            if (uPos < 0) {
                uPos = ~uPos;
            } else {
                final SubChange<E> change = updateChanges.get(uPos);
                if (change.from == change.to - 1) {
                    updateChanges.remove(uPos);
                } else {
                    change.to--;
                    ++uPos; // Do the update from the next position
                }
            }
            for (int i = uPos; i < updateChanges.size(); ++i) {
                updateChanges.get(i).from--;
                updateChanges.get(i).to--;
            }
        }

    }

    private void checkAddRemoveList() {
        if (addRemoveChanges == null) {
            addRemoveChanges = new ArrayList<SubChange<E>>();
        }
    }

    private void insertAdd(final int from, final int to) {
        int idx = findSubChange(from, addRemoveChanges);
        final int numberOfAdded = to - from;

        if (idx < 0) { // Not found
            idx = ~idx;

            final SubChange<E> change;
            if (idx > 0 && (change = addRemoveChanges.get(idx - 1)).to == from) {
                change.to = to;
                --idx;
            } else {
                addRemoveChanges.add(idx, new SubChange<E>(from, to, new ArrayList<E>(), EMPTY_PERM, false));
            }
        } else {
            final SubChange<E> change = addRemoveChanges.get(idx);
            change.to += numberOfAdded;
        }

        for (int i = idx + 1; i < addRemoveChanges.size(); ++i) {
            final SubChange<E> change = addRemoveChanges.get(i);
            change.from += numberOfAdded;
            change.to += numberOfAdded;
        }
    }

    private void insertRemoved(final int pos, final E removed) {
        int idx = findSubChange(pos, addRemoveChanges);
        if (idx < 0) { // Not found
            idx = ~idx;
            SubChange<E> change;

            if (idx > 0 && (change = addRemoveChanges.get(idx - 1)).to == pos) {
                change.removed.add(removed);
                --idx; // Idx index will be used as a starting point for update
            } else if (idx < addRemoveChanges.size() && (change = addRemoveChanges.get(idx)).from == pos + 1) {
                change.from--;
                change.to--;
                change.removed.add(0, removed);
            } else {
                final ArrayList<E> removedList = new ArrayList<E>();
                removedList.add(removed);
                addRemoveChanges.add(idx, new SubChange<E>(pos, pos, removedList, EMPTY_PERM, false));
            }
        } else {
            final SubChange<E> change = addRemoveChanges.get(idx);
            change.to--; // Removed one element from the previously added list
            if (change.from == change.to && (change.removed == null || change.removed.isEmpty())) {
                addRemoveChanges.remove(idx);
            }
        }
        for (int i = idx + 1; i < addRemoveChanges.size(); ++i) {
            final SubChange<E> change = addRemoveChanges.get(i);
            change.from--;
            change.to--;
        }
    }

    public void nextSet(final int idx, final E old) {
        nextRemove(idx, old);
        nextAdd(idx, idx + 1);

    }

    public void nextUpdate(final int idx) {
        checkState();
        if (updateChanges == null) {
            updateChanges = new ArrayList<SubChange<E>>();
        }
        final SubChange<E> last = updateChanges.isEmpty() ? null : updateChanges.get(updateChanges.size() - 1);
        if (last != null && last.to == idx) {
            last.to = idx + 1;
        } else {
            insertUpdate(idx);
        }
    }

    private void insertUpdate(final int pos) {
        int idx = findSubChange(pos, updateChanges);
        if (idx < 0) { //If not found
            idx = ~idx;
            SubChange<E> change;
            if (idx > 0 && (change = updateChanges.get(idx - 1)).to == pos) {
                change.to = pos + 1;
            } else if (idx < updateChanges.size() && (change = updateChanges.get(idx)).from == pos + 1) {
                change.from = pos;
            } else {
                updateChanges.add(idx, new SubChange<E>(pos, pos + 1, null, EMPTY_PERM, true));
            }
        } // If found, no need to do another update
    }

    public void beginChange() {
        changeLock++;
    }

    public void endChange() {
        if (changeLock <= 0) {
            throw new IllegalStateException("Called endChange before beginChange");
        }
        changeLock--;
        commit();
    }

    private void commit() {
        final boolean addRemoveNotEmpty = addRemoveChanges != null && !addRemoveChanges.isEmpty();
        final boolean updateNotEmpty = updateChanges != null && !updateChanges.isEmpty();
        if (changeLock == 0
            && (addRemoveNotEmpty
            || updateNotEmpty
            || permutationChange != null)) {
            int totalSize = (updateChanges != null ? updateChanges.size() : 0) +
                (addRemoveChanges != null ? addRemoveChanges.size() : 0) + (permutationChange != null ? 1 : 0);
            if (totalSize == 1) {
                if (addRemoveNotEmpty) {
                    list.fireChange(new SingleChange<E>(finalizeSubChange(addRemoveChanges.get(0)), list));
                    addRemoveChanges.clear();
                } else if (updateNotEmpty) {
                    list.fireChange(new SingleChange<E>(finalizeSubChange(updateChanges.get(0)), list));
                    updateChanges.clear();
                } else {
                    list.fireChange(new SingleChange<E>(finalizeSubChange(permutationChange), list));
                    permutationChange = null;
                }
            } else {
                if (updateNotEmpty) {
                    final int removed = compress(updateChanges);
                    totalSize -= removed;
                }
                if (addRemoveNotEmpty) {
                    final int removed = compress(addRemoveChanges);
                    totalSize -= removed;
                }

                final SubChange<E>[] array = new SubChange[totalSize];
                int ptr = 0;
                if (permutationChange != null) {
                    array[ptr++] = permutationChange;
                }
                if (addRemoveNotEmpty) {
                    final int sz = addRemoveChanges.size();
                    for (int i = 0; i < sz; ++i) {
                        final SubChange<E> change = addRemoveChanges.get(i);
                        if (change != null) {
                            array[ptr++] = change;
                        }
                    }
                }
                if (updateNotEmpty) {
                    final int sz = updateChanges.size();
                    for (int i = 0; i < sz; ++i) {
                        final SubChange<E> change = updateChanges.get(i);
                        if (change != null) {
                            array[ptr++] = change;
                        }
                    }
                }
                list.fireChange(new IterableChange<E>(finalizeSubChangeArray(array), list));
                if (addRemoveChanges != null) addRemoveChanges.clear();
                if (updateChanges != null) updateChanges.clear();
                permutationChange = null;
            }
        }
    }

    private static <E> SubChange<E> finalizeSubChange(final SubChange<E> c) {
        if (c.perm == null) {
            c.perm = EMPTY_PERM;
        }
        if (c.removed == null) {
            c.removed = Collections.emptyList();
        } else {
            c.removed = Collections.unmodifiableList(c.removed);
        }
        return c;
    }

    private int compress(final List<SubChange<E>> list) {
        int removed = 0;

        SubChange<E> prev = list.get(0);
        for (int i = 1, sz = list.size(); i < sz; ++i) {
            final SubChange<E> cur = list.get(i);
            if (prev.to == cur.from) {
                prev.to = cur.to;
                if (prev.removed != null || cur.removed != null) {
                    if (prev.removed == null) {
                        prev.removed = new ArrayList<E>();
                    }
                    prev.removed.addAll(cur.removed);
                }
                list.set(i, null);
                ++removed;
            } else {
                prev = cur;
            }
        }
        return removed;

    }

    private static <E> SubChange<E>[] finalizeSubChangeArray(final SubChange<E>[] changes) {
        for (final SubChange<E> c : changes) {
            finalizeSubChange(c);
        }
        return changes;
    }

    private static class SubChange<E> {

        int from, to;
        List<E> removed;
        int[] perm;
        boolean updated;

        public SubChange(final int from, final int to, final List<E> removed, final int[] perm, final boolean updated) {
            this.from = from;
            this.to = to;
            this.removed = removed;
            this.perm = perm;
            this.updated = updated;
        }
    }

    private static class SingleChange<E> extends ListChangeListener.Change<E> {
        private final SubChange<E> change;
        private boolean onChange;

        public SingleChange(final SubChange<E> change, final ObservableListBase<E> list) {
            super(list);
            this.change = change;
        }

        @Override
        public boolean next() {
            if (onChange) {
                return false;
            }
            onChange = true;
            return true;
        }

        @Override
        public void reset() {
            onChange = false;
        }

        @Override
        public int getFrom() {
            checkState();
            return change.from;
        }

        private void checkState() {
            if (!onChange) {
                throw new IllegalStateException(
                    "Invalid Change state: next() must be called before inspecting the Change.");
            }
        }

        @Override
        public int getTo() {
            checkState();
            return change.to;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return change.removed;
        }

        @Override
        protected int[] getPermutation() {
            checkState();
            return change.perm;
        }

        @Override
        public boolean wasUpdated() {
            checkState();
            return change.updated;
        }

        @Override
        public String toString() {
            final String ret;
            if (change.perm.length != 0) {
                ret = ChangeHelper.permChangeToString(change.perm);
            } else if (change.updated) {
                ret = ChangeHelper.updateChangeToString(change.from, change.to);
            } else {
                ret = ChangeHelper.addRemoveChangeToString(change.from, change.to, getList(), change.removed);
            }
            return "{ " + ret + " }";
        }

    }


    private static class IterableChange<E> extends ListChangeListener.Change<E> {

        private final SubChange[] changes;
        private int cursor = -1;

        private IterableChange(final SubChange[] changes, final ObservableList<E> list) {
            super(list);
            this.changes = changes;
        }

        @Override
        public boolean next() {
            if (cursor + 1 < changes.length) {
                ++cursor;
                return true;
            }
            return false;
        }

        @Override
        public void reset() {
            cursor = -1;
        }

        @Override
        public int getFrom() {
            checkState();
            return changes[cursor].from;
        }

        private void checkState() {
            if (cursor == -1) {
                throw new IllegalStateException(
                    "Invalid Change state: next() must be called before inspecting the Change.");
            }
        }

        @Override
        public int getTo() {
            checkState();
            return changes[cursor].to;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return changes[cursor].removed;
        }

        @Override
        protected int[] getPermutation() {
            checkState();
            return changes[cursor].perm;
        }

        @Override
        public boolean wasUpdated() {
            checkState();
            return changes[cursor].updated;
        }

        @Override
        public String toString() {
            int c = 0;
            final StringBuilder b = new StringBuilder();
            b.append("{ ");
            while (c < changes.length) {
                if (changes[c].perm.length != 0) {
                    b.append(ChangeHelper.permChangeToString(changes[c].perm));
                } else if (changes[c].updated) {
                    b.append(ChangeHelper.updateChangeToString(changes[c].from, changes[c].to));
                } else {
                    b.append(ChangeHelper.addRemoveChangeToString(
                        changes[c].from,
                        changes[c].to,
                        getList(),
                        changes[c].removed
                    ));
                }
                if (c != changes.length - 1) {
                    b.append(", ");
                }
                ++c;
            }
            b.append(" }");
            return b.toString();
        }

    }
}
