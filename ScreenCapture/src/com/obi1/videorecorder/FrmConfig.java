package com.obi1.videorecorder;

import java.awt.Color;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class FrmConfig extends JFrame {
	
	private static final long serialVersionUID = 1L;

	private Properties props = new Properties();
	private File propFile = new File("screencapture.properties");
	private FileInputStream inputPropFile;

	private String strTempDir;
	private String strColor;
	private String trayMessages;
	private String recordMouse;
	
	private JTextField txtTempDir = new JTextField();;
	private JCheckBox ckTrayMessages = new JCheckBox("Allow Tray messages");
	private JCheckBox ckRecordMouse = new JCheckBox("Record mouse pointer");
	
	private JPanel btnMagenta = new JPanel();
	private JPanel btnRed = new JPanel();
	private JPanel btnGreen = new JPanel();
	private JPanel btnYellow = new JPanel();
	private JPanel btnBlue = new JPanel();
	
	public FrmConfig() throws IOException {
		
		propFile.createNewFile();
		inputPropFile = new FileInputStream(propFile);

		loadProperties();
		
		setAlwaysOnTop(true);
		setType(Type.UTILITY);
		setResizable(false);
		setUndecorated(true);
		getContentPane().setLayout(null);
		
		setBackground(new Color(10, 0, 0, 100));
		
		JLabel lblTemporaryDirectory = new JLabel("Temporary directory:");
		lblTemporaryDirectory.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblTemporaryDirectory.setForeground(Color.WHITE);
		lblTemporaryDirectory.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemporaryDirectory.setBounds(12, 13, 139, 16);
		getContentPane().add(lblTemporaryDirectory);
		
		JLabel lblBorderColor = new JLabel("Border color:");
		lblBorderColor.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblBorderColor.setForeground(Color.WHITE);
		lblBorderColor.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBorderColor.setBounds(12, 51, 139, 16);
		getContentPane().add(lblBorderColor);
		
		ImageIcon saveIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/save.png")));
		JButton btnSave = new JButton(saveIcon);
		btnSave.setBounds(444, 13, 40, 40);
		getContentPane().add(btnSave);

		ImageIcon loadIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/images/load.png")));
		JButton btnLoad = new JButton(loadIcon);
		btnLoad.setBounds(444, 60, 40, 40);
		getContentPane().add(btnLoad);

		
		txtTempDir.setBounds(163, 13, 269, 22);
		getContentPane().add(txtTempDir);
		txtTempDir.setColumns(10);
		
		
		ckRecordMouse.setFont(new Font("Tahoma", Font.BOLD, 13));
		ckRecordMouse.setForeground(Color.WHITE);
		ckRecordMouse.setOpaque(false);
		ckRecordMouse.setBounds(159, 108, 177, 25);
		getContentPane().add(ckRecordMouse);
		
		ckRecordMouse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recordMouse = String.valueOf(ckRecordMouse.isSelected());
			}
		});

		ckTrayMessages.setFont(new Font("Tahoma", Font.BOLD, 13));
		ckTrayMessages.setForeground(Color.WHITE);
		ckTrayMessages.setOpaque(false);
		ckTrayMessages.setBounds(159, 82, 177, 25);
		getContentPane().add(ckTrayMessages);
		
		ckTrayMessages.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				trayMessages = String.valueOf(ckTrayMessages.isSelected());
			}
		});
		
		btnMagenta.setBackground(Color.MAGENTA);
		btnMagenta.setBounds(163, 50, 20, 20);
		btnMagenta.addMouseListener(new ColorListener(btnMagenta, "magenta"));
		getContentPane().add(btnMagenta);

		btnRed.setBackground(Color.RED);
		btnRed.setBounds(184, 50, 20, 20);
		btnRed.addMouseListener(new ColorListener(btnRed, "red"));
		getContentPane().add(btnRed);
		
		btnGreen.setBackground(Color.GREEN);
		btnGreen.setBounds(205, 50, 20, 20);
		btnGreen.addMouseListener(new ColorListener(btnGreen, "green"));
		getContentPane().add(btnGreen);
		
		btnYellow.setBackground(Color.YELLOW);
		btnYellow.setBounds(226, 50, 20, 20);
		btnYellow.addMouseListener(new ColorListener(btnYellow, "yellow"));
		getContentPane().add(btnYellow);
		
		btnBlue.setBackground(Color.CYAN);
		btnBlue.setBounds(247, 50, 20, 20);
		btnBlue.addMouseListener(new ColorListener(btnBlue, "blue"));
		getContentPane().add(btnBlue);
		
		setBounds(0, 0, 498, 140);
		
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
		
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveProperties();
					setVisible(false);
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					loadProperties();
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private void saveProperties() throws FileNotFoundException, IOException {
		props.setProperty("tempDir", txtTempDir.getText());
		props.setProperty("borderColor", strColor);
		props.setProperty("trayMessages", trayMessages);
		props.setProperty("recordMouse", recordMouse);
		
		props.store(new FileOutputStream("screencapture.properties"), null);
	}
	
	private void loadProperties() throws IOException {
		props.load(inputPropFile);

		strTempDir = props.getProperty("tempDir", "c:" + File.separatorChar +"ScreenCapture");
		strColor = props.getProperty("borderColor", "magenta");
		trayMessages = props.getProperty("trayMessages", "true");
		recordMouse = props.getProperty("recordMouse", "true");
		
		txtTempDir.setText(strTempDir);
		ckTrayMessages.setSelected(trayMessages.equals("true"));
		ckRecordMouse.setSelected(recordMouse.equals("true"));
		
		btnMagenta.setBorder(strColor.equals("magenta") ? new LineBorder(Color.BLACK) : null);
		btnRed.setBorder(strColor.equals("red") ? new LineBorder(Color.BLACK) : null);
		btnGreen.setBorder(strColor.equals("green") ? new LineBorder(Color.BLACK) : null);
		btnYellow.setBorder(strColor.equals("yellow") ? new LineBorder(Color.BLACK) : null);
		btnBlue.setBorder(strColor.equals("blue") ? new LineBorder(Color.BLACK) : null);
	}
	
	public String getTempDir() {
		return strTempDir;
	}
	
	public boolean isTrayMessages() {
		return trayMessages.equals("true");
	}

	public boolean isRecordMouse() {
		return recordMouse.equals("true");
	}
	
	public Color getBorderColor() {
		Color result = Color.MAGENTA;
		if (strColor.equals("magenta"))
			result = Color.MAGENTA;
		else if (strColor.equals("red"))
			result = Color.RED;
		else if (strColor.equals("green"))
			result = Color.GREEN;
		else if (strColor.equals("yellow"))
			result = Color.YELLOW;
		else if (strColor.equals("blue"))
			result = Color.CYAN;
			
		return result;
	}
	
	class ColorListener implements MouseListener {
		
		private JPanel selected;
		private String value;
		
		private ColorListener(JPanel selected, String value) {
			this.selected = selected;
			this.value = value;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			btnMagenta.setBorder(null);
			btnRed.setBorder(null);
			btnGreen.setBorder(null);
			btnYellow.setBorder(null);
			btnBlue.setBorder(null);
			
			selected.setBorder(new LineBorder(Color.BLACK));
			strColor = value;
		}
		
		@Override
		public void mousePressed(MouseEvent e) { }
		@Override
		public void mouseExited(MouseEvent e) { }
		@Override
		public void mouseEntered(MouseEvent e) { }
		@Override
		public void mouseClicked(MouseEvent e) { }
	};
}
