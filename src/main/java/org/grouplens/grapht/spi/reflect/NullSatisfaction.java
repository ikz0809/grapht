/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.grapht.spi.reflect;

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.InstanceProvider;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * NullSatisfaction is a satisfaction that explicitly satisfies desires with the
 * <code>null</code> value.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class NullSatisfaction implements Satisfaction, Externalizable {
    // "final"
    private Class<?> type;
    
    /**
     * Create a NullSatisfaction that uses <code>null</code> to satisfy the
     * given class type.
     * 
     * @param type The type to satisfy
     * @throws NullPointerException if type is null
     */
    public NullSatisfaction(Class<?> type) {
        Preconditions.notNull("type", type);
        this.type = Types.box(type);
    }
    
    /**
     * Constructor required by {@link Externalizable}.
     */
    public NullSatisfaction() { }
    
    @Override
    public CachePolicy getDefaultCachePolicy() {
        return (getErasedType().getAnnotation(Singleton.class) != null ? CachePolicy.MEMOIZE : CachePolicy.NO_PREFERENCE);
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getErasedType() {
        return type;
    }

    @Override
    public boolean hasInstance() {
        // Null satisfactions have instances, just null ones.
        return true;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Provider<?> makeProvider(ProviderSource dependencies) {
        return new InstanceProvider(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NullSatisfaction)) {
            return false;
        }
        return ((NullSatisfaction) o).type.equals(type);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
    @Override
    public String toString() {
        return "Null(" + type.getSimpleName() + ")";
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = Types.readClass(in);
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Types.writeClass(out, type);
    }
}
