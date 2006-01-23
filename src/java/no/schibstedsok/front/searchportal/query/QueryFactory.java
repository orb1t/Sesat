// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * QueryFactory is part of no.schibstedsok.front.searchportal.query.
 *
 * Use QueryFactory to create a new RunningQuery instance. The RunningQuery
 * should contain all information on how to search in fast.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola@schibstedsok.no</a>
 * @version $Revision$, $Author$, $Date$
 */
public abstract class QueryFactory {

    private static final Log LOG = LogFactory.getLog(QueryFactory.class);
    private static QueryFactory instance;

    /**
     * Create a new instance of QueryFactory
     * @return instance
     */
    public static QueryFactory getInstance() {
        if (instance == null) {

            if (LOG.isInfoEnabled()) {
                LOG.info("getInstance(): Creating new QueryFactory instance");
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
    public abstract RunningQuery createQuery(RunningQuery.Context cxt,
                             HttpServletRequest request,
                             HttpServletResponse response);

}
