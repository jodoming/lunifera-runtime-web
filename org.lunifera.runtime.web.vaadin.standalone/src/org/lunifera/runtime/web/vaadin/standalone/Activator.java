/*******************************************************************************
 * Copyright (c) 2012 by committers of lunifera.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Information:
 * 		Based on original sources of 
 * 				- org.vaadin.osgi.Activator from Chris Brind
 *				- com.c4biz.osgiutils.vaadin.equinox.shiro.Activator from Cristiano Gaviao
 *
 * Contributors:
 *    Florian Pirchner - migrated to vaadin 7 and copied into org.lunifera namespace
 *    
 *******************************************************************************/
package org.lunifera.runtime.web.vaadin.standalone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.equinox.http.jetty.JettyConstants;
import org.lunifera.runtime.web.vaadin.standalone.common.Constants;
import org.lunifera.runtime.web.vaadin.standalone.common.IVaadinWebApplication;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * Activator for this bundle.
 * 
 * <p/>
 * In order to turn production mode on a configuration must be provided for the
 * application. The PID to use is
 * <code>lunifera.web.vaadin.config.<em>alias</em></code>. See also
 * {@link Constants#PROP_MANAGED_SERVICE_PREFIX}
 * 
 * <p/>
 * An easy way to provide this configuration is to use FileInstall and create a
 * file of the same name as the PID but with the extension .cfg. e.g.
 * <code>lunifera.web.vaadin.config/guessit</code> would require a file called
 * <code>lunifera.web.vaadin.config.guessit.cfg</code>. The contents of this
 * file would contain the property <code>productionMode=true</code> and any
 * other parameters that would normally passed to the Vaadin servlet as init
 * parameters.
 * 
 * brindy (with help from Neil Bartlett) <br>
 * cvgaviao - Integration with Shiro Security Framework.<br>
 * florian pirchner - migration to vaadin 7
 */
public class Activator implements BundleActivator {

	private LogService logService;
	private Bundle jettyBundle;
	private Bundle vaadinBundle;

	private static BundleContext bundleContext;

	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	protected void bindLogService(BundleContext context) {
		ServiceReference<LogService> ref = context
				.getServiceReference(LogService.class);
		logService = context.getService(ref);

		logService.log(LogService.LOG_DEBUG, "Binded LogService.");
	}

	/**
	 * Returns the vaadin web application with the given name.
	 * 
	 * @param name
	 * @return
	 */
	protected static Collection<IVaadinWebApplication> getVaadinWebApplications() {
		List<IVaadinWebApplication> services = new ArrayList<IVaadinWebApplication>();
		try {
			for (ServiceReference<IVaadinWebApplication> reference : bundleContext
					.getServiceReferences(IVaadinWebApplication.class, null)) {
				services.add(bundleContext.getService(reference));
			}
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
		return services;
	}

	/**
	 * Returns the vaadin web application with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public static IVaadinWebApplication getVaadinWebApplication(String name) {
		try {
			Collection<ServiceReference<IVaadinWebApplication>> refs = bundleContext
					.getServiceReferences(IVaadinWebApplication.class,
							"(component.name=" + name + ")");
			if (refs.size() > 0) {
				ServiceReference<IVaadinWebApplication> ref = refs.iterator()
						.next();
				return bundleContext.getService(ref);
			}
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	protected LogService getLogService() {
		return logService;
	}

	@Override
	public void start(BundleContext context) throws Exception {

		bundleContext = context;

		// bind the log service
		bindLogService(context);

		// start the jetty with data from CM
		startJetty(context);

		// start the Vaadin bundle
		startVaadin(context);

	}

	private void startJetty(BundleContext context) {

		jettyBundle = FrameworkUtil.getBundle(JettyConstants.class);
		if (jettyBundle == null) {
			getLogService()
					.log(LogService.LOG_ERROR,
							"Bundle org.eclipse.equinox.http.jetty is not in target platform");
		}

	}

	private void startVaadin(BundleContext context) {

	}

	@Override
	public void stop(BundleContext context) throws Exception {

		vaadinBundle.stop();

		jettyBundle.stop();

		vaadinBundle = null;
		jettyBundle = null;
		logService = null;
	}

}
