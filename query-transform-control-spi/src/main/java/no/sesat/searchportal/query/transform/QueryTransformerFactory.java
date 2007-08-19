/*
 * Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * QueryTransformerFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.sesat.searchportal.query.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import no.sesat.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import no.sesat.searchportal.site.config.*;
import no.sesat.searchportal.site.SiteContext;
import no.sesat.searchportal.site.Site;

/** Obtain a working QueryTransformer from a given QueryTransformerConfig.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class QueryTransformerFactory {

    public interface Context extends SiteContext, BytecodeContext {
    }

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    private Context context;

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of QueryTransformerFactory */
    public QueryTransformerFactory(Context context) {
        this.context = context;
    }

    // Public --------------------------------------------------------

    /**
     *
     * @param config
     * @return
     */
    public QueryTransformer getController(final QueryTransformerConfig config){

        final String controllerName = "no.sesat.searchportal.query.transform."
                + config.getClass().getAnnotation(Controller.class).value();

        try{
            final SiteClassLoaderFactory.Context ctrlCxt = new SiteClassLoaderFactory.Context() {
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
                public Site getSite() {
                    return context.getSite();
                }
                public Spi getSpi() {
                    return Spi.QUERY_TRANSFORM_CONTROL;
                }
            };

            final ClassLoader ctrlClassLoader = SiteClassLoaderFactory.valueOf(ctrlCxt).getClassLoader();

            @SuppressWarnings("unchecked")
            final Class<? extends QueryTransformer> cls
                    = (Class<? extends QueryTransformer>)ctrlClassLoader.loadClass(controllerName);

            final Constructor<? extends QueryTransformer> constructor = cls.getConstructor(QueryTransformerConfig.class);

            return constructor.newInstance(config);

        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}