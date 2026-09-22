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

/**
 * {@link uk.ac.ox.poseidon.biology.buckets.Bucket}s: immutable, per-species quantities of
 * {@link uk.ac.ox.poseidon.biology.Content}, the unit in which fish are caught, carried, landed
 * and sold. Only the {@code Bucket} and
 * {@link uk.ac.ox.poseidon.biology.buckets.BucketBuilder} interfaces are visible outside this
 * package; the implementations are specialized for different shapes of content and chosen for you
 * at build time, since buckets sit on the model's hottest paths.
 *
 * <p>Buckets are deliberately generic over {@link uk.ac.ox.poseidon.biology.Content} rather than
 * being about biomass specifically, so that an age- or length-structured abundance can be added
 * later without duplicating the machinery that moves fish around — see
 * {@link uk.ac.ox.poseidon.biology.Content} for why.
 */
package uk.ac.ox.poseidon.biology.buckets;
