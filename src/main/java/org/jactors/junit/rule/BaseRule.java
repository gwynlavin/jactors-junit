package org.jactors.junit.rule;

import org.junit.runner.Description;

/**
 * Base rule providing test case description at runtime. The base rule should be initialized by the
 * rule statement in the constructor. As safty measure the base rule enforces to be initialized only
 * once.
 */
public class BaseRule {

    /**
     * Test case description.
     */
    private Description descr;

    /**
     * Initialize base rule with test case description.
     *
     * @param  descr  test case description.
     */
    protected final void init(Description descr) {
        if (this.descr != null) {
            throw new RuntimeException("rule description must not be initialized twice");
        }
        this.descr = descr;
    }

    /**
     * Return test case description.
     *
     * @return  test case description.
     */
    public final Description describe() {
        if (this.descr == null) {
            throw new RuntimeException("rule description not initialized");
        }
        return this.descr;
    }
}
