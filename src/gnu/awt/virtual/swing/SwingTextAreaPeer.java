package gnu.awt.virtual.swing;

/**
* <br><br><center><table border="1" width="80%"><hr>
* <strong><a href="http://www.amherst.edu/~tliron/vncj">VNCj</a></strong>
* <p>
* Copyright (C) 2000-2002 by Tal Liron
* <p>
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation; either version 2.1
* of the License, or (at your option) any later version.
* <p>
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* <a href="http://www.gnu.org/copyleft/lesser.html">GNU Lesser General Public License</a>
* for more details.
* <p>
* You should have received a copy of the <a href="http://www.gnu.org/copyleft/lesser.html">
* GNU Lesser General Public License</a> along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
* <hr></table></center>
**/

import java.awt.*;
import java.awt.peer.*;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

class SwingTextAreaPeer extends JTextArea implements TextAreaPeer
{
	//
	// Construction
	//
	
	public SwingTextAreaPeer( TextArea textArea )
	{
		super();
		
		switch( textArea.getScrollbarVisibility() )
		{
			case TextArea.SCROLLBARS_BOTH:
				SwingFramePeer.add( textArea, new JScrollPane( this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS ) );
				break;
			case TextArea.SCROLLBARS_HORIZONTAL_ONLY:
				SwingFramePeer.add( textArea, new JScrollPane( this, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS ) );
				break;
			case TextArea.SCROLLBARS_VERTICAL_ONLY:
				SwingFramePeer.add( textArea, new JScrollPane( this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ) );
				break;
			case TextArea.SCROLLBARS_NONE:
				SwingFramePeer.add( textArea, this );
				break;
		}

		setText( textArea.getText() );
		setRows( textArea.getRows() );
		setColumns( textArea.getColumns() );
		setEditable( textArea.isEditable() );
	}
	
	//
	// TextAreaPeer
	//
	
	public Dimension getPreferredSize( int rows, int columns )
	{
		return null;
	}
	
	public Dimension getMinimumSize( int rows, int columns )
	{
		return null;
	}
	
	// Deprectated
	
	public void insertText( String txt, int pos )
	{
		insert( txt, pos );
	}	
	
	public void replaceText( String txt, int start, int end )
	{
		replaceRange( txt, start, end );
	}
	
	public Dimension preferredSize( int rows, int cols )
	{
		return getPreferredSize( rows, cols );
	}
	
	public Dimension minimumSize( int rows, int cols )
	{
		return getMinimumSize( rows, cols );
	}
	
	//
	// TextComponentPeer
	//
	
	public int getIndexAtPoint( int x, int y )
	{
		return 0;
	}
	
	public Rectangle getCharacterBounds( int i )
	{
		return null;
	}
	
	public long filterEvents( long mask )
	{
		return 0;
	}
	
	//
	// ComponentPeer
	//
	
	// Events
	
	public void handleEvent( AWTEvent e )
	{
		//System.err.println(e);
	}
	
	public void coalescePaintEvent( PaintEvent e )
	{
		System.err.println(e);
	}
	
	// Misc
	
	public void dispose()
	{
	}

	public boolean canDetermineObscurity() {
		// TODO Auto-generated method stub
		return false;
	}

	public void createBuffers(int arg0, BufferCapabilities arg1) throws AWTException {
		// TODO Auto-generated method stub
		
	}

	public void destroyBuffers() {
		// TODO Auto-generated method stub
		
	}

	public void flip(FlipContents arg0) {
		// TODO Auto-generated method stub
		
	}

	public Image getBackBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean handlesWheelScrolling() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isObscured() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean requestFocus(Component arg0, boolean arg1, boolean arg2, long arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateCursorImmediately() {
		// TODO Auto-generated method stub
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Private
}
