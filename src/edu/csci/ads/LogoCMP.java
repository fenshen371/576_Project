package edu.csci.ads;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.csci.playVideo.VideoLoader;

public class LogoCMP {

	
	int width = 480;
	int height = 270;
	int frameCount = 0;
	int freq = 15;
	int fps = 30;
	int logoCache[][];
	int frameCache[][];
	double scoreThreshold = 1.3;
	double ratioThreshold = 1.4;
	JFrame frame = new JFrame("display");
	
	public Vector<Integer> findLogoInVideo(String logoPath, String videoPath) throws FileNotFoundException, InterruptedException{
    	VideoLoader logoLoader = new VideoLoader();
    	InputStream is = new FileInputStream(logoPath);
    	BufferedImage logo = logoLoader.getFrame(is, width, height);

    	ImageAnalyzer analyzer = new ImageAnalyzer();
    	Vector<Integer> colors = analyzer.getDominateColors(logo);
    	
    	logoCache = new int[height][width];
    	frameCache = new int[height][width];
    	
    	for(int i = 0; i < width; i++){
    		for(int j = 0; j < height; j++){
    			logoCache[j][i] = logo.getRGB(i, j);
    		}
    	}
    	
    	logo = analyzer.getFilteredImage(logo, colors);
    	is = new FileInputStream(videoPath);
    	VideoLoader loader = new VideoLoader();
    	Vector<Double> scores = new Vector<Double>();
    	while(loader.notLastFrame){
    		
    		frameCount++;
    		BufferedImage thisFrame = loader.getFrame(is, width, height);

    		BufferedImage newFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    		BufferedImage edgeFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    		
    		//skip frame
//    		if(frameCount < 290 * 30){
//    			continue;
//    		}


    		
    		if(frameCount % freq != 0){
    			continue;
    		}
    		

    		System.out.printf("%.4f : ", frameCount / 30.0);
    		newFrame = analyzer.getFilteredImage(thisFrame, colors);
    		//for debug
    		show(thisFrame,newFrame);
    		scores.add(analyzer.calculateScore(newFrame, logo));
    	}
    	
    	System.out.println(scores);
    	return getAdsFrames(scores);
	}
	
	public Vector<Integer> getAdsFrames(Vector<Double> scores){
		
		Vector<Integer> adsFrame = new Vector<Integer>();
		
    	double sum = 0.0;
    	int count = 0;
    	for(int i = 0; i < scores.size(); i++){
    		if(scores.get(i) > 0.1){
    			count++;
    			sum = sum + scores.get(i);
    		}
    	}
    	double avg = sum / count;
		
    	int lastAdsFrame = 0;
    	
    	for(int i = 0; i < scores.size(); i++){
    		if(i - lastAdsFrame <= 20){
    			continue;
    		}
    		
    		if(scores.get(i) > scoreThreshold && scores.get(i) / avg > ratioThreshold ){
    			adsFrame.add(freq * (i+1));
    			lastAdsFrame = i;
    		}
    	}
    	
    	System.out.println(adsFrame);
		return adsFrame;
		
	}
	
	public void show(BufferedImage img1, BufferedImage img2){
		JPanel  panel = new JPanel ();
	    panel.add (new JLabel (new ImageIcon (img1)));
	    panel.add (new JLabel (new ImageIcon (img2)));
	    
	    frame.getContentPane().add (panel);
	    frame.pack();
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
	}
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		LogoCMP cmp = new LogoCMP();
		cmp.findLogoInVideo("src/data/subway_logo.rgb", "src/data/data_test1.rgb");

	}

}
