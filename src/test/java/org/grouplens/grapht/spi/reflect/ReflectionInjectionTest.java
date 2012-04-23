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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.grouplens.grapht.InjectorConfiguration;
import org.grouplens.grapht.MockInjectorConfiguration;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.solver.DefaultInjector;
import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.types.InterfaceA;
import org.grouplens.grapht.spi.reflect.types.InterfaceB;
import org.grouplens.grapht.spi.reflect.types.ParameterA;
import org.grouplens.grapht.spi.reflect.types.ProviderA;
import org.grouplens.grapht.spi.reflect.types.RoleA;
import org.grouplens.grapht.spi.reflect.types.RoleD;
import org.grouplens.grapht.spi.reflect.types.TypeA;
import org.grouplens.grapht.spi.reflect.types.TypeB;
import org.grouplens.grapht.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionInjectionTest {
    @Test
    public void testTypeCInjectionWithDefaults() throws Exception {
        // Test that TypeC can be resolved successfully without any bind rules.
        // All of TypeC's dependencies have defaults or are satisfiable.
        InjectSPI spi = new ReflectionInjectSPI();
        Desire rootDesire = spi.desire(null, TypeC.class, false);
        InjectorConfiguration config = new MockInjectorConfiguration(spi, new HashMap<ContextChain, Collection<? extends BindRule>>());
        DefaultInjector r = new DefaultInjector(config);
        
        TypeC instance = r.getInstance(TypeC.class);
        Assert.assertEquals(5, instance.getIntValue());
        Assert.assertNotNull(instance.getInterfaceA());
        Assert.assertTrue(instance.getInterfaceA() instanceof TypeB); // ProviderA actually creates TypeB's
        Assert.assertSame(instance.getInterfaceA(), instance.getTypeA());
        Assert.assertNotNull(instance.getInterfaceB());
        Assert.assertTrue(instance.getInterfaceB() instanceof TypeB);
        Assert.assertSame(instance.getInterfaceB(), instance.getTypeB());
        
        // also verify memoization
        Assert.assertSame(instance, r.getInstance(TypeC.class));
        
        Node<Satisfaction> resolvedRoot = r.getSolver().getGraph().getOutgoingEdge(r.getSolver().getRootNode(), rootDesire).getTail();
        Assert.assertEquals(5, r.getSolver().getGraph().getOutgoingEdges(resolvedRoot).size());
        
        Map<InjectionPoint, Node<Satisfaction>> deps = new HashMap<InjectionPoint, Node<Satisfaction>>();
        for (Edge<Satisfaction, Desire> e: r.getSolver().getGraph().getOutgoingEdges(resolvedRoot)) {
            ReflectionDesire d = (ReflectionDesire) e.getLabel();
            
            if (d.getInjectionPoint().equals(TypeC.CONSTRUCTOR)) {
                // A ParameterA defaults to 5
                Assert.assertFalse(deps.containsKey(TypeC.CONSTRUCTOR));
                Assert.assertTrue(e.getTail().getLabel() instanceof InstanceSatisfaction);
                Assert.assertEquals(5, ((InstanceSatisfaction) e.getTail().getLabel()).getInstance());
                deps.put(TypeC.CONSTRUCTOR, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.INTERFACE_A)) {
                // An InterfaceA is implemented by TypeA, which is then provided by Provider A
                Assert.assertFalse(deps.containsKey(TypeC.INTERFACE_A));
                Assert.assertTrue(e.getTail().getLabel() instanceof ProviderClassSatisfaction);
                Assert.assertEquals(ProviderA.class, ((ProviderClassSatisfaction) e.getTail().getLabel()).getProviderType());
                deps.put(TypeC.INTERFACE_A, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.TYPE_A)) {
                // A TypeA is provided by a ProviderA
                Assert.assertFalse(deps.containsKey(TypeC.TYPE_A));
                Assert.assertTrue(e.getTail().getLabel() instanceof ProviderClassSatisfaction);
                Assert.assertEquals(ProviderA.class, ((ProviderClassSatisfaction) e.getTail().getLabel()).getProviderType());
                deps.put(TypeC.TYPE_A, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.INTERFACE_B)) {
                // RoleE inherits RoleD and that defaults to TypeB
                Assert.assertFalse(deps.containsKey(TypeC.INTERFACE_B));
                Assert.assertTrue(e.getTail().getLabel() instanceof ClassSatisfaction);
                Assert.assertEquals(TypeB.class, e.getTail().getLabel().getErasedType());
                deps.put(TypeC.INTERFACE_B, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.TYPE_B)) {
                // TypeB is satisfiable on its own
                Assert.assertFalse(deps.containsKey(TypeC.TYPE_B));
                Assert.assertTrue(e.getTail().getLabel() instanceof ClassSatisfaction);
                Assert.assertEquals(TypeB.class, e.getTail().getLabel().getErasedType());
                deps.put(TypeC.TYPE_B, e.getTail());
            } else {
                Assert.fail();
            }
        }
        
        // verify that all injection points were tested
        Assert.assertTrue(deps.containsKey(TypeC.CONSTRUCTOR));
        Assert.assertTrue(deps.containsKey(TypeC.TYPE_A));
        Assert.assertTrue(deps.containsKey(TypeC.INTERFACE_A));
        Assert.assertTrue(deps.containsKey(TypeC.TYPE_B));
        Assert.assertTrue(deps.containsKey(TypeC.INTERFACE_B));
        
        // make sure that nodes are shared where appropriate
        Assert.assertSame(deps.get(TypeC.INTERFACE_A), deps.get(TypeC.TYPE_A));
        Assert.assertSame(deps.get(TypeC.INTERFACE_B), deps.get(TypeC.TYPE_B));
    }
    
    @Test
    public void testTypeCInjectionWithBindings() throws Exception {
        // Test that TypeC can be injected correctly using bind rules, although
        // the bind rule configuration does not need to be very complicated, since
        // the resolver and bind rules are already tested.
        InjectSPI spi = new ReflectionInjectSPI();
        Desire rootDesire = spi.desire(null, TypeC.class, false);
        
        TypeA a = new TypeA();
        TypeB b = new TypeB();
        
        Map<ContextChain, Collection<? extends BindRule>> bindRules = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindRules.put(new ContextChain(new ArrayList<ContextMatcher>()),
                      Arrays.asList(spi.bindInstance(spi.match(ParameterA.class), Integer.class, 10, 0),
                                    spi.bindType(spi.match(RoleA.class), InterfaceA.class, PrimeA.class, 0, false),
                                    spi.bindType(spi.match(RoleD.class), InterfaceB.class, PrimeB.class, 0, false),
                                    spi.bindInstance(spi.matchAny(), TypeA.class, a, 0),
                                    spi.bindInstance(spi.matchAny(), TypeB.class, b, 0)));
        
        DefaultInjector r = new DefaultInjector(new MockInjectorConfiguration(spi, bindRules));

        TypeC instance = r.getInstance(TypeC.class);
        Assert.assertEquals(10, instance.getIntValue());
        Assert.assertNotNull(instance.getInterfaceA());
        Assert.assertTrue(instance.getInterfaceA() instanceof PrimeA);
        Assert.assertSame(a, instance.getTypeA());
        Assert.assertNotNull(instance.getInterfaceB());
        Assert.assertTrue(instance.getInterfaceB() instanceof PrimeB);
        Assert.assertSame(b, instance.getTypeB());
        
        // also verify memoization
        Assert.assertSame(instance, r.getInstance(TypeC.class));
        
        Node<Satisfaction> resolvedRoot = r.getSolver().getGraph().getOutgoingEdge(r.getSolver().getRootNode(), rootDesire).getTail();
        Assert.assertEquals(5, r.getSolver().getGraph().getOutgoingEdges(resolvedRoot).size());
        
        Map<InjectionPoint, Node<Satisfaction>> deps = new HashMap<InjectionPoint, Node<Satisfaction>>();
        for (Edge<Satisfaction, Desire> e: r.getSolver().getGraph().getOutgoingEdges(resolvedRoot)) {
            ReflectionDesire d = (ReflectionDesire) e.getLabel();
            
            if (d.getInjectionPoint().equals(TypeC.CONSTRUCTOR)) {
                // ParameterA was set to 10
                Assert.assertFalse(deps.containsKey(TypeC.CONSTRUCTOR));
                Assert.assertTrue(e.getTail().getLabel() instanceof InstanceSatisfaction);
                Assert.assertEquals(10, ((InstanceSatisfaction) e.getTail().getLabel()).getInstance());
                deps.put(TypeC.CONSTRUCTOR, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.INTERFACE_A)) {
                // An InterfaceA has been bound to PrimeA
                Assert.assertFalse(deps.containsKey(TypeC.INTERFACE_A));
                Assert.assertTrue(e.getTail().getLabel() instanceof ClassSatisfaction);
                Assert.assertEquals(PrimeA.class, e.getTail().getLabel().getErasedType());
                deps.put(TypeC.INTERFACE_A, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.TYPE_A)) {
                // A TypeA has been bound to an instance
                Assert.assertFalse(deps.containsKey(TypeC.TYPE_A));
                Assert.assertTrue(e.getTail().getLabel() instanceof InstanceSatisfaction);
                Assert.assertSame(a, ((InstanceSatisfaction) e.getTail().getLabel()).getInstance());
                deps.put(TypeC.TYPE_A, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.INTERFACE_B)) {
                // RoleE has been bound to PrimeB
                Assert.assertFalse(deps.containsKey(TypeC.INTERFACE_B));
                Assert.assertTrue(e.getTail().getLabel() instanceof ClassSatisfaction);
                Assert.assertEquals(PrimeB.class, e.getTail().getLabel().getErasedType());
                deps.put(TypeC.INTERFACE_B, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.TYPE_B)) {
                // TypeB has been bound to an instance
                Assert.assertFalse(deps.containsKey(TypeC.TYPE_B));
                Assert.assertTrue(e.getTail().getLabel() instanceof InstanceSatisfaction);
                Assert.assertSame(b, ((InstanceSatisfaction) e.getTail().getLabel()).getInstance());
                deps.put(TypeC.TYPE_B, e.getTail());
            } else {
                Assert.fail();
            }
        }
        
        // verify that all injection points were tested
        Assert.assertTrue(deps.containsKey(TypeC.CONSTRUCTOR));
        Assert.assertTrue(deps.containsKey(TypeC.TYPE_A));
        Assert.assertTrue(deps.containsKey(TypeC.INTERFACE_A));
        Assert.assertTrue(deps.containsKey(TypeC.TYPE_B));
        Assert.assertTrue(deps.containsKey(TypeC.INTERFACE_B));
    }
    
    public static class PrimeA implements InterfaceA {
        
    }
    
    public static class PrimeB implements InterfaceB {
        
    }
}
