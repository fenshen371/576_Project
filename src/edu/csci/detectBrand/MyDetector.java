package edu.csci.detectBrand;
import edu.csci.utils.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class MyDetector {
	String rgbFilePath;

	int width;
	int height;
	int fps;

	JFrame display_frame;
	JPanel display_panel;
	JLabel label1;
	JLabel label2;
	JLabel label3;

	public MyDetector(String videoPath) {
		rgbFilePath = videoPath;
		width = 480;
		height = 270;
		fps = 30;

		display_frame = new JFrame("Brand Detector");
		display_panel = new JPanel();
		display_panel.setLayout(new BorderLayout());
		display_frame.add(display_panel);
		display_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		display_frame.pack();
		display_frame.setVisible(true);
		label1 = new JLabel();
		label2 = new JLabel();
		display_panel.add(label1, BorderLayout.WEST);
		display_panel.add(label2, BorderLayout.EAST);
	}

	public ArrayList<Integer> detect(String brandImage) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		int frame_index = 0;
		int frame_skip = fps / 2;

		InputStream is = null;
		try {
			is = new FileInputStream(rgbFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (is == null) {
			return results;
		}

		VideoUtil.skip_frame(is, width, height, 60 * 30);
		while (true) {
			BufferedImage image = VideoUtil.get_frame(is, width, height);
			if (image == null) {
				break;
			}
			BufferedImage edge_image = get_edge(image);

			label1.setIcon(new ImageIcon(image));
			label1.repaint();

			label2.setIcon(new ImageIcon(edge_image));
			label2.repaint();

			display_frame.pack();

			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			VideoUtil.skip_frame(is, width, height, frame_skip);
			frame_index += frame_skip;
		}
		return results;
	}

	private BufferedImage get_edge(BufferedImage original_image) {
		BufferedImage edge_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 1; x < width - 1; ++x) {
			for (int y = 1; y < height - 1; ++y) {
				int gradient = calc_gradient(original_image, x, y);
				if (gradient > 400)
				{
					edge_image.setRGB(x, y, Color.black.getRGB());
				} else {
					edge_image.setRGB(x, y, Color.white.getRGB());
				}
			}
		}
		return edge_image;
	}

	private int calc_gradient(BufferedImage image, int x, int y) {
		int[][] matrix = new int[3][3];
		matrix[0][0] = gray_scale(new Color(image.getRGB(x - 1, y - 1)));
		matrix[0][1] = gray_scale(new Color(image.getRGB(x, y - 1)));
		matrix[0][2] = gray_scale(new Color(image.getRGB(x + 1, y - 1)));
		matrix[1][0] = gray_scale(new Color(image.getRGB(x - 1, y)));
		matrix[1][1] = gray_scale(new Color(image.getRGB(x, y)));
		matrix[1][2] = gray_scale(new Color(image.getRGB(x + 1, y)));
		matrix[2][0] = gray_scale(new Color(image.getRGB(x - 1, y + 1)));
		matrix[2][1] = gray_scale(new Color(image.getRGB(x, y + 1)));
		matrix[2][2] = gray_scale(new Color(image.getRGB(x + 1, y + 1)));

		int x_gradient = matrix[0][2] + 2 * matrix[1][2] + matrix[2][2];
		x_gradient -= matrix[0][0] + 2 * matrix[1][0] + matrix[2][0];

		int y_gradient = matrix[0][0] + 2 * matrix[0][1] + matrix[0][2];
		y_gradient -= matrix[2][0] + 2 * matrix[2][1] + matrix[2][2];

		return (int)Math.sqrt(x_gradient * x_gradient + y_gradient * y_gradient);
	}

	private int gray_scale(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
		if (gray > 255) {
			gray = 255;
		}
		return gray;
	}
}

