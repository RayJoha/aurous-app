package me.aurous.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author https://github.com/nikkiii
 * @author Andrew
 *
 *
 */
public class Utils {
	/**
	 *
	 * An enum containing operating system types
	 *
	 *
	 *
	 */
	public static enum OperatingSystem {
		LINUX, SOLARIS, WINDOWS, MAC, UNKNOWN
	}

	public static boolean isNull(final Object obj) {
		return obj == null;
	}

	private static String hashString(final String s) {
		byte[] hash = null;
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			hash = md.digest(s.getBytes());

		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		final StringBuilder sb = new StringBuilder();
		for (final byte element : hash) {
			final String hex = Integer.toHexString(element);
			if (hex.length() == 1) {
				sb.append(0);
				sb.append(hex.charAt(hex.length() - 1));
			} else {
				sb.append(hex.substring(hex.length() - 2));
			}
		}
		return sb.toString();
	}

	public static ImageIcon getSongIcon(final String url) {
		// Constants.DATA_PATH + "icons"
		final String urlHash = hashString(url);
		final Path path = Paths.get(Constants.DATA_PATH + "icons/" + urlHash);
		if (Files.exists(path)) {
			ImageIcon imageIcon = new ImageIcon(path.toAbsolutePath()
					.toString());
			final Image image = imageIcon.getImage();
			final Image newimg = image.getScaledInstance(40, 40,
					java.awt.Image.SCALE_SMOOTH);
			imageIcon = new ImageIcon(newimg);
			return imageIcon;
		} else {
			try {
				BufferedImage bi = ImageIO.read(new URL(url));
				bi = getScaledInstance(bi, 40, 40,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
				final File outputfile = new File(path.toAbsolutePath()
						.toString());
				ImageIO.write(bi, "png", outputfile);

				ImageIcon imageIcon = new ImageIcon(path.toAbsolutePath()
						.toString());
				final Image image = imageIcon.getImage(); // transform it
				final Image newimg = image.getScaledInstance(40, 40,
						java.awt.Image.SCALE_SMOOTH);
				imageIcon = new ImageIcon(newimg);
				return imageIcon;
			} catch (final IOException e) {
				return null;

			}
		}

	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img
	 *            The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(final Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		final BufferedImage bimage = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		final Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public static BufferedImage makeRoundedCorner(final BufferedImage image,
			final int cornerRadius) {
		final int w = image.getWidth();
		final int h = image.getHeight();
		final BufferedImage output = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);

		final Graphics2D g2 = output.createGraphics();

		// This is what we want, but it only does hard-clipping, i.e. aliasing
		// g2.setClip(new RoundRectangle2D ...)

		// so instead fake soft-clipping by first drawing the desired clip shape
		// in fully opaque white with antialiasing enabled...
		g2.setComposite(AlphaComposite.Src);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius,
				cornerRadius));

		// ... then compositing the image on top,
		// using the white shape from above as alpha source
		g2.setComposite(AlphaComposite.SrcAtop);
		g2.drawImage(image, 0, 0, null);

		g2.dispose();

		return output;
	}

	public static BufferedImage getScaledInstance(final BufferedImage img,
			final int targetWidth, final int targetHeight, final Object hint,
			final boolean higherQuality) {
		final int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && (w > targetWidth)) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && (h > targetHeight)) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			final BufferedImage tmp = new BufferedImage(w, h, type);
			final Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while ((w != targetWidth) || (h != targetHeight));

		return ret;
	}

	/**
	 * @author Andrew Simple method to check if a laine is contained in a file
	 */
	public static boolean containsLine(final String fileName,
			final String lineStop) {
		try {
			final Scanner s = new Scanner(System.in);

			final File f = new File(fileName);
			final Scanner numScan = new Scanner(f);

			String line;

			while (numScan.hasNext()) {
				line = numScan.nextLine();
				if (line.contains(lineStop)) {

					s.close();
					numScan.close();
					return true;
				}
			}
			s.close();
			numScan.close();
			return false;
		} catch (final FileNotFoundException e) {
			return true;
		}
	}

	public static String stripExtension(final String s) {
		return (s != null) && (s.lastIndexOf(".") > 0) ? s.substring(0,
				s.lastIndexOf(".")) : s;
	}

	/**
	 * @author Andrew
	 *
	 *         Calculates a Spotify-like shorthand date
	 *
	 */
	public static String getDate() {
		final Date now = new Date();

		final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d",
				Locale.US);
		final String asWeek = dateFormat.format(now);
		return asWeek;
	}

	/**
	 * Center a frame on the main display
	 *
	 * @param frame
	 *            The frame to center
	 */
	public static void centerFrameOnMainDisplay(final JFrame frame) {
		final GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		final GraphicsDevice[] screens = ge.getScreenDevices();
		if (screens.length < 1) {
			return; // Silently fail.
		}
		final Rectangle screenBounds = screens[0].getDefaultConfiguration()
				.getBounds();
		final int x = (int) ((screenBounds.getWidth() - frame.getWidth()) / 2);
		final int y = (int) ((screenBounds.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}

	/**
	 * A method to get the unix time...
	 *
	 * @return The current time in seconds
	 */
	public static long currentTimeSeconds() {
		return (System.currentTimeMillis() / 1000);
	}

	/**
	 * Finds the first supported program in the list (for UNIX-like platforms
	 * only).
	 *
	 * @param kind
	 *            The kind of program, used in the exception message if no
	 *            suitable program could be found.
	 * @param names
	 *            The array of program names to try.
	 * @return The first supported program from the array of names.
	 * @throws Exception
	 *             if no supported program could be found.
	 */
	private static String findSupportedProgram(final String kind,
			final String[] names) throws Exception {
		for (final String name : names) {
			final Process process = Runtime.getRuntime().exec(
					new String[] { "which", name });
			if (process.waitFor() == 0) {
				return name;
			}
		}

		throw new Exception("Unable to find supported " + kind);
	}

	/**
	 * Get the current platform
	 *
	 * @return The current platform
	 */
	public static OperatingSystem getPlatform() {
		final String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			return OperatingSystem.WINDOWS;
		}
		if (osName.contains("mac")) {
			return OperatingSystem.MAC;
		}
		if (osName.contains("solaris")) {
			return OperatingSystem.SOLARIS;
		}
		if (osName.contains("sunos")) {
			return OperatingSystem.SOLARIS;
		}
		if (osName.contains("linux")) {
			return OperatingSystem.LINUX;
		}
		if (osName.contains("unix")) {
			return OperatingSystem.LINUX;
		}
		return OperatingSystem.UNKNOWN;
	}

	/**
	 * Get the system architecture
	 *
	 * @return The system architecture integer
	 */
	public static int getSystemArch() {
		final String archs = System.getProperty("os.arch");
		return Integer.parseInt(archs.substring(archs.length() - 2));
	}

	/**
	 * Returns icon loaded from example icons package.
	 *
	 * @param path
	 *            path to the icon inside icons package
	 * @return loaded icon
	 */
	public static ImageIcon loadIcon(final String path) {
		final String key = Utils.class.getCanonicalName() + ":" + path;
		if (!iconsCache.containsKey(key)) {
			iconsCache
					.put(key,
							new ImageIcon(Utils.class.getResource("/resources/"
									+ path)));
		}
		return iconsCache.get(key);
	}

	/**
	 * Will take a url such as http://www.youtube.com and return www.youtube.com
	 *
	 * @param url
	 * @return
	 */
	public static String getHost(final String url) {
		if ((url == null) || (url.length() == 0)) {
			return "";
		}

		int doubleslash = url.indexOf("//");
		if (doubleslash == -1) {
			doubleslash = 0;
		} else {
			doubleslash += 2;
		}

		int end = url.indexOf('/', doubleslash);
		end = end >= 0 ? end : url.length();

		final int port = url.indexOf(':', doubleslash);
		end = ((port > 0) && (port < end)) ? port : end;

		return url.substring(doubleslash, end);
	}

	/**
	 * Get the base domain for a given host or url. E.g. cs4625.vk.me will
	 * return vk.me
	 *
	 * @param host
	 * @return
	 */
	public static String getBaseDomain(final String url) {
		final String host = getHost(url);

		int startIndex = 0;
		int nextIndex = host.indexOf('.');
		final int lastIndex = host.lastIndexOf('.');
		while (nextIndex < lastIndex) {
			startIndex = nextIndex + 1;
			nextIndex = host.indexOf('.', startIndex);
		}
		if (startIndex > 0) {
			return host.substring(startIndex);
		} else {
			return host;
		}
	}

	/**
	 * Open a file using {@link Desktop} if supported, or a manual
	 * platform-specific method if not.
	 *
	 * @param file
	 *            The file to open.
	 * @throws Exception
	 *             if the file could not be opened.
	 */
	public static void openFile(final File file) throws Exception {
		final Desktop desktop = Desktop.isDesktopSupported() ? Desktop
				.getDesktop() : null;
		if ((desktop != null) && desktop.isSupported(Desktop.Action.OPEN)) {
			desktop.open(file);
		} else {
			final OperatingSystem system = Utils.getPlatform();
			switch (system) {
			case MAC:
			case WINDOWS:
				Utils.openURL(file.toURI().toURL());
				break;
			default:
				final String fileManager = Utils.findSupportedProgram(
						"file manager", Utils.FILE_MANAGERS);
				Runtime.getRuntime().exec(
						new String[] { fileManager, file.getAbsolutePath() });
				break;
			}
		}
	}

	public static String getStackTraceString(final Throwable e,
			final String indent) {
		final StringBuilder sb = new StringBuilder();
		sb.append(e.toString());
		sb.append("\n");

		final StackTraceElement[] stack = e.getStackTrace();
		if (stack != null) {
			for (final StackTraceElement stackTraceElement : stack) {
				sb.append(indent);
				sb.append("\tat ");
				sb.append(stackTraceElement.toString());
				sb.append("\n");
			}
		}

		final Throwable[] suppressedExceptions = e.getSuppressed();
		// Print suppressed exceptions indented one level deeper.
		if (suppressedExceptions != null) {
			for (final Throwable throwable : suppressedExceptions) {
				sb.append(indent);
				sb.append("\tSuppressed: ");
				sb.append(getStackTraceString(throwable, indent + "\t"));
			}
		}

		final Throwable cause = e.getCause();
		if (cause != null) {
			sb.append(indent);
			sb.append("Caused by: ");
			sb.append(getStackTraceString(cause, indent));
		}

		return sb.toString();
	}

	/**
	 * Open a URL using java.awt.Desktop or a couple different manual methods
	 *
	 * @param url
	 *            The URL to open
	 * @throws Exception
	 *             If an error occurs attempting to open the url
	 */
	public static void openURL(final URL url) throws Exception {
		final Desktop desktop = Desktop.isDesktopSupported() ? Desktop
				.getDesktop() : null;
				if ((desktop != null) && desktop.isSupported(Desktop.Action.BROWSE)) {
					desktop.browse(url.toURI());
				} else {
					final OperatingSystem system = Utils.getPlatform();
					switch (system) {
					case MAC:
						Class.forName("com.apple.eio.FileManager")
						.getDeclaredMethod("openURL",
								new Class[] { String.class })
								.invoke(null, new Object[] { url.toString() });
						break;
					case WINDOWS:
						Runtime.getRuntime()
						.exec(new String[] { "rundll32",
								"url.dll,FileProtocolHandler", url.toString() });
						break;
					default:
						final String browser = Utils.findSupportedProgram("browser",
								Utils.BROWSERS);
						Runtime.getRuntime().exec(
								new String[] { browser, url.toString() });
						break;
					}
				}
	}

	public static String readFile(final String path, final Charset encoding) {

		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (final IOException e) {

			e.printStackTrace();
		}
		return new String(encoded, encoding);
	}

	/**
	 * Trim the string's start/finish of the specified character
	 *
	 * @param str
	 *            The string
	 * @param ch
	 *            The character
	 * @return The trimmed string
	 */
	public static String trim(String str, final char ch) {
		if ((Utils.isNull(str)) || str.isEmpty()) {
			return str;
		} else if (str.length() == 1) {
			return str.charAt(0) == ch ? "" : str;
		}
		try {
			if (str.charAt(0) == ch) {
				str = str.substring(1);
			}
			final int l = str.length() - 1;
			if (str.charAt(l) == ch) {
				str = str.substring(0, l);
			}
			return str;
		} catch (final Exception e) {
			return str;
		}
	}

	/**
	 * Make each word in a string uppercase
	 *
	 * @param string
	 *            The string to parse
	 * @return The formatted string
	 */
	public static String ucwords(final String string) {
		final StringBuilder out = new StringBuilder();
		final String[] split = string.split(" ");
		for (int i = 0; i < split.length; i++) {
			final String str = split[i];
			out.append(Character.toUpperCase(str.charAt(0)));
			if (str.length() > 1) {
				out.append(str.substring(1).toLowerCase());
			}
			if (i < (split.length - 1)) {
				out.append(" ");
			}
		}
		return out.toString();
	}

	public static void writeFile(final String content, final String path) {

		try {
			final PrintWriter writer = new PrintWriter(path);
			writer.print("");
			writer.close();
			Files.write(Paths.get(path), content.getBytes(),
					StandardOpenOption.CREATE);
		} catch (final IOException e) {

			e.printStackTrace();
		}
	}

	public static boolean isJSONValid(final String test) {
		try {
			new JSONObject(test);
		} catch (final JSONException ex) {
			try {
				new JSONArray(test);
			} catch (final JSONException ex1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * icons cache.
	 */
	private static final Map<String, ImageIcon> iconsCache = new HashMap<String, ImageIcon>();

	static final String errMsg = "Error opening browser";
	/**
	 * Open a URL in the users default browser
	 *
	 * @param url
	 *            The URL to open
	 * @throws Exception
	 *             If an error occurs attempting to open the url
	 */
	/*
	 * public static void openURL(String url) { try { //attempt to use Desktop
	 * library from JDK 1.6+ Class<?> d = Class.forName("java.awt.Desktop");
	 * d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
	 * d.getDeclaredMethod("getDesktop").invoke(null), new Object[]
	 * {java.net.URI.create(url)}); //above code mimicks:
	 * java.awt.Desktop.getDesktop().browse() } catch (Exception ignore) {
	 * //library not available or failed String osName =
	 * System.getProperty("os.name"); try { if (osName.startsWith("Mac OS")) {
	 * Class.forName("com.apple.eio.FileManager").getDeclaredMethod( "openURL",
	 * new Class[] {String.class}).invoke(null, new Object[] {url}); } else if
	 * (osName.startsWith("Windows")) Runtime.getRuntime().exec(
	 * "rundll32 url.dll,FileProtocolHandler " + url); else { //assume Unix or
	 * Linux String browser = null; for (String b : BROWSERS) if (browser ==
	 * null && Runtime.getRuntime().exec(new String[] {"which",
	 * b}).getInputStream().read() != -1) Runtime.getRuntime().exec(new String[]
	 * {browser = b, url}); if (browser == null) throw new
	 * Exception(Arrays.toString(BROWSERS)); } } catch (Exception e) {
	 * JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString()); } }
	 *
	 *
	 * }
	 */// bad codei s bad,

	/**
	 * The cached operating system
	 */
	public static final OperatingSystem SYSTEM = Utils.getPlatform();

	/**
	 * A list of popular file managers
	 */
	private static final String[] FILE_MANAGERS = new String[] { "xdg-open",
			"nautilus", "dolphin", "thunar", "pcmanfm", "konqueror" };

	/**
	 * A list of popular browsers
	 */
	private static final String[] BROWSERS = new String[] { "xdg-open",
			"google-chrome", "chromium", "firefox", "opera", "epiphany",
			"konqueror", "conkeror", "midori", "kazehakase", "mozilla" };

}