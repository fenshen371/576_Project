package edu.csci.processVideo;
import edu.csci.wav.WavFile;
import edu.csci.wav.WavFileException;
import edu.csci.wav.WavPiece;
import edu.csci.playVideo.VideoLoader;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by rujun on 12/4/2016.
 */
public class Writer {
    private String rgbFileName;
    private String wavFileName;
    private FileOutputStream rgbOutputStream;
    private WavFile wavOutputStream;
    private ArrayList<WavPiece> wavPieces = new ArrayList<WavPiece>();
    private int width = 480;
    private int height = 270;
    private int frame_size = width * height * 3;
    private double fps = 30.0;
    private double wavFrameRate = 48000.0;
    /**
     * Constructor
     * @param rgbFile path of rgb file that you want to create
     * @param wavFile path of wav file that yo uwant to create
     */
    public Writer(String rgbFile, String wavFile){
        rgbFileName = rgbFile;
        wavFileName = wavFile;
        try {
            rgbOutputStream = new FileOutputStream(rgbFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write data to the rgb file and wav file being generated from given source files. The data is described by a start frame
     * index and an end frame index of the rgb file. Wave data will be copied synchronously.
     * @param rgbFile The source rgb file
     * @param wavFile The source wav file
     * @param startFrameIndex From which frame index of the source rgb file to copy
     * @param endFrameIndex The last frame index of the source rgb file to copy. Excluded.
     */
    public void load(String rgbFile, String wavFile, long startFrameIndex, long endFrameIndex){
        try {
            InputStream is = new FileInputStream(rgbFile);
            byte[] oneFrameData = new byte[frame_size];
            int readCount = 0;
            //skip frames before the start point
            while (readCount < startFrameIndex) {
                is.read(oneFrameData, 0, frame_size);
                readCount++;
            }
            while (readCount < endFrameIndex) {
                is.read(oneFrameData, 0, frame_size);
                rgbOutputStream.write(oneFrameData);
                rgbOutputStream.flush();
                readCount++;
            }
            is.close();
            long startTimeInMilliSeconds = Math.round(startFrameIndex / fps * 1000);
            long endTimeInMilliSeconds = Math.round(endFrameIndex / fps * 1000);
            wavPieces.add(new WavPiece(wavFile, startTimeInMilliSeconds, endTimeInMilliSeconds));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write data to the rgb file and wav file being generated from given source files. All data from the start frame index of
     * the source rgb file will be copied until the end of the file. Wave data will be copied synchronously.
     * @param rgbFile The source rgb file
     * @param wavFile The source wave file
     * @param startFrameIndex From which frame index of the source rgb file to copy
     */
    public void load(String rgbFile, String wavFile, long startFrameIndex){
        try {
            InputStream is = new FileInputStream(rgbFile);
            byte[] oneFrameData = new byte[frame_size];
            int readCount = 0;
            //skip frames before the start point
            while (readCount < startFrameIndex) {
                is.read(oneFrameData, 0, frame_size);
                readCount++;
            }
            while (true) {
                int bytesRead = is.read(oneFrameData, 0, frame_size);
                if (bytesRead < 0) break;
                rgbOutputStream.write(oneFrameData, 0, Math.min(bytesRead, frame_size));
                readCount++;
            }
            is.close();
            long startTimeInMilliSeconds = Math.round(startFrameIndex / fps * 1000);
            long endTimeInMilliSeconds = Math.round((readCount - 1) / fps * 1000);
            wavPieces.add(new WavPiece(wavFile, startTimeInMilliSeconds, endTimeInMilliSeconds));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Because we need to provide wav file size before creating a wav file using WavFile class, we use this function to calculate it.
     * @return number of frames that are going to be in the wav file
     */
    private long countWavFrames(){
        long totalTimeInMilliSeconds = 0;
        for (int i = 0; i < wavPieces.size(); i++)
            totalTimeInMilliSeconds += wavPieces.get(i).endOffsetInMilliseconds - wavPieces.get(i).startOffsetInMilliSeconds;
        return Math.round(totalTimeInMilliSeconds / 1000.0 * wavFrameRate);
    }

    /**
     * Write all wave audio pieces in wavPieces to the wave file being generated.
     */
    private void writeWav(){
        long numFrames = countWavFrames();
        try {
            wavOutputStream = WavFile.newWavFile(new File(wavFileName), 1, numFrames, 16, Math.round(wavFrameRate));
            for (int i = 0; i < wavPieces.size(); i++)
                writeWavPiece(wavPieces.get(i));
        } catch (WavFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write one single wave audio piece to the wave file being generated.
     * @param piece
     */
    private void writeWavPiece(WavPiece piece){
        long framesToSkip = Math.round(piece.startOffsetInMilliSeconds / 1000.0 * wavFrameRate);
        long lastFrameToCopy = Math.round(piece.endOffsetInMilliseconds / 1000.0 * wavFrameRate);
        long framesToRead = lastFrameToCopy - framesToSkip;
        int bufferSize = 48000;
        int[] buffer = new int[bufferSize];
        try {
            WavFile source = WavFile.openWavFile(new File(piece.wavFileName));
            while (framesToSkip > 0){
                if (framesToSkip >= bufferSize){
                    source.readFrames(buffer, bufferSize);
                    framesToSkip -= bufferSize;
                } else {
                    source.readFrames(buffer, (int)framesToSkip);
                    framesToSkip = 0;
                }
            }
            while (framesToRead > 0){
                if (framesToRead >= bufferSize){
                    source.readFrames(buffer, bufferSize);
                    wavOutputStream.writeFrames(buffer, bufferSize);
                    framesToRead -= bufferSize;
                } else {
                    source.readFrames(buffer, (int)framesToRead);
                    wavOutputStream.writeFrames(buffer, (int)framesToRead);
                    framesToRead = 0;
                }
            }
            source.close();
        } catch (WavFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * You need to call this function to write all wave data and close rgb output stream and wave output stream after finish
     */
    public void writeWavThenCloseOutputStreams(){
        writeWav();
        try {
            rgbOutputStream.close();
            wavOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}





