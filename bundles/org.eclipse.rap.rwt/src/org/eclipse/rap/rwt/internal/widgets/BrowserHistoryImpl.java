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
package org.eclipse.rap.rwt.internal.widgets;

import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.readEventPropertyValueAsString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.Adaptable;
import org.eclipse.rap.rwt.IBrowserHistory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.events.BrowserHistoryEvent;
import org.eclipse.rap.rwt.events.BrowserHistoryListener;
import org.eclipse.rap.rwt.internal.application.RWTFactory;
import org.eclipse.rap.rwt.internal.events.Event;
import org.eclipse.rap.rwt.internal.events.EventAdapter;
import org.eclipse.rap.rwt.internal.events.IEventAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.RequestParams;
import org.eclipse.rap.rwt.lifecycle.PhaseEvent;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.PhaseListener;
import org.eclipse.rap.rwt.lifecycle.PhaseListenerUtil;
import org.eclipse.rap.rwt.service.SessionStoreEvent;
import org.eclipse.rap.rwt.service.SessionStoreListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;


public final class BrowserHistoryImpl
  implements IBrowserHistory, PhaseListener, Adaptable, SessionStoreListener
{

  private final static String TYPE = "rwt.client.BrowserHistory";
  private final static String PROP_NAVIGATION_LISTENER = "navigation";
  private final static String PROP_ENTRIES = "entries";
  private final static String METHOD_ADD = "add";
  private static final String EVENT_HISTORY_NAVIGATED = "historyNavigated";
  private static final String EVENT_HISTORY_NAVIGATED_ENTRY_ID = "entryId";

  private final Display display;
  private final List<HistoryEntry> entriesToAdd;
  private IEventAdapter eventAdapter;
  private boolean hasNavigationListener;

  public BrowserHistoryImpl() {
    display = Display.getCurrent();
    entriesToAdd = new ArrayList<HistoryEntry>();
    RWTFactory.getLifeCycleFactory().getLifeCycle().addPhaseListener( this );
    RWT.getSessionStore().addSessionStoreListener( this );
  }

  //////////////////
  // IBrowserHistory

  public void createEntry( String id, String text ) {
    if( null == id ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( id.length() == 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    entriesToAdd.add( new HistoryEntry( id, text ) );
  }

  public void addBrowserHistoryListener( BrowserHistoryListener listener ) {
    if( null == listener ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    BrowserHistoryEvent.addListener( this, listener );
  }

  public void removeBrowserHistoryListener( BrowserHistoryListener listener ) {
    if( null == listener ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    BrowserHistoryEvent.removeListener( this, listener );
  }

  ////////////////
  // PhaseListener

  public void afterPhase( PhaseEvent event ) {
    Display sessionDisplay = LifeCycleUtil.getSessionDisplay();
    if( display == sessionDisplay ) {
      if( PhaseListenerUtil.isPrepareUIRoot( event ) && isStartup() ) {
        processNavigationEvent();
      } else if( PhaseListenerUtil.isReadData( event ) ) {
        preserveNavigationListener();
      } else if( PhaseListenerUtil.isRender( event ) ) {
        renderNavigationListener();
        renderAdd();
      }
    }
  }

  public void beforePhase( PhaseEvent event ) {
    Display sessionDisplay = LifeCycleUtil.getSessionDisplay();
    if( display == sessionDisplay ) {
      if( PhaseListenerUtil.isProcessAction( event ) && !isStartup() ) {
        processNavigationEvent();
      }
    }
  }

  public PhaseId getPhaseId() {
    return PhaseId.ANY;
  }

  ////////////
  // Adaptable

  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    T result = null;
    if( adapter == IEventAdapter.class ) {
      if( eventAdapter == null ) {
        eventAdapter = new EventAdapter();
      }
      result = ( T )eventAdapter;
    }
    return result;
  }

  ///////////////////////
  // SessionStoreListener

  public void beforeDestroy( SessionStoreEvent event ) {
    RWTFactory.getLifeCycleFactory().getLifeCycle().removePhaseListener( this );
  }

  //////////////////
  // Helping methods

  private static boolean isStartup() {
    return "true".equals( ProtocolUtil.readHeadPropertyValue( RequestParams.RWT_INITIALIZE ) );
  }

  private void processNavigationEvent() {
    if( ProtocolUtil.wasEventSent( TYPE, EVENT_HISTORY_NAVIGATED ) ) {
      String entryId = readEventPropertyValueAsString( TYPE,
                                                       EVENT_HISTORY_NAVIGATED,
                                                       EVENT_HISTORY_NAVIGATED_ENTRY_ID );
      Event evt = new BrowserHistoryEvent( this, entryId );
      evt.processEvent();
    }
  }

  private void preserveNavigationListener() {
    hasNavigationListener = BrowserHistoryEvent.hasListener( this );
  }

  private boolean getPreservedNavigationListener() {
    return hasNavigationListener;
  }

  private void renderNavigationListener() {
    boolean actual = BrowserHistoryEvent.hasListener( this );
    boolean preserved = getPreservedNavigationListener();
    if( preserved != actual ) {
      ProtocolMessageWriter protocolWriter = ContextProvider.getProtocolWriter();
      protocolWriter.appendListen( TYPE, PROP_NAVIGATION_LISTENER, actual );
    }
  }

  private void renderAdd() {
    if( !entriesToAdd.isEmpty() ) {
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put( PROP_ENTRIES, getEntriesAsArray() );
      ProtocolMessageWriter protocolWriter = ContextProvider.getProtocolWriter();
      protocolWriter.appendCall( TYPE, METHOD_ADD, properties );
      entriesToAdd.clear();
    }
  }

  private Object[] getEntriesAsArray() {
    HistoryEntry[] entries = entriesToAdd.toArray( new HistoryEntry[ 0 ] );
    Object[][] result = new Object[ entries.length ][ 2 ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ][ 0 ] = entries[ i ].id;
      result[ i ][ 1 ] = entries[ i ].text;
    }
    return result;
  }

  ////////////////
  // Inner classes

  private final class HistoryEntry {
    public final String id;
    public final String text;

    public HistoryEntry( String id, String text ) {
      this.id = id;
      this.text = text;
    }
  }
}
