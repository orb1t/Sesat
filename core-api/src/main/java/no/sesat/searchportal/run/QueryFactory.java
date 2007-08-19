/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.searchportal.run;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.sesat.searchportal.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;

/**
 * QueryFactory is part of no.sesat.searchportal.query.
 *
 * Use QueryFactory to create a new RunningQuery instance. The RunningQuery
 * should contain all information on how to search in fast.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola@schibstedsok.no</a>
 * @version $Id$
 */
public abstract class QueryFactory {

    private static final Logger LOG = Logger.getLogger(QueryFactory.class);
    private static QueryFactory instance;

    /**
     * Create a new instance of QueryFactory.
     * @return instance
     */
    public static QueryFactory getInstance() {
        if (instance == null) {

            if (LOG.isInfoEnabled()) {
                LOG.info("Creating new QueryFactory instance");
            }
            instance = new QueryFactoryImpl();
        }
        return instance;
    }

    /**
     * Create a new instance of running query. The implementation can
     * be RunningWebQuery, RunningAdvWebQuery for example.
     * @param mode with SearchConfiguration
     * @param request with parameters
     * @param response
     * @return instance of RunningQuery
     */
    public abstract RunningQuery createQuery(
                             RunningQuery.Context cxt,
                             HttpServletRequest request,
                             HttpServletResponse response) throws SiteKeyedFactoryInstantiationException;

}