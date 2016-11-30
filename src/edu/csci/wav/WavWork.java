package edu.csci.wav;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by rujun on 11/24/2016.
 */
public class WavWork {
    private File wavFile;
    private AudioFormat format;
    private RandomAccessFile audioStream;
    private long audioLengthInBytes;
    private int sampleSize;
    private int channelNum;
    private double sampleRate;
    private double MaxSampleValue;

    //path of the wav file you want to check
    public WavWork(String filePath){
        try{
            wavFile = new File(filePath);
            AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
            format = stream.getFormat();
            sampleSize = format.getSampleSizeInBits();
            channelNum = format.getChannels();
            sampleRate = format.getSampleRate();
            switch (sampleSize){
                case 8: MaxSampleValue = Byte.MAX_VALUE * 2; break;
                case 16: MaxSampleValue = Short.MAX_VALUE * 2; break;
                case 32: MaxSampleValue = Integer.MAX_VALUE * 2; break;
                default: MaxSampleValue = Long.MAX_VALUE * 2; break;
            }
            stream.close();
            audioStream = new RandomAccessFile(wavFile, "r");
            audioLengthInBytes = audioStream.length();
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

    //capture the primal frequency in the next several milliseconds
    private double highestFrequency(long bytesToSkip) {
        int N = 1024; //size of FFT and sample window
        Complex[] data = new Complex[N]; //input data buffer
        double[] magnitude = new double[N];
        try{
            //read audio data
            audioStream.seek(bytesToSkip);
            for (int i = 0; i < N; i++) {
                //sample size is supposed to be 8 bits(byte), 16 bits(short), 32 bits(int) or 64 bits(long)
                if (sampleSize == 8)
                    data[i] = new Complex(audioStream.readUnsignedByte(), 0);
                else if (sampleSize == 16)
                    data[i] = new Complex(audioStream.readUnsignedShort(), 0);
                else if (sampleSize == 32)
                    data[i] = new Complex(audioStream.readInt(), 0);
                else data[i] = new Complex(audioStream.readLong(), 0);
            }

            //perform FFT
            data = FFT.fft(data);

            //calculate power spectrum and find the largest peak in power spectrum
            double max_magnitude = -1e10;
            int max_index = -1;
            for (int i = 1; i < N / 2; i++) {
                double re = data[i].re();
                double im = data[i].im();
                magnitude[i] = Math.sqrt(re * re + im * im);
                if (magnitude[i] > max_magnitude) {
                    max_magnitude = magnitude[i];
                    max_index = i;
                }
            }
            return max_index * sampleRate / N;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**call this function to check if the sound changes before and after the given time point.
     *the parameter BreakpointInNanoTime is the time point you want to check
     * It is the number of nano seconds pasted from the beginning of the wav file
     */
    public boolean volumeChanged(long BreakpointInNanoTime){
        double secondsToBreakpoint = BreakpointInNanoTime / 1e9;
        double samplesToBreakpoint = sampleRate * secondsToBreakpoint;
        long bytesToBreakpoint = Math.round(samplesToBreakpoint * sampleSize * channelNum / 8);
        long bytesInPointOneSecond = Math.round(sampleSize * sampleRate * channelNum / 800);
        long bytesToSkip = bytesToBreakpoint - bytesInPointOneSecond;
        long bytesToSkip2 = bytesToBreakpoint + bytesInPointOneSecond;
        long offsetOfLastByteToMeasure = bytesToSkip2 + bytesInPointOneSecond;
        if (bytesToSkip <= 0 || offsetOfLastByteToMeasure >= audioLengthInBytes) return true;
        double freq1 = highestFrequency(bytesToSkip);
        double freq2 = highestFrequency(bytesToSkip2);
        System.out.println("freq1:" + String.valueOf(freq1) + " freq2:" + String.valueOf(freq2));
        return hugeDifference(freq1, freq2, 0.35);
    }
}
