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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.protocol.StylesUtil;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;


public class TemplateSerializer {

  private static final String PROPERTY_TYPE = "type";
  private static final String PROPERTY_STYLE = "style";

  private final RowTemplate template;

  public TemplateSerializer( RowTemplate template ) {
    checkTemplate( template );
    this.template = template;
  }

  private void checkTemplate( RowTemplate template ) {
    if( template == null ) {
      throw new IllegalArgumentException( "RowTemplate must not be null" );
    }
  }

  public JsonValue toJson() {
    JsonArray serializedCells = new JsonArray();
    List<Cell> cells = template.getCells();
    if( !cells.isEmpty() ) {
      addCells( serializedCells, cells );
    }
    return serializedCells;
  }

  private void addCells( JsonArray serializedCells, List<Cell> cells ) {
    for( Cell cell : cells ) {
      addCell( serializedCells, ( CellImpl )cell );
    }
  }

  private void addCell( JsonArray serializedCells, CellImpl cell ) {
    JsonObject serializedCell = new JsonObject();
    serializedCell.add( PROPERTY_TYPE, cell.getType() );
    addStyles( cell, serializedCell );
    addAttributes( cell, serializedCell );
    serializedCells.add( serializedCell );
  }

  private void addStyles( CellImpl cell, JsonObject serializedCell ) {
    String[] styles = StylesUtil.filterStyles( cell.getStyle(), CellImpl.ALLOWED_STYLES );
    serializedCell.add( PROPERTY_STYLE, JsonUtil.createJsonArray( styles ) );
  }

  private void addAttributes( CellImpl cell, JsonObject serializedCell ) {
    Map<String, Object> attributes = cell.getAttributes();
    for( Entry<String, Object> entry : attributes.entrySet() ) {
      serializedCell.add( entry.getKey(), getJsonValue( entry.getValue() ) );
    }
  }

  private JsonValue getJsonValue( Object value ) {
    if( value instanceof Image ) {
      return ProtocolUtil.getJsonForImage( ( Image )value );
    }
    if( value instanceof Color ) {
      return ProtocolUtil.getJsonForColor( ( Color )value, false );
    }
    if( value instanceof RGB ) {
      return ProtocolUtil.getJsonForColor( ( RGB )value, false );
    }
    if( value instanceof Font ) {
      return ProtocolUtil.getJsonForFont( ( Font )value );
    }
    if( value instanceof FontData ) {
      return ProtocolUtil.getJsonForFont( ( FontData )value );
    }
    if( value instanceof String ) {
      return JsonValue.valueOf( ( String )value );
    }
    if( value instanceof Integer ) {
      return JsonValue.valueOf( ( ( Integer )value ).intValue() );
    }
    if( value instanceof Boolean ) {
      return JsonValue.valueOf( ( ( Boolean )value ).booleanValue() );
    }
    return JsonValue.NULL;
  }
}
