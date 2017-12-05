package com.obi1.videorecorder;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.obi1.videorecorder.engine.VideoCapture;
import com.obi1.videorecorder.util.Obi1Utils;

public class RecorderUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JLabel lblVideoName = new JLabel("Video name:");
	private JLabel lblSaveToFile = new JLabel("Save to file:");
	
	private JTextField txtVideoName = new JTextField("How to do something...");
	private JTextField txtSaveDir;

	private JCheckBox chckbxCleanup = new JCheckBox("Clean-up");
	private JButton btnRecord = new JButton("");;
	private JButton btnStop = new JButton("");
	private JButton btnExit = new JButton("Exit");
	
	private String rootDir = "";
	
	public static void main(String[] args) {
		new RecorderUI();
	}
	
	public RecorderUI() {
		
		if (String.valueOf(File.separatorChar).equals("\\"))
			rootDir = "c:";
		else
			rootDir = "/opt";
		
		txtSaveDir = new JTextField(rootDir + File.separatorChar +"VideoRecorder"+ File.separatorChar +"video001.mov");
		
		setTitle("Jacob Video Recorder! (v.1.0)");
		//setUndecorated(true);
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(RecorderUI.class.getResource("/images/movie.png")));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(null);
		
		lblVideoName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVideoName.setBounds(12, 28, 81, 16);
		getContentPane().add(lblVideoName);
		
		lblSaveToFile.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSaveToFile.setBounds(12, 72, 81, 16);
		getContentPane().add(lblSaveToFile);
		
		txtVideoName.setBounds(105, 24, 357, 25);
		getContentPane().add(txtVideoName);
		txtVideoName.setColumns(10);
		
		txtSaveDir.setColumns(10);
		txtSaveDir.setBounds(105, 68, 357, 25);
		getContentPane().add(txtSaveDir);
		
		btnRecord.setContentAreaFilled(false);
		btnRecord.setBorderPainted(false);
		btnRecord.setIcon(new ImageIcon(RecorderUI.class.getResource("/images/media_record.png")));
		btnRecord.setBounds(469, 9, 52, 49);
		getContentPane().add(btnRecord);
		btnRecord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recording(true);
				
				try {
					VideoCapture.startRecord(txtVideoName.getText(), 
							DateTime.now().toString(DateTimeFormat.forPattern("MMMM d, Y - H:ma")), 
							txtSaveDir.getText().substring(0, txtSaveDir.getText().lastIndexOf(File.separatorChar)) + File.separatorChar + "tmp", 
							txtSaveDir.getText().substring(0, txtSaveDir.getText().lastIndexOf(File.separatorChar)),
							txtSaveDir.getText().substring(txtSaveDir.getText().lastIndexOf(File.separatorChar) + 1, txtSaveDir.getText().indexOf(".")),
							GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration(), 
							getClass().getClassLoader());
				}
				catch (Exception x) {
					recording(false);
					x.printStackTrace();
				}
			}
		});
		
		btnStop.setEnabled(false);
		btnStop.setBorderPainted(false);
		btnStop.setContentAreaFilled(false);
		btnStop.setIcon(new ImageIcon(RecorderUI.class.getResource("/images/media_playback_stop.png")));
		btnStop.setBounds(533, 9, 52, 49);
		getContentPane().add(btnStop);
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recording(false);
				
				File video = new File("");
				try {
					video = VideoCapture.stopRecord(chckbxCleanup.isSelected());
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
					try {
						Obi1Utils.cleanupDir(txtSaveDir.getText().substring(0, txtSaveDir.getText().lastIndexOf(File.separatorChar)) + File.separatorChar + "tmp");
					}
					catch (Exception x) {
						x.printStackTrace();
					}
				}
			}
		});
		
		btnExit.setBounds(469, 68, 117, 25);
		getContentPane().add(btnExit);
		
		//chckbxCleanup.setBounds(105, 75, 113, 25);
		//getContentPane().add(chckbxCleanup);
		
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Obi1Utils.cleanupDir(txtSaveDir.getText().substring(0, txtSaveDir.getText().lastIndexOf(File.separatorChar)) + File.separatorChar + "tmp");
				}
				catch (Exception x) {
					x.printStackTrace();
				}
				
				System.exit(0);
			}
		});
		
		setBounds(200, 200, 599, 151);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		setVisible(true);
	}
	
	private void recording(boolean bool) {
		btnRecord.setEnabled(!bool);
		btnStop.setEnabled(bool);
		txtVideoName.setEnabled(!bool);
		txtSaveDir.setEnabled(!bool);
	}
}
