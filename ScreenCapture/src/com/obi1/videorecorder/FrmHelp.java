package com.obi1.videorecorder;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class FrmHelp extends JFrame {

	private static final long serialVersionUID = 1L;

	public FrmHelp() {
		setAlwaysOnTop(true);
		setType(Type.UTILITY);
		setResizable(false);
		setUndecorated(true);
		getContentPane().setLayout(null);
		
		JLabel lblCreatedByFelipe = new JLabel("<html>\r\n<center>\r\nCreated by Felipe Jacob<br>\r\n<a href=\"https://github.com/adelbs/ScreenCapture\">Click to visit the GitHub Repository</a><br><br>\r\nProject under the GNU General Public License v3.0\r\n</center>\r\n</html>");
		lblCreatedByFelipe.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblCreatedByFelipe.setForeground(Color.WHITE);
		lblCreatedByFelipe.setBounds(12, 13, 334, 75);
		getContentPane().add(lblCreatedByFelipe);
		
		setBackground(new Color(10, 0, 0, 100));

		
		setBounds(0, 0, 350, 95);
		
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) { }
			
			@Override
			public void mousePressed(MouseEvent e) { }
			
			@Override
			public void mouseExited(MouseEvent e) {
				
				int mouseX = MouseInfo.getPointerInfo().getLocation().x;
				int mouseY = MouseInfo.getPointerInfo().getLocation().y;
				
				boolean isOut = (mouseX < getBounds().getX() 
								|| mouseY < getBounds().getY()
								|| mouseX > (getBounds().getX() + getWidth())
								|| mouseY > (getBounds().getY() + getHeight()));
						
				if (isOut)
					setVisible(false);
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) { }
			
			@Override
			public void mouseClicked(MouseEvent e) { }
		});

		lblCreatedByFelipe.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://github.com/adelbs/ScreenCapture"));
				} 
				catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
