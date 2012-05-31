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

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;

/**
 * ReflectionInjectSPI is a complete implementation of {@link InjectSPI}. It
 * uses Java's reflection API to find constructor and setter method injection
 * points that have been annotated with {@link Inject} to determine a type's
 * dependencies.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionInjectSPI implements InjectSPI {
    @Override
    public ContextMatcher context(QualifierMatcher qualifier, Class<?> type) {
        return new ReflectionContextMatcher(type, qualifier);
    }
    
    @Override
    public Desire desire(@Nullable Annotation qualifier, Class<?> type, boolean nullable) {
        return new ReflectionDesire(new SimpleInjectionPoint(qualifier, type, nullable));
    }
    
    @Override
    public QualifierMatcher match(Class<? extends Annotation> qualifier) {
        return Qualifiers.match(qualifier);
    }

    @Override
    public QualifierMatcher match(Annotation annot) {
        return Qualifiers.match(annot);
    }

    @Override
    public QualifierMatcher matchAny() {
        return Qualifiers.matchAny();
    }

    @Override
    public QualifierMatcher matchNone() {
        return Qualifiers.matchNone();
    }

    @Override
    public Satisfaction satisfy(Class<?> type) {
        return new ClassSatisfaction(type);
    }

    @Override
    public Satisfaction satisfyWithNull(Class<?> type) {
        return new NullSatisfaction(type);
    }

    @Override
    public Satisfaction satisfy(Object o) {
        return new InstanceSatisfaction(o);
    }

    @Override
    public Satisfaction satisfyWithProvider(Class<? extends Provider<?>> providerType) {
        return new ProviderClassSatisfaction(providerType);
    }

    @Override
    public Satisfaction satisfyWithProvider(Provider<?> provider) {
        return new ProviderInstanceSatisfaction(provider);
    }
}
