package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;

import org.grouplens.inject.spi.SatisfactionAndRole;
import org.grouplens.inject.spi.reflect.types.RoleA;
import org.grouplens.inject.spi.reflect.types.RoleB;
import org.grouplens.inject.spi.reflect.types.RoleC;
import org.grouplens.inject.spi.reflect.types.RoleD;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionContextMatcherTest {
    @Test
    public void testEquals() {
        ReflectionContextMatcher m1 = new ReflectionContextMatcher(A.class);
        ReflectionContextMatcher m2 = new ReflectionContextMatcher(B.class);
        ReflectionContextMatcher m3 = new ReflectionContextMatcher(A.class, new AnnotationRole(RoleA.class));
        ReflectionContextMatcher m4 = new ReflectionContextMatcher(A.class, new AnnotationRole(RoleB.class));
        
        Assert.assertEquals(m1, new ReflectionContextMatcher(A.class));
        Assert.assertEquals(m2, new ReflectionContextMatcher(B.class));
        Assert.assertEquals(m3, new ReflectionContextMatcher(A.class, new AnnotationRole(RoleA.class)));
        Assert.assertEquals(m4, new ReflectionContextMatcher(A.class, new AnnotationRole(RoleB.class)));
        
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
    public void testSubclassSubRoleMatch() {
        doTestMatch(A.class, RoleA.class, B.class, RoleB.class, true);
        doTestMatch(A.class, RoleA.class, A.class, RoleC.class, true);
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
    
    private void doTestMatch(Class<?> matcherType, Class<? extends Annotation> matcherRole,
                             Class<?> satisfactionType, Class<? extends Annotation> satisfactionRole, 
                             boolean expected) {
        AnnotationRole mr = (matcherRole == null ? null : new AnnotationRole(matcherRole));
        AnnotationRole sr = (satisfactionRole == null ? null : new AnnotationRole(satisfactionRole));
        SatisfactionAndRole node = new SatisfactionAndRole(new ClassSatisfaction(satisfactionType), sr);
        
        ReflectionContextMatcher cm = new ReflectionContextMatcher(matcherType, mr);
        Assert.assertEquals(expected, cm.matches(node));
    }
    
    public static class A { }
    
    public static class B extends A { }
    
    public static class C { }
}
