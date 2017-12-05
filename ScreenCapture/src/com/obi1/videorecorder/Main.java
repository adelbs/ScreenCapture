package com.obi1.videorecorder;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.obi1.videorecorder.engine.VideoCapture;
import com.obi1.videorecorder.util.Obi1Utils;

public class Main implements NativeKeyListener {

	public static final String baseDir = "c:" + File.separatorChar +"VideoRecorder";
	private String fileName = "";

	private static TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(RecorderUI.class.getResource("/images/camera.gif")), "Jacob Screen Capture");
	private PopupMenu popup = new PopupMenu();
	private MenuItem captureMenu = new MenuItem("Capture Screen (Ctrl + PrintScreen)");
	private MenuItem captureFullMenu = new MenuItem("Capture Full Screen (Shift + PrintScreen)");
	private MenuItem startRecordMenu = new MenuItem("Start Recording");
	private MenuItem stopRecordMenu = new MenuItem("Stop Recording (Shift + F12)");
	private MenuItem optionsMenu = new MenuItem("Options");
	private MenuItem exitItem = new MenuItem("Exit");

	public static void main(String[] args) throws NativeHookException {
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(new Main());
	}
	
	public Main() {
		SystemTray tray = SystemTray.getSystemTray();
		
        stopRecordMenu.setEnabled(false);
        
        popup.add(captureMenu);
        popup.add(captureFullMenu);
        popup.addSeparator();
        popup.add(startRecordMenu);
        popup.add(stopRecordMenu);
        popup.addSeparator();
        popup.add(optionsMenu);
        popup.addSeparator();
        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } 
        catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
        
        trayIcon.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			//	captureScreen();				
			}
		});
        
        captureMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				captureScreen();
			}
		});
        
        startRecordMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startRecording();
			}
		});
        
        stopRecordMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopRecording();
			}
		});
        
        exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}

	private void captureScreen() {
		try {
			new CaptureScreen();
		} 
		catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	private void startRecording() {
		try {
			fileName = "movie-"+ DateTime.now().toString(DateTimeFormat.forPattern("YYYY-MM-dd_H-ma"));
			VideoCapture.startRecord(null, null, 
					baseDir + File.separatorChar + "tmp", 
					baseDir,
					fileName,
					GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration(), 
					getClass().getClassLoader());
			
			sendMessage("Recording video. Press Shift + F12 to stop.");
		}
		catch (Exception x) {
			x.printStackTrace();
		}
		finally {
			startRecordMenu.setEnabled(false);
			stopRecordMenu.setEnabled(true);
		}
	}
	
	private void stopRecording() {
		String filePath = baseDir + File.separatorChar + fileName + ".mov";
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
			startRecordMenu.setEnabled(true);
			stopRecordMenu.setEnabled(false);
			try {
				Obi1Utils.cleanupDir(baseDir + File.separatorChar + "tmp");
			}
			catch (Exception x) {
				x.printStackTrace();
			}
		}
	}
	
	public void nativeKeyPressed(NativeKeyEvent e) {
		if ((e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) && ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0))
			captureScreen();
		
		if ((e.getKeyCode() == NativeKeyEvent.VC_F12) && ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0))
			stopRecording();
	}

	public void nativeKeyReleased(NativeKeyEvent e) { }
	public void nativeKeyTyped(NativeKeyEvent e) { }

	public static void sendMessage(String message) {
		trayIcon.displayMessage("Jacob Screen Capture", message, TrayIcon.MessageType.INFO);
	}
}
