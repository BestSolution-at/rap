/*******************************************************************************
 * Copyright (c) 2002-2006 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.rap.rwt.internal.engine;

import com.w4t.AdapterFactory;

public final class TestAdapterFactory implements AdapterFactory {
  
  public static final String CREATED = "Created|";
  
  public static String log = "";
  
  public TestAdapterFactory() {
    log += CREATED;
  }
  
  public Object getAdapter( final Object adaptable, final Class adapter ) {
    return new Runnable(){
      public void run() {
      }
    };
  }
  
  public Class[] getAdapterList() {
    return new Class[] { Runnable.class };
  }
}