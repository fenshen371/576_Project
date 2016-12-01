import edu.csci.wav.WavWork;
/**
 * Created by rujun on 11/25/2016.
 */
public class testWavWork {
    public static void main(String[] args) {
        String path = "../data/data_test1.wav";
        WavWork ww = new WavWork(path);
        //String num = "79000000000";
        ww.volumeChanged(Long.valueOf(args[0]));
    }
}
