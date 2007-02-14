/*
 * QueryDataObject.java
 *
 * Created on 23 January 2007, 12:42
 *
 */

package no.schibstedsok.searchportal.datamodel.query;

import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import static no.schibstedsok.searchportal.datamodel.access.ControlLevel.*;
import no.schibstedsok.searchportal.datamodel.access.AccessAllow;
import no.schibstedsok.searchportal.datamodel.access.AccessDisallow;
import no.schibstedsok.searchportal.query.Query;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface QueryDataObject extends StringDataObject{

    @AccessDisallow(REQUEST_CONSTRUCTION)
    Query getQuery();

    @AccessAllow(RUNNING_QUERY_CONSTRUCTION)
    void setQuery(Query query);

    @AccessAllow({REQUEST_CONSTRUCTION, RUNNING_QUERY_CONSTRUCTION})
    String getString();

}