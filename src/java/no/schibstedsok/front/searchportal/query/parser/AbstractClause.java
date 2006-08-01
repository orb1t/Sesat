/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractClause.java
 *
 * Created on 11 January 2006, 14:17
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.Visitor;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;


/** Basic implementation of the Clause interface.
 * Provides basic implementation of the predicates lists, defaulting to empty lists.
 * Also provides weak reference hashmaps to keep record and reuse the Clauses already in use in the JVM.
 * <b>Objects of this class are immutable</b>
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractClause implements Clause {

    private static final Logger LOG = Logger.getLogger(AbstractClause.class);
    private static final String INFO_WEAK_CACHE_SIZE ="WeakCache size at ";
    private static final String DEBUG_REFERENCE_REUSED = "Gjenbruk weakReference. Størrelse: ";

    private final String term;
    private final Set<TokenPredicate> knownPredicates;
    private final Set<TokenPredicate> possiblePredicates;

    /**
     * See if there is an identical and immutable Clause already in use in the JVM.
     * @param key the <B>unique</B> (for this AbstractClause subtype) key for the Clause we are looking for.
     * @param weakCache the map containing the key to WeakReference (of the Clause) mappings.
     * @return the AbstractClause in use already, matching the key. <B>May be <CODE>null</CODE></B>.
     */
    protected static final <T extends AbstractClause> T findClauseInUse(
            final String key,
            final Map<String,WeakReference<T>> weakCache) {

        T result = null;

        final WeakReference<T> weakRef = weakCache.get(key);
        if (weakRef != null) {
            result = weakRef.get();
        }
        if (result != null && LOG.isDebugEnabled()) {
            LOG.debug(DEBUG_REFERENCE_REUSED + weakCache.size());
        }

        return result;
    }

    /**
     * Note there is an identical and immutable Clause ready to use in the JVM.
     * @param key the <B>unique</B> (for this AbstractClause subtype) key for the Clause we are about to add to the mappings.
     * @param clause the Clause we are about to add to the mappings.
     * @param weakCache the map containing the key to WeakReference (of the Clause) mappings.
     */
    protected static final <T extends AbstractClause> void addClauseInUse(
            final String key,
            final T clause,
            final Map<String,WeakReference<T>> weakCache) {

        weakCache.put(key, new WeakClauseReference<T>(key, clause, weakCache));

        // log weakCache size every 100 increments
        if(weakCache.size() % 100 == 0){
            LOG.info(INFO_WEAK_CACHE_SIZE +  weakCache.size());
        }
    }

    /**
     * Find the predicates that are applicable to the clause.
     * (Only the clause's term is known and is kept in state inside the TokenEvaluationEngine).
     * Add known predicates to <CODE>knownPredicates</CODE>.
     * Add possible (requires further checking against the whole query heirarchy) predicates to <CODE>possiblePredicates</CODE>.
     *
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @param predicates2check the complete list of predicates that could apply to the current clause we are finding predicates for.
     */
    protected static final void findPredicates(
            final TokenEvaluationEngine predicate2evaluatorFactory,
            final Collection<TokenPredicate> predicates2check) {

        final Set<TokenPredicate> knownPredicates = predicate2evaluatorFactory.getClausesKnownPredicates();
        final Set<TokenPredicate> possiblePredicates = predicate2evaluatorFactory.getClausesPossiblePredicates();
        final String currTerm = predicate2evaluatorFactory.getCurrentTerm();


        for (TokenPredicate token : predicates2check) {

            // check it hasn't already been added
            if(!(knownPredicates.contains(token) || possiblePredicates.contains(token))){

                if (token.evaluate(predicate2evaluatorFactory)) {
                    final TokenEvaluator evaluator = predicate2evaluatorFactory.getEvaluator(token);
                    if (evaluator.isQueryDependant(token)) {
                        possiblePredicates.add(token);
                        LOG.debug(DEBUG_FOUND_PREDICATE_PREFIX + currTerm + DEBUG_FOUND_PREDICATE_POSSIBLE + token);
                    }  else  {
                        knownPredicates.add(token);
                        LOG.debug(DEBUG_FOUND_PREDICATE_PREFIX + currTerm + DEBUG_FOUND_PREDICATE_KNOWN + token);
                    }
                }
            }
        }
    }

    /** You must use <CODE>AbstractClause(String, Set&lt;Predicate&gt;, Set&lt;Predicate&gt;)</CODE> instead.
     * This constructor will throw an IllegalArgumentException.
     **/
    protected AbstractClause() {
        throw new IllegalArgumentException(ERR_MUST_ALWAYS_USE_ARGED_CONSTRUCTOR);
    }

    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected AbstractClause(
            final String term,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        this.term = term;
        this.knownPredicates = Collections.unmodifiableSet(knownPredicates);
        this.possiblePredicates = Collections.unmodifiableSet(possiblePredicates);
    }

    /**
     * Get the term for this Clause.
     * Does not include any field values (eg "firstname:").
     * @return the term for this clause.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Get the set of knownPredicates for this Clause.
     * The set is unmodifiable.
     * @return set of knownPredicates.
     */
    public Set<TokenPredicate> getKnownPredicates() {
        return knownPredicates;
    }

    /**
     * Get the set of possiblePredicates for this Clause.
     * The set is unmodifiable.
     * @return set of possiblePredicates.
     */
    public Set<TokenPredicate> getPossiblePredicates() {
        return possiblePredicates;
    }


    /** {@inheritDoc}
     */
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    /** {@inheritDoc}
     */
    public String toString() {
        return getClass().getSimpleName() + "[" + getTerm() + "]";
    }

    /**
     * Error message when reflection cannot find the required constructor.
     */
    protected static final String ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR
            = "Failed to find (or use) constructor with parameters (String, String, Set, Set) for class: ";
    /**
     * Error message when trying to use the incorrect constructor.
     **/
    protected static final String ERR_MUST_ALWAYS_USE_ARGED_CONSTRUCTOR = "Illegal to call constructor without arguments!";
    private static final String DEBUG_FOUND_PREDICATE_PREFIX = "Found (for \"";
    private static final String DEBUG_FOUND_PREDICATE_KNOWN = "\") known ";
    private static final String DEBUG_FOUND_PREDICATE_POSSIBLE = "\") possible ";


    private static final class WeakClauseReference<T> extends WeakReference{

        private final Map<String,WeakReference<T>> weakCache;
        private final String key;

        WeakClauseReference(
                final String key,
                final T clause,
                final Map<String,WeakReference<T>> weakCache){

            super(clause);
            this.key = key;
            this.weakCache = weakCache;
        }

        public void clear() {
            // clear the hashmap entry too!
            weakCache.remove(key);
            // clear the referent
            super.clear();
        }
    }
}
