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

package org.eclipse.rap.rwt.internal.widgets.sashkit;

import java.io.IOException;
import junit.framework.TestCase;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.RWTFixture;
import org.eclipse.rap.rwt.events.SelectionEvent;
import org.eclipse.rap.rwt.events.SelectionListener;
import org.eclipse.rap.rwt.graphics.Rectangle;
import org.eclipse.rap.rwt.internal.widgets.IWidgetAdapter;
import org.eclipse.rap.rwt.internal.widgets.Props;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.widgets.*;
import com.w4t.Fixture;

public class SashLCA_Test extends TestCase {

  public void testPreserveValues() {
    Display display = new Display();
    Composite shell = new Shell( display , RWT.NONE );
    Sash sash = new Sash( shell, RWT.NONE );
    RWTFixture.preserveWidgets();
    IWidgetAdapter adapter = WidgetUtil.getAdapter( sash );
    Object[] listeners;
    listeners = ( Object[] )adapter.getPreserved( Props.SELECTION_LISTENERS );
    assertEquals( 0, listeners.length );
    SelectionListener listener = new SelectionListener() {

      public void widgetSelected( SelectionEvent event ) {
      }
    };
    sash.addSelectionListener( listener );
    RWTFixture.preserveWidgets();
    listeners = ( Object[] )adapter.getPreserved( Props.SELECTION_LISTENERS );
    assertEquals( 1, listeners.length );
    assertEquals( listener, listeners[ 0 ] );
    display.dispose();
  }

  public void testRenderChanges() throws IOException {
    Fixture.fakeResponseWriter();
    Display display = new Display();
    Composite shell = new Shell( display , RWT.NONE );
    Sash sash = new Sash( shell, RWT.NONE );
    RWTFixture.markInitialized( sash );
    RWTFixture.preserveWidgets();
    sash.setBounds( new Rectangle( 20, 100, 50, 60 ) );
    SashLCA sashLCA = new SashLCA();
    sashLCA.renderChanges( sash );
    assertTrue( Fixture.getAllMarkup().endsWith( "setSplitterSize( 50 );" ) );
    RWTFixture.clearPreserved();
    Fixture.fakeResponseWriter();
    RWTFixture.preserveWidgets();
    sashLCA.renderChanges( sash );
    assertEquals( "", Fixture.getAllMarkup() );
  }

  protected void setUp() throws Exception {
    RWTFixture.setUp();
  }

  protected void tearDown() throws Exception {
    RWTFixture.tearDown();
  }
}
