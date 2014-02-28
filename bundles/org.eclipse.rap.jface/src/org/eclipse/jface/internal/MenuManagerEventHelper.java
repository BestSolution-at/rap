/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal;

import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.rap.rwt.SingletonUtil;

/**
 * @since 3.8.100
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
public class MenuManagerEventHelper {
	private IMenuListener2 showHelper;
	private IMenuListener2 hideHelper;

	public static MenuManagerEventHelper getInstance() {
		return SingletonUtil.getSessionInstance(MenuManagerEventHelper.class);
	}

	public void setShowHelper(IMenuListener2 showHelper) {
		this.showHelper = showHelper;
	}

	public void setHideHelper(IMenuListener2 hideHelper) {
		this.hideHelper = hideHelper;
	}

	public IMenuListener2 getShowHelper() {
		return showHelper;
	}

	public IMenuListener2 getHideHelper() {
		return hideHelper;
	}

	/**
	 * Do show pre-processing.
	 *
	 * @param manager
	 */
	public void showEventPreHelper(MenuManager manager) {
		if (showHelper != null) {
			showHelper.menuAboutToShow(manager);
		}
	}

	/**
	 * Do show post-processing.
	 *
	 * @param manager
	 */
	public void showEventPostHelper(MenuManager manager) {
		if (showHelper != null) {
			showHelper.menuAboutToHide(manager);
		}
	}

	/**
	 * Do hide pre-processing.
	 *
	 * @param manager
	 */
	public void hideEventPreHelper(MenuManager manager) {
		if (hideHelper != null) {
			hideHelper.menuAboutToShow(manager);
		}
	}

	/**
	 * Do hide post-processing.
	 *
	 * @param manager
	 */
	public void hideEventPostHelper(MenuManager manager) {
		if (hideHelper != null) {
			hideHelper.menuAboutToHide(manager);
		}
	}
}