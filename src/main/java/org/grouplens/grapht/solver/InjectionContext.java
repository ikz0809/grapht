/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.grapht.solver;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.internal.SimpleInjectionPoint;
import org.grouplens.grapht.util.AbstractChain;

import javax.annotation.Nullable;

/**
 * <p>
 * InjectionContext represents the current path through the dependency graph to
 * the desire being resolved by
 * {@link BindingFunction#bind(InjectionContext, DesireChain)}. The InjectionContext
 * is most significantly represented as a list of satisfactions and the
 * associated injection point attributes. This list represents the "type path"
 * from the root node in the graph to the previously resolved satisfaction.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InjectionContext extends AbstractChain<Pair<Satisfaction,InjectionPoint>> {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a singleton injection context.
     * @param satisfaction The satisfaction.
     * @param ip The injection point.
     * @return The injection context.
     */
    public static InjectionContext singleton(Satisfaction satisfaction, InjectionPoint ip) {
        return new InjectionContext(null, satisfaction, ip);
    }

    /**
     * Construct a singleton injection context with no attributes.
     * @param satisfaction The satisfaction.
     * @return The injection context.
     */
    public static InjectionContext singleton(Satisfaction satisfaction) {
        return singleton(satisfaction, new SimpleInjectionPoint(null, satisfaction.getErasedType(), true));
    }

    private InjectionContext(InjectionContext prior, Satisfaction satisfaction, InjectionPoint ip) {
        super(prior, Pair.of(satisfaction, ip));
    }

    /**
     * Create a new context that is updated to have the satisfaction and attribute pushed to the
     * end of its type path. The value cache for the new context will be empty.
     * 
     * @param satisfaction The next satisfaction in the dependency graph
     * @param ip The injection point receiving the satisfaction
     * @return A new context with updated type path
     */
    public InjectionContext extend(Satisfaction satisfaction, InjectionPoint ip) {
        return new InjectionContext(this, satisfaction, ip);
    }

    /**
     * Get everything except the last element of this context.
     *
     * @return Everything except the last element of this context, or {@code null} if the context is
     *         a singleton.
     */
    @Nullable
    public InjectionContext getLeading() {
        return (InjectionContext) previous;
    }
}
