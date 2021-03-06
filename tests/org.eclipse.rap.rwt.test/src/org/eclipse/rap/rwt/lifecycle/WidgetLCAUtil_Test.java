/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.lifecycle;

import static org.eclipse.rap.rwt.lifecycle.WidgetLCAUtil.renderToolTip;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.registerDataKeys;
import static org.eclipse.rap.rwt.testfixture.Fixture.getProtocolMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerUtil;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CallOperation;
import org.eclipse.rap.rwt.testfixture.Message.SetOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.widgets.ControlUtil;
import org.eclipse.swt.internal.widgets.IControlAdapter;
import org.eclipse.swt.internal.widgets.IWidgetGraphicsAdapter;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class WidgetLCAUtil_Test {

  private Display display;
  private Shell shell;
  private Control widget;

  @Before
  public void setUp() {
    Fixture.setUp();
    Fixture.fakeResponseWriter();
    display = new Display();
    shell = new Shell( display , SWT.NONE );
    widget = new Button( shell, SWT.PUSH );
  }

  @After
  public void tearDown() {
    display.dispose();
    Fixture.tearDown();
  }

  @Test
  public void testHasChanged() {
    Text text = new Text( shell, SWT.NONE );
    // test initial behaviour, text is same as default value -> no 'change'
    text.setText( "" );
    boolean hasChanged;
    hasChanged = WidgetLCAUtil.hasChanged( text, Props.TEXT, text.getText(), "" );
    assertFalse( hasChanged );
    // test initial behaviour, text is different as default value -> 'change'
    text.setText( "other value" );
    hasChanged = WidgetLCAUtil.hasChanged( text, Props.TEXT, text.getText(), "" );
    assertTrue( hasChanged );
    // test subsequent behaviour (when already initialized)
    Fixture.markInitialized( display );
    Fixture.markInitialized( text );
    Fixture.clearPreserved();
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( text, Props.TEXT, text.getText(), "" );
    assertFalse( hasChanged );
    Fixture.clearPreserved();
    Fixture.preserveWidgets();
    text.setText( "whatsoevervaluehasbeensetduringrequest" );
    hasChanged = WidgetLCAUtil.hasChanged( text, Props.TEXT, text.getText(), "" );
    assertTrue( hasChanged );
  }

  @Test
  public void testHasChangedWidthArrays() {
    List list = new List( shell, SWT.MULTI );

    boolean hasChanged;
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", new String[] { "a" } );
    assertTrue( hasChanged );

    list.setItems( new String[] { "a" } );
    Fixture.markInitialized( display );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", new String[] { "a" } );
    assertFalse( hasChanged );

    list.setItems( new String[] { "a" } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", new String[] { "b" } );
    assertTrue( hasChanged );

    list.setItems( new String[] { "a" } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", new String[] { "a", "b" } );
    assertTrue( hasChanged );

    list.setItems( new String[] { "a" } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "items", null );
    assertTrue( hasChanged );

    list.setItems( new String[] { "a", "b", "c" } );
    list.setSelection( new int[] { 0, 1, 2 } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "selectionIndices", new int[] { 0, 1, 4 } );
    assertTrue( hasChanged );

    list.setItems( new String[] { "a", "b", "c" } );
    list.setSelection( new int[] { 0, 1, 2 } );
    Fixture.preserveWidgets();
    hasChanged = WidgetLCAUtil.hasChanged( list, "selectionIndices", new int[] { 0, 1, 2 } );
    assertFalse( hasChanged );
  }

  @Test
  public void testEquals() {
    assertTrue( WidgetLCAUtil.equals( null, null ) );
    assertFalse( WidgetLCAUtil.equals( null, "1" ) );
    assertFalse( WidgetLCAUtil.equals( "1", null ) );
    assertFalse( WidgetLCAUtil.equals( "1", "2" ) );
    assertTrue( WidgetLCAUtil.equals( "1", "1" ) );
    assertTrue( WidgetLCAUtil.equals( new String[] { "1" },
                                   new String[] { "1" } ) );
    assertTrue( WidgetLCAUtil.equals( new int[] { 1 },
                                   new int[] { 1 } ) );
    assertTrue( WidgetLCAUtil.equals( new boolean[] { true },
                                   new boolean[] { true } ) );
    assertTrue( WidgetLCAUtil.equals( new long[] { 232 },
                                   new long[] { 232 } ) );
    assertTrue( WidgetLCAUtil.equals( new float[] { 232 },
                                   new float[] { 232 } ) );
    assertTrue( WidgetLCAUtil.equals( new double[] { 345 },
                                   new double[] { 345 } ) );
    assertTrue( WidgetLCAUtil.equals( new Date[] { new Date( 1 ) },
                                   new Date[] { new Date( 1 ) } ) );
    assertFalse( WidgetLCAUtil.equals( new double[] { 345 },
                                    new float[] { 345 } ) );
    assertFalse( WidgetLCAUtil.equals( new int[] { 345 },
                                    new float[] { 345 } ) );
    assertFalse( WidgetLCAUtil.equals( new int[] { 345 },
                                    new long[] { 345 } ) );
    assertFalse( WidgetLCAUtil.equals( new Date[] { new Date( 3 ) }, null ) );
  }

  @Test
  public void testParseFontName() {
    // IE doesn't like quoted font names (or whatever qooxdoo makes out of them)
    String systemFontName
      = "\"Segoe UI\", Corbel, Calibri, Tahoma, \"Lucida Sans Unicode\", "
      + "sans-serif";
    String[] fontNames = ProtocolUtil.parseFontName( systemFontName );
    assertEquals( 6, fontNames.length );
    assertEquals( "Segoe UI", fontNames[ 0 ] );
    assertEquals( "Corbel", fontNames[ 1 ] );
    assertEquals( "Calibri", fontNames[ 2 ] );
    assertEquals( "Tahoma", fontNames[ 3 ] );
    assertEquals( "Lucida Sans Unicode", fontNames[ 4 ] );
    assertEquals( "sans-serif", fontNames[ 5 ] );

    // Empty font names don't cause trouble (at least for the browsers
    // currently tested - therefore don't make extra effort to eliminate them
    fontNames = ProtocolUtil.parseFontName( "a, , b" );
    assertEquals( 3, fontNames.length );
    assertEquals( "a", fontNames[ 0 ] );
    assertEquals( "", fontNames[ 1 ] );
    assertEquals( "b", fontNames[ 2 ] );
  }

  //////////////////////////////////////////////
  // Tests for new render methods using protocol

  @Test
  public void testRenderIntialBackgroundNull() {
    WidgetLCAUtil.renderBackground( widget, null );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "background" ) );
  }

  @Test
  public void testRenderBackground() {
    WidgetLCAUtil.renderBackground( widget, new Color( display, 0, 16, 255 ) );

    Message message = getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[0, 16, 255, 255]" );
    assertEquals( expected, message.findSetProperty( widget, "background" ) );
  }

  @Test
  public void testRenderBackgroundNull() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setBackground( new Color( display, 0, 16, 255 ) );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, null, false );

    Message message = getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( widget, "background" ) );
  }

  @Test
  public void testRenderBackgroundUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setBackground( new Color( display, 0, 16, 255 ) );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, new Color( display, 0, 16, 255 ) );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "background" ) );
  }

  @Test
  public void testRenderIntialBackgroundTransparent() {
    WidgetLCAUtil.renderBackground( widget, null, true );

    Message message = getProtocolMessage();

    JsonArray expected = JsonArray.readFrom( "[0, 0, 0, 0]" );
    assertEquals( expected, message.findSetProperty( widget, "background" ) );
  }

  @Test
  public void testRenderBackgroundTransparencyUnchanged() {
    widget = new Button( shell, SWT.CHECK );
    shell.setBackgroundMode( SWT.INHERIT_DEFAULT );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( widget );
    assertTrue( controlAdapter.getBackgroundTransparency() );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, null, true );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "background" ) );
  }

  @Test
  public void testRenderBackgroundNoMoreTransparent() {
    widget = new Button( shell, SWT.CHECK );
    shell.setBackgroundMode( SWT.INHERIT_DEFAULT );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( widget );
    assertTrue( controlAdapter.getBackgroundTransparency() );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, new Color( display, 0, 16, 255 ), false );

    Message message = getProtocolMessage();

    JsonArray expected = JsonArray.readFrom( "[0, 16, 255, 255]" );
    assertEquals( expected, message.findSetProperty( widget, "background" ) );
  }

  @Test
  public void testRenderBackgroundReset() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setBackground( new Color( display, 0, 16, 255 ) );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderBackground( widget, null );

    Message message = getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( widget, "background" ) );
  }

  @Test
  public void testRenderIntialForeground() {
    ControlLCAUtil.renderForeground( widget );

    Message message = getProtocolMessage();

    assertNull( message.findSetOperation( widget, "foreground" ) );
  }

  @Test
  public void testRenderForeground() {
    widget.setForeground( new Color( display, 0, 16, 255 ) );
    ControlLCAUtil.renderForeground( widget );

    Message message = getProtocolMessage();

    JsonArray expected = JsonArray.readFrom( "[0, 16, 255, 255]" );
    assertEquals( expected, message.findSetProperty( widget, "foreground" ) );
  }

  @Test
  public void testRenderForegroundUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setForeground( new Color( display, 0, 16, 255 ) );

    Fixture.preserveWidgets();
    ControlLCAUtil.renderForeground( widget );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "foreground" ) );
  }

  @Test
  public void testRenderInitialCustomVariant() {
    WidgetLCAUtil.renderCustomVariant( widget );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "customVariant" ) );
  }

  @Test
  public void testRenderCustomVariant() {
    widget.setData( RWT.CUSTOM_VARIANT, "my_variant" );
    WidgetLCAUtil.renderCustomVariant( widget );

    Message message = getProtocolMessage();
    assertEquals( "variant_my_variant",
                  message.findSetProperty( widget, "customVariant" ).asString() );
  }

  @Test
  public void testRenderCustomVariantUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setData( RWT.CUSTOM_VARIANT, "my_variant" );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderCustomVariant( widget );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "customVariant" ) );
  }

  @Test
  public void testRenderInitialListenHelp() {
    WidgetLCAUtil.renderListenHelp( widget );

    Message message = getProtocolMessage();
    assertNull( message.findListenOperation( widget, "Help" ) );
  }

  @Test
  public void testRenderListenHelp() {
    widget.addHelpListener( mock( HelpListener.class ) );
    WidgetLCAUtil.renderListenHelp( widget );

    Message message = getProtocolMessage();
    assertEquals( JsonValue.TRUE, message.findListenProperty( widget, "Help" ) );
  }

  @Test
  public void testRenderListenHelpUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.addHelpListener( mock( HelpListener.class ) );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderListenHelp( widget );

    Message message = getProtocolMessage();
    assertNull( message.findListenOperation( widget, "Help" ) );
  }

  @Test
  public void testRenderListenHelpRemoved() {
    HelpListener listener = mock( HelpListener.class );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.addHelpListener( listener );
    Fixture.preserveWidgets();

    widget.removeHelpListener( listener );
    WidgetLCAUtil.renderListenHelp( widget );

    Message message = getProtocolMessage();
    assertEquals( JsonValue.FALSE, message.findListenProperty( widget, "Help" ) );
  }

  @Test
  public void testRenderBackgroundGradient() {
    Control control = new Composite( shell, SWT.NONE );
    Object adapter = control.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color[] gradientColors = new Color[] {
      new Color( display, 0, 255, 0 ),
      new Color( display, 0, 0, 255 )
    };
    int[] percents = new int[] { 0, 100 };

    gfxAdapter.setBackgroundGradient( gradientColors, percents, true );
    WidgetLCAUtil.renderBackgroundGradient( control );

    Message message = getProtocolMessage();
    JsonArray expected
      = JsonArray.readFrom( "[[[0, 255, 0, 255], [0, 0, 255, 255]], [0, 100], true]" );
    assertEquals( expected, message.findSetProperty( control, "backgroundGradient" ) );
  }

  @Test
  public void testRenderBackgroundGradientHorizontal() {
    Control control = new Composite( shell, SWT.NONE );
    Object adapter = control.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color[] gradientColors = new Color[] {
      new Color( display, 0, 255, 0 ),
      new Color( display, 0, 0, 255 )
    };
    int[] percents = new int[] { 0, 100 };

    gfxAdapter.setBackgroundGradient( gradientColors, percents, false );
    WidgetLCAUtil.renderBackgroundGradient( control );

    Message message = getProtocolMessage();
    JsonArray expected
      = JsonArray.readFrom( "[[[0, 255, 0, 255], [0, 0, 255, 255]], [0, 100], false]" );
    assertEquals( expected, message.findSetProperty( control, "backgroundGradient" ) );
  }

  @Test
  public void testRenderBackgroundGradientUnchanged() {
    Control control = new Composite( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( control );
    Object adapter = control.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color[] gradientColors = new Color[] {
      new Color( display, 0, 255, 0 ),
      new Color( display, 0, 0, 255 )
    };
    int[] percents = new int[] { 0, 100 };

    gfxAdapter.setBackgroundGradient( gradientColors, percents, true );
    WidgetLCAUtil.preserveBackgroundGradient( control );
    WidgetLCAUtil.renderBackgroundGradient( control );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( control, "backgroundGradient" ) );
  }

  @Test
  public void testResetBackgroundGradient() {
    Control control = new Composite( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( control );
    Object adapter = control.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter gfxAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color[] gradientColors = new Color[] {
      new Color( display, 0, 255, 0 ),
      new Color( display, 0, 0, 255 )
    };
    int[] percents = new int[] { 0, 100 };
    gfxAdapter.setBackgroundGradient( gradientColors, percents, true );
    WidgetLCAUtil.preserveBackgroundGradient( control );

    gfxAdapter.setBackgroundGradient( null, null, true );
    WidgetLCAUtil.renderBackgroundGradient( control );

    Message message = getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( control, "backgroundGradient" ) );
  }

  @Test
  public void testRenderRoundedBorder() {
    Widget widget = new Composite( shell, SWT.NONE );
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter graphicsAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color color = new Color( display, 0, 255, 0 );

    graphicsAdapter.setRoundedBorder( 2, color, 5, 6, 7, 8 );
    WidgetLCAUtil.renderRoundedBorder( widget );

    Message message = getProtocolMessage();
    JsonArray expected = JsonArray.readFrom( "[2,[0,255,0,255],5,6,7,8]" );
    assertEquals( expected, message.findSetProperty( widget, "roundedBorder" ) );
  }

  @Test
  public void testRenderRoundedBorderUnchanged() {
    Widget widget = new Composite( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter graphicsAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color color = new Color( display, 0, 255, 0 );
    graphicsAdapter.setRoundedBorder( 2, color, 5, 6, 7, 8 );

    WidgetLCAUtil.preserveRoundedBorder( widget );
    WidgetLCAUtil.renderRoundedBorder( widget );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "roundedBorder" ) );
  }

  @Test
  public void testResetRoundedBorder() {
    Widget widget = new Composite( shell, SWT.NONE );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    Object adapter = widget.getAdapter( IWidgetGraphicsAdapter.class );
    IWidgetGraphicsAdapter graphicsAdapter = ( IWidgetGraphicsAdapter )adapter;
    Color color = new Color( display, 0, 255, 0 );
    graphicsAdapter.setRoundedBorder( 2, color, 5, 6, 7, 8 );
    WidgetLCAUtil.preserveRoundedBorder( widget );

    graphicsAdapter.setRoundedBorder( 0, null, 0, 0, 0, 0 );
    WidgetLCAUtil.renderRoundedBorder( widget );

    Message message = getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( widget, "roundedBorder" ) );
  }

  @Test
  public void testRenderInitialMenu() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );

    WidgetLCAUtil.renderMenu( widget, widget.getMenu() );

    Message message = getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  @Test
  public void testRenderMenu() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    Menu menu = new Menu( widget );
    widget.setMenu( menu );

    WidgetLCAUtil.renderMenu( widget, widget.getMenu() );

    Message message = getProtocolMessage();
    assertEquals( getId( menu ), message.findSetProperty( widget, "menu" ).asString() );
  }

  @Test
  public void testRenderMenuReset() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    Menu menu = new Menu( widget );
    widget.setMenu( menu );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderMenu( widget, null );

    Message message = getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( widget, "menu" ) );
  }

  @Test
  public void testRenderInitialData() {
    WidgetLCAUtil.renderData( widget );

    Message message = getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  @Test
  public void testRenderData() {
    registerDataKeys( new String[]{ "foo", "bar" } );
    widget.setData( "foo", "string" );
    widget.setData( "bar", Integer.valueOf( 1 ) );

    WidgetLCAUtil.renderData( widget );

    Message message = getProtocolMessage();
    JsonObject data = ( JsonObject )message.findSetProperty( widget, "data" );
    assertEquals( "string", data.get( "foo" ).asString() );
    assertEquals( 1, data.get( "bar" ).asInt() );
  }

  @Test
  public void testRenderDataWithoutDataWhiteListService() {
    widget.setData( "foo", "string" );

    WidgetLCAUtil.renderData( widget );

    Message message = getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  @Test
  public void testRenderData_MissingData() {
    registerDataKeys( new String[]{ "missing" } );

    WidgetLCAUtil.renderData( widget );

    Message message = getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  @Test
  public void testRenderData_NullKey() {
    registerDataKeys( new String[]{ null } );

    WidgetLCAUtil.renderData( widget );

    Message message = getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  @Test
  public void testRenderDataUnchanged() {
    registerDataKeys( new String[]{ "foo" } );
    widget.setData( "foo", "string" );
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );

    Fixture.preserveWidgets();
    WidgetLCAUtil.renderData( widget );

    Message message = getProtocolMessage();
    assertEquals( 0, message.getOperationCount() );
  }

  @Test
  public void testRenderIntialToolTipMarkupEnabled() {
    widget.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );

    renderToolTip( widget, "foo" );

    Message message = getProtocolMessage();
    assertTrue( "foo", message.findSetProperty( widget, "toolTipMarkupEnabled" ).asBoolean() );
  }

  @Test
  public void testRenderToolTipMarkupEnabled() {
    widget.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );
    Fixture.markInitialized( widget );

    renderToolTip( widget, "foo" );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "toolTipMarkupEnabled" ) );
  }

  @Test
  public void testRenderIntialToolTip() {
    renderToolTip( widget, null );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "toolTip" ) );
  }

  @Test
  public void testRenderToolTip() {
    renderToolTip( widget, "foo" );

    Message message = getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( widget, "toolTip" ).asString() );
  }

  @Test
  public void testRenderToolTip_withAmpersand() {
    renderToolTip( widget, "&foo" );

    Message message = getProtocolMessage();
    assertEquals( "foo", message.findSetProperty( widget, "toolTip" ).asString() );
  }

  @Test
  public void testRenderToolTip_withAmpersandAndMarkupEnabled() {
    widget.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );

    renderToolTip( widget, "foo &#38; bar" );

    Message message = getProtocolMessage();
    assertEquals( "foo &#38; bar", message.findSetProperty( widget, "toolTip" ).asString() );
  }

  @Test
  public void testRenderToolTipUnchanged() {
    Fixture.markInitialized( display );
    Fixture.markInitialized( widget );
    widget.setToolTipText( "foo" );
    Fixture.preserveWidgets();

    renderToolTip( widget, "foo" );

    Message message = getProtocolMessage();
    assertNull( message.findSetOperation( widget, "toolTip" ) );
  }

  @Test
  public void testRenderClientListeners_withoutClientListeners() {
    WidgetLCAUtil.renderClientListeners( widget );

    assertEquals( 0, getProtocolMessage().getOperationCount() );
  }

  @Test
  public void testRenderClientListeners_withClientListenerAdded() {
    ClientListener listener = new ClientListener( "" );
    String listenerId = ClientListenerUtil.getRemoteId( listener );

    widget.addListener( SWT.Selection, listener );
    WidgetLCAUtil.renderClientListeners( widget );

    CallOperation operation = ( CallOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( "addListener", operation.getMethodName() );
    assertEquals( "Selection", operation.getProperty( "eventType" ).asString() );
    assertEquals( listenerId, operation.getProperty( "listenerId" ).asString() );
  }

  @Test
  public void testRenderClientListeners_withClientListenerAddedTwice() {
    ClientListener listener = new ClientListener( "" );

    widget.addListener( SWT.Selection, listener );
    widget.addListener( SWT.Selection, listener );
    WidgetLCAUtil.renderClientListeners( widget );

    // SWT allows for duplicate listeners
    assertEquals( 2, getProtocolMessage().getOperationCount() );
  }

  @Test
  public void testRenderClientListeners_withClientListenerRemoved() {
    ClientListener listener = new ClientListener( "" );
    String listenerId = ClientListenerUtil.getRemoteId( listener );

    widget.removeListener( SWT.Selection, listener );
    WidgetLCAUtil.renderClientListeners( widget );

    CallOperation operation = ( CallOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( "removeListener", operation.getMethodName() );
    assertEquals( "Selection", operation.getProperty( "eventType" ).asString() );
    assertEquals( listenerId, operation.getProperty( "listenerId" ).asString() );
  }

  @Test
  public void testRenderClientListeners_withClientListenerRemovedTwice() {
    ClientListener listener = new ClientListener( "" );

    widget.removeListener( SWT.Selection, listener );
    widget.removeListener( SWT.Selection, listener );
    WidgetLCAUtil.renderClientListeners( widget );

    // SWT allows for duplicate listeners
    assertEquals( 2, getProtocolMessage().getOperationCount() );
  }

  @Test
  public void testRenderClientListeners_preserveOperationsOrder() {
    ClientListener listener = new ClientListener( "" );

    widget.addListener( SWT.Selection, listener );
    widget.removeListener( SWT.Selection, listener );
    widget.addListener( SWT.Selection, listener );
    WidgetLCAUtil.renderClientListeners( widget );

    Message message = getProtocolMessage();
    assertEquals( 3, message.getOperationCount() );
    CallOperation operation = ( CallOperation )message.getOperation( 0 );
    assertEquals( "addListener", operation.getMethodName() );
    operation = ( CallOperation )message.getOperation( 1 );
    assertEquals( "removeListener", operation.getMethodName() );
    operation = ( CallOperation )message.getOperation( 2 );
    assertEquals( "addListener", operation.getMethodName() );
  }

  @Test
  public void testRenderProperty_string() {
    WidgetLCAUtil.renderProperty( widget, "foo", "bar", null );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.valueOf( "bar" ), operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_string_withNull() {
    WidgetLCAUtil.renderProperty( widget, "foo", null, "" );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.NULL, operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_integer() {
    WidgetLCAUtil.renderProperty( widget, "foo", Integer.valueOf( 23 ), Integer.valueOf( 0 ) );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.valueOf( 23 ), operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_integer_withNull() {
    WidgetLCAUtil.renderProperty( widget, "foo", null, Integer.valueOf( 0 ) );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.NULL, operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_stringArray() {
    WidgetLCAUtil.renderProperty( widget, "foo", new String[] { "bar" }, null );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( new JsonArray().add( "bar" ), operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_stringArray_withNull() {
    WidgetLCAUtil.renderProperty( widget, "foo", null, new String[ 0 ] );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.NULL, operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_booleanArray() {
    WidgetLCAUtil.renderProperty( widget, "foo", new boolean[] { true }, null );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( new JsonArray().add( true ), operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_booleanArray_withNull() {
    WidgetLCAUtil.renderProperty( widget, "foo", null, new boolean[ 0 ] );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.NULL, operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_intArray() {
    WidgetLCAUtil.renderProperty( widget, "foo", new int[] { 23 }, null );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( new JsonArray().add( 23 ), operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_intArray_withNull() {
    WidgetLCAUtil.renderProperty( widget, "foo", null, new int[ 0 ] );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.NULL, operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_int() {
    WidgetLCAUtil.renderProperty( widget, "foo", 23, 0 );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.valueOf( 23 ), operation.getProperty( "foo" ) );
  }

  @Test
  public void testRenderProperty_boolean() {
    WidgetLCAUtil.renderProperty( widget, "foo", true, false );

    SetOperation operation = ( SetOperation )getProtocolMessage().getOperation( 0 );
    assertEquals( JsonValue.TRUE, operation.getProperty( "foo" ) );
  }

}
