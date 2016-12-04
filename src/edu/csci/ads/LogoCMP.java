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
	int frequency = 2;
	int fps = 30;
	int logoCache[][];
	int frameCache[][];
	JFrame frame = new JFrame("display");
	
	public void findLogoInVideo(String logoPath, String videoPath) throws FileNotFoundException{
    	VideoLoader loader = new VideoLoader();
    	InputStream is = new FileInputStream(logoPath);
    	BufferedImage logo = loader.getFrame(is, width, height);
    	
    	ImageAnalyzer analyzer = new ImageAnalyzer(logo);
    	Vector<Integer> colors = analyzer.getDominateColors();
    	
    	logoCache = new int[height][width];
    	frameCache = new int[height][width];
    	
    	for(int i = 0; i < width; i++){
    		for(int j = 0; j < height; j++){
    			logoCache[j][i] = logo.getRGB(i, j);
    		}
    	}
    	
    	is = new FileInputStream(videoPath);
    	loader.notLastFrame = true;
		
    	
    	while(loader.notLastFrame){
    		
    		frameCount++;
    		BufferedImage thisFrame = loader.getFrame(is, width, height);
    		
    		BufferedImage oldFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    		
    		
    		//skip frame
//    		if(frameCount < 170 * 30){
//    			continue;
//    		}
    		
    		if(frameCount % Math.max((fps / frequency), 1) != 0){
    			continue;
    		}

    		System.out.printf("%.4f : ", frameCount / 30.0);
    		
    		Vector<Integer> colorCount = new Vector<Integer>();
    		for(int i = 0; i < colors.size(); i++){
    			colorCount.add(0);
    		}
    		
        	for(int i = 0; i < width; i++){
        		for(int j = 0; j < height; j++){
        			Color c = new Color(thisFrame.getRGB(i, j));
        			oldFrame.setRGB(i, j, c.getRGB());
        			int similarIndex = -1;
        			Integer minDis = 10000000;
        			for(int index = 0; index < colors.size(); index++){
        				int rgb = colors.get(index);
        				
        				if(analyzer.caculateColorDis(c, new Color(rgb)) < minDis){
        					minDis = analyzer.caculateColorDis(c, new Color(rgb));
        					similarIndex = index;
        				}
        			}
        			

        			if(minDis < analyzer.colorThreshold * 2){
        				thisFrame.setRGB(i, j, colors.get(similarIndex));
        				colorCount.set(similarIndex, colorCount.get(similarIndex) + 1);
        			}else{
        				thisFrame.setRGB(i, j, Color.gray.getRGB());
        			}
        			
        		}
        	}
        	

    		show(oldFrame, thisFrame);
        	
    		//keep img for a while
        	long start = System.currentTimeMillis();
        	while(System.currentTimeMillis() - start < 1500){
        		
        	}
        	
        	int minValue = 100000000;
        	for(Integer count : colorCount){
        		minValue = Math.min(minValue, count);
        	}
        	
        	System.out.println(colorCount + " " + minValue);
        	
        	
        	
    	}
    	
	}
	
	public void show(BufferedImage logo, BufferedImage thisFrame){
		JPanel  panel = new JPanel ();
	    panel.add (new JLabel (new ImageIcon (logo)));
	    panel.add (new JLabel (new ImageIcon (thisFrame)));
	    
	    frame.getContentPane().add (panel);
	    frame.pack();
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		LogoCMP cmp = new LogoCMP();
		cmp.findLogoInVideo("src/data/starbucks_logo.rgb", "src/data/data_test1.rgb");
	}

}
