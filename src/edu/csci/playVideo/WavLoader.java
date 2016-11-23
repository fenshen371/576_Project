package edu.csci.playVideo;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;


/**
 * Created by rujun on 11/22/2016.
 */
public class WavLoader implements  Runnable {
    private Thread t;
    private String threadName;
    private String wavFilePath;
    private boolean isPlaying = true;
    private Clip clip;
    private long lastRunClipTime = 0;

    WavLoader(String name, String path) {
        threadName = name;
        wavFilePath = path;
    }


    public void pauseOrResume(){
        isPlaying = !isPlaying;
        if (isPlaying) {
            clip.setMicrosecondPosition(lastRunClipTime);
            clip.start();
        } else{
            lastRunClipTime = clip.getMicrosecondPosition();
            clip.stop();
        }
    }


    public void run() {
        try{
            File wavFile = new File(wavFilePath);
            AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
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
