package edu.csci.ads;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.csci.playVideo.VideoLoader;

public class ImageAnalyzer {
	
	public int width = 480;
	public int height = 270;
	public int colorNumber = 5;
	public int numThreshold = 1000;
	public int colorThreshold = 30;
	public int defaultColor = Color.GRAY.getRGB();
	public int frameDisThreshold = 20;
	public int logoDisThreshold = 40;
	public int blockThreshold = 20;
	public int blockSizeThreshold = 400;
	private int[] dx = {0, 0, 1, -1};
	private int[] dy = {-1, 1, 0, 0};
	JFrame frame = new JFrame("blocks");

	public Vector<Integer> getDominateColors(BufferedImage img){
		
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
	
	public BufferedImage getFilteredImage(BufferedImage oldImage, Vector<Integer> colors){
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
    	for(int i = 0; i < width; i++){
    		for(int j = 0; j < height; j++){
    			Color c = new Color(oldImage.getRGB(i, j));
    			
    			int similarIndex = -1;
    			Integer minDis = 10000000;
    			for(int index = 0; index < colors.size(); index++){
    				int rgb = colors.get(index);
    				
    				if(caculateColorDis(c, new Color(rgb)) < minDis){
    					minDis = caculateColorDis(c, new Color(rgb));
    					similarIndex = index;
    				}
    			}
    			

    			if(minDis < colorThreshold * 2){
    				img.setRGB(i, j, colors.get(similarIndex));
    			}else{
    				img.setRGB(i, j, defaultColor);
    			}
    			
    		}
    	}
		return img;
	}
	
	public double calculateScore(BufferedImage frame, BufferedImage logo) throws InterruptedException{
		
		boolean visited[][] = new boolean[width][height];
		
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				visited[i][j] = false;
			}
		}
		double maxScore = 0.0;
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++)
			if(frame.getRGB(i, j) != defaultColor && visited[i][j] == false){
				visited[i][j] = true;
				//System.out.println(i + " " + " " + j + " " + frame.getRGB(i, j));
				Vector<Integer> loc = bfs(i, j, frame.getRGB(i, j), frameDisThreshold, frame, visited);
				loc.add(frame.getRGB(i, j));
				
				if(preFilter(loc)){
					continue;
				}
				
				boolean logoVisited[][] = new boolean[width][height];
				for(int x = 0; x < width; x++){
					for(int y = 0; y < height; y++ ){
						logoVisited[x][y] = false;
					}
				}
				
				BufferedImage block = frame.getSubimage(loc.get(1), loc.get(2), loc.get(3), loc.get(4));
				//System.out.println(loc);
				for(int x = 0; x < width; x++){
					for(int y = 0; y < height; y++)
					if(logo.getRGB(x, y) == frame.getRGB(i, j) && logoVisited[i][j] == false){
						
						Vector<Integer> logoLoc = bfs(x, y, frame.getRGB(i, j), logoDisThreshold, logo, logoVisited);
						logoLoc.add(logo.getRGB(x, y));
						
						if(preFilter(logoLoc)){
							continue;
						}
						
						BufferedImage logoBlock = logo.getSubimage(logoLoc.get(1), logoLoc.get(2), logoLoc.get(3), logoLoc.get(4));
						
						double score = 0.0;
						for(int ii = 0; ii < block.getWidth(); ii++){
							for(int jj = 0; jj < block.getHeight(); jj++){
								int rgb = block.getRGB(ii, jj);
								int targetX = (int) (ii * 1.0 / block.getWidth() * logoBlock.getWidth());
								int targetY = (int) (jj * 1.0 / block.getHeight() * logoBlock.getHeight());
								targetX = Math.min(targetX, logoBlock.getWidth() - 1);
								targetY = Math.min(targetY, logoBlock.getHeight() - 1);
								int logoRgb = logoBlock.getRGB(targetX, targetY);
								
								score = score + calculateScore(rgb, logoRgb, frame.getRGB(i, j));
								
							}
						}
						
						//for debug
						show(block, logoBlock);
						
						score = score / (block.getWidth() * block.getHeight());
						maxScore = Math.max(score, maxScore);
					}
				}
				
			}
		}
		System.out.println(maxScore);
		return maxScore;
	}
	
	private boolean preFilter(Vector<Integer> loc) {
		if(loc.get(0) < blockThreshold){
			return true;
		}
		
		double lenX = loc.get(3);
		double lenY = loc.get(4);
		if( (lenX / lenY > 8) || (lenY / lenX > 8) || lenX * lenY < blockSizeThreshold){
			return true;
		}
		
		return false;
	}
	
	private double calculateScore(int rgb, int logoRgb, int frameRgb){
		if(rgb == frameRgb){
			if(rgb == logoRgb){
				return 2;
			}else{
				return 0;
			}
		}else{
			if(rgb == logoRgb){
				return 3;
			}else{
				if(logoRgb == frameRgb){
					return 0;
				}else{
					return 1;
				}
			}
		}
	}

	private Vector<Integer> bfs(int x, int y, int rgb, int length, BufferedImage img, boolean visited[][]){
		
		int minX = x, minY = y, maxX = x, maxY = y, count = 0;
		Queue<Integer> q = new ArrayDeque<Integer>();
		Vector<Integer> ret = new Vector<Integer>();
		q.add(hashXY(x,y));
		
		while(q.isEmpty() == false){
			x = q.remove();
			y = x % width;
			x = x / width;
			count++;
			for(int i = 0; i < length; i++){
				for(int j = -length; j <= length; j++){
					int newX = x + i, newY = y + j;
					
					if(newX >=0 && newX < width && newY >=0 && newY < height && visited[newX][newY] ==false && img.getRGB(newX, newY) == rgb){
						visited[newX][newY] = true;
						minX = Math.min(minX, newX);
						minY = Math.min(minY, newY);
						maxX = Math.max(maxX, newX);
						maxY = Math.max(maxY, newY);
						q.add(hashXY(newX,newY));
					}
					
				}
			}
			
		}
		ret.add(count);
		ret.add(minX);
		ret.add(minY);
		ret.add(maxX - minX + 1);
		ret.add(maxY - minY + 1);
		return ret;
	}
	
	private int hashXY(int x, int y){
		return x * width + y;
	}
	
	public void show(BufferedImage img1, BufferedImage img2){
		JPanel  panel = new JPanel ();
	    panel.add (new JLabel (new ImageIcon (img1)));
	    panel.add (new JLabel (new ImageIcon (img2)));

		frame.getContentPane().removeAll();
	    frame.getContentPane().add (panel);
	    frame.pack();
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
	}
	
	
	public int caculateColorDis(Color a, Color b){
		return Math.abs(a.getRed() - b.getRed()) +
			   Math.abs(a.getGreen() - b.getGreen()) +
			   Math.abs(a.getBlue() - b.getBlue());
	}
}
