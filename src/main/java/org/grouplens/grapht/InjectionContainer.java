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
package org.grouplens.grapht;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Container for dependency-injected components.  A container is the scope of memoization, so
 * components with a cache policy of {@link CachePolicy#MEMOIZE} will share an instance so long
 * as they are instantiated by the same instantiator.
 *
 * @since 0.9
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class InjectionContainer {
    private static final Logger logger = LoggerFactory.getLogger(InjectionContainer.class);

    private final CachePolicy defaultCachePolicy;
    private final Map<DAGNode<Component, Dependency>, Instantiator> providerCache;

    /**
     * Create a new instantiator with a default policy of {@code MEMOIZE}.
     * @return The instantiator.
     */
    public static InjectionContainer create() {
        return create(CachePolicy.MEMOIZE);
    }

    /**
     * Create a new instantiator.
     * @param dft The default cache policy.
     * @return The instantiator.
     */
    public static InjectionContainer create(CachePolicy dft) {
        return new InjectionContainer(dft);
    }

    private InjectionContainer(CachePolicy dft) {
        defaultCachePolicy = dft;
        providerCache = new WeakHashMap<DAGNode<Component, Dependency>, Instantiator>();
    }

    /**
     * Get a provider that, when invoked, will return an instance of the component represented
     * by a graph.
     *
     *
     * @param node The graph.
     * @return A provider to instantiate {@code graph}.
     * @see #makeInstantiator(DAGNode, SetMultimap)
     */
    public Instantiator makeInstantiator(DAGNode<Component, Dependency> node) {
        return makeInstantiator(node, ImmutableSetMultimap.<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>>of());
    }

    /**
     * Get a provider that, when invoked, will return an instance of the component represented
     * by a graph with back edges.  The provider will implement the cache policy, so cached nodes
     * will return a memoized provider.
     *
     * @param node The graph.
     * @param backEdges A multimap of back edges for cyclic dependencies.
     * @return A provider to instantiate {@code graph}.
     */
    public Instantiator makeInstantiator(DAGNode<Component, Dependency> node,
                                         SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> backEdges) {
        Instantiator cached = providerCache.get(node);
        if (cached == null) {
            logger.debug("Node has not been memoized, instantiating: {}", node.getLabel());
            Map<Desire, Instantiator> depMap = makeDependencyMap(node, backEdges);

            Instantiator raw = node.getLabel().getSatisfaction().makeInstantiator(depMap);

            CachePolicy policy = node.getLabel().getCachePolicy();
            if (policy.equals(CachePolicy.NO_PREFERENCE)) {
                policy = defaultCachePolicy;
            }
            if (policy.equals(CachePolicy.MEMOIZE)) {
                // enforce memoization on providers for MEMOIZE policy
                cached = Instantiators.memoize(raw);
            } else {
                // Satisfaction.makeInstantiator() returns providers that are expected
                // to create new instances with each invocation
                assert policy.equals(CachePolicy.NEW_INSTANCE);
                cached = raw;
            }
            providerCache.put(node, cached);
        }
        return cached;
    }

    private Map<Desire, Instantiator> makeDependencyMap(DAGNode<Component, Dependency> node, SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> backEdges) {
        Set<DAGEdge<Component,Dependency>> edges = node.getOutgoingEdges();
        if (backEdges.containsKey(node)) {
            ImmutableSet.Builder<DAGEdge<Component,Dependency>> bld = ImmutableSet.builder();
            edges = bld.addAll(edges)
                       .addAll(backEdges.get(node))
                       .build();
        }

        ImmutableSet.Builder<Desire> desires = ImmutableSet.builder();
        for (DAGEdge<Component,Dependency> edge: edges) {
            desires.add(edge.getLabel().getInitialDesire());
        }

        return Maps.asMap(desires.build(), new DepLookup(edges, backEdges));
    }

    /**
     * Function to look up a desire in a set of dependency edges.
     */
    private class DepLookup implements Function<Desire,Instantiator> {
        private final Set<DAGEdge<Component, Dependency>> edges;
        private final SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> backEdges;

        /**
         * Construct a depenency lookup funciton.
         * @param edges The set of edges to consult.
         * @param backEdges The back edge map (to pass to {@link #makeInstantiator(DAGNode,SetMultimap)}).
         */
        public DepLookup(Set<DAGEdge<Component,Dependency>> edges,
                         SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> backEdges) {
            this.edges = edges;
            this.backEdges = backEdges;
        }

        @Nullable
        @Override
        public Instantiator apply(@Nullable Desire input) {
            for (DAGEdge<Component,Dependency> edge: edges) {
                if (edge.getLabel().getInitialDesire().equals(input)) {
                    return makeInstantiator(edge.getTail(), backEdges);
                }
            }
            return null;
        }
    }
}
