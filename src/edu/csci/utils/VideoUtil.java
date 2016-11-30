package edu.csci.utils;
import java.awt.image.BufferedImage;
import java.io.*;

public class VideoUtil {
	public static BufferedImage get_frame(InputStream is, int width, int height) {
		boolean eof = false;
		int frame_size = width * height * 3;
		byte[] raw_content = new byte[frame_size];
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
			int bits_read = is.read(raw_content);
			if (bits_read < frame_size) {
				eof = true;
			} else {
				int index = 0;
				for (int y = 0; y < height; ++y) {
					for (int x = 0; x < width; ++x) {
						byte r = raw_content[index];
						byte g = raw_content[index + height * width];
						byte b = raw_content[index + height * width * 2];
						int pixel = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						img.setRGB(x, y, pixel);
						index++;
					}
				}
			}
		} catch (FileNotFoundException e) {
				e.printStackTrace();
		} catch (IOException e) {
				e.printStackTrace();
		}
		if (eof) {
			return null;
		}
		return img;
	}
}

