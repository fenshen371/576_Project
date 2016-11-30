package edu.csci.playVideo;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by rujun on 11/21/2016.
 */
public class VideoLoader implements Runnable {
    private Thread t;
    private String threadName;
    private String videoFilePath;
    private JFrame player;
    private boolean isPlaying = true;
    private int width = 480;
    private int height = 470;
    private int fps = 30;
    public boolean notLastFrame = true;

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setFps(int val){
        this.fps = val;
    }
    
    public VideoLoader(){
    	
    }

    VideoLoader(String name, String path, JFrame frame) {
        threadName = name;
        videoFilePath = path;
        player = frame;
        player.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //read one frame from the rgb file
    public BufferedImage getFrame(InputStream is, int width, int height){
        int frame_size = width * height * 3;
        byte[] bytes = new byte[frame_size];
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            //check if we have reached the end of the video
            int bitsRead = is.read(bytes, 0, frame_size);
            if (bitsRead < frame_size) notLastFrame = false;

            int ind = 0;
            for(int y = 0; y < height; y++){

                for(int x = 0; x < width; x++){
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    frame.setRGB(x,y,pix);
                    ind++;
                }
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frame;
    }

    public void pauseOrResume(){
        isPlaying = !isPlaying;
    }

    public void run() {
        long frameDuration = Math.round(1e9 / fps);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        JLabel lbImg = new JLabel(new ImageIcon(img));

        try {
            InputStream is = new FileInputStream(videoFilePath);

            long lastTime = System.nanoTime();
            while (notLastFrame){
                boolean thisVariableIsUsedHereToRefreshTheValueOfIsPlaying = isPlaying;
                if (isPlaying) {
                    long time = System.nanoTime();
                    //time to fresh
                    if (time - lastTime >= frameDuration) {
                        img = getFrame(is, width, height);
                        lbImg = new JLabel(new ImageIcon(img));
                        player.getContentPane().remove(0);
                        player.getContentPane().add(lbImg, 0);
                        player.revalidate();
                        player.repaint();
                        lastTime = time;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
