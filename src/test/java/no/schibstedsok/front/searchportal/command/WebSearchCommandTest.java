// Copyright (2006) Schibsted Søk AS
/*
 * WebSearchCommandTest.java
 * JUnit based test
 *
 * Created on March 7, 2006, 4:53 PM
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.*;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.SearchModeFactory;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.query.run.RunningQueryImpl;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.view.config.SearchTab;
import no.schibstedsok.front.searchportal.view.config.SearchTabFactory;

/**
 *
 * @author magnuse
 */
public class WebSearchCommandTest extends TestCase {

    public WebSearchCommandTest(final String testName) {
        super(testName);
    }

    /**
     * Test of the site prefix.
     */
    public void testSiteFilter() {
        executeTestOfQuery(
                "site:zmag.org bil",
                "bil",
                "+site:zmag.org");
    }

    /**
     * Test of the site prefix whith quotes.
     */
    public void testSiteFilterWithQuotes() {
        executeTestOfQuery(
                "site:\"zmag.org\" bil",
                "bil",
                "+site:zmag.org");
    }

    /**
     *
     * Make sure that that phrase searches works.
     */
    public void testPhraseSearches() {
        executeTestOfQuery(
                "\"george bush\"",
                "\"george bush\"",
                "");
    }

    /** Test that the nyhetskilde prefix is ignored.
     */
    public void testIgnoreField() {
        executeTestOfQuery(
                "nyhetskilde:vg bil",
                "bil",
                "");
    }

    public void testExclusion() {
        executeTestOfQuery("magnus -eklund",
                "magnus -eklund",
                "");
        executeTestOfQuery("-whatever",
                "-whatever",
                "");
    }


    public void testTwoTerms() {
        executeTestOfQuery("magnus eklund",
                "magnus eklund",
                "");
    }

    /**
     *
     *
     */
    public void testSiteExclusion() {
//        executeTestOfQuery(
//                "-site:zmag.org bil",
//                "bil",
//                "-site:zmag.org"
//                );
//
//        executeTestOfQuery(
//                "NOT site:zmag.org bil",
//                "bil",
//                "-site:zmag.org"
//                );
    }

    /**
     *
     * Make sure phone numbers are not normalized.
     *
     */
    public void testPhoneNumberSearches() {
        executeTestOfQuery(
                "97 40 33 06",
                "97 40 33 06",
                "");
    }

    private void executeTestOfQuery(final String query, final String wantedQuery, final String wantedFilter) {

        final SearchCommand.Context cxt = createCommandContext(query);

        final WebSearchCommand cmd = new WebSearchCommand(cxt, Collections.EMPTY_MAP);

        final String generatedQuery = cmd.getQueryRepresentation(cxt.getQuery());

        assertEquals(wantedQuery, generatedQuery.trim());
        assertEquals(wantedFilter, cmd.getAdditionalFilter());
    }

    private SearchCommand.Context createCommandContext(final String query) {

        final FastConfiguration config = new FastConfiguration();

        final RunningQuery.Context rqCxt = new RunningQuery.Context() {
            private final SearchMode mode = new SearchMode();

            public SearchMode getSearchMode() {
                return SearchModeFactory.valueOf(
                        ContextWrapper.wrap(SearchModeFactory.Context.class, this))
                        .getMode("norsk-magic");
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.valueOf(
                    ContextWrapper.wrap(SearchTabFactory.Context.class, this))
                    .getTabByKey("d");
            }
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }

            public Site getSite() {
                return Site.DEFAULT;
            }
        };

        final RunningQuery rq = new RunningQueryImpl(rqCxt, query, new HashMap());

        final SearchCommand.Context searchCmdCxt = ContextWrapper.wrap(
                SearchCommand.Context.class,
                new BaseContext() {
                    public SearchConfiguration getSearchConfiguration() {
                        return rqCxt.getSearchMode().getSearchConfiguration("defaultSearch");
                    }

                    public RunningQuery getRunningQuery() {
                        return rq;
                    }
                    public Query getQuery(){
                        return rq.getQuery();
                    }
                },
                rqCxt);

        return searchCmdCxt;
    }
}
