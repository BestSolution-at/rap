/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.template;

import static org.eclipse.rap.rwt.internal.util.ParamCheck.notNullOrEmpty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class CellImpl implements Serializable, Cell {

  static final String PROPERTY_LEFT = "left";
  static final String PROPERTY_TOP = "top";
  static final String PROPERTY_RIGHT = "right";
  static final String PROPERTY_BOTTOM = "bottom";
  static final String PROPERTY_WIDTH = "width";
  static final String PROPERTY_HEIGHT = "height";
  static final String PROPERTY_BINDING_INDEX = "bindingIndex";
  static final String PROPERTY_SELECTABLE = "selectable";
  static final String PROPERTY_NAME = "name";

  private final Map<String, Object> attributes;
  private final String type;

  public CellImpl( RowTemplate template, String type ) {
    checkType( type );
    checkTemplate( template );
    this.type = type;
    this.attributes = new HashMap<String, Object>();
    template.addCell( this );
  }

  private void checkType( String type ) {
    if( type == null || type.isEmpty() ) {
      throw new IllegalArgumentException( "Invalid type" );
    }
  }

  private void checkTemplate( RowTemplate template ) {
    if( template == null ) {
      throw new IllegalArgumentException( "RowTemplate must not be null" );
    }
  }

  public String getType() {
    return type;
  }

  public Cell setName( String name ) {
    checkNotNullOrEmpty( name );
    attributes.put( PROPERTY_NAME, name );
    return this;
  }

  public Cell setBindingIndex( int index ) {
    ensurePositive( index, "BindingIndex" );
    attributes.put( PROPERTY_BINDING_INDEX, Integer.valueOf( index ) );
    return this;
  }

  public Cell setSelectable( boolean selectable ) {
    attributes.put( PROPERTY_SELECTABLE, Boolean.valueOf( selectable ) );
    return this;
  }

  public Cell setLeft( int offset ) {
    checkHorizontalParameters( PROPERTY_RIGHT, PROPERTY_WIDTH );
    attributes.put( PROPERTY_LEFT, Integer.valueOf( offset ) );
    return this;
  }

  public Cell setRight( int offset ) {
    checkHorizontalParameters( PROPERTY_LEFT, PROPERTY_WIDTH );
    attributes.put( PROPERTY_RIGHT, Integer.valueOf( offset ) );
    return this;
  }

  public Cell setTop( int offset ) {
    checkVerticalParameters( PROPERTY_BOTTOM, PROPERTY_HEIGHT );
    attributes.put( PROPERTY_TOP, Integer.valueOf( offset ) );
    return this;
  }

  public Cell setBottom( int offset ) {
    checkVerticalParameters( PROPERTY_TOP, PROPERTY_HEIGHT );
    attributes.put( PROPERTY_BOTTOM, Integer.valueOf( offset ) );
    return this;
  }

  public Cell setWidth( int width ) {
    ensurePositive( width, "Width" );
    checkHorizontalParameters( PROPERTY_LEFT, PROPERTY_RIGHT );
    attributes.put( PROPERTY_WIDTH, Integer.valueOf( width ) );
    return this;
  }

  public Cell setHeight( int height ) {
    ensurePositive( height, "Height" );
    checkVerticalParameters( PROPERTY_TOP, PROPERTY_BOTTOM );
    attributes.put( PROPERTY_HEIGHT, Integer.valueOf( height ) );
    return this;
  }

  private void ensurePositive( int value, String valueName ) {
    if( value < 0 ) {
      throw new IllegalArgumentException( valueName + " must be >= 0 but was " + value );
    }
  }

  private void checkHorizontalParameters( String key1, String key2 ) {
    if( attributes.get( key1 ) != null && attributes.get( key2 ) != null ) {
      throw new IllegalArgumentException( "Can only set two horizontal attributes" );
    }
  }

  private void checkVerticalParameters( String key1, String key2 ) {
    if( attributes.get( key1 ) != null && attributes.get( key2 ) != null ) {
      throw new IllegalArgumentException( "Can only set two vertical attributes" );
    }
  }

  public void addAttribute( String name, Object attribute ) {
    checkAttribute( name, attribute );
    attributes.put( name, attribute );
  }

  private void checkAttribute( String name, Object attribute ) {
    checkNotNullOrEmpty( name );
    if( attribute == null ) {
      throw new IllegalArgumentException( "Attribute must not be null" );
    }
  }

  private void checkNotNullOrEmpty( String name ) {
    if( name == null ) {
      throw new IllegalArgumentException( "Name must not be null" );
    }
    notNullOrEmpty( name, "Attribute name" );
  }

  public Map<String, Object> getAttributes() {
    return new HashMap<String, Object>( attributes );
  }
}
