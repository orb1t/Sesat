// Copyright (2006) Schibsted Søk AS
/*
 * FileResourcesSearchTabsCreatorTest.java
 * JUnit based test
 *
 * Created on 22 January 2006, 16:05
 */

package no.schibstedsok.front.searchportal.configuration;


import java.util.Properties;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.site.Site;

/** Tests using SearchTabsCreator against URL-based configuration files.
 * Only to be run when an application server is up and running.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class UrlResourcesSearchTabsCreatorTomcatest extends TestCase {

    private static final String FAIL_CONFIG_NOT_RUNNING =
            "\n\n"
            + "Unable to obtain configuration resources from search-front-config. \n"
            + "Please start this service before trying to build/deploy search-front-html."
            + "\n";


    /**
     * Test of valueOf method, of class no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator.
     */
    public void testDefaultSite() {

        final Site site = Site.DEFAULT;

        final SearchTabsCreator result = XMLSearchTabsCreator.valueOf(site);
        assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
    }

    /**
     * Test of getSearchTabs method, of class no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator.
     */
    public void testDefaultSiteGetSearchTabs() {

        final SearchTabsCreator instance = XMLSearchTabsCreator.valueOf(Site.DEFAULT);

        final SearchTabs result = instance.getSearchTabs();
        assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
    }

    /**
     * Test of getProperties method, of class no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator.
     */
    public void testDefaultSiteGetProperties() {

        final SearchTabsCreator instance = XMLSearchTabsCreator.valueOf(Site.DEFAULT);

        final Properties result = instance.getProperties();
        assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
    }

}
