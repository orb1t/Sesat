/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
/*
 * AbstractImportVelocityTemplateTag.java
 *
 * Created on May 26, 2006, 3:17 PM
 */
package no.sesat.searchportal.view.taglib;


import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.util.OutputConverter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import no.sesat.searchportal.datamodel.DataModel;
import no.sesat.searchportal.http.filters.SiteLocatorFilter;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.config.SiteConfiguration;
import no.sesat.searchportal.site.config.TextMessages;
import no.sesat.searchportal.view.velocity.VelocityEngineFactory;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;


/** Base class to help with importing velocity templates.
 *
 * @author  <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */

public abstract class AbstractVelocityTemplateTag extends SimpleTagSupport {


    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractVelocityTemplateTag.class);
    private static final String ERR_MERGE_FAILURE = "Template merging failed";


    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------


    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     * @param templateName 
     * @param map 
     * @throws javax.servlet.jsp.JspException 
     */
    protected final void importTemplate(final String templateName, final Map<String,Object> map) throws JspException {

        final String missing = "Missing_" + templateName.replaceAll("/","") + "_Template";

        final PageContext cxt = (PageContext) getJspContext();
        cxt.removeAttribute(missing);
        final JspWriter out = cxt.getOut();

        try {

            final JspFragment f = getJspBody();
            if (f != null){
                f.invoke(out);
            }

            final DataModel datamodel = (DataModel) cxt.findAttribute(DataModel.KEY);

            final Site site = null != datamodel && null != datamodel.getSite()
                    ? datamodel.getSite().getSite()
                    // we haven't gone through the SiteLocatorFilter so get site manually
                    : SiteLocatorFilter.getSite(cxt.getRequest());

            assert null != site : "doTag() got null Site";

            final TextMessages text = null != cxt.findAttribute("text")
                    ? (TextMessages)cxt.findAttribute("text")
                    // we haven't gone through the SearchServlet so create TextMessages
                    : TextMessages.valueOf(site);

            assert null != text : "doTag() got null TextMessages";

            final VelocityEngine engine = VelocityEngineFactory.valueOf(site).getEngine();

            final Template template = VelocityEngineFactory.getTemplate(engine, site, templateName);

            final VelocityContext context = VelocityEngineFactory.newContextInstance(engine);

            // populate context with request and response // TODO remove, since all attributes are copied in
            context.put("request", cxt.getRequest());
            context.put("response", cxt.getResponse());
            // populate context with  search-portal attributes
            for(Map.Entry<String,Object> entry : map.entrySet()){
                context.put(entry.getKey(), entry.getValue());
            }
            context.put("datamodel", datamodel);
            context.put("text", text);
            
            // it's quite long to write $datamodel.site.siteConfiguration.properties so put this in for convenience
            context.put("configuration", null != datamodel && null != datamodel.getSite()
                    ? datamodel.getSite().getSiteConfiguration().getProperties()
                    // we haven't gone through the SiteLocatorFilter so get site manually
                    : SiteConfiguration.valueOf(site).getProperties());
            
            //context.put("channelCategories", Channel.Category.values());

            // push all parameters into velocity context attributes
            for (Enumeration<String> e = (Enumeration<String>)cxt.getRequest().getAttributeNames()
                    ; e.hasMoreElements();) {

                final String attrName = e.nextElement();
                /* do not overwrite parameters already in the velocity context */
                if (!context.containsKey(attrName)) {
                    context.put(attrName, cxt.getRequest().getAttribute(attrName));
                }
            }

            // populate sitemesh attributes -- will be removed
            final Page siteMeshPage = (Page) cxt.findAttribute(RequestConstants.PAGE);
                if(siteMeshPage != null){
                context.put("page", siteMeshPage);
                context.put("title", OutputConverter.convert(siteMeshPage.getTitle()));
                {
                    final StringWriter buffer = new StringWriter();
                    siteMeshPage.writeBody(OutputConverter.getWriter(buffer));
                    context.put("body", buffer.toString());
                }
                //{ // missing from our version of sitemesh
                //    final StringWriter buffer = new StringWriter();
                //    siteMeshPage.writeHead(OutputConverter.getWriter(buffer));
                //    context.put("head", buffer.toString());
                //}
            }

            // merge it into the JspWriter
            template.merge(context, out);


        } catch (ResourceNotFoundException ex) {
            // often normal usage to 'explore' for templates
            LOG.debug(ex.getMessage());
            cxt.setAttribute(missing, Boolean.TRUE);

        } catch (Exception ex) {
            LOG.error(ERR_MERGE_FAILURE, ex);
            throw new JspException(ex);
        }

    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}