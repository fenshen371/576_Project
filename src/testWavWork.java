import edu.csci.utils.Interval;
import edu.csci.wav.WavWork;
import edu.csci.processVideo.Writer;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by rujun on 11/25/2016.
 */
public class testWavWork {
    public static void main(String[] args) throws FileNotFoundException {
        String sourceRGB = "../data/data_test1.rgb";
        String sourceWav = "../data/data_test1.wav";
        Filter filter = new Filter(sourceRGB, sourceWav);
        System.out.println("Detecting advertisements...");
        ArrayList<Interval> boundaries = filter.getAdBoundaries();

        System.out.println("Generating result rgb file and wave file...");
        //cmd line parameters: path of rgb file and wave file that you want to create
        Writer test = new Writer(args[0], args[1]);
        for (int i = 0; i < boundaries.size(); i++){
            if (i == 0 && boundaries.get(i).startFrameIndex != 0)
                test.load(sourceRGB, sourceWav, 0, boundaries.get(i).startFrameIndex);
            else if (i > 0)
                test.load(sourceRGB, sourceWav, boundaries.get(i-1).endFrameIndex, boundaries.get(i).startFrameIndex);
            if (i + 1 == boundaries.size())
                test.load(sourceRGB, sourceWav, boundaries.get(i).endFrameIndex + 1);
        }
        test.writeWavThenCloseOutputStreams();
    }
}
