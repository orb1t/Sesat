/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import no.sesat.searchportal.query.WordClause;
import no.sesat.searchportal.query.token.TokenEvaluationEngine;
import no.sesat.searchportal.query.token.TokenPredicate;
import no.sesat.searchportal.site.Site;
/**
 * Represent a word in the query. May contain the optional field (field:word).
 * May contain both character and digits but cannot contain only digits
 * (a IntegerClause will be used instead then).
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class WordClauseImpl extends AbstractLeafClause implements WordClause {
    
    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,Map<String,WeakReference<WordClauseImpl>>> WEAK_CACHE
            = new ConcurrentHashMap<Site,Map<String,WeakReference<WordClauseImpl>>>();

    /* A WordClauseImpl specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection<TokenPredicate> PREDICATES_APPLICABLE;

    static {
        final Collection<TokenPredicate> predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);
        // Predicates from RegExpEvaluators
        predicates.addAll(TokenPredicate.getMagicTokenPredicates());
        predicates.addAll(TokenPredicate.getTriggerTokenPredicates());


        predicates.add(TokenPredicate.COMPANYSUFFIX);
        predicates.add(TokenPredicate.ORGNR);
        predicates.add(TokenPredicate.SITEPREFIX);

        // Add all FastTokenPredicates
        predicates.addAll(TokenPredicate.getFastTokenPredicates());
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    /**
     * Creator method for WordClauseImpl objects. By avoiding the constructors,
     * and assuming all WordClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the WordClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     *
     * @param term the term this clause represents.
     * @param field any field this clause was specified against.
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a WordClauseImpl instance matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static WordClauseImpl createWordClause(
            final String term,
            final String field,
            final TokenEvaluationEngine predicate2evaluatorFactory) {

        // the weakCache to use.
        Map<String,WeakReference<WordClauseImpl>> weakCache = WEAK_CACHE.get(predicate2evaluatorFactory.getSite());
        if(weakCache == null ){
            
            weakCache = new ConcurrentHashMap<String,WeakReference<WordClauseImpl>>(
                    WEAK_CACHE_INITIAL_CAPACITY, 
                    WEAK_CACHE_LOAD_FACTOR,
                    WEAK_CACHE_CONCURRENCY_LEVEL);
            
            WEAK_CACHE.put(predicate2evaluatorFactory.getSite(),weakCache);
        }

        // use helper method from AbstractLeafClause
        return createClause(
                WordClauseImpl.class,
                term,
                field,
                predicate2evaluatorFactory,
                PREDICATES_APPLICABLE, weakCache);
    }

    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param field the field for this clause. <b>May be <code>null</code></b>.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected WordClauseImpl(
            final String term,
            final String field,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, field, knownPredicates, possiblePredicates);

    }


}