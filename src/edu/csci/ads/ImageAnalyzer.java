package edu.csci.ads;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Vector;

import edu.csci.playVideo.VideoLoader;

public class ImageAnalyzer {
	
	public int width = 480;
	public int height = 270;
	public int colorNumber = 5;
	public int numThreshold = 1000;
	public int colorThreshold = 30;
	BufferedImage img;
    
	public ImageAnalyzer(BufferedImage img){
		this.img = img;
	}
    
	public Vector<Integer> getDominateColors(){
		
		HashMap<Integer, Integer> colorCount = new HashMap<Integer, Integer>();
		
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				int rgb = img.getRGB(i, j);
				if(colorCount.containsKey(rgb)){
					colorCount.put(rgb, colorCount.get(rgb)+1);
				}else{
					colorCount.put(rgb, 1);
				}
			}
		}
		
		Iterator<Entry<Integer, Integer>> it = colorCount.entrySet().iterator();
		
		Vector<Integer> colors = new Vector<Integer>();
		
		while(it.hasNext()){
			
			Entry<Integer, Integer> pair = it.next();
			
			if(pair.getValue() > width * height / numThreshold){
//				colors.add(pair.getKey());

				Color newC = new Color(pair.getKey());
				
				boolean hasSimiliarColor = false;
				
				for(int i = 0; i < colors.size(); i++){
					Color oldC = new Color(colors.get(i));
					
					if(caculateColorDis(newC, oldC) < colorThreshold){
						hasSimiliarColor = true;
						break;
					}
				}
				
				if(hasSimiliarColor == false){
					colors.addElement(pair.getKey());

					System.out.println(colors.size() + " " + newC.getRed() + " " + newC.getGreen() + " " + newC.getBlue());
				}
				
			}
		}
		
		
		return colors;
		
	}
	
	public int caculateColorDis(Color a, Color b){
		return Math.abs(a.getRed() - b.getRed()) +
			   Math.abs(a.getGreen() - b.getGreen()) +
			   Math.abs(a.getBlue() - b.getBlue());
	}
}
