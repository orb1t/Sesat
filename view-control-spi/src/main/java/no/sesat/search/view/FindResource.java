/* Copyright (2007-2012) Schibsted ASA
 *   This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * FindResource.java
 *
 * Created on 16/09/2007, 09:37:35
 *
 */

package no.sesat.search.view;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import no.sesat.search.site.Site;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.UrlResourceLoader;
import org.apache.log4j.Logger;

/** http://sesat.no/scarab/issues/id/SKER3452
 * Move 302 resource redirects to internal resource directives
 *
 *
 * @version $Id$
 */
public final class FindResource {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FindResource.class);

    private static final String HTTP = "http://";
    private static final String PUBLISH_DIR = "/img/";

    public static final long START_TIME = System.currentTimeMillis();

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    /** Find the real location, in URL format, of the resource for the given site.
     *
     * Cannot be used in tests as is hardcoded to work with the UrlResourceLoader.
     *
     * @param site the site
     * @param resource the resource
     * @return a string in URL format where the resource can be found, or null if not found.
     * @throws IOException exception when pinging to find real location
     */
    public static String find(final Site site, final String resource) throws IOException{

        // This URL does not belong to search-portal
        final String url;

        if (resource.startsWith(PUBLISH_DIR)) { // publishing system
            // the publishing system is responsible for this.
            final Properties props = SiteConfiguration.instanceOf(site).getProperties();
            url = props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_URL)
                + '/' + resource;

        }  else  {
            // strip the version number out of the resource
            final String noVersionRsc = resource.replaceFirst("/(\\d)+/","/");

            // Find resource in current site or any of its
            // ancestors
            url = recursivelyFindResource(noVersionRsc, site);

        }

        return url;
    }

    // Constructors --------------------------------------------------

    private FindResource() {
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private static String recursivelyFindResource(final String resource, final Site site) throws IOException {

        // Problem with this approach is that skins can be updated without the server restarting (& updating START_TIME)
        // TODO an alternative approach would be to collect the lastModified timestamp of the resource and use it.
        final String datedResource = resource
                .replaceAll("/", "/" + START_TIME + "/")
                .replaceFirst("/" + START_TIME + "/", "");

        final String url = HTTP + site.getName() + site.getConfigContext() + '/' + datedResource;

        final URL u = new URL(url);

        if (UrlResourceLoader.doesUrlExist(u)) {
            // return a relative url to ensure it can survice through an out-of-cluster server.
            return '/' + site.getConfigContext() + '/' + datedResource;

        } else if (site.getParent() != null) {
            return recursivelyFindResource(resource, site.getParent());

        } else {
            return null;
        }
    }

    // Inner classes -------------------------------------------------
}
