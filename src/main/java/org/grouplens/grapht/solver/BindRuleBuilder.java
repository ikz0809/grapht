package org.grouplens.grapht.solver;

import com.google.common.base.Preconditions;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.Qualifiers;

/**
 * Builder for bind rules.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BindRuleBuilder {
    private Class<?> dependencyType;
    private QualifierMatcher qualifierMatcher = Qualifiers.matchAny();

    private Satisfaction satisfaction;
    private Class<?> implementation;

    private CachePolicy cachePolicy = CachePolicy.NO_PREFERENCE;
    private boolean terminal = false;

    public static BindRuleBuilder create() {
        return new BindRuleBuilder();
    }

    /**
     * Get the dependency type to match.
     * @return The dependency type to match.
     */
    public Class<?> getDependencyType() {
        return dependencyType;
    }

    /**
     * Set the dependency type to match.
     * @param type The dependency type to match.
     */
    public BindRuleBuilder setDependencyType(Class<?> type) {
        dependencyType = type;
        return this;
    }

    /**
     * Get the configured qualifer matcher.  The initial qualifier matcher is {@link org.grouplens.grapht.spi.reflect.Qualifiers#matchAny()}.
     * @return The qualifier matcher.
     */
    public QualifierMatcher getQualifierMatcher() {
        return qualifierMatcher;
    }

    /**
     * Set the qualifier matcher.
     * @param qm The qualifier matcher.
     */
    public BindRuleBuilder setQualifierMatcher(QualifierMatcher qm) {
        qualifierMatcher = qm;
        return this;
    }

    /**
     * Get the target satisfaction.
     * @return The configured satisfaction, or {@code null} if none is configured.
     */
    public Satisfaction getSatisfaction() {
        return satisfaction;
    }

    /**
     * Set the satisfaction to bind to.  This will unset the implementation class and result in
     * a satisfaction binding.
     *
     * @param sat The satisfaction.
     */
    public BindRuleBuilder setSatisfaction(Satisfaction sat) {
        satisfaction = sat;
        return this;
    }

    /**
     * Get the target implementation.
     * @return The target implementation, or {@code null} if none is configured.
     */
    public Class<?> getImplementation() {
        return implementation;
    }

    /**
     * Set the target implementation. This will unset the satisfaction and result in an implementation
     * class binding.
     *
     * @param type The implementation class.
     */
    public BindRuleBuilder setImplementation(Class<?> type) {
        implementation = type;
        return this;
    }

    /**
     * Query whether the binding will be terminal.
     * @return {@code true} if the binding will be terminal.
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * Set whether the binding will be terminal.
     *
     * @param term {@code true} to create a terminal binding.
     * @see org.grouplens.grapht.solver.BindRule#isTerminal()
     */
    public BindRuleBuilder setTerminal(boolean term) {
        terminal = term;
        return this;
    }

    /**
     * Get the cache policy.
     * @return The cache policy.
     */
    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    /**
     * Set the cache policy.
     * @param policy The cache policy.
     */
    public BindRuleBuilder setCachePolicy(CachePolicy policy) {
        cachePolicy = policy;
        return this;
    }

    public BindRule build() {
        Preconditions.checkState(dependencyType != null, "no dependency type specified");
        if (implementation != null) {
            assert satisfaction == null;
            return new BindRuleImpl(dependencyType, implementation, cachePolicy, qualifierMatcher, terminal);
        } else if (satisfaction != null) {
            return new BindRuleImpl(dependencyType, satisfaction, cachePolicy, qualifierMatcher, terminal);
        } else {
            throw new IllegalStateException("no binding target specified");
        }
    }
}
