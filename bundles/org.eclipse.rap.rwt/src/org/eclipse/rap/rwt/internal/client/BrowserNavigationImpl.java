/*******************************************************************************
 * Copyright (c) 2009, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Ralf Zahn (ARS) - browser history support (Bug 283291)
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.readEventPropertyValueAsString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.rap.rwt.internal.application.RWTFactory;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.RequestParams;
import org.eclipse.rap.rwt.lifecycle.PhaseEvent;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.PhaseListener;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;


public final class BrowserNavigationImpl
  implements BrowserNavigation, PhaseListener, UISessionListener
{

  private final static String TYPE = "rwt.client.BrowserNavigation";
  private final static String PROP_NAVIGATION_LISTENER = "Navigation";
  private final static String PROP_ENTRIES = "entries";
  private final static String METHOD_ADD_TO_HISTORY = "addToHistory";
  private static final String EVENT_HISTORY_NAVIGATED_ENTRY_ID = "entryId";

  private final Display display;
  private final List<HistoryEntry> entriesToAdd;
  private final Collection<BrowserNavigationListener> listeners;
  private boolean hasNavigationListener;

  public BrowserNavigationImpl() {
    display = Display.getCurrent();
    entriesToAdd = new ArrayList<HistoryEntry>();
    listeners = new LinkedList<BrowserNavigationListener>();
    RWTFactory.getLifeCycleFactory().getLifeCycle().addPhaseListener( this );
    RWT.getUISession().addUISessionListener( this );
  }

  //////////
  // History

  public void createHistoryEntry( String id, String text ) {
    if( id == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( id.length() == 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    entriesToAdd.add( new HistoryEntry( id, text ) );
  }

  public void addBrowserNavigationListener( BrowserNavigationListener listener ) {
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    listeners.add( listener );
  }

  public void removeBrowserNavigationListener( BrowserNavigationListener listener ) {
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    listeners.remove( listener );
  }

  ////////////////
  // PhaseListener

  public void afterPhase( PhaseEvent event ) {
    Display sessionDisplay = LifeCycleUtil.getSessionDisplay();
    if( display == sessionDisplay ) {
      if( event.getPhaseId() == PhaseId.PREPARE_UI_ROOT && isStartup() ) {
        processNavigationEvent();
      } else if( event.getPhaseId() == PhaseId.READ_DATA ) {
        preserveNavigationListener();
      } else if( event.getPhaseId() == PhaseId.RENDER ) {
        renderNavigationListener();
        renderAdd();
      }
    }
  }

  public void beforePhase( PhaseEvent event ) {
    Display sessionDisplay = LifeCycleUtil.getSessionDisplay();
    if( display == sessionDisplay ) {
      if( event.getPhaseId() == PhaseId.PROCESS_ACTION && !isStartup() ) {
        processNavigationEvent();
      }
    }
  }

  public PhaseId getPhaseId() {
    return PhaseId.ANY;
  }

  ////////////////////
  // UISessionListener

  public void beforeDestroy( UISessionEvent event ) {
    RWTFactory.getLifeCycleFactory().getLifeCycle().removePhaseListener( this );
  }

  //////////////////
  // Helping methods

  private static boolean isStartup() {
    return "true".equals( ProtocolUtil.readHeadPropertyValue( RequestParams.RWT_INITIALIZE ) );
  }

  private void processNavigationEvent() {
    if( ProtocolUtil.wasEventSent( TYPE, PROP_NAVIGATION_LISTENER ) ) {
      String entryId = readEventPropertyValueAsString( TYPE,
                                                       PROP_NAVIGATION_LISTENER,
                                                       EVENT_HISTORY_NAVIGATED_ENTRY_ID );
      BrowserNavigationEvent event = new BrowserNavigationEvent( this, entryId );
      BrowserNavigationListener[] listeners = getListeners();
      for( BrowserNavigationListener listener : listeners ) {
        listener.navigated( event );
      }
    }
  }

  private BrowserNavigationListener[] getListeners() {
    return listeners.toArray( new BrowserNavigationListener[ listeners.size() ] );
  }

  private void preserveNavigationListener() {
    hasNavigationListener = !listeners.isEmpty();
  }

  private boolean getPreservedNavigationListener() {
    return hasNavigationListener;
  }

  private void renderNavigationListener() {
    boolean actual = !listeners.isEmpty();
    boolean preserved = getPreservedNavigationListener();
    if( preserved != actual ) {
      ProtocolMessageWriter protocolWriter = ContextProvider.getProtocolWriter();
      protocolWriter.appendListen( TYPE, PROP_NAVIGATION_LISTENER, actual );
    }
  }

  private void renderAdd() {
    if( !entriesToAdd.isEmpty() ) {
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put( PROP_ENTRIES, entriesAsArray() );
      ProtocolMessageWriter protocolWriter = ContextProvider.getProtocolWriter();
      protocolWriter.appendCall( TYPE, METHOD_ADD_TO_HISTORY, properties );
      entriesToAdd.clear();
    }
  }

  private Object[] entriesAsArray() {
    HistoryEntry[] entries = getEntries();
    Object[][] result = new Object[ entries.length ][ 2 ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ][ 0 ] = entries[ i ].id;
      result[ i ][ 1 ] = entries[ i ].text;
    }
    return result;
  }

  HistoryEntry[] getEntries() {
    return entriesToAdd.toArray( new HistoryEntry[ entriesToAdd.size() ] );
  }

  ////////////////
  // Inner classes

  final class HistoryEntry {
    final String id;
    final String text;

    HistoryEntry( String id, String text ) {
      this.id = id;
      this.text = text;
    }
  }

}