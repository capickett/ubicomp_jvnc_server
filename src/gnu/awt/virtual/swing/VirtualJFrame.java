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

import gnu.awt.*;
import gnu.awt.virtual.*;

import java.awt.*;

import javax.swing.*;

public class VirtualJFrame extends ToolkitJFrame implements PixelsOwner
{
	//
	// Construction
	//
	
	public VirtualJFrame(String name )
	{
		super(name );
	}
	
	public VirtualJFrame( )
	{
		super();
	}
	
	//
	// PixelsOwner
	//
	
	public int[] getPixels()
	{
		return pixelArray;
	}
	
	public void setPixelArray( int[] pixelArray, int pixelWidth, int pixelHeight )
	{
		this.pixelArray = pixelArray;
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
	}
	
	public int getPixelWidth()
	{
		return pixelWidth;
	}
	
	public int getPixelHeight()
	{
		return pixelHeight;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	private int[] pixelArray = null;
	private int pixelWidth = -1;
	private int pixelHeight = -1;
}
