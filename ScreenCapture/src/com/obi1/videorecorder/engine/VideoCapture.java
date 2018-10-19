package com.obi1.videorecorder.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.MediaLocator;

import com.obi1.videorecorder.exception.VideoRecorderException;
import com.obi1.videorecorder.util.Obi1Utils;

/**
 * Classe para realizar a captura de vídeo baseado em prints de tela.
 *
 */
public final class VideoCapture {

	private static final int CAPTURE_INTERVAL = 25;
	private static boolean recording;
	private static String baseUrlRender;
	private static String baseUrlSave;
	private static String fileName;
	private static String scriptName;
	private static String strDate;
	
	private static GraphicsConfiguration grConfig;
	private static ClassLoader cl;
	
	private static int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private static int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

	private static final String PRE_IMG = File.separatorChar + "preImg.jpg";
	
	//Array para manter a ordem das imagens
	private static ArrayList<String> imgNames;
	
	//Variáveis para o posicionamento dos textos na tela de apresentação do video
	private static final int NUM30 = 30;
	private static final int NUM45 = 45;
	private static final int NUM60 = 60;
	private static final int NUM120 = 120;
	
	//Número de frames em que a pré-img deve aparecer no vídeo, determinando o tempo de aparição
	private static final int FRAMES_PRE_IMG = 35;
	
	//Framerate do vídeo
	private static final int FRAME_RATE = 10;
	
	/**
	 * Construtor oculto para a classe não ser instanciada.
	 */
	private VideoCapture() { }
	
	/**
	 * Interrompe a captura de telas e renderiza o video.
	 * @throws IOException exception
	 * @throws VideoRecorderException exception
	 */
	public static File stopRecord(boolean cleanup) throws IOException, VideoRecorderException {
		recording = false;
		
		if (scriptName != null)
			generateTitleImage(scriptName, strDate, baseUrlRender + PRE_IMG, grConfig, cl);
		makeVideo("file:" + baseUrlSave + File.separatorChar + fileName + ".mov");
		
		if (cleanup)
			Obi1Utils.cleanupDir(baseUrlRender);
		
		return new File(baseUrlSave + File.separatorChar + fileName + ".mov");
	}
	
	/**
	 * Inicia a captura de telas para a geração do video.
	 * @param scriptName nome do script que está sendo executado
	 * @param strDate data em formato string
	 * @param baseUrlRender local das imagens
	 * @param baseUrlSave local para o video ser salvo
	 * @param fileName nome do arquivo do video
	 * @param grConfig graphicsConfiguration do java
	 * @param cl classLoader
	 * @throws Exception exception
	 */
	public static void startRecord(final boolean recordMouse, String scriptName, String strDate, String baseUrlRender, String baseUrlSave, String fileName, GraphicsConfiguration grConfig, ClassLoader cl) throws Exception {
		VideoCapture.baseUrlRender = baseUrlRender;
		VideoCapture.baseUrlSave = baseUrlSave;
		VideoCapture.fileName = fileName;
		VideoCapture.scriptName = scriptName;
		VideoCapture.strDate = strDate;
		VideoCapture.grConfig = grConfig; 
		VideoCapture.cl = cl;
		
		imgNames = new ArrayList<String>();
		
		Obi1Utils.cleanupDir(baseUrlRender);
		final Thread recordThread = new Thread() {
			@Override
			public void run() {
				try {
					final Robot robot = new Robot();
					final Image cursor = ImageIO.read(getClass().getClassLoader().getResource("images/cursor.png").openStream());
					int cnt = 0;
					
					String fileName = "";
					
					while (cnt == 0 || isRecording()) {
						final int x = MouseInfo.getPointerInfo().getLocation().x;
						final int y = MouseInfo.getPointerInfo().getLocation().y;

						final BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
						final Graphics2D graphics2D = screenShot.createGraphics();
						if(recordMouse) graphics2D.drawImage(cursor, x, y, null);
						
						if (!new File(VideoCapture.baseUrlRender).exists())
							new File(VideoCapture.baseUrlRender).mkdirs();
						
						fileName = VideoCapture.baseUrlRender + File.separatorChar + "S" + System.currentTimeMillis() + ".jpg";
						ImageIO.write(screenShot, "JPG", new File(fileName));
						imgNames.add(fileName);
						
						if (cnt == 0) {
							recording = true;
							cnt = 1;
						}
						
						Thread.sleep(CAPTURE_INTERVAL);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		recordThread.start();
	}

	/**
	 * Gera a imagem de titulo do video contendo informações sobre o script executado.
	 * @param scriptName nome do script
	 * @param strDate data em string
	 * @param srcSaveImage imagem a ser salva
	 * @param gConfig graphicsConfiguration do java
	 * @param cl classloader
	 * @throws IOException exception
	 */
	private static void generateTitleImage(String scriptName, String strDate, String srcSaveImage, GraphicsConfiguration gConfig, ClassLoader cl) throws IOException {
		Insets insets;
		Dimension dTela;
		
		insets = Toolkit.getDefaultToolkit().getScreenInsets(gConfig);
		dTela = Toolkit.getDefaultToolkit().getScreenSize();
		final int widthTela = dTela.width - (insets.left + insets.top);
		final int heightTela = dTela.height - (insets.top + insets.bottom);
		
		final BufferedImage buffer = new BufferedImage(widthTela, heightTela, BufferedImage.TYPE_INT_RGB);
		final BufferedImage logo = ImageIO.read(cl.getResource("images/preImg.png").openStream());
		
		final Graphics g = buffer.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, widthTela, heightTela);
		
		final Font font = new Font("Tahoma", Font.PLAIN, 20);
		g.setColor(Color.DARK_GRAY);
		g.setFont(font);
		g.drawString(scriptName, ((widthTela / 2) - (logo.getWidth() / 2)) - NUM45, ((heightTela / 2) - ((logo.getHeight() / 2)) + NUM30));
		g.drawString("Time: " + strDate, ((widthTela / 2) - (logo.getWidth() / 2)) - NUM45, ((heightTela / 2) - ((logo.getHeight() / 2)) + NUM60));
		
		g.drawImage(logo, ((widthTela / 2) - (logo.getWidth() / 2)), (((heightTela / 2) - ((logo.getHeight() / 2))) - NUM120), null);
		
		ImageIO.write(buffer, "jpg", new File(srcSaveImage)); 
	}
	
	/**
	 * Renderiza as imagens capturadas gerando o arquivo de video.
	 * @param movFile local para a geração do vídeo
	 * @throws MalformedURLException exception
	 * @throws VideoRecorderException exception
	 */
	public static void makeVideo(String movFile) throws MalformedURLException, VideoRecorderException {
		final VideoRender imageToMovie = new VideoRender();
		final Vector<String> imgLst = new Vector<String>();
		
		for (int i = 0; i < FRAMES_PRE_IMG; i++) {
			imgLst.add(baseUrlRender + PRE_IMG);
		}
		
		for (String fileName : imgNames) {
			imgLst.add(fileName);
		}
		
		// Generate the output media locators.
		MediaLocator oml;
		if ((oml = VideoRender.createMediaLocator(movFile)) == null) {
			throw new MalformedURLException("Cannot build media locator from: " + movFile);
		}
		
		imageToMovie.doIt(screenWidth, screenHeight, FRAME_RATE, imgLst, oml);
	}

	/**
	 * Retorna boolean informando se a captura de telas está em execução.
	 * @return boolean informando se a captura de telas está em execução
	 */
	public static boolean isRecording() {
		return recording;
	}
}