<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
        xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:search="urn:jsptld:/WEB-INF/SearchPortal.tld"><!-- XXX a little awkward since SearchPortal.tld never exists in the skin --><jsp:output
        doctype-root-element="html"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" />
 <jsp:directive.page contentType="text/html" />
 
 <!--
 * Copyright (2012) Schibsted ASA
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
    Document   : index
    Author     : mick
    Version    : $Id$
-->

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <search:include include="head-element"/>
</head>
<body>
    <table id="frame"><tr><td><!-- use a table instead of a div here to get automatic width plus centering. -->
        <div id="header">
            <search:include include="top-col-one" />
        </div>

        <br/><hr/>

        <div id="footer">
            <search:include include="bottom-col-four" />
        </div>
    </td></tr></table>

</body>
</html>
</jsp:root>