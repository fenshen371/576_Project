import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.csci.playVideo.VideoLoader;
import edu.csci.wav.WavWork;

public class Filter {
	
    private int width = 480;
    private int height = 270;
	private String videoFilePath = "../data/data_test1.rgb";
	private String wavFilePath = "../data/data_test1.wav";
	private double adLengthThreshold = 7.0; //videos shorter than which will be considered as ad; unit: second
	
    VideoLoader loader;

	public Filter(String rgbFile, String wavFile){
		videoFilePath = rgbFile;
		wavFilePath = wavFile;
	}
    public ArrayList<Integer> countDiffArray(String videoFilePath, String wavFilePath) throws FileNotFoundException{
    	loader = new VideoLoader();
		WavWork ww = new WavWork(wavFilePath);
    	ArrayList<Integer> diffs = new ArrayList<Integer>();
    	InputStream is = new FileInputStream(videoFilePath);
    	
    	BufferedImage lastFrame = loader.getFrame(is, width, height);
    	int frameCount = -1;
    	while(loader.notLastFrame){
    		frameCount++;
    		BufferedImage thisFrame = loader.getFrame(is, width, height);
    		int diff = countDiff(lastFrame, thisFrame);
    		double hist = countHist(lastFrame, thisFrame);
    		diffs.add(diff);
    		
    		if(diff > 18000000 && hist > 5000){
				double currentTime = frameCount / 30.0;
				System.out.println("=================================================");
	    		System.out.println("current time: " + String.valueOf(currentTime));
				System.out.println("audio changed: " + String.valueOf(ww.volumeChanged(Math.round(currentTime * 1000.0))));
	    		System.out.println("RGB: " + diff + ", H: " + hist);
    		}
    		
    		lastFrame = thisFrame;
    	}
		return diffs;
    }

	/**
	 * Call this function to determine ad parts of the video.
	 * The path of video file and wav file are given by member variables (videoFilePath and wavFilePath) of this Filter class.
	 * @return an array of frame indices where ad begins and ends. So every time you read from this array, you need to read two numbers!
	 * @throws FileNotFoundException
	 */
	public ArrayList<Integer> getAdBoundaries() throws FileNotFoundException {
		ArrayList<Integer> switches = getSwitchPoints();
		boolean[] marks = markAd(switches);
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (int i = 1; i < marks.length; i++) {
			if (marks[i] == false && marks[i-1] == true) {
				if (res.size() == 0) res.add(0);
				res.add(switches.get(i-1));
			} else if (marks[i] == true && marks[i-1] == false){
				res.add(switches.get(i-1));
			}
		}
		return res;
	}

	/**
	 * This function returns an array of frame indices where the camera switches to a different scene,
	 * The last value in the array is the index of the last frame, in other word, the number of frames of the rgb file.
	 */
	public ArrayList<Integer> getSwitchPoints() throws FileNotFoundException{
		loader = new VideoLoader();
		ArrayList<Integer> switches = new ArrayList<Integer>();
		InputStream is = new FileInputStream(videoFilePath);

		BufferedImage lastFrame = loader.getFrame(is, width, height);

		int frameCount = -1;
		while(loader.notLastFrame){
			frameCount++;
			BufferedImage thisFrame = loader.getFrame(is, width, height);
			int diff = countDiff(lastFrame, thisFrame);
			double hist = countHist(lastFrame, thisFrame);

			if(diff > 18000000 && hist > 5000){
				switches.add(frameCount);
			}
			lastFrame = thisFrame;
		}

		//add the index of the last frame in the video
		switches.add(frameCount);
		return switches;
	}


	/**
	 * This function determines which range is ad
	 * @param switches: frame indices array returned by getSwitchPoints function
	 * @return An array of boolean marks. Res[i] indicates if range(switches[i-1], switches[i]) is ad
	 */
	public boolean[] markAd(ArrayList<Integer> switches){
		ArrayList<Double> durations = getDurations(switches);
		boolean[] soundChanges = getSoundChangesArray(switches);
		boolean[] marks = new boolean[durations.size()];

		//1st iteration only uses duration information. Durations shorter than 7s are considered ad
		for (int i = 0; i < durations.size(); i++) {
			if (durations.get(i) > adLengthThreshold) marks[i] = false;
			else marks[i] = true;
		}
		//2nd iteration uses sound change information and expands non-ad range if marks[i] != marks[i+1] and sound doesn't change there
		boolean somethingChanged = true;
		while (somethingChanged) {
			somethingChanged = false;
			for (int i = 0; i < durations.size() - 1; i++) {
				if (marks[i] != marks[i + 1] && soundChanges[i] == false) {
					marks[i] = false;
					marks[i + 1] = false;
					somethingChanged = true;
				}
			}
		}
		return marks;
	}

	/**
	 * This function checks if sound changes in the time points in switches array
	 * @param switches
	 * @return a boolean array. Res[i] indicates if sound changes at frame switches[i]
	 */
	private boolean[] getSoundChangesArray(ArrayList<Integer> switches) {
		WavWork ww = new WavWork(wavFilePath);
		boolean[] res = new boolean[switches.size()];
		for (int i = 0; i < switches.size(); i++) {
			int frameIndex = switches.get(i);
			long time = Math.round(frameIndex * 1000.0 / 30.0);
			res[i] = ww.volumeChanged(time);
		}
		return res;
	}

	private ArrayList<Double> getDurations(ArrayList<Integer> switches) {
		//durations[i] = (switches[i] - switches[i-1]) / 30
		ArrayList<Double> durations = new ArrayList<Double>();
		for (int i = 0; i < switches.size(); i++) {
			if (i == 0) {
				int ind = switches.get(i);
				durations.add(ind / 30.0);
			} else {
				int dur = switches.get(i) - switches.get(i-1);
				durations.add(dur / 30.0);
			}
		}
		return durations;
	}
    
    public int countDiff(BufferedImage lastFrame, BufferedImage thisFrame){
    	int countR = 0;
    	int countG = 0;
    	int countB = 0;
    	for(int i = 0; i < width; i++){
    		for(int j = 0; j < height; j++){
    			Color lastColor = new Color(lastFrame.getRGB(i, j));
    			Color thisColor = new Color(thisFrame.getRGB(i, j));
    			countR = countR + Math.abs(lastColor.getRed() - thisColor.getRed());
    			countG = countG + Math.abs(lastColor.getGreen() - thisColor.getGreen());
    			countB = countB + Math.abs(lastColor.getBlue() - thisColor.getBlue());
    		}
    	}
    	int count = countR + countG + countB;
//    	if(count > 20000000){
//    		System.out.printf("%.4f ", frameCount / 30.0);
//    		System.out.println(": " + count + ", R: " + countR + ", G: " + countG + ", B: " + countB);
//    	}
    	return count;
    }
    
    public double countHist(BufferedImage lastFrame, BufferedImage thisFrame){
    	double count = 0;
    	int[] lastHist = new int[256];
    	int[] thisHist = new int[256];
    	
    	for(int i = 0; i < width; i++){
    		for(int j =0; j < height; j++){
    			Color lastColor = new Color(lastFrame.getRGB(i, j));
    			Color thisColor = new Color(thisFrame.getRGB(i, j));
    			int lastY = getY(lastColor);
    			int thisY = getY(thisColor);
    			lastHist[lastY]++;
    			thisHist[thisY]++;
    		}
    	}
    	for(int i = 0; i < 256; i++){
    		count = count + (lastHist[i] - thisHist[i]) * (lastHist[i] - thisHist[i]);
    	}
    	return Math.sqrt(count);
    }
    
    public int getY(Color c){
    	int y = (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue() );
    	if(y > 255){
    		y = 255;
    	}
    	return y;
    }
	/*
	public static void main(String[] args) throws FileNotFoundException{
		Filter filter = new Filter();
		//filter.countDiffArray("../data/data_test2.rgb", "../data/data_test2.wav");
		ArrayList<Integer> boundaries = filter.getAdBoundaries();
		for (int i = 0; i < boundaries.size(); i = i + 2) {
			double st = boundaries.get(i) / 30.0;
			double ed = boundaries.get(i+1) / 30.0;
			System.out.println("Ad range: " + String.valueOf(st) + ", " + String.valueOf(ed));
		}
	}
	*/
}
