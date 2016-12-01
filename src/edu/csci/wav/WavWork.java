package edu.csci.wav;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by rujun on 11/24/2016.
 */
public class WavWork {
    private File wavFile;
    private AudioFormat format;
    private int channelNum;
    private double frameRate;
    private int N = 1024 * 2; //size of FFT and sample window

    //path of the wav file you want to check
    public WavWork(String filePath){
        try{
            wavFile = new File(filePath);
            AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
            format = stream.getFormat();
            channelNum = format.getChannels();
            frameRate = format.getFrameRate();
            stream.close();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //return true if two frequency values have huge difference, otherwise return false
    private boolean hugeDifference(double a, double b, double threshold){
        if (Math.max(a, b) * threshold > Math.min(a, b)) return true;
        return false;
    }

    //apply window function before FFT
    private int[] HanningWindow(int[] data) {
        int size = data.length;
        for (int i = 0; i < size; i++)
            data[i] = (int)(data[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / size)));
        return data;
    }

    //capture the primal frequency in the next several milliseconds
    private double[] getMagnitude(double startTimeInMilliSecond) {
        long framesToSkip = Math.round((startTimeInMilliSecond / 1000) * frameRate);
        int bufferSize = 48000;
        int[] buffer = new int[bufferSize * channelNum];
        double[] magnitude = new double[N / 2];
        try {
            WavFile curWav = WavFile.openWavFile(wavFile);
            while (framesToSkip > 0) {
                if (framesToSkip >= bufferSize){
                    curWav.readFrames(buffer, bufferSize);
                    framesToSkip -= bufferSize;
                }
                else {
                    curWav.readFrames(buffer, (int)framesToSkip);
                    framesToSkip = 0;
                }
            }
            int[] preData = new int[N];
            curWav.readFrames(preData, N);
            curWav.close();
            preData = HanningWindow(preData);
            Complex[] data = new Complex[N]; //input data buffer
            for (int i = 0; i < N; i++)
                data[i] = new Complex(preData[i], 0);

            //perform fft
            data = FFT.fft(data);

            //calculate power spectrum
            for (int i = 1; i < N / 2; i++) {
                double re = data[i].re();
                double im = data[i].im();
                magnitude[i] = Math.sqrt(re * re + im * im);
            }
            return magnitude;
        } catch (WavFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return magnitude;
    }

    private double getMainFreq(double[] magnitude) {
        double maxVal = -1;
        int maxInd = 0;
        for (int i = 1; i < N / 2; i++) {
            if (magnitude[i] > maxVal) {
                maxVal = magnitude[i];
                maxInd = i;
            }
        }
        return maxInd * frameRate / N;
    }

    private double L2Distance(double[] a, double[] b) {
        //calculate start index and end index of frequencies between 1khz and 4khz
        //int st = (int)Math.round(Math.floor(1000 * N / frameRate));
        //int ed = (int)Math.round(Math.ceil(4000 * N / frameRate));
        int st = 1;
        int ed = a.length/2;

        double dist = 0;
        for (int i = st; i < ed; i++) {
            dist += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(dist);
    }

    private double entropy(double[] a) {
        //calculate start index and end index of frequencies between 1khz and 4khz
        //int st = (int)Math.round(Math.floor(1000 * N / frameRate));
        //int ed = (int)Math.round(Math.ceil(4000 * N / frameRate));
        int st = 1;
        int ed = a.length/2;
        double sum = 0;
        for (int i = st; i < ed; i++) sum += a[i];
        if (sum < 0.001) return 0;
        double entropy = 0;
        for (int i = st; i < ed; i++) {
            double prob = a[i] / sum;
            entropy += -(prob * Math.log(prob));
        }
        return entropy;
    }

    /**call this function to check if the sound changes before and after the given time point.
     *the parameter BreakpointInNanoTime is the time point you want to check
     * It is the number of nano seconds pasted from the beginning of the wav file
     */
    public boolean volumeChanged(long startTimeInMilliSecond){
        double[] test1 = getMagnitude(startTimeInMilliSecond - 100);
        double freq1 = getMainFreq(test1);
        System.out.println("frequency 1: " + String.valueOf(freq1));
        System.out.println("entropy 1: " + String.valueOf(entropy(test1)));
        double[] test2 = getMagnitude(startTimeInMilliSecond + 100);
        double freq2 = getMainFreq(test2);
        System.out.println("frequency 2: " + String.valueOf(freq2));
        System.out.println("entropy 2: " + String.valueOf(entropy(test2)));
        System.out.println("L2 distance: " + String.valueOf(L2Distance(test1, test2)));
        return hugeDifference(freq1, freq2, 0.3333);
    }
}
