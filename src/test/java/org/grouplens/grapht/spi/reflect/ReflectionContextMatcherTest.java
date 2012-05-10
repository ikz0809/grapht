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
import java.util.Collection;
import java.util.Collections;

import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.types.RoleA;
import org.grouplens.grapht.spi.reflect.types.RoleB;
import org.grouplens.grapht.spi.reflect.types.RoleD;
import org.grouplens.grapht.util.AnnotationBuilder;
import org.grouplens.grapht.util.Pair;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionContextMatcherTest {
    @Test
    public void testEquals() {
        InjectSPI spi = new ReflectionInjectSPI();
        
        ReflectionContextMatcher m1 = new ReflectionContextMatcher(A.class);
        ReflectionContextMatcher m2 = new ReflectionContextMatcher(B.class);
        ReflectionContextMatcher m3 = new ReflectionContextMatcher(A.class, spi.match(RoleA.class));
        ReflectionContextMatcher m4 = new ReflectionContextMatcher(A.class, spi.match(RoleB.class));
        
        Assert.assertEquals(m1, new ReflectionContextMatcher(A.class));
        Assert.assertEquals(m2, new ReflectionContextMatcher(B.class));
        Assert.assertEquals(m3, new ReflectionContextMatcher(A.class, spi.match(RoleA.class)));
        Assert.assertEquals(m4, new ReflectionContextMatcher(A.class, spi.match(RoleB.class)));
        
        Assert.assertFalse(m1.equals(m2));
        Assert.assertFalse(m2.equals(m3));
        Assert.assertFalse(m3.equals(m4));
        Assert.assertFalse(m4.equals(m1));
    }
    
    @Test
    public void testExactClassNoRoleMatch() {
        doTestMatch(A.class, null, A.class, null, true);
        doTestMatch(C.class, null, C.class, null, true);
    }
    
    @Test
    public void testExactClassExactRoleMatch() { 
        doTestMatch(A.class, RoleA.class, A.class, RoleA.class, true);
    }
    
    @Test
    public void testSubclassNoRoleMatch() {
        doTestMatch(A.class, null, B.class, null, true);
    }
    
    @Test
    public void testNoClassInheritenceSubRoleNoMatch() {
        doTestMatch(C.class, RoleA.class, A.class, RoleB.class, false);
        doTestMatch(B.class, RoleA.class, A.class, RoleB.class, false);
    }
    
    @Test
    public void testSubclassNoRoleInheritenceNoMatch() {
        doTestMatch(A.class, RoleA.class, B.class, RoleD.class, false);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void doTestMatch(Class<?> matcherType, Class<? extends Annotation> matcherRole,
                             Class<?> satisfactionType, Class<? extends Annotation> satisfactionRole, 
                             boolean expected) {
        QualifierMatcher mr = (matcherRole == null ? new ReflectionInjectSPI().matchAny() : new ReflectionInjectSPI().match(matcherRole));
        final Annotation sr = (satisfactionRole == null ? null : new AnnotationBuilder(satisfactionRole).build());
        Pair<Satisfaction, Attributes> node = Pair.<Satisfaction, Attributes>of(new ClassSatisfaction(satisfactionType),
                                                                                new Attributes() {
                                                                                    @Override
                                                                                    public Annotation getQualifier() {
                                                                                        return sr;
                                                                                    }

                                                                                    @Override
                                                                                    public <T extends Annotation> T getAttribute(Class<T> atype) {
                                                                                        return null;
                                                                                    }
                                                                                    
                                                                                    @Override
                                                                                    public Collection<Annotation> getAttributes() {
                                                                                        return Collections.emptyList();
                                                                                    }
        });
        
        ReflectionContextMatcher cm = new ReflectionContextMatcher(matcherType, mr);
        Assert.assertEquals(expected, cm.matches(node));
    }
    
    public static class A { }
    
    public static class B extends A { }
    
    public static class C { }
}
