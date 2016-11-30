package edu.csci.processVideo;
import edu.csci.utils.*;
import edu.csci.wav.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class MyProcessor {
	String rgbFilePath;
	String wavFilePath;

	int width;
	int height;
	int fps;

	int sample_rate;
	int bits_per_sample;


	public MyProcessor(String videoPath, String audioPath) {
		rgbFilePath = videoPath;
		wavFilePath = audioPath;
		width = 480;
		height = 270;
		fps = 30;

		sample_rate = 48000;
		bits_per_sample = 16;
	}

	public void process() {
		ArrayList<Integer> possible_shots = video_process();
		ArrayList<Double> break_shots = audio_process(possible_shots);
		// TODO: If two break shots has a distance of 10 to 20sec, and during that we detected
		// more than 5 possible shots, then we regard those frames between the two break shots
		// as ad.
	}

	public void save(String videoPath, String audioPath) {

	}

	private ArrayList<Integer> video_process() {
		ArrayList<Integer> possible_shots = new ArrayList<Integer>();
		int frame_count = 1;
		try {
			InputStream is = new FileInputStream(rgbFilePath);
			BufferedImage prev_image = VideoUtil.get_frame(is, width, height);
			while (true) {
				BufferedImage current_image = VideoUtil.get_frame(is, width, height);
				if (current_image == null) {
					break;
				}
				double[] visual_difference = getVisualDifference(prev_image, current_image);
				prev_image = current_image;
				if (visual_difference[0] > 5000000 && visual_difference[1] > 5000)
				{
					possible_shots.add(frame_count);
				}
				//System.out.println(frame_count + ", " + visual_difference[0] + ", " + visual_difference[1]);
				++frame_count;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return possible_shots;
	}

	private ArrayList<Double> audio_process(ArrayList<Integer> possible_shots) {
		ArrayList<Double> break_shots = new ArrayList<Double>();
		WavWork wav_decision = new WavWork(wavFilePath);
		for (int i = 0; i < possible_shots.size(); ++i) {
			double time = possible_shots.get(i) / 30.0;
			String time_str = String.format("%.2f", time);
			System.out.println("time" + time_str);
			Long time_in_ns = Math.round(time * 1e9);
			if (wav_decision.volumeChanged(time_in_ns)) {
				System.out.println("break shots");
			}
		}
		return break_shots;
	}

	// The sum of absolute difference and histogram difference.
	private double[] getVisualDifference(BufferedImage prev_image, BufferedImage current_image) {
		double[] result = new double[2];

		double red_weight = 0.30;
		double green_weight = 0.59;
		double blue_weight = 0.11;

		int prev_histogram[] = new int[256];
		int current_histogram[] = new int[256];

		int r_sad = 0;
		int g_sad = 0;
		int b_sad = 0;
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int prev_pixel = prev_image.getRGB(x, y);
				Color c1 = new Color(prev_pixel);
				int r1 = c1.getRed();
				int g1 = c1.getGreen();
				int b1 = c1.getBlue();
				double gray1 = red_weight * r1 + green_weight * g1 + blue_weight * b1;
				int gray_int1 = (int)Math.round(gray1);
				if (gray_int1 > 255) {
					gray_int1 = 255;
				}
				++prev_histogram[gray_int1];

				int current_pixel = current_image.getRGB(x, y);
				Color c2 = new Color(current_pixel);
				int r2 = c2.getRed();
				int g2 = c2.getGreen();
				int b2 = c2.getBlue();
				double gray2 = red_weight * r2 + green_weight * g2 + blue_weight * b2;
				int gray_int2 = (int)Math.round(gray2);
				if (gray_int2 > 255) {
					gray_int2 = 255;
				}
				++current_histogram[gray_int2];

				r_sad += Math.abs(r1 - r2);
				g_sad += Math.abs(g1 - g2);
				b_sad += Math.abs(b1 - b2);
			}
		}
		result[0] = red_weight * r_sad + green_weight * g_sad + blue_weight * b_sad;
		for (int i = 0; i < 256; ++i) {
			double diff = Math.abs(prev_histogram[i] - current_histogram[i]);
			result[1] += diff * diff;
		}
		result[1] = Math.sqrt(result[1]);
		return result;
	}

}
