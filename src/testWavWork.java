import edu.csci.wav.WavWork;
import edu.csci.processVideo.Writer;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by rujun on 11/25/2016.
 */
public class testWavWork {
    public static void main(String[] args) throws FileNotFoundException {
        Filter filter = new Filter();
        String sourceRGB = "../data/data_test1.rgb";
        String sourceWav = "../data/data_test1.wav";
        //filter.countDiffArray("../data/data_test2.rgb", "../data/data_test2.wav");
        ArrayList<Integer> boundaries = filter.getAdBoundaries();
        Writer test = new Writer(args[0], args[1]);
        for (int i = 0; i < boundaries.size(); i += 2){
            if (i == 0 && boundaries.get(i) != 0)
                test.load(sourceRGB, sourceWav, 0, boundaries.get(i));
            else if (i > 0)
                test.load(sourceRGB, sourceWav, boundaries.get(i-1), boundaries.get(i));
            if (i + 2 == boundaries.size())
                test.load(sourceRGB, sourceWav, boundaries.get(i+1) + 1);
        }
        test.writeWavThenCloseOutputStreams();
    }
}
