/* Copyright (2012) Schibsted ASA
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
package no.sesat.search.mode.command;


/**
 * Command producing queries in advanced query syntax for Fast FDS4.
 * @deprecated use instead the Fast4SearchCommand with a infix-query-builder configured.
 * An example is set up and ready to use.
 * see generic.sesam/war/src/main/conf/modes.xml id="default-fast4-command-advanced-query"
 */
public abstract class AbstractAdvancedFastSearchCommand extends AbstractFast4SearchCommand {

    /**
     * Creates new advanced commmand.
     *
     * @param cxt        The context.
     */
    public AbstractAdvancedFastSearchCommand(final Context cxt) {

        super(cxt);
    }

    @Override
    protected synchronized String getQueryRepresentation() {

        final String query = super.getQueryRepresentation();

        return query.trim().startsWith("ANDNOT ")
                ? '#' + query
                : query;
    }

    // protected ----------------------------------------------



}
