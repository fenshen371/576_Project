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
    private int N = 4096; //size of FFT and sample window

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
    private double[] getMagnitude(long bytesToSkip) {
        Complex[] data = new Complex[N]; //input data buffer
        double[] magnitude = new double[N / 2];
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

            //calculate power spectrum
            for (int i = 1; i < N / 2; i++) {
                double re = data[i].re();
                double im = data[i].im();
                magnitude[i] = Math.sqrt(re * re + im * im);
            }
            return magnitude;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return magnitude;
    }


    private double L2Distance(double[] a, double[] b) {
        //calculate start index and end index of frequencies between 1khz and 4khz
        int st = (int)Math.round(Math.floor(1000 * N / sampleRate));
        int ed = (int)Math.round(Math.ceil(4000 * N / sampleRate));

        double dist = 0;
        for (int i = st; i <= ed; i++) {
            dist += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(dist);
    }

    private double entropy(double[] a) {
        //calculate start index and end index of frequencies between 1khz and 4khz
        int st = (int)Math.round(Math.floor(1000 * N / sampleRate));
        int ed = (int)Math.round(Math.ceil(4000 * N / sampleRate));
        double sum = 0;
        for (int i = st; i <= ed; i++) sum += a[i];
        if (sum < 0.001) return 0;
        double entropy = 0;
        for (int i = st; i <= ed; i++) {
            double prob = a[i] / sum;
            entropy += -(prob * Math.log(prob));
        }
        return entropy;
    }

    /**call this function to check if the sound changes before and after the given time point.
     *the parameter BreakpointInNanoTime is the time point you want to check
     * It is the number of nano seconds pasted from the beginning of the wav file
     */
    public boolean volumeChanged(long BreakpointInNanoTime){
        double secondsToBreakpoint = BreakpointInNanoTime / 1e9;
        double samplesToBreakpoint = sampleRate * secondsToBreakpoint;
        long bytesToBreakpoint = Math.round(samplesToBreakpoint * sampleSize * channelNum / 8);
        long bytesIn10MilliSeconds = Math.round(sampleSize * sampleRate * channelNum / 80);
        long bytesToSkip = bytesToBreakpoint - bytesIn10MilliSeconds;
        long bytesToSkip2 = bytesToBreakpoint + bytesIn10MilliSeconds;
        long offsetOfLastByteToMeasure = bytesToSkip2 + bytesIn10MilliSeconds;
        if (bytesToSkip <= 0 || offsetOfLastByteToMeasure >= audioLengthInBytes) return true;
        double[] piece1 = getMagnitude(bytesToSkip);
        double[] piece2 = getMagnitude(bytesToSkip2);
        System.out.println("L2 distance: " + String.valueOf(L2Distance(piece1, piece2)));
        System.out.println("1st entropy: " + String.valueOf(entropy(piece1)));
        System.out.println("2nd entropy: " + String.valueOf(entropy(piece2)));
        return true;
    }
}
