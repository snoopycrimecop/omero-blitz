/*
 * Copyright (C) 2020 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.util;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import com.google.common.collect.Maps;

/**
 * Efficiently applies prefix transformations from a set thereof.
 * @author m.t.b.carroll@dundee.ac.uk
 * @param <X> the type of sequences that have prefixes needing substituting
 */
public class PrefixSubstituter<X extends Comparable<X>> {

    private final BiFunction<X, X, X> addPrefix, delPrefix;
    private final SortedMap<X, Entry<X, X>> lookup;

    /**
     * Construct a new prefix substituter.
     * @param isPrefixOf test if the first argument prefixes the second
     * @param addPrefix prefix the first argument to the second
     * @param delPrefix remove the first argument from the start of the second
     */
    public PrefixSubstituter(BiPredicate<X, X> isPrefixOf, BiFunction<X, X, X> addPrefix, BiFunction<X, X, X> delPrefix) {
        this.addPrefix = addPrefix;
        this.delPrefix = delPrefix;
        this.lookup = new TreeMap<>(new Comparator<X>() {
            /* Note: This anonymous class has a natural ordering that is inconsistent with Object.equals. */
            @Override
            public int compare(X p, X q) {
                if (isPrefixOf.test(p, q) || isPrefixOf.test(q, p)) {
                    return 0;
                } else {
                    return p.compareTo(q);
                }
            }
        });
    }

    /**
     * Add a prefix substitution to the applicable set.
     * @param from the prefix to match
     * @param to the replacement prefix
     */
    public void put(X from, X to) {
        if (lookup.put(from, Maps.immutableEntry(from, to)) != null) {
            throw new IllegalArgumentException("can add only prefixes that are not a prefix of another");
        }
    }

    /**
     * Apply a prefix substitution if any applies.
     * @param item the sequence whose prefix may need substituting, not {@code null}
     * @return the given sequence, has prefix substituted if any applied
     */
    public X apply(X item) {
        final Entry<X, X> substitution = lookup.get(item);
        if (substitution == null) {
            return item;
        }
        final X prefixFrom = substitution.getKey();
        final X prefixTo = substitution.getValue();
        final X suffix = delPrefix.apply(prefixFrom, item);
        return addPrefix.apply(prefixTo, suffix);
    }
}
