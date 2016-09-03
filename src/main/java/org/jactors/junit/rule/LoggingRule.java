package org.jactors.junit.rule;

import java.util.Arrays;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple logging rule to distinguish test output. May be applied on class level as well as method
 * level.
 */
public class LoggingRule extends BaseRule implements TestRule {

    /**
     * Separator line.
     */
    private final String sep;

    /**
     * Print stream for output.
     */
    private final Logger log;

    /**
     * Create logging rule with default separator character ('#') and with (128).
     */
    public LoggingRule() {
        this('#', 128);
    }

    /**
     * Create logging rule with given separator character an separator width.
     *
     * @param  sep   separator character.
     * @param  size  separator width.
     */
    public LoggingRule(char sep, int size) {
        this.sep = this.separtor(sep, size);
        this.log = LoggerFactory.getLogger(LoggingRule.class);
    }

    /**
     * Return separator line using given separator character an separator width.
     *
     * @param   sep   separator character.
     * @param   size  separator width.
     *
     * @return  separator line.
     */
    private String separtor(char sep, int size) {
        char[] array = new char[size];
        Arrays.fill(array, sep);
        return new String(array);
    }

    /**
     * Append given context name and context value to given string builder.
     *
     * @param   builder  string builder for appending.
     * @param   name     context name.
     * @param   value    context value.
     *
     * @return  builder for further setup.
     */
    private StringBuilder append(StringBuilder builder, String name, String value) {
        builder.append("# ").append(name).append(value);
        int missing = this.sep.length() - name.length() - value.length() - 3;
        for (int count = 1; count <= missing; count++) {
            builder.append(" ");
        }
        return builder.append("#\n");
    }

    /**
     * Print test footer for given test class and method name, and given test description.
     *
     * @param  name   test class and method name.
     * @param  descr  test description.
     */
    protected void footer(String name, String descr) {
        StringBuilder builder = new StringBuilder((this.sep.length() * 3) + 12);
        builder.append('\n').append(this.sep).append('\n');
        this.append(builder, "End-Test: ", name);
        this.log.info(builder.append(this.sep).toString());
    }

    /**
     * Print test header for given test class and method name, and given test description.
     *
     * @param  name   test class and method name.
     * @param  descr  test description.
     */
    protected void header(String name, String descr) {
        StringBuilder builder = new StringBuilder((this.sep.length() * 4) + 12);
        builder.append('\n').append(this.sep).append('\n');
        this.append(builder, "Start-Test: ", name);
        if (!name.equals(descr)) {
            this.append(builder, "Description: ", descr);
        }
        this.log.info(builder.append(this.sep).toString());
    }

    /**
     * {@inheritDoc}
     */
    public Statement apply(Statement base, Description descr) {
        return new LoggingStatement(this, base, descr);
    }

    /**
     * Internal logging statement.
     */
    private static final class LoggingStatement extends Statement {

        /**
         * Logging rule instance.
         */
        private final LoggingRule rule;

        /**
         * Base statement to execute.
         */
        private final Statement base;

        /**
         * Create logging statement for given logging rule instance, base statement to execute, and
         * test case description (containing class and method name).
         *
         * @param  rule   logging rule instance.
         * @param  base   base statement to execute.
         * @param  descr  test case description.
         */
        public LoggingStatement(LoggingRule rule, Statement base, Description descr) {
            this.rule = rule;
            this.base = base;
            this.rule.init(descr);
        }

        /**
         * {@inheritDoc}
         */
        public void evaluate() throws Throwable {
            String name = this.rule.describe().getClassName()
                + ((this.rule.describe().getMethodName() != null) ? ("." + this.rule.describe().getMethodName()) : "");
            try {
                this.rule.header(name, this.rule.describe().getDisplayName());
                this.base.evaluate();
            } finally {
                this.rule.footer(name, this.rule.describe().getDisplayName());
            }
        }
    }
}
