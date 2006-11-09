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

package org.eclipse.rap.rwt.events;

import junit.framework.TestCase;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.RWTFixture;
import org.eclipse.rap.rwt.widgets.*;


public class DisposeEvent_Test extends TestCase {
  
  private static final String WIDGET_DISPOSED = "widgetDiposed|";
  
  private String log = "";
  
  protected void setUp() throws Exception {
    RWTFixture.setUp();
  }
  
  protected void tearDown() throws Exception {
    RWTFixture.tearDown();
  }
  
  public void testAddRemoveListener() {
    DisposeListener listener = new DisposeListener() {
      public void widgetDisposed( final DisposeEvent event ) {
        log += WIDGET_DISPOSED;
      }
    };
    Display display = new Display();
    Composite shell = new Shell( display , RWT.NONE );
    shell.addDisposeListener( listener );
    
    DisposeEvent event = new DisposeEvent( shell ); 
    event.processEvent();
    assertEquals( WIDGET_DISPOSED, log );
    
    log = "";
    shell.removeDisposeListener( listener );
    event = new DisposeEvent( shell ); 
    event.processEvent();
    assertEquals( "", log );
  }
}
