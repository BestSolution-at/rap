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

package org.eclipse.rap.rwt.widgets;

import junit.framework.TestCase;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.RWTFixture;

public class TreeItem_Test extends TestCase {

  public void testConstructor() {
    Display display = new Display();
    Shell shell = new Shell( display , RWT.NONE );
    Tree tree = new Tree( shell, RWT.NONE );
    TreeItem item = new TreeItem( tree, RWT.NONE );
    assertSame( display, item.getDisplay() );
    assertEquals( "", item.getText() );
    try {
      new TreeItem( ( TreeItem )null, RWT.NONE );
      fail( "Must not allow null-parent" );
    } catch( NullPointerException e ) {
      // expected
    }
    try {
      new TreeItem( ( Tree )null, RWT.NONE );
      fail( "Must not allow null-parent" );
    } catch( NullPointerException e ) {
      // expected
    }
  }

  protected void setUp() throws Exception {
    RWTFixture.setUp();
  }

  protected void tearDown() throws Exception {
    RWTFixture.tearDown();
  }
}
