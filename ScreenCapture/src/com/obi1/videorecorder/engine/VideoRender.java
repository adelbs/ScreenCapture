package com.obi1.videorecorder.engine;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

import com.obi1.videorecorder.exception.VideoRecorderException;

/**
 * Classe para renderizar as capturas de tela em um vídeo.
 *
 */
public final class VideoRender implements ControllerListener, DataSinkListener {

	private Object waitSync = new Object();
	private boolean stateTransitionOK = true;

	private Object waitFileSync = new Object();
	private boolean fileDone;
	private boolean fileSuccess = true;

	/**
	 * Renderiza o video.
	 * @param width largura da tela
	 * @param height altura da tela
	 * @param frameRate framerate
	 * @param inFiles array de imagens
	 * @param outML medialocator
	 * @throws MalformedURLException exception
	 * @throws VideoRecorderException exception
	 */
	public void doIt(int width, int height, int frameRate, Vector<String> inFiles, MediaLocator outML) throws MalformedURLException, VideoRecorderException {
		
		final ImageDataSource ids = new ImageDataSource(width, height, frameRate, inFiles);

		Processor p;

		try {
			p = Manager.createProcessor(ids);
		}
		catch (Exception e) {
			throw new VideoRecorderException(e, "Cannot create a processor from the data source.");
		}

		p.addControllerListener(this);

		// Put the Processor into configured state so we can set
		// some processing options on the processor.
		p.configure();
		if (!waitForState(p, Processor.Configured)) {
			throw new VideoRecorderException("Failed to configure the processor.");
		}

		// Set the output content descriptor to QuickTime.
		p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

		// Query for the processor for supported formats.
		// Then set it on the processor.
		final TrackControl[] tcs = p.getTrackControls();
		final Format[] f = tcs[0].getSupportedFormats();
		if (f == null || f.length <= 0) {
			throw new VideoRecorderException("The mux does not support the input format: " + tcs[0].getFormat());
		}

		tcs[0].setFormat(f[0]);

		//System.err.println("Setting the track format to: " + f[0]);

		// We are done with programming the processor. Let's just
		// realize it.
		p.realize();
		if (!waitForState(p, Controller.Realized)) {
			throw new VideoRecorderException("Failed to realize the processor.");
		}

		// Now, we'll need to create a DataSink.
		DataSink dsink = createDataSink(p, outML);
		dsink.addDataSinkListener(this);
		fileDone = false;

		System.out.println("Generating the video : " + outML.getURL().toString());

		// OK, we can now start the actual transcoding.
		try {
			p.start();
			dsink.start();
		}
		catch (IOException e) {
			throw new VideoRecorderException("IO error during processing");
		}

		// Wait for EndOfStream event.
		waitForFileDone();

		// Cleanup.
		try {
			dsink.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		p.removeControllerListener(this);
	}

	/**
	 * Create the DataSink.
	 *
	 * @param p p
	 * @param outML outML
	 * @return the data sink
	 * @throws VideoRecorderException 
	 */
	private DataSink createDataSink(Processor p, MediaLocator outML) throws VideoRecorderException {

		DataSource ds = p.getDataOutput();

		if (ds == null) {
			throw new VideoRecorderException("Failed to create a DataSink (null DataSource) for the given output MediaLocator: " + outML);
		}

		DataSink dsink;

		try {
			//System.err.println("- create DataSink for: " + outML);
			dsink = Manager.createDataSink(ds, outML);
			dsink.open();
		} 
		catch (Exception e) {
			throw new VideoRecorderException("Failed to create a DataSink for the given output MediaLocator: " + outML);
		}

		return dsink;
	}

	/**
	 * Block until the processor has transitioned to the given state. Return
	 * false if the transition failed.
	 *
	 * @param p p
	 * @param state state
	 * @return true, if wait for state
	 */
	private boolean waitForState(Processor p, int state) {
		synchronized (waitSync) {
			try {
				while (p.getState() < state && stateTransitionOK) {
					waitSync.wait();
				}
			} 
			catch (Exception e) {
				//do nothing
				System.out.println(e.getMessage());
			}
		}
		return stateTransitionOK;
	}

	/**
	 * Controller Listener.
	 *
	 * @param evt evt
	 */
	public void controllerUpdate(ControllerEvent evt) {

		if (evt instanceof ConfigureCompleteEvent
				|| evt instanceof RealizeCompleteEvent
				|| evt instanceof PrefetchCompleteEvent) {
			synchronized (waitSync) {
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		} 
		else if (evt instanceof ResourceUnavailableEvent) {
			synchronized (waitSync) {
				stateTransitionOK = false;
				waitSync.notifyAll();
			}
		} 
		else if (evt instanceof EndOfMediaEvent) {
			evt.getSourceController().stop();
			evt.getSourceController().close();
		}
	}

	/**
	 * Block until file writing is done.
	 *
	 * @return true, if wait for file done
	 */
	private boolean waitForFileDone() {
		synchronized (waitFileSync) {
			try {
				while (!fileDone) {
					waitFileSync.wait();
				}
			} 
			catch (Exception e) {
				//do nothing
				System.out.println(e.getMessage());
			}
		}
		return fileSuccess;
	}

	/**
	 * Event handler for the file writer.
	 *
	 * @param evt evt
	 */
	public void dataSinkUpdate(DataSinkEvent evt) {

		if (evt instanceof EndOfStreamEvent) {
			synchronized (waitFileSync) {
				fileDone = true;
				waitFileSync.notifyAll();
			}
		} 
		else if (evt instanceof DataSinkErrorEvent) {
			synchronized (waitFileSync) {
				fileDone = true;
				fileSuccess = false;
				waitFileSync.notifyAll();
			}
		}
	}

	/**
	 * Create a media locator from the given string.
	 *
	 * @param url url
	 * @return the media locator
	 */
	static MediaLocator createMediaLocator(String url) {

		MediaLocator ml = new MediaLocator(url);
		String fileProtocol = "file:";
		
		if (url.indexOf(":") <= 0 || ml == null) {
			if (url.startsWith(File.separator)) {
				ml = new MediaLocator(fileProtocol + url);
			} 
			else {
				final String file = fileProtocol + System.getProperty("user.dir") + File.separator + url;
				ml = new MediaLocator(file);
			}
		}
		
		return ml;
	}

	// /////////////////////////////////////////////
	// Inner classes.
	// /////////////////////////////////////////////

	/**
	 * A DataSource to read from a list of JPEG image files and turn that into a
	 * stream of JMF buffers. The DataSource is not seekable or positionable.
	 */
	private class ImageDataSource extends PullBufferDataSource {

		private ImageSourceStream[] streams;

		/**
		 * Instancia um novo ImageDataSource.
		 *
		 * @param width width
		 * @param height height
		 * @param frameRate frameRate
		 * @param images images
		 */
		public ImageDataSource(int width, int height, int frameRate, Vector<String> images) {
			streams = new ImageSourceStream[1];
			streams[0] = new ImageSourceStream(width, height, frameRate, images);
		}

		@Override
		public void setLocator(MediaLocator source) { }

		@Override
		public MediaLocator getLocator() {
			return null;
		}

		/**
		 * Content type is of RAW since we are sending buffers of video frames
		 * without a container format.
		 *
		 * @return ContentType ContentType
		 */
		public String getContentType() {
			return ContentDescriptor.RAW;
		}

		@Override
		public void connect() { }

		@Override
		public void disconnect() { }

		@Override
		public void start() { }

		@Override
		public void stop() { }

		/**
		 * Return the ImageSourceStreams.
		 *
		 * @return Streams Streams
		 */
		public PullBufferStream[] getStreams() {
			return streams;
		}

		/**
		 * We could have derived the duration from the number of frames and
		 * frame rate. But for the purpose of this program, it's not necessary.
		 *
		 * @return Duration Duration
		 */
		public Time getDuration() {
			return DURATION_UNKNOWN;
		}

		@Override
		public Object[] getControls() {
			return new Object[0];
		}

		@Override
		public Object getControl(String type) {
			return null;
		}
	}

	/**
	 * The source stream to go along with ImageDataSource.
	 */
	private class ImageSourceStream implements PullBufferStream {

		private Vector<String> images;
		@SuppressWarnings("unused")
		private int width;
		@SuppressWarnings("unused")
		private int height;
		private VideoFormat format;

		//index of the next image to be read.
		private int nextImage;
		private boolean ended;

		/**
		 * Instancia um novo ImageSourceStream.
		 *
		 * @param width width
		 * @param height height
		 * @param frameRate frameRate
		 * @param images images
		 */
		public ImageSourceStream(int width, int height, int frameRate, Vector<String> images) {
			this.width = width;
			this.height = height;
			this.images = images;

			format = new VideoFormat(VideoFormat.JPEG, new Dimension(width,
					height), Format.NOT_SPECIFIED, Format.byteArray, (float) frameRate);
		}

		/**
		 * We should never need to block assuming data are read from files.
		 *
		 * @return true, if will read block
		 */
		public boolean willReadBlock() {
			return false;
		}

		/**
		 * This is called from the Processor to read a frame worth of video
		 * data.
		 *
		 * @param buf buf
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void read(Buffer buf) throws IOException {

			// Check if we've finished all the frames.
			if (nextImage >= images.size()) {
				// We are done. Set EndOfMedia.
				//System.err.println("Done reading all images.");
				buf.setEOM(true);
				buf.setOffset(0);
				buf.setLength(0);
				ended = true;
				return;
			}

			final String imageFile = (String) images.elementAt(nextImage);
			nextImage++;

			//System.err.println("  - reading image file: " + imageFile);

			// Open a random access file for the next image.
			RandomAccessFile raFile;
			raFile = new RandomAccessFile(imageFile, "r");

			byte[] data = null;

			// Check the input buffer type & size.

			if (buf.getData() instanceof byte[]) {
				data = (byte[]) buf.getData();
			}

			// Check to see the given buffer is big enough for the frame.
			if (data == null || data.length < raFile.length()) {
				data = new byte[(int) raFile.length()];
				buf.setData(data);
			}

			// Read the entire JPEG image from the file.
			raFile.readFully(data, 0, (int) raFile.length());

			//System.err.println("    read " + raFile.length() + " bytes.");

			buf.setOffset(0);
			buf.setLength((int) raFile.length());
			buf.setFormat(format);
			buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME);

			// Close the random access file.
			raFile.close();
		}

		/**
		 * Return the format of each video frame. That will be JPEG.
		 *
		 * @return Format Format
		 */
		public Format getFormat() {
			return format;
		}

		@Override
		public ContentDescriptor getContentDescriptor() {
			return new ContentDescriptor(ContentDescriptor.RAW);
		}

		@Override
		public long getContentLength() {
			return 0;
		}

		@Override
		public boolean endOfStream() {
			return ended;
		}

		@Override
		public Object[] getControls() {
			return new Object[0];
		}

		@Override
		public Object getControl(String type) {
			return null;
		}
	}
}