package org.jactors.junit.rule;

import net.jcip.annotations.NotThreadSafe;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.jactors.junit.EnumTest;
import org.jactors.junit.helper.AccessHelper;
import org.jactors.junit.test.SimpleTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * Simple spring rule test.
 */
@Ignore
@NotThreadSafe
@FixMethodOrder(MethodSorters.JVM)
@ContextConfiguration("classpath:spring-test.xml")
public class SpringRuleTest extends SimpleTest {
    /**
     * Ensure enums are extended.
     */
    @BeforeClass
    public static void before() {
       EnumTest.ensure();
    }

    /**
     * Additional spring class rule for dependency injection to check scanning.
     */
    @ClassRule
    public static final SpringRule.ClassRule MORE = null;

    /**
     * Additional spring method rule for transaction behavior.
     */
    @Rule
    public final SpringRule.MethodRule more = new SpringRule.MethodRule();

    /**
     * Autowire application context.
     */
    @Autowired
    public ApplicationContext context;

    /**
     * Autowire additional bean.
     */
    @Autowired
    public String bar;

    /**
     * Check before class setup.
     */
    @BeforeClass
    public static void beforeClass() {
        Assert.assertThat(INJECTION.manager(), CoreMatchers.notNullValue());
    }

    /**
     * Check after class setup.
     */
    @AfterClass
    public static void afterClass() {
        Assert.assertThat(INJECTION.manager(), CoreMatchers.notNullValue());
    }

    /**
     * Check before method setup.
     */
    @Before
    public void beforeMethod() {
        Assert.assertThat(this.transaction.manager(), CoreMatchers.notNullValue());
    }

    /**
     * Check after method setup.
     */
    @After
    public void afterMethod() {
        Assert.assertThat(this.transaction.manager(), CoreMatchers.notNullValue());
    }

    /**
     * Check whether additional bean has been injected.
     */
    @Test
    public void autowireBar() {
        Assert.assertThat(this.bar, CoreMatchers.equalTo("bar"));
    }

    /**
     * Check whether application context has been injected.
     */
    @Test
    public void autowireContext() {
        Assert.assertThat(this.context, CoreMatchers.notNullValue());
    }

    /**
     * Check class rule if no configuration is available.
     */
    @Test
    public void classRuleWithoutConfig() {
        Description description = Description.createSuiteDescription("suite");
        Assertions.assertThat(new SpringRule.ClassRule().apply(null, description)).isNull();
    }

    /**
     * Check method rule if no configuration is available.
     */
    @Test
    public void methodRuleWithoutConfig() {
        FrameworkMethod method = new FrameworkMethod(AccessHelper.Methods.resolve(SpringRule.class, "manager"));
        Assertions.assertThat(new SpringRule.MethodRule().apply(null, method, null)).isNull();
    }

    /**
     * Check method rule statement evaluation.
     *
     * @throws Throwable if test fails.
     */
    @Test
    public void methodRuleEvaluation() throws Throwable {
        TestStatement statement = new TestStatement();
        FrameworkMethod method = new FrameworkMethod(AccessHelper.Methods.resolve(TestStatement.class, "evaluate"));
        new SpringRule.MethodRule().apply(statement, method, this).evaluate();
        Assertions.assertThat(statement.evaluated).isTrue();
    }

    /**
     * Check failed test handling.
     */
    @Test(expected = IllegalArgumentException.class)
    public void evaluateFailure() {
        throw new IllegalArgumentException("should fail");
    }

    /**
     * Alternate test with config but without spring rules.
     */
    @ContextConfiguration("classpath:spring-test.xml")
    public static class TestStatement extends Statement {
        /**
         * Whether test statement has been evaluated.
         */
        protected boolean evaluated = false;

        /**
         * {@inheritDoc}
         */
        public void evaluate() throws Throwable {
            this.evaluated = true;
        }
    }
}
