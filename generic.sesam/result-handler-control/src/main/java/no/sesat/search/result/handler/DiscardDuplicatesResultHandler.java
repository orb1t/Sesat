/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
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
 */
package no.sesat.search.result.handler;

import java.util.ArrayList;
import java.util.Collection;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;

/** @see DiscardDuplicatesResultHandlerConfig
 *
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Id$</tt>
 */
public final class DiscardDuplicatesResultHandler implements ResultHandler {

    private final Collection<String> keys = new ArrayList<String>();

    private final DiscardDuplicatesResultHandlerConfig config;

    public DiscardDuplicatesResultHandler(final ResultHandlerConfig config) {
        this.config = (DiscardDuplicatesResultHandlerConfig) config;
    }

    public void handleResult(final Context cxt, final DataModel datamodel) {

        final Collection<ResultItem> toDelete = new ArrayList<ResultItem>();

        // scan for duplicates
        for (ResultItem searchResultItem : cxt.getSearchResult().getResults()) {

            String uniqueField = searchResultItem.getField(config.getField()) + "";	//avoid nullpointers

            if (config.isIgnoreCase()) {
                uniqueField = uniqueField.toLowerCase();
            }

            //remove entries with same name (not emtpy ones)
            if (uniqueField.length() > 0 && keys.contains(uniqueField)) {

                toDelete.add(searchResultItem);

            }else {
                keys.add(uniqueField);
            }
        }

        // now delete
        for(ResultItem item : toDelete){
            cxt.getSearchResult().removeResult(item);
        }
    }
}