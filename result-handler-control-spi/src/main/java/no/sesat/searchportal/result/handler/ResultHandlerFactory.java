/*
 * Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * ResultHandlerFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.sesat.searchportal.result.handler;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.config.SiteClassLoaderFactory;
import no.sesat.searchportal.site.config.Spi;

/** Obtain a working ResultHandler from a given ResultHandlerConfig.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class ResultHandlerFactory {

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of QueryTransformerFactory */
    private ResultHandlerFactory() {
    }

    // Public --------------------------------------------------------


    /**
     *
     * @param config
     * @return
     */
    public static ResultHandler getController(
            final ResultHandler.Context context,
            final ResultHandlerConfig config){

        final String name = "no.sesat.searchportal.result.handler."
                + config.getClass().getAnnotation(Controller.class).value();

        try{

            final SiteClassLoaderFactory.Context ctlContext = ContextWrapper.wrap(
                    SiteClassLoaderFactory.Context.class,
                    new BaseContext() {
                        public Spi getSpi() {
                            return Spi.RESULT_HANDLER_CONTROL;
                        }
                    },
                    context
                );

            final ClassLoader ctlLoader = SiteClassLoaderFactory.valueOf(ctlContext).getClassLoader();

            @SuppressWarnings("unchecked")
            final Class<? extends ResultHandler> cls = (Class<? extends ResultHandler>)ctlLoader.loadClass(name);

            final Constructor<? extends ResultHandler> constructor = cls.getConstructor(ResultHandlerConfig.class);

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