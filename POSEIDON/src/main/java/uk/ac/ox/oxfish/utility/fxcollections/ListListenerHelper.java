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
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
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


import java.util.Arrays;
import java.util.function.Predicate;

/**
 *
 */
@SuppressWarnings({"overloads", "unchecked", "rawtypes"})
public abstract class ListListenerHelper<E> {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static methods

    public static <E> ListListenerHelper<E> addListener(final ListListenerHelper<E> helper, final InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        return (helper == null) ? new SingleInvalidation<E>(listener) : helper.addListener(listener);
    }

    protected abstract ListListenerHelper<E> addListener(InvalidationListener listener);

    public static <E> ListListenerHelper<E> removeListener(
        final ListListenerHelper<E> helper,
        final InvalidationListener listener
    ) {
        if (listener == null) {
            throw new NullPointerException();
        }
        return (helper == null) ? null : helper.removeListener(listener);
    }

    protected abstract ListListenerHelper<E> removeListener(InvalidationListener listener);

    public static <E> ListListenerHelper<E> addListener(
        final ListListenerHelper<E> helper,
        final ListChangeListener<? super E> listener
    ) {
        if (listener == null) {
            throw new NullPointerException();
        }
        return (helper == null) ? new SingleChange<E>(listener) : helper.addListener(listener);
    }

    protected abstract ListListenerHelper<E> addListener(ListChangeListener<? super E> listener);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Common implementations

    public static <E> ListListenerHelper<E> removeListener(
        final ListListenerHelper<E> helper,
        final ListChangeListener<? super E> listener
    ) {
        if (listener == null) {
            throw new NullPointerException();
        }
        return (helper == null) ? null : helper.removeListener(listener);
    }

    protected abstract ListListenerHelper<E> removeListener(ListChangeListener<? super E> listener);

    public static <E> void fireValueChangedEvent(
        final ListListenerHelper<E> helper,
        final ListChangeListener.Change<? extends E> change
    ) {
        if (helper != null) {
            change.reset();
            helper.fireValueChangedEvent(change);
        }
    }

    protected abstract void fireValueChangedEvent(ListChangeListener.Change<? extends E> change);

    public static <E> boolean hasListeners(final ListListenerHelper<E> helper) {
        return helper != null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Implementations

    protected static int trim(int size, final Object[] listeners) {
        final Predicate<Object> p = t -> false; // removed weaklistening
        int index = 0;
        for (; index < size; index++) {
            if (p.test(listeners[index])) {
                break;
            }
        }
        if (index < size) {
            for (int src = index + 1; src < size; src++) {
                if (!p.test(listeners[src])) {
                    listeners[index++] = listeners[src];
                }
            }
            final int oldSize = size;
            size = index;
            for (; index < oldSize; index++) {
                listeners[index] = null;
            }
        }

        return size;
    }

    private static class SingleInvalidation<E> extends ListListenerHelper<E> {

        private final InvalidationListener listener;

        private SingleInvalidation(final InvalidationListener listener) {
            this.listener = listener;
        }

        @Override
        protected ListListenerHelper<E> addListener(final InvalidationListener listener) {
            return new Generic<E>(this.listener, listener);
        }

        @Override
        protected ListListenerHelper<E> removeListener(final InvalidationListener listener) {
            return (listener.equals(this.listener)) ? null : this;
        }

        @Override
        protected ListListenerHelper<E> addListener(final ListChangeListener<? super E> listener) {
            return new Generic<E>(this.listener, listener);
        }

        @Override
        protected ListListenerHelper<E> removeListener(final ListChangeListener<? super E> listener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent(final ListChangeListener.Change<? extends E> change) {
            try {
                listener.invalidated(change.getList());
            } catch (final Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class SingleChange<E> extends ListListenerHelper<E> {

        private final ListChangeListener<? super E> listener;

        private SingleChange(final ListChangeListener<? super E> listener) {
            this.listener = listener;
        }

        @Override
        protected ListListenerHelper<E> addListener(final InvalidationListener listener) {
            return new Generic<E>(listener, this.listener);
        }

        @Override
        protected ListListenerHelper<E> removeListener(final InvalidationListener listener) {
            return this;
        }

        @Override
        protected ListListenerHelper<E> addListener(final ListChangeListener<? super E> listener) {
            return new Generic<E>(this.listener, listener);
        }

        @Override
        protected ListListenerHelper<E> removeListener(final ListChangeListener<? super E> listener) {
            return (listener.equals(this.listener)) ? null : this;
        }

        @Override
        protected void fireValueChangedEvent(final ListChangeListener.Change<? extends E> change) {
            try {
                listener.onChanged(change);
            } catch (final Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class Generic<E> extends ListListenerHelper<E> {

        private InvalidationListener[] invalidationListeners;
        private ListChangeListener<? super E>[] changeListeners;
        private int invalidationSize;
        private int changeSize;
        private boolean locked;

        private Generic(final InvalidationListener listener0, final InvalidationListener listener1) {
            this.invalidationListeners = new InvalidationListener[]{listener0, listener1};
            this.invalidationSize = 2;
        }

        private Generic(final ListChangeListener<? super E> listener0, final ListChangeListener<? super E> listener1) {
            this.changeListeners = new ListChangeListener[]{listener0, listener1};
            this.changeSize = 2;
        }

        private Generic(final InvalidationListener invalidationListener, final ListChangeListener<? super E> changeListener) {
            this.invalidationListeners = new InvalidationListener[]{invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ListChangeListener[]{changeListener};
            this.changeSize = 1;
        }

        @Override
        protected Generic<E> addListener(final InvalidationListener listener) {
            if (invalidationListeners == null) {
                invalidationListeners = new InvalidationListener[]{listener};
                invalidationSize = 1;
            } else {
                final int oldCapacity = invalidationListeners.length;
                if (locked) {
                    final int newCapacity = (invalidationSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                } else if (invalidationSize == oldCapacity) {
                    invalidationSize = trim(invalidationSize, invalidationListeners);
                    if (invalidationSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                    }
                }
                invalidationListeners[invalidationSize++] = listener;
            }
            return this;
        }

        @Override
        protected ListListenerHelper<E> removeListener(final InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (listener.equals(invalidationListeners[index])) {
                        if (invalidationSize == 1) {
                            if (changeSize == 1) {
                                return new SingleChange<E>(changeListeners[0]);
                            }
                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else if ((invalidationSize == 2) && (changeSize == 0)) {
                            return new SingleInvalidation<E>(invalidationListeners[1 - index]);
                        } else {
                            final int numMoved = invalidationSize - index - 1;
                            final InvalidationListener[] oldListeners = invalidationListeners;
                            if (locked) {
                                invalidationListeners = new InvalidationListener[invalidationListeners.length];
                                System.arraycopy(oldListeners, 0, invalidationListeners, 0, index);
                            }
                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, invalidationListeners, index, numMoved);
                            }
                            invalidationSize--;
                            if (!locked) {
                                invalidationListeners[invalidationSize] = null; // Let gc do its work
                            }
                        }
                        break;
                    }
                }
            }
            return this;
        }

        @Override
        protected ListListenerHelper<E> addListener(final ListChangeListener<? super E> listener) {
            if (changeListeners == null) {
                changeListeners = new ListChangeListener[]{listener};
                changeSize = 1;
            } else {
                final int oldCapacity = changeListeners.length;
                if (locked) {
                    final int newCapacity = (changeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                } else if (changeSize == oldCapacity) {
                    changeSize = trim(changeSize, changeListeners);
                    if (changeSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                    }
                }
                changeListeners[changeSize++] = listener;
            }
            return this;
        }

        @Override
        protected ListListenerHelper<E> removeListener(final ListChangeListener<? super E> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (listener.equals(changeListeners[index])) {
                        if (changeSize == 1) {
                            if (invalidationSize == 1) {
                                return new SingleInvalidation<E>(invalidationListeners[0]);
                            }
                            changeListeners = null;
                            changeSize = 0;
                        } else if ((changeSize == 2) && (invalidationSize == 0)) {
                            return new SingleChange<E>(changeListeners[1 - index]);
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final ListChangeListener<? super E>[] oldListeners = changeListeners;
                            if (locked) {
                                changeListeners = new ListChangeListener[changeListeners.length];
                                System.arraycopy(oldListeners, 0, changeListeners, 0, index);
                            }
                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, changeListeners, index, numMoved);
                            }
                            changeSize--;
                            if (!locked) {
                                changeListeners[changeSize] = null; // Let gc do its work
                            }
                        }
                        break;
                    }
                }
            }
            return this;
        }

        @Override
        protected void fireValueChangedEvent(final ListChangeListener.Change<? extends E> change) {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final ListChangeListener<? super E>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;

            try {
                locked = true;
                for (int i = 0; i < curInvalidationSize; i++) {
                    try {
                        curInvalidationList[i].invalidated(change.getList());
                    } catch (final Exception e) {
                        Thread.currentThread()
                            .getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    }
                }
                for (int i = 0; i < curChangeSize; i++) {
                    change.reset();
                    try {
                        curChangeList[i].onChanged(change);
                    } catch (final Exception e) {
                        Thread.currentThread()
                            .getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    }
                }
            } finally {
                locked = false;
            }
        }
    }


}
