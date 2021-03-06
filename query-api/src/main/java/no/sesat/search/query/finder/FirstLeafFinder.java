/* Copyright (2007-2012) Schibsted ASA
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
package no.sesat.search.query.finder;

import no.sesat.commons.visitor.AbstractReflectionVisitor;
import java.io.Serializable;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.UnaryClause;
import no.sesat.search.query.parser.*;


public final class FirstLeafFinder extends AbstractReflectionVisitor implements Serializable {
    private boolean searching = true;
    private LeafClause firstLeaf;

    public synchronized LeafClause getFirstLeaf(final Clause root) {

        visit(root);
        return firstLeaf;
    }

    protected void visitImpl(final UnaryClause clause) {
        if (searching) { // still looking
            clause.getFirstClause().accept(this);
        }
    }

    protected void visitImpl(final NotClause clause) {
        // this cancels the search for a firstLeafClause...
        searching = false;
    }

    protected void visitImpl(final AndNotClause clause) {
        // this cancels the search for a firstLeafClause...
        searching = false;
    }

    protected void visitImpl(final LeafClause clause) {
        // Bingo! Goto "Go". Collect $200.
        firstLeaf = clause;
        searching = false;
    }

}