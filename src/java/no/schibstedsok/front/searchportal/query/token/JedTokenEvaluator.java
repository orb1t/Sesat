/* Copyright (2005-2006) Schibsted Søk AS
 *
 * JedTokenEvaluator.java
 *
 * Created on 14 March 2006, 23:14
 *
 */

package no.schibstedsok.front.searchportal.query.token;

import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;
import org.nfunk.jep.type.Complex;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class JedTokenEvaluator implements TokenEvaluator {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(JedTokenEvaluator.class);
    private static final String DEBUG_NOT_INTEGER = "Was not an integer ";

    // Attributes ----------------------------------------------------

    private final Complex result;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of JedTokenEvaluator */
    public JedTokenEvaluator(final String query) {

        final JEP parser = new JEP();

        parser.addStandardConstants();
        parser.addStandardFunctions();
        parser.addComplex();
        parser.setImplicitMul(true);


        parser.parseExpression(query);

        result = parser.getComplexValue();
    }

    // Public --------------------------------------------------------

    public Complex getComplex() {
        return result;
    }

    // TokenEvaluator implementation ----------------------------------------------

    /**
     * Returns true if any of the query satifies a JED expression.
     *
     * @param token
     *            not used by this implementation.
     * @param term
     *            the term currently parsing.
     * @param query
     *            the query to find matches in.
     *              can be null. this indicates we can just use the term.
     *
     * @return true if any of the patterns matches.
     */
    public boolean evaluateToken(final String token, final String term, final String query) {

        return term != null || result != null;
    }

    public boolean isQueryDependant() {
        return true;
    }

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------



}
