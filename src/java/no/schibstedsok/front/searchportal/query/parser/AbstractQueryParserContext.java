/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQueryParserContext.java
 *
 * Created on 12 January 2006, 12:06
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.EmailClause;
import no.schibstedsok.front.searchportal.query.IntegerClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.OrganisationNumberClause;
import no.schibstedsok.front.searchportal.query.PhoneNumberClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.UrlClause;
import no.schibstedsok.front.searchportal.query.WordClause;
import no.schibstedsok.front.searchportal.query.XorClause;
import org.apache.log4j.Logger;

/** Default implementation of QueryParser.Context's createXxxClause methods.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQueryParserContext implements AbstractQueryParser.Context {

    private static final Logger LOG = Logger.getLogger(AbstractQueryParserContext.class);

    /** Creates a new instance of AbstractQueryParserContext.
     */
    public AbstractQueryParserContext() {
    }

    /** {@inheritDoc}
     */
    public final String getQueryString() {
        return getTokenEvaluatorFactory().getQueryString();
    }


    //// Operator creators
    /** {@inheritDoc}
     */
    public DefaultOperatorClause createDefaultOperatorClause(final Clause first, final Clause second){
        
        LOG.debug("createDefaultOperatorClause(" + first + "," + second + ")");
        return DefaultOperatorClauseImpl.createDefaultOperatorClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final AndClause createAndClause(
        final Clause first,
        final Clause second) {

        LOG.debug("createAndClause(" + first + "," + second + ")");
        return AndClauseImpl.createAndClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final OrClause createOrClause(
        final Clause first,
        final Clause second) {

        LOG.debug("createOrClause(" + first + "," + second + ")");
        return OrClauseImpl.createOrClause(first, second, getTokenEvaluatorFactory());
    }
    
    /** {@inheritDoc}
     */
    public final XorClause createXorClause(
        final Clause first,
        final Clause second) {

        LOG.debug("createXorClause(" + first + "," + second + ")");
        return XorClauseImpl.createXorClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final AndNotClause createAndNotClause(
        final Clause first) {

        LOG.debug("createOrClause(" + first + ")");
        return AndNotClauseImpl.createAndNotClause(first, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final NotClause createNotClause(
        final Clause first) {

        LOG.debug("createNotClause(" + first + ")");
        return NotClauseImpl.createNotClause(first, getTokenEvaluatorFactory());
    }


    //// Leaf creators

    /** {@inheritDoc}
     */
    public final WordClause createWordClause(
        final String term,
        final String field) {

        LOG.debug("createWordClause(" + term + "," + field + ")");
        return WordClauseImpl.createWordClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final PhraseClause createPhraseClause(
        final String term,
        final String field) {

        LOG.debug("createPhraseClause(" + term + "," + field + ")");
        return PhraseClauseImpl.createPhraseClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final IntegerClause createIntegerClause(
        final String term,
        final String field) {

        LOG.debug("createIntegerClause(" + term + "," + field + ")");
        return IntegerClauseImpl.createIntegerClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final PhoneNumberClause createPhoneNumberClause(
        final String term,
        final String field) {

        LOG.debug("createPhoneNumberClause(" + term + "," + field + ")");
        return PhoneNumberClauseImpl.createPhoneNumberClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final OrganisationNumberClause createOrganisationNumberClause(
        final String term,
        final String field) {

        LOG.debug("createOrganisationNumberClause(" + term + "," + field + ")");
        return OrganisationNumberClauseImpl.createOrganisationNumberClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public final UrlClause createUrlClause(final String term, final String field){

        LOG.debug("createUrlClause(" + term + "," + field + ")");
        return UrlClauseImpl.createUrlClause(term, field, getTokenEvaluatorFactory());        
    }
    /** {@inheritDoc}
     */
    public final EmailClause createEmailClause(final String term, final String field){

        LOG.debug("createEmailClause(" + term + "," + field + ")");
        return EmailClauseImpl.createEmailClause(term, field, getTokenEvaluatorFactory());        
    }   
}
