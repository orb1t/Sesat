// Copyright (2006) Schibsted Søk AS
/*
 * HittaSearchCommand.java
 *
 * Created on May 30, 2006, 3:27 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.rpc.ServiceException;
import no.schibstedsok.searchportal.mode.config.HittaSearchConfiguration;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.view.spell.QuerySuggestion;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;
import org.apache.log4j.Logger;
import se.hitta.www.HittaService.HittaServiceLocator;
import se.hitta.www.HittaService.HittaServiceSoap;

/** Search against the Swedish Hitta WebService. ✆
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class HittaSearchCommand extends AbstractWebServiceSearchCommand{


    // Constants -----------------------------------------------------

    private static final Collection<TokenPredicate> WHO_PREDICATES = Collections.unmodifiableCollection(
            Arrays.asList(
            TokenPredicate.COMPANYENRICHMENT,
            TokenPredicate.FIRSTNAME,
            TokenPredicate.LASTNAME
            ));

    private static final Logger LOG = Logger.getLogger(HittaSearchCommand.class);
    private static final String ERR_FAILED_HITTA_SEARCH = "Failed Hitta search command";
    private static final String DEBUG_CONF_NFO = "Conf details --> ";
    private static final String DEBUG_SEARCHING_1 = "Searching for who->";
    private static final String DEBUG_SEARCHING_2 = "Searching for where->";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of HittaSearchCommand.
     */
    public HittaSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);

    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    /** @inherit **/
    public SearchResult execute(){

        final HittaSearchConfiguration conf = (HittaSearchConfiguration) context.getSearchConfiguration();
        int hits = 0;

        LOG.debug(DEBUG_CONF_NFO + conf.getCatalog() + ' ' + conf.getKey());

        try {
            final HittaServiceLocator locator = new HittaServiceLocator();
            final HittaServiceSoap service = locator.getHittaServiceSoap();
            final SplitQueryTransformer splitter = new SplitQueryTransformer();

            if(getTransformedQuery().equals(context.getQuery().getQueryString())){


                final String[] splitQuery = splitter.getQuery();

                getParameters().put("hittaWho", splitQuery[0]);
                getParameters().put("hittaWhere", splitQuery[1]);

                LOG.debug(DEBUG_SEARCHING_1 + splitQuery[0]);
                LOG.debug(DEBUG_SEARCHING_2 + splitQuery[1]);

                if(conf.getCatalog().equalsIgnoreCase("white")){
                    hits = service.getWhiteAmount(splitQuery[0], splitQuery[1], conf.getKey());

                }else if(conf.getCatalog().equalsIgnoreCase("pink")){
                    hits = service.getPinkAmount(splitQuery[0], splitQuery[1], conf.getKey());

                }
            }

        } catch (ServiceException ex) {
            LOG.error(ERR_FAILED_HITTA_SEARCH, ex);
        } catch (RemoteException ex) {
            LOG.error(ERR_FAILED_HITTA_SEARCH, ex);
        }

        final SearchResult result = new WebServiceSearchResult(this);
        result.setHitCount(hits);


        return result;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------




    // Inner classes -------------------------------------------------

    private static final class WebServiceSearchResult extends BasicSearchResult{

        private static final String ERR_NOT_SUPPORTED = "Not part of this implementation";

        public WebServiceSearchResult(final SearchCommand command) {
            super(command);
        }

        public void addResult(final SearchResultItem item) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public void addSpellingSuggestion(final SpellingSuggestion suggestion) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public Map<String, List<SpellingSuggestion>> getSpellingSuggestions() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public List<QuerySuggestion> getQuerySuggestions() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public void addQuerySuggestion(final QuerySuggestion query) {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

        public List<SearchResultItem> getResults() {
            throw new UnsupportedOperationException(ERR_NOT_SUPPORTED);
        }

    }

    private final class SplitQueryTransformer extends AbstractReflectionVisitor{

        private StringBuilder who;
        private StringBuilder where;

        private boolean queryContainsFullnameOrCompany = false;
        private boolean tooManyFullnameOrCompany = false;
        private FullnameOrCompanyFinder fullnameOrCompanyFinder = new FullnameOrCompanyFinder();

        public String[] getQuery(){

            if(where == null){
                who = new StringBuilder();
                where = new StringBuilder();
                fullnameOrCompanyFinder.visit(context.getQuery().getRootClause());
                if(!tooManyFullnameOrCompany){
                        visit(context.getQuery().getRootClause());
                }
            }

            return new String[]{
                who.toString().trim(),
                where.toString().trim()
            };
        }

        protected void visitImpl(final LeafClause clause) {

            boolean onlyGeo = clause.getKnownPredicates().contains(TokenPredicate.GEOLOCAL);
            onlyGeo |= clause.getKnownPredicates().contains(TokenPredicate.GEOGLOBAL);
            onlyGeo &= clause.getField() == null;

            if(queryContainsFullnameOrCompany){

                // check if any possible parents of this clause match the fullname predicate.
                final Query query = context.getQuery();
                final List<OperationClause> parents
                        = query.getParentFinder().getParents(query.getRootClause(), clause);

                for(OperationClause oc : parents){
                    onlyGeo &= !oc.getKnownPredicates().contains(TokenPredicate.FULLNAME);
                }

                // and check for company
                onlyGeo &= !clause.getKnownPredicates().contains(TokenPredicate.COMPANYENRICHMENT);

            }else{
                // no fullname or company exists in the query, so firstname or lastname will do
                onlyGeo &= !clause.getKnownPredicates().contains(TokenPredicate.FIRSTNAME);
                onlyGeo &= !clause.getKnownPredicates().contains(TokenPredicate.LASTNAME);
            }

            if (onlyGeo) {
                // add this term to the geo query string
                where.append(getTransformedTerm(clause));
            }else{
                who.append(getTransformedTerm(clause));
            }
        }

        protected void visitImpl(final OperationClause clause) {
            clause.getFirstClause().accept(this);
        }

        protected void visitImpl(final DoubleOperatorClause clause) {
            clause.getFirstClause().accept(this);
            where.append(' ');
            who.append(' ');
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final NotClause clause) {
        }

        protected void visitImpl(final AndNotClause clause) {
        }

        protected void visitImpl(final XorClause clause) {
            clause.getFirstClause().accept(this);
        }

        private final class FullnameOrCompanyFinder extends AbstractReflectionVisitor{

            protected void visitImpl(final LeafClause clause) {

                final Set<TokenPredicate> predicates = clause.getKnownPredicates();

                tooManyFullnameOrCompany =queryContainsFullnameOrCompany && predicates.contains(TokenPredicate.COMPANYENRICHMENT);
                queryContainsFullnameOrCompany |= predicates.contains(TokenPredicate.COMPANYENRICHMENT);
            }

            protected void visitImpl(final OperationClause clause) {
                if(!tooManyFullnameOrCompany){
                    clause.getFirstClause().accept(this);
                }
            }

            protected void visitImpl(final DoubleOperatorClause clause) {
                if(!tooManyFullnameOrCompany){
                    clause.getFirstClause().accept(this);
                    clause.getSecondClause().accept(this);
                }
            }

            protected void visitImpl(final DefaultOperatorClause clause) {

                final Set<TokenPredicate> predicates = clause.getKnownPredicates();
                tooManyFullnameOrCompany =queryContainsFullnameOrCompany && predicates.contains(TokenPredicate.COMPANYENRICHMENT);
                queryContainsFullnameOrCompany |= predicates.contains(TokenPredicate.FULLNAME);
                if(!tooManyFullnameOrCompany){
                    clause.getFirstClause().accept(this);
                    clause.getSecondClause().accept(this);
                }

            }
            protected void visitImpl(final NotClause clause) {
            }

            protected void visitImpl(final AndNotClause clause) {
            }

            protected void visitImpl(final XorClause clause) {
                if(!tooManyFullnameOrCompany){
                    clause.getFirstClause().accept(this);
                }
            }
        }
    }

}