/*******************************************************************************
 * Copyright (c) 2010, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.List", {
  extend : rwt.widgets.base.BasicList,

  construct : function( multiSelection ) {
    this.base( arguments, multiSelection );
    this.setScrollBarsVisible( false, false );
    this._topIndex = 0;
    var selMgr = this.getManager();
    selMgr.addEventListener( "changeLeadItem", this._onChangeLeadItem, this );
    selMgr.addEventListener( "changeSelection", this._onSelectionChange, this );
    this.addEventListener( "focus", this._onFocusIn, this );
    this.addEventListener( "blur", this._onFocusOut, this );
    this.addEventListener( "dblclick", this._onDblClick, this );
    this.addEventListener( "appear", this._onAppear, this );
    this.addEventListener( "userScroll", this._onUserScroll );
    this.addEventListener( "activateHyperlink", this._onActivateHyperlink );
  },

  destruct : function() {
    var selMgr = this.getManager();
    selMgr.removeEventListener( "changeLeadItem", this._onChangeLeadItem, this );
    selMgr.removeEventListener( "changeSelection", this._onSelectionChange, this );
    this.removeEventListener( "focus", this._onFocusIn, this );
    this.removeEventListener( "blur", this._onFocusOut, this );
    this.removeEventListener( "dblclick", this._onDblClick, this );
    this.removeEventListener( "appear", this._onAppear, this );
  },

  members : {

    setTopIndex : function( value ) {
      this._topIndex = value;
      this._applyTopIndex( value );
    },

    _applyTopIndex : function( newIndex ) {
      var items = this.getManager().getItems();
      if( items.length > 0 && items[ 0 ].isCreated() ) {
        if( this._itemHeight > 0 ) {
          this.setVBarSelection( newIndex * this._itemHeight );
        }
      }
    },

    _getTopIndex : function() {
      var topIndex = 0;
      var scrollTop = this._clientArea.getScrollTop();
      var items = this.getManager().getItems();
      if( items.length > 0 ) {
        var itemHeight = this.getManager().getItemHeight( items[ 0 ] );
        if( itemHeight > 0 ) {
          topIndex = Math.round( scrollTop / itemHeight );
        }
      }
      return topIndex;
    },

    _onAppear : function( evt ) {
      // [ad] Fix for Bug 277678
      // when #showSelection() is called for invisible widget
      this._applyTopIndex( this._topIndex );
    },

    _updateScrollDimension : function() {
      this.base( arguments );
      this._applyTopIndex( this._topIndex );
    },

    _onChangeLeadItem : function( evt ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var focusIndex = this._clientArea.indexOf( this.getManager().getLeadItem() );
        rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "focusIndex", focusIndex );
      }
    },

    _onSelectionChange : function( evt ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        this._sendSelectionChange();
        rwt.remote.EventUtil.notifySelected( this );
      }
      this._updateSelectedItemState();
    },

    _sendSelectionChange : function() {
      var selection = [];
      var selectedItems = this.getManager().getSelectedItems();
      for( var i = 0; i < selectedItems.length; i++ ) {
        var index = this._clientArea.indexOf( selectedItems[ i ] );
        selection.push( index );
      }
      rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "selection", selection );
    },

    _onUserScroll : function( horizontal ) {
      this._topIndex = this._isCreated ? this._getTopIndex() : 0;
      rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "topIndex", this._topIndex );
    },

    _onDblClick : function( evt ) {
      rwt.remote.EventUtil.notifyDefaultSelected( this );
    },

    _onActivateHyperlink : function( event ) {
      var hyperlink = event.target;
      var text = hyperlink.getAttribute( "href" );
      if( !text ) {
        text = hyperlink.innerHTML;
      }
      var properties = {
        "detail" : "hyperlink",
        "text" : text
      };
      rwt.remote.EventUtil.notifySelected( this, properties );
    },

    _onFocusIn : function( evt ) {
      this._updateSelectedItemState();
    },

    _onFocusOut : function( evt ) {
      this._updateSelectedItemState();
    },

    _updateSelectedItemState : function() {
      var selectedItems = this.getManager().getSelectedItems();
      for( var i = 0; i < selectedItems.length; i++ ) {
        if( this.getFocused() ) {
          selectedItems[ i ].removeState( "parent_unfocused" );
        } else {
          selectedItems[ i ].addState( "parent_unfocused" );
        }
      }
    }

  }

} );
