/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.widgets" );

(function() {
  var addListener = rwt.html.EventRegistration.addEventListener;
  var removeListener = rwt.html.EventRegistration.removeEventListener;
  var callWithElement = rwt.widgets.util.WidgetUtil.callWithElement;

  rwt.widgets.DropTarget = function( control, operations ) {
    this.control = control;
    this.actions = rwt.remote.DNDSupport.getInstance()._operationsToActions( operations );
    rwt.remote.DNDSupport.getInstance().registerDropTarget( this );
    this._onDragEvent = rwt.util.Functions.bind( this._onDragEvent, this );
  };

  rwt.widgets.DropTarget.prototype = {

    dispose : function() {
      rwt.remote.DNDSupport.getInstance().deregisterDropTarget( this );
    },

    setTransfer : function( transferTypes ) {
      rwt.remote.DNDSupport.getInstance().setDropTargetTransferTypes( this.control, transferTypes );
    },

    changeFeedback : function( feedback, flags ) {
      rwt.remote.DNDSupport.getInstance().setFeedback( this.control, feedback, flags );
    },

    changeDetail : function( detail ) {
      rwt.remote.DNDSupport.getInstance().setOperationOverwrite( this.control, detail );
    },

    changeDataType : function( dataType ) {
      rwt.remote.DNDSupport.getInstance().setDataType( this.control, dataType );
    },

    setFileDropEnabled : function( enabled ) {
      if( rwt.client.Client.supportsFileDrop() ) {
        var listener = this._onDragEvent;
        callWithElement( this.control, function( element ) {
          if( enabled ) {
            addListener( element, "dragenter", listener, false );
            addListener( element, "dragover", listener, false );
            addListener( element, "drop", listener, false );
          } else {
            removeListener( element, "dragenter", listener, false );
            removeListener( element, "dragover", listener, false );
            removeListener( element, "drop", listener, false );
          }
        } );
      }
    },

    _onDragEvent : function( event ) {
      try {
        if( this._isDraggingFiles( event ) ) {
          // NOTE: Feedback effects are currenlty only respected by webkit, will ignore for now
          rwt.event.EventHandlerUtil.stopDomEvent( event );
          event.stopPropagation();
          if( event.type === "drop" ) {
            this._sendDropAccept( event );
          }
        }
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _sendDropAccept : function( event ) {
      var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
      remoteObject.notify( "DropAccept", {
        "x" : event.pageX,
        "y" : event.pageY,
        "time" : rwt.remote.EventUtil.eventTimestamp(),
        "operation" : "move",
        "feedback" : 0,
        "files" : this._getFileIds( event )
      } );
    },

    _isDraggingFiles : function( event ) {
      var types = event.dataTransfer.types;
      return types.indexOf ? ( types.indexOf( "Files" ) !== -1 ) : types.contains( "Files" );
    },

    _getFileIds : function( event ) {
      var fileUploader = rwt.client.FileUploader.getInstance();
      var files = event.dataTransfer.files;
      var result = [];
      for( var i = 0; i < files.length; i++ ) {
        result[ i ] = fileUploader.addFile( files.item( i ) );
      }
      return result;
    }

  };

}());
