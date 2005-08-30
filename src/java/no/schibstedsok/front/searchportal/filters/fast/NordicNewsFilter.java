/*
 * Copyright (2005) Schibsted S�k AS
 * 
 */
package no.schibstedsok.front.searchportal.filters.fast;

import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.filters.AsynchronusBaseFilter;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class NordicNewsFilter extends AsynchronusBaseFilter {

    protected FilterConfig filterConfig = null;

    public void doExecuteAsynch(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        filterConfig.getServletContext().log("- Fast Nordic - Searching");

        FastSearchConfiguration configuration = new FastSearchConfigurationImpl();
        configuration = (FastSearchConfiguration)setUpSearchConfiguration(request, configuration);
        configuration.setCollection(SearchConstants.NORDIC_NEWS_COLLECTION);
        configuration.setTemplate(VelocityTemplates.NORDIC_NEWS_COLLECTION_SEARCH);

        // start this search in separate thread
        doSearch(response, configuration, request);


        // go to next filter in chain
        chain.doFilter(request, response);

    }

    /**
     *
     * @param response
     * @param configuration
     * @param request
     */
    private void doSearch(ServletResponse response, SearchConfiguration configuration, ServletRequest request) {
        final SearchConsumer w = new FastSearchConsumer(response,configuration);
        startThread(w, request);
    }


    public void init(FilterConfig filterConfig) {
        super.init(filterConfig);
        this.filterConfig = filterConfig;
    }


}
