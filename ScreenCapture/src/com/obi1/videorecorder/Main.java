package com.obi1.videorecorder;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.obi1.videorecorder.engine.VideoCapture;
import com.obi1.videorecorder.util.Obi1Utils;

public class Main extends JFrame implements NativeKeyListener {
	
	private static final long serialVersionUID = 1L;
	
	private Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
	private Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	
	private String fileName = "";
	
	private static TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/camera.gif")), "Jacob Screen Capture");
	private static FrmConfig frmConfig;
	private FrmHelp frmHelp = new FrmHelp();
	
	private ImageIcon windowIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/window.png")));
	private ImageIcon screenIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/screen.png")));
	private ImageIcon recordIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/record.png")));
	private ImageIcon stopIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/stop.png")));
	private ImageIcon configIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/config.png")));
	private ImageIcon infoIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/info.png")));
	private ImageIcon exitIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/exit.png")));
	
	private JButton btnWindow = new JButton(windowIcon);
	private JButton btnScreen = new JButton(screenIcon);
	private JButton btnRecord = new JButton(recordIcon);
	private JButton btnStop = new JButton(stopIcon);
	private JButton btnConfig = new JButton(configIcon);
	private JButton btnInfo = new JButton(infoIcon);
	private JButton btnExit = new JButton(exitIcon);

	public static void main(String[] args) throws NativeHookException, IOException {
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(new Main());
	}

	public Main() throws IOException {
		
		SystemTray tray = SystemTray.getSystemTray();
		
        try {
        	frmConfig = new FrmConfig();
            tray.add(trayIcon);
        } 
        catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }

		setAlwaysOnTop(true);
		setType(Type.UTILITY);
		setResizable(false);
		setUndecorated(true);
		getContentPane().setLayout(null);
		
		setBackground(new Color(10, 0, 0, 100));

		btnWindow.setBounds(2, 2, 40, 40);
		btnScreen.setBounds(44, 2, 40, 40);
		btnRecord.setBounds(86, 2, 40, 40);
		btnStop.setBounds(128, 2, 40, 40);
		btnConfig.setBounds(170, 2, 40, 40);
		btnInfo.setBounds(212, 2, 40, 40);
		btnExit.setBounds(254, 2, 40, 40);
		
		getContentPane().add(btnWindow);
		getContentPane().add(btnScreen);
		getContentPane().add(btnRecord);
		getContentPane().add(btnStop);
		getContentPane().add(btnConfig);
		getContentPane().add(btnInfo);
		getContentPane().add(btnExit);
		
		setSize(296, 44);

		btnStop.setEnabled(false);
		
		addMouseListener(focusListener);
		btnWindow.addMouseListener(focusListener);
		btnScreen.addMouseListener(focusListener);
		btnRecord.addMouseListener(focusListener);
		btnStop.addMouseListener(focusListener);
		btnConfig.addMouseListener(focusListener);
		btnInfo.addMouseListener(focusListener);
		btnExit.addMouseListener(focusListener);

        trayIcon.addMouseListener(new MouseListener() {
			
            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                	int xClick = e.getX() - (getWidth() / 2);
                	int x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - getWidth();
                	int y = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()) - (scrnSize.height - winSize.height) - getHeight();
                	
                	if (xClick < x)
                		x = xClick;
                	
                	setBounds(x, y, getWidth(), getHeight());
                	frmConfig.setVisible(false);
                	setVisible(true);
                }
            }
            
			@Override
			public void mouseExited(MouseEvent e) { }
			
			@Override
			public void mouseEntered(MouseEvent e) { }
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					captureScreen();
				}
			}
		});

		btnWindow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				captureScreen();
			}
		});
		
		btnScreen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		btnRecord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startRecording();
			}
		});
		
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopRecording();
			}
		});
		
		btnConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
            	int x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - frmConfig.getWidth();
            	int y = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()) - (scrnSize.height - winSize.height) - frmConfig.getHeight();
            	
            	frmConfig.setBounds(x, y, frmConfig.getWidth(), frmConfig.getHeight());
				frmConfig.setVisible(true);
			}
		});
		
		btnInfo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
            	int x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - frmHelp.getWidth();
            	int y = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()) - (scrnSize.height - winSize.height) - frmHelp.getHeight();
            	
            	frmHelp.setBounds(x, y, frmHelp.getWidth(), frmHelp.getHeight());
            	frmHelp.setVisible(true);
			}
		});
		
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}
	
	private MouseListener focusListener = new MouseListener() {
		
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
	};
	
	public void captureScreen() {
		try {
			setVisible(false);
			new CaptureScreen(frmConfig.getTempDir(), frmConfig.getBorderColor());
		} 
		catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private void startRecording() {
		try {
			setVisible(false);
			
			fileName = "movie-"+ DateTime.now().toString(DateTimeFormat.forPattern("YYYY-MM-dd_H-ma"));
			VideoCapture.startRecord(null, null, 
					frmConfig.getTempDir() + File.separatorChar + "tmp", 
					frmConfig.getTempDir(),
					fileName,
					GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration(), 
					getClass().getClassLoader());
			
			sendMessage("Recording video. Press Shift + F12 to stop.");
		}
		catch (Exception x) {
			x.printStackTrace();
		}
		finally {
			btnRecord.setEnabled(false);
			btnStop.setEnabled(true);
		}
	}
	
	private void stopRecording() {
		setVisible(false);
		
		String filePath = frmConfig.getTempDir() + File.separatorChar + fileName + ".mov";
		File video = new File(filePath);
		try {
			video = VideoCapture.stopRecord(false);
			sendMessage("Video saved at "+ filePath);
			Desktop.getDesktop().open(video);
		}
		catch (Exception x) {
			try {
				Desktop.getDesktop().open(video.getParentFile());
			}
			catch (Exception xx) {
				xx.printStackTrace();
			}
		}
		finally {
			btnRecord.setEnabled(true);
			btnStop.setEnabled(false);
			try {
				Obi1Utils.cleanupDir(frmConfig.getTempDir() + File.separatorChar + "tmp");
			}
			catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	public static void sendMessage(String message) {
		if (frmConfig.isTrayMessages())
			trayIcon.displayMessage("Felipe Jacob's ScreenCapture", message, TrayIcon.MessageType.INFO);
	}

	public void nativeKeyPressed(NativeKeyEvent e) {
		if ((e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) && ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0))
			captureScreen();
		
		if ((e.getKeyCode() == NativeKeyEvent.VC_F12) && ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0))
			stopRecording();
	}

	public void nativeKeyReleased(NativeKeyEvent e) { }
	public void nativeKeyTyped(NativeKeyEvent e) { }

}
