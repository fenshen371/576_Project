import edu.csci.processVideo.Writer;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by rujun on 12/4/2016.
 */
public class test {
    public static void main(String[] args) throws FileNotFoundException {
        //cmd line parameter: path of rgb file, path of wav file that you want to create
        Writer test = new Writer(args[0], args[1]);
        test.load("../data/data_test1.rgb", "../data/data_test1.wav", 0, 1);
        test.writeWavThenCloseOutputStreams();
    }
}
