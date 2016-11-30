import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.csci.playVideo.VideoLoader;

public class Filter {
	
    private int width = 480;
    private int height = 270;
    private int frameCount = 0;
	
    VideoLoader loader;
    
    
    
    public ArrayList<Integer> countDiffArray(String videoFilePath) throws FileNotFoundException{
    	loader = new VideoLoader();
    	ArrayList<Integer> diffs = new ArrayList<Integer>();
    	InputStream is = new FileInputStream(videoFilePath);
    	
    	BufferedImage lastFrame = loader.getFrame(is, width, height);
    	
    	while(loader.notLastFrame){
    		frameCount++;
    		BufferedImage thisFrame = loader.getFrame(is, width, height);
    		int diff = countDiff(lastFrame, thisFrame);
    		double hist = countHist(lastFrame, thisFrame);
    		diffs.add(diff);
    		
    		if(diff > 18000000 && hist > 5000){
	    		System.out.printf("%.4f ", frameCount / 30.0);
	    		System.out.println("RGB: " + diff + ", H: " + hist);
    		}
    		
    		lastFrame = thisFrame;
    	}
		return diffs;
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
	
	public static void main(String[] args) throws FileNotFoundException{
		Filter filter = new Filter();
		filter.countDiffArray("dataset/data_test2.rgb");
	}
}
