/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.spi;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.inject.spi.ContextChain;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.spi.Satisfaction;
import org.junit.Test;

public class ContextChainTest {
    @Test
    public void testEmptyChainEmptyContextSuccess() throws Exception {
        // Test that an empty ContextChain matches an empty context
        doTest(new Class<?>[0], new Class<?>[0], true);
    }
    
    @Test
    public void testEmptyChainNonEmptyContextSuccess() throws Exception {
        // Test that an empty ContextChain matches contexts that have any nodes
        doTest(new Class<?>[0], new Class<?>[] { A.class }, true);
        doTest(new Class<?>[0], new Class<?>[] { A.class, B.class }, true);
        doTest(new Class<?>[0], new Class<?>[] { A.class, Ap.class }, true);
        doTest(new Class<?>[0], new Class<?>[] { A.class, B.class, C.class}, true);
    }
    
    @Test
    public void testEqualChainContextSuccess() throws Exception {
        // Test that a ContextChain equaling the context matches
        doTest(new Class<?>[] { A.class }, new Class<?>[] { A.class }, true);
        doTest(new Class<?>[] { A.class, B.class }, new Class<?>[] { A.class, B.class }, true);
        doTest(new Class<?>[] { A.class, B.class, C.class }, new Class<?>[] { A.class, B.class, C.class }, true);
    }
    
    @Test
    public void testSubstringChainSuccess() throws Exception {
        // Test that a ContextChain that is a substring (front/middle/end)
        // matches the context
        doTest(new Class<?>[] { A.class }, new Class<?>[] { A.class, B.class, C.class }, true);
        doTest(new Class<?>[] { B.class }, new Class<?>[] { A.class, B.class, C.class }, true);
        doTest(new Class<?>[] { C.class }, new Class<?>[] { A.class, B.class, C.class }, true);

        doTest(new Class<?>[] { A.class, B.class }, new Class<?>[] { A.class, B.class, C.class }, true);
        doTest(new Class<?>[] { B.class, C.class }, new Class<?>[] { A.class, B.class, C.class }, true);
        
        doTest(new Class<?>[] { B.class, C.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class }, true);
    }
    
    @Test
    public void testSubsequenceChainSuccess() throws Exception {
        // Test that a ContextChain that is a subsequence (with elements 
        // separated in the context) matches the context
        doTest(new Class<?>[] { A.class, C.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class, Bp.class, Cp.class }, true);
        doTest(new Class<?>[] { A.class, Ap.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class, Bp.class, Cp.class }, true);
        doTest(new Class<?>[] { A.class, Bp.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class, Bp.class, Cp.class }, true);
        doTest(new Class<?>[] { A.class, C.class, Bp.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class, Bp.class, Cp.class }, true);
        doTest(new Class<?>[] { A.class, B.class, Ap.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class, Bp.class, Cp.class }, true);
        doTest(new Class<?>[] { B.class, Cp.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class, Bp.class, Cp.class }, true);
        doTest(new Class<?>[] { A.class, Cp.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class, Bp.class, Cp.class }, true);
        doTest(new Class<?>[] { C.class, Ap.class, Bp.class }, new Class<?>[] { A.class, B.class, C.class, Ap.class, Bp.class, Cp.class }, true);
    }
    
    @Test
    public void testMatcherInheritenceSuccess() throws Exception {
        // Test that a ContextChain that has supertypes of the context
        // is still matched
        doTest(new Class<?>[] { A.class }, new Class<?>[] { Ap.class }, true);

        doTest(new Class<?>[] { A.class, C.class }, new Class<?>[] { Ap.class, Cp.class }, true);
        doTest(new Class<?>[] { A.class, C.class }, new Class<?>[] { A.class, Cp.class }, true);
        doTest(new Class<?>[] { A.class, C.class }, new Class<?>[] { Ap.class, C.class }, true);
    }
    
    @Test
    public void testNonEmptyChainEmptyContextFail() throws Exception {
        // Test that a non-empty ContextChain fails to match an empty context
        doTest(new Class<?>[] { A.class }, new Class<?>[0], false);
        doTest(new Class<?>[] { A.class, B.class }, new Class<?>[0], false);
    }
    
    @Test
    public void testNonSubsequenceFail() throws Exception {
        // Test that a ContextChain that is not a subsequence fails to match
        doTest(new Class<?>[] { A.class }, new Class<?>[] { B.class }, false);
        doTest(new Class<?>[] { A.class, B.class }, new Class<?>[] { B.class, A.class }, false);
        doTest(new Class<?>[] { B.class, A.class, C.class }, new Class<?> [] { C.class, B.class, A.class }, false);
        doTest(new Class<?>[] { A.class, B.class, C.class }, new Class<?> [] { C.class, B.class, A.class }, false);
    }
    
    @Test
    public void testSuperstringFail() throws Exception {
        // Test that a ContextChain that is the context plus additional matchers
        // fails to match the context
        doTest(new Class<?>[] { A.class, B.class }, new Class<?>[] { A.class }, false);
        doTest(new Class<?>[] { A.class, B.class, C.class }, new Class<?>[] { A.class, C.class }, false);
        doTest(new Class<?>[] { A.class, B.class, C.class}, new Class<?>[] { A.class, B.class }, false);
    }
    
    private void doTest(Class<?>[] chainTypes, Class<?>[] contextTypes, boolean expectedMatch) throws Exception {
        List<ContextMatcher> matchers = new ArrayList<ContextMatcher>();
        for (Class<?> type: chainTypes) {
            matchers.add(new MockContextMatcher(type));
        }
        ContextChain chain = new ContextChain(matchers);
        
        List<Pair<Satisfaction, Qualifier>> context = new ArrayList<Pair<Satisfaction, Qualifier>>();
        for (Class<?> type: contextTypes) {
            context.add(Pair.<Satisfaction, Qualifier>of(new MockSatisfaction(type, new ArrayList<Desire>()), null));
        }
        
        Assert.assertEquals(expectedMatch, chain.matches(context));
    }
    
    private static class A {}
    private static class B {}
    private static class C {}
    
    private static class Ap extends A {}
    private static class Bp extends B {}
    private static class Cp extends C {}
}