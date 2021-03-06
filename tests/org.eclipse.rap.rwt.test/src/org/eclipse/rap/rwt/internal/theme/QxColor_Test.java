/*******************************************************************************
 * Copyright (c) 2007, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.graphics.Color;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class QxColor_Test {

  @Before
  public void setUp() {
    Fixture.setUp();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testIllegalArguments() {
    try {
      QxColor.valueOf( "#0000" );
      fail( "Exception expected" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
    try {
      QxColor.valueOf( "#xyz" );
      fail( "Exception expected" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
    try {
      QxColor.valueOf( "grey" );
      fail( "Exception expected" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
  }

  @Test
  public void testIllegalArguments_OutOfRangeAlpha() {
    try {
      QxColor.valueOf( "1, 2, 3, 1.01" );
      fail( "Exception expected" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
  }

  @Test
  public void testIllegalArguments_NegativeAlpha() {
    try {
      QxColor.valueOf( "1, 2, 3, -0.01" );
      fail( "Exception expected" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
  }

  @Test
  public void test6HexNotation() {
    QxColor color1 = QxColor.valueOf( "#0023ff" );
    assertEquals( 0, color1.red );
    assertEquals( 35, color1.green );
    assertEquals( 255, color1.blue );
    assertEquals( 1f, color1.alpha, 0 );
    QxColor color2 = QxColor.valueOf( "#efeFEF" );
    assertEquals( 239, color2.red );
    assertEquals( 239, color2.green );
    assertEquals( 239, color2.blue );
  }

  @Test
  public void test3HexNotation() {
    QxColor color1 = QxColor.valueOf( "#03f" );
    assertEquals( 0, color1.red );
    assertEquals( 51, color1.green );
    assertEquals( 255, color1.blue );
    assertEquals( 1f, color1.alpha, 0 );
    QxColor color2 = QxColor.valueOf( "#ccc" );
    assertEquals( 204, color2.red );
    assertEquals( 204, color2.green );
    assertEquals( 204, color2.blue );
    assertEquals( QxColor.valueOf( "#ffffff"), QxColor.valueOf( "#fff" ) );
  }

  @Test
  public void testNamedColors() {
    QxColor color1 = QxColor.valueOf( "red" );
    assertEquals( 255, color1.red );
    assertEquals( 0, color1.green );
    assertEquals( 0, color1.blue );
    assertEquals( 1f, color1.alpha, 0 );
    QxColor color2 = QxColor.valueOf( "blue" );
    assertEquals( 0, color2.red );
    assertEquals( 0, color2.green );
    assertEquals( 255, color2.blue );
  }

  @Test
  public void testCommaSeparatedValues() {
    QxColor color = QxColor.valueOf( "100, 23, 42" );
    assertEquals( 100, color.red );
    assertEquals( 23, color.green );
    assertEquals( 42, color.blue );
    assertEquals( 1f, color.alpha, 0 );
  }

  @Test
  public void testCommaSeparatedValues_WithAlpha() {
    QxColor color = QxColor.valueOf( "100, 23, 42, 0.5" );
    assertEquals( 100, color.red );
    assertEquals( 23, color.green );
    assertEquals( 42, color.blue );
    assertEquals( 0.5, color.alpha, 0 );
  }

  @Test
  public void testTransparent() {
    assertTrue( QxColor.TRANSPARENT.isTransparent() );
  }

  @Test
  public void testShared() {
    assertSame( QxColor.WHITE, QxColor.valueOf( "white" ) );
    assertSame( QxColor.WHITE, QxColor.valueOf( "255, 255, 255" ) );
    assertSame( QxColor.WHITE, QxColor.valueOf( "#ffffff" ) );
    assertSame( QxColor.BLACK, QxColor.valueOf( "Black" ) );
    assertSame( QxColor.BLACK, QxColor.valueOf( "0, 0, 0" ) );
    assertSame( QxColor.BLACK, QxColor.valueOf( "#000" ) );
    assertSame( QxColor.TRANSPARENT, QxColor.valueOf( "transparent" ) );
  }

  @Test
  public void testToString() {
    QxColor color = QxColor.valueOf( "100, 23, 42" );
    assertEquals( "QxColor{ 100, 23, 42, 1.0 }", color.toString() );
  }

  @Test
  public void testToString_WithAlpha() {
    QxColor color = QxColor.valueOf( "100, 23, 42, 0.5" );
    assertEquals( "QxColor{ 100, 23, 42, 0.5 }", color.toString() );
  }

  @Test
  public void testDefaultString() {
    QxColor color = QxColor.valueOf( "100, 23, 42" );
    assertEquals( "#64172a", color.toDefaultString() );
  }

  @Test
  public void testDefaultString_Transparent() {
    QxColor color = QxColor.valueOf( "100, 23, 42, 0" );
    assertEquals( "transparent", color.toDefaultString() );
  }

  @Test
  public void testDefaultString_WithAlpha() {
    QxColor color = QxColor.valueOf( "100, 23, 42, 0.5" );
    assertEquals( "rgba(100,23,42,0.5)", color.toDefaultString() );
  }

  @Test
  public void testWithTurkishLocale() {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault( new Locale( "tr", "TR" ) );
      assertSame( QxColor.WHITE, QxColor.valueOf( "WHITE" ) );
    } finally {
      Locale.setDefault( originalLocale );
    }
  }

  @Test
  public void testCreateColor_WithoutAlpha() {
    QxColor color = QxColor.valueOf( "100, 23, 42" );
    Color result = QxColor.createColor( color );
    assertNotNull( result );
    assertEquals( 100, result.getRed() );
    assertEquals( 23, result.getGreen() );
    assertEquals( 42, result.getBlue() );
  }

  @Test
  public void testCreateColor_WithAlpha() {
    QxColor color = QxColor.valueOf( "100, 23, 42, 0.5" );
    Color result = QxColor.createColor( color );
    assertNotNull( result );
    assertEquals( 100, result.getRed() );
    assertEquals( 23, result.getGreen() );
    assertEquals( 42, result.getBlue() );
  }

  @Test
  public void testCreateColor_FullyTransparent() {
    QxColor color = QxColor.valueOf( "100, 23, 42, 0" );
    Color result = QxColor.createColor( color );
    assertNull( result );
  }

}
