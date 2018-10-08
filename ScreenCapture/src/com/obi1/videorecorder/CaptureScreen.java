package com.obi1.videorecorder;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class CaptureScreen extends JFrame implements ClipboardOwner {

	private static final long serialVersionUID = 1L;

	private BufferedImage printScreen;
	
	private int mouseXStart = 0;
	private int mouseYStart = 0;

	private int actualInstanceId = -1;
	private ArrayList<RectangleComponent> rectList = new ArrayList<RectangleComponent>();
	
	private String tempDir;
	private Color borderColor;
	
	public CaptureScreen(String tempDir, Color borderColor) throws AWTException {
		
		this.tempDir = tempDir;
		this.borderColor = borderColor;
		printScreen = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

		setExtendedState(JFrame.MAXIMIZED_BOTH); 
		setType(Type.UTILITY);
		setUndecorated(true);
		setBackground(new Color(1.0f,1.0f,1.0f,0.8f));
		setLayout(null);
		
		rectList.add(new RectangleComponent(0));
		add(rectList.get(0));
		
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) { }
			
			@Override
			public void mousePressed(MouseEvent e) {
				mouseXStart = MouseInfo.getPointerInfo().getLocation().x;
				mouseYStart = MouseInfo.getPointerInfo().getLocation().y;
				
				actualInstanceId++;
				if (actualInstanceId > 0) {
					rectList.add(new RectangleComponent(actualInstanceId));
					rectList.get(0).add(rectList.get(actualInstanceId));
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) { }
			@Override
			public void mouseEntered(MouseEvent e) { }
			@Override
			public void mouseClicked(MouseEvent e) { }
		});
		
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) { }
			
			@Override
			public void mouseDragged(MouseEvent e) {
				int w = MouseInfo.getPointerInfo().getLocation().x - mouseXStart;
				int h = MouseInfo.getPointerInfo().getLocation().y - mouseYStart;
				if (actualInstanceId > 0)
					rectList.get(actualInstanceId).setBounds(mouseXStart - rectList.get(0).getX(), mouseYStart - rectList.get(0).getY(), w, h);
				else
					rectList.get(actualInstanceId).setBounds(mouseXStart, mouseYStart, w, h);
			}
		});

		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) { }
			
			@Override
			public void keyReleased(KeyEvent e) { }
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
					exit();
				
				if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
					save(true);
				
				if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
					save(false);
			}
		});
		
		setAlwaysOnTop(true);
		setVisible(true);
	}
	
	private void save(boolean isClipboard) {
		if (isClipboard) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(rectList.get(0), this);
		}
		else {
			try {
				String fileName = tempDir + File.separatorChar + "screen-"+ DateTime.now().toString(DateTimeFormat.forPattern("YYYY-MM-dd_H-ma-s")) + ".png";
				ImageIO.write(rectList.get(0).getImage(), "PNG", new File(fileName));
				Main.sendMessage("Screen saved at "+ fileName);
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		exit();		
	}
	
	private void exit() {
		setVisible(false);
	}
	
	public void lostOwnership( Clipboard clip, Transferable trans ) {
        System.out.println( "Lost Clipboard Ownership" );
    }

	private class RectangleComponent extends JPanel implements Transferable {
        
		private static final long serialVersionUID = 1L;

        private Color black = new Color(0.0F,0.0F,0.0F);
        private int instanceId;

        private int x1Dest, y1Dest, x2Dest, y2Dest, x1Src, y1Src, x2Src, y2Src;
        
        RectangleComponent(int instanceId) {
        	this.instanceId = instanceId;
        }
        
		@Override
	    public void paintComponent(Graphics g) {
			super.paintComponent(g);

			g.setColor(instanceId == 0 ? black : borderColor);
	        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	        g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);

			if (actualInstanceId == instanceId) {
		        x1Dest = 2;
		        y1Dest = 2;
		        x2Dest = getWidth() - 2;
		        y2Dest = getHeight() - 2;
		        
		        x1Src = mouseXStart + 2;
		        y1Src = mouseYStart + 2;
		        x2Src = mouseXStart + getWidth() - 2;
		        y2Src = mouseYStart + getHeight() - 2;
	        }

	        g.drawImage(printScreen, x1Dest, y1Dest, x2Dest, y2Dest, x1Src, y1Src, x2Src, y2Src, this);
	    }
		
		public BufferedImage getImage() {
			BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			printAll(g);
			return image;
		}
		
        public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
            Image i = getImage();
        	if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
                return i;
            }
            else {
                throw new UnsupportedFlavorException( flavor );
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[ 1 ];
            flavors[ 0 ] = DataFlavor.imageFlavor;
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for ( int i = 0; i < flavors.length; i++ ) {
                if ( flavor.equals( flavors[ i ] ) ) {
                    return true;
                }
            }

            return false;
        }
	}
}
