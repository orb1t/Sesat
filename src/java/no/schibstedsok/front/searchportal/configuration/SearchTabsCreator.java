// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;

import java.util.Properties;
import no.schibstedsok.front.searchportal.configuration.loaders.ResourceContext;
import no.schibstedsok.front.searchportal.site.SiteContext;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchTabsCreator {
    /** The context any SearchTabsCreator must work against. **/
    public interface Context extends ResourceContext, SiteContext {
    }

    SearchTabs getSearchTabs();
    Properties getProperties();
}
