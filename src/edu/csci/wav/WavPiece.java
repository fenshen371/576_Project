package edu.csci.wav;

/**
 * Created by rujun on 12/4/2016.
 */
public class WavPiece {
    private double frameRate = 48000.0;
    public String wavFileName;
    public long startOffsetInMilliSeconds;
    public long endOffsetInMilliseconds;

    /**
     * WavPiece refers to a part of a given wav file by a file name, a start point and an end point
     * @param fileName Path of the wav file
     * @param st start point in milliseconds
     * @param ed end point in milliseconds
     */
    public WavPiece(String fileName, long st, long ed) {
        wavFileName = fileName;
        startOffsetInMilliSeconds = st;
        endOffsetInMilliseconds = ed;
    }
}
