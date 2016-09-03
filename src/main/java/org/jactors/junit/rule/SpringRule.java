package org.jactors.junit.rule;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jactors.junit.helper.AccessHelper;
import org.junit.Assume;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.test.annotation.ProfileValueUtils;
import org.springframework.test.context.TestContextManager;

/**
 * Simple spring rules that allows to create a spring application contexts that (1) inject spring
 * beans and (2) introduce transaction behavior. The rule may either be applied on class level
 * {@link SpringRule.ClassRule} for simple dependency injection, or in addition on method level
 * {@link SpringRule.MethodRule} for adding transactions behavior. Both spring rules are implemented
 * to automatically share the {@link TestContextManager} by searching for the first initialized rule
 * on the target test object instance.
 *
 * <p>The following example shows how to apply both rules together:</p>
 *
 * <pre><code>
 * public class ExampleTest {
 *
 *   &#064;ClassRule
 *   public static final SpringRule.ClassRule CLASS_RULE = new SpringRule.ClassRule();
 *
 *   &#064;Rule
 *   public static final SpringRule.MethodRule methodRule = new SpringRule.MethodRule();
 * }
 * </code></pre>
 */
public abstract class SpringRule {

    /**
     * Test context manager.
     */
    private TestContextManager manager;

    /**
     * Return test context manager.
     *
     * @return  test context manager.
     */
    public TestContextManager manager() {
        return this.manager;
    }

    /**
     * Prepare test local test context manager and inject application context into given target test
     * object instance, if available. The test context manager is either resolved from the first
     * spring rule instance found on the target test object instance, or is created from scratch
     * using the given target test class type.
     *
     * @param   type    target test class type.
     * @param   target  target test object instance (may be null).
     *
     * @throws  Exception  if test context manager failed.
     */
    protected void prepare(Class<?> type, Object target) throws Exception {
        if (target != null) {
            for (Field field : AccessHelper.Fields.resolve(type, SpringRule.class, //
                    AccessHelper.Resolve.Type.CHILD)) {
                SpringRule rule = AccessHelper.Fields.get(target, field);
                if ((rule != null) && (rule.manager != null)) {
                    this.manager = rule.manager;
                    break;
                }
            }
        }
        if (this.manager == null) {
            this.manager = new TestContextManager(type);
        }
        if (target != null) {
            this.manager.prepareTestInstance(target);
        }
    }

    /**
     * Notify test context manager about before state. If no target test method or target test
     * object instance are provided, a before class notification is triggered otherwise a before
     * method notification is triggered.
     *
     * @param   method  target test method (may be null).
     * @param   target  target test object instance (may be null).
     *
     * @throws  Exception  if test context manager failed.
     */
    protected void before(Method method, Object target) throws Exception {
        if ((method != null) & (target != null)) {
            this.manager.beforeTestMethod(target, method);
        } else {
            this.manager.beforeTestClass();
        }
    }

    /**
     * Notify test context manager about after state. If no target test method or target test object
     * instance are provided, a after class notification is triggered otherwise a after method
     * notification is triggered.
     *
     * @param   method  target test method (may be null).
     * @param   target  target test object instance (may be null).
     * @param   cause   target exception thrown during execution (may be null).
     *
     * @throws  Exception  if test context manager failed.
     */
    protected void after(Method method, Object target, Throwable cause) throws Exception {
        if ((method != null) & (target != null)) {
            this.manager.afterTestMethod(target, method, cause);
        } else {
            this.manager.afterTestClass();
        }
        this.manager = null;
    }

    /**
     * Spring rule helper.
     */
    private static final class Helper {

        /**
         * Check whether context configuration is available at given test class.
         *
         * @param   clazz  test class.
         *
         * @return  whether context configuration is available at given test class.
         */
        protected static boolean isContextConfigured(Class<?> clazz) {
            Class<?> type = AccessHelper.Classes.resolve("org.springframework.test.context.ContextConfiguration",
                    AccessHelper.Failure.Mode.RETURN_NULL);
            return (clazz != null) && (type != null) && clazz.isAnnotationPresent(type.asSubclass(Annotation.class));
        }
    }

    /**
     * Class level spring rule to support {@link TestContextManager} setup for dependency injection.
     */
    public static final class ClassRule extends SpringRule implements org.junit.rules.TestRule {

        /**
         * {@inheritDoc}
         */
        public Statement apply(Statement base, Description descr) {
            Class<?> clazz = descr.getTestClass();
            if (Helper.isContextConfigured(clazz)) {
                String name = descr.getMethodName();
                Method method = AccessHelper.Methods.resolve(clazz, name, AccessHelper.Failure.Mode.RETURN_NULL);
                return new SpringStatement(this, base, clazz, method, null);
            }
            return base;
        }
    }

    /**
     * Method level spring rule to support {@link TestContextManager} setup for transaction
     * handling.
     */
    public static final class MethodRule extends SpringRule implements org.junit.rules.MethodRule {

        /**
         * {@inheritDoc}
         */
        public Statement apply(Statement base, FrameworkMethod method, Object target) {
            Class<?> clazz = method.getMethod().getDeclaringClass();
            if (Helper.isContextConfigured(clazz)) {
                return new SpringStatement(this, base, clazz, method.getMethod(), target);
            }
            return base;
        }
    }

    /**
     * Internal common spring statement.
     */
    private static final class SpringStatement extends Statement {

        /**
         * Base spring rule.
         */
        private final SpringRule rule;

        /**
         * Base statement to execute.
         */
        private final Statement base;

        /**
         * Target test class type.
         */
        private final Class<?> type;

        /**
         * Target test method.
         */
        private final Method method;

        /**
         * Target object instance.
         */
        private final Object target;

        /**
         * Create abstract sprint statement with given spring rule, base statement to execute,
         * target test class type, target test method, and target object instance.
         *
         * @param  rule    spring rule.
         * @param  base    base statement to execute.
         * @param  type    target test class type.
         * @param  method  target test method (may be null).
         * @param  target  target object instance (may be null).
         */
        public SpringStatement(SpringRule rule, Statement base, Class<?> type, Method method, Object target) {
            this.rule = rule;
            this.base = base;
            this.target = target;
            this.type = type;
            this.method = method;
        }

        /**
         * {@inheritDoc}
         */
        public void evaluate() throws Throwable {
            Assume.assumeTrue("required profile not active", this.active());
            this.rule.prepare(this.type, this.target);
            this.rule.before(this.method, this.target);
            try {
                this.base.evaluate();
            } catch (Throwable except) {
                this.rule.after(this.method, this.target, except);
                throw except;
            }
            this.rule.after(this.method, this.target, null);
        }

        /**
         * Check whether test is enabled for this environment.
         *
         * @return  whether test is enabled for this environment.
         */
        private boolean active() {
            if (this.method == null) {
                return ProfileValueUtils.isTestEnabledInThisEnvironment(this.type);
            }
            return ProfileValueUtils.isTestEnabledInThisEnvironment(this.method, this.type);
        }
    }
}
