/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.spi.reflect;

import javax.annotation.Nullable;

import org.grouplens.inject.spi.Desire;

/**
 * InstanceBindRule is a reflection bind rule that satisfies matching desires
 * with an {@link InstanceSatisfaction}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class InstanceBindRule extends ReflectionBindRule {
    private final Object instance;

    /**
     * Create an InstanceBindRule that binds the given instance to desires for
     * the source type and role.
     * 
     * @param instance The instance to bind
     * @param sourceType The source type matched by this bind rule
     * @param role The role matched by this bind rule
     * @param generated True if the rule was automatically generated
     * @throws NullPointerException if instance or sourceType are null
     * @throws IllegalArgumentException if instance is not an instance of the
     *             source type
     */
    public InstanceBindRule(Object instance,  Class<?> sourceType, @Nullable AnnotationRole role, boolean generated) {
        super(sourceType, role, generated);
        if (instance == null) {
            throw new NullPointerException("Binding instance cannot be null");
        }
        if (!Types.box(sourceType).isInstance(instance)) {
            throw new IllegalArgumentException("Instance does not extend source type");
        }
        this.instance = instance;
    }

    @Override
    public Desire apply(Desire desire) {
        ReflectionDesire origDesire = (ReflectionDesire) desire;
        return new ReflectionDesire(instance.getClass(), origDesire.getInjectionPoint(), new InstanceSatisfaction(instance));
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InstanceBindRule)) {
            return false;
        }
        return super.equals(o) && ((InstanceBindRule) o).instance == instance;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ System.identityHashCode(instance);
    }
}
