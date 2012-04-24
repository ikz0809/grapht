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

import java.lang.reflect.Constructor;

import org.grouplens.grapht.util.Types;

/**
 * ConstructorParameterInjectionPoint is an injection point wrapping a parameter
 * of a constructor.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ConstructorParameterInjectionPoint implements InjectionPoint {
    private final AnnotationQualifier qualifier;
    private final Constructor<?> ctor;
    private final int parameter;

    /**
     * Create a ConstructorParameterInjectionPoint that wraps the given
     * parameter index for the given constructor, ctor.
     * 
     * @param ctor The constructor to wrap
     * @param parameter The parameter index of this injection point within
     *            ctor's parameters
     * @throws NullPointerException if ctor is null
     * @throws IndexOutOfBoundsException if parameter is not a valid index into
     *             the constructor's parameters
     */
    public ConstructorParameterInjectionPoint(Constructor<?> ctor, int parameter) {
        if (ctor == null) {
            throw new NullPointerException("Constructor cannot be null");
        }
        
        int numArgs = ctor.getParameterTypes().length;
        if (parameter < 0 || parameter >= numArgs) {
            throw new IndexOutOfBoundsException("Constructor parameter is invalid");
        }
        
        this.qualifier = Qualifiers.getQualifier(ctor.getParameterAnnotations()[parameter]);
        this.ctor = ctor;
        this.parameter = parameter;
    }

    /**
     * @return The constructor wrapped by this injection point
     */
    public Constructor<?> getConstructor() {
        return ctor;
    }
    
    /**
     * @return The parameter index of this injection point within the
     *         constructor's parameters
     */
    public int getParameterIndex() {
        return parameter;
    }
    
    @Override
    public boolean isNullable() {
        return Types.hasNullableAnnotation(ctor.getParameterAnnotations()[parameter]);
    }
    
    @Override
    public boolean isTransient() {
        return Types.hasTransientAnnotation(ctor.getParameterAnnotations()[parameter]);
    }

    @Override
    public Class<?> getType() {
        return Types.box(ctor.getParameterTypes()[parameter]);
    }

    @Override
    public AnnotationQualifier getQualifier() {
        return qualifier;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorParameterInjectionPoint)) {
            return false;
        }
        ConstructorParameterInjectionPoint cp = (ConstructorParameterInjectionPoint) o;
        return cp.ctor.equals(ctor) && cp.parameter == parameter;
    }
    
    @Override
    public int hashCode() {
        return ctor.hashCode() ^ (37 * 17 * parameter);
    }
    
    @Override
    public String toString() {
        String q = (qualifier == null ? "" : qualifier + ":");
        String p = ctor.getParameterTypes()[parameter].getSimpleName();
        return ctor.getDeclaringClass().getSimpleName() + "(" + parameter + ", " + q + p + ")";
    }
}