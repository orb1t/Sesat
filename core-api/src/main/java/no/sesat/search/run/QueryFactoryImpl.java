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

 */
package no.sesat.search.run;


import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * QueryFactoryImpl is part of no.sesat.search.query.
 * Use this class to create an instance of a RunningQuery.
 *
 * <p/>
 * TODO Instantiate a RunningQuery specified in the mode. With default as RunningQueryImpl.
 *
 * @version $Id$
 */
public final class QueryFactoryImpl extends QueryFactory {

    private static final Logger LOG = Logger.getLogger(QueryFactoryImpl.class);

    /**
     * Create a new instance of running query. The implementation can
     * be RunningWebQuery for example.
     *
     * @param cxt
     * @param request with parameters populated with search params
     * @return instance of RunningQuery
     * @throws SiteKeyedFactoryInstantiationException
     */
    @Override
    public RunningQuery createQuery(
            final RunningQuery.Context cxt,
            final HttpServletRequest request,
            final HttpServletResponse response) throws SiteKeyedFactoryInstantiationException {

        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);
        final ParametersDataObject parametersDO = datamodel.getParameters();

        final String qParam = null != parametersDO.getValue("q") ? parametersDO.getValue("q").getString() : "";

        final RunningQueryImpl query = new RunningWebQuery(cxt, qParam, request, response);

        return query;
    }
}
