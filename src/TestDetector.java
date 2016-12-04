import edu.csci.detectBrand.MyDetector;
import java.util.*;

public class TestDetector {
	public static void main(String[] args) {
		// args[0] : rgb file path.
		// args[1] : brand image path.
		MyDetector h_detector = new MyDetector(args[0]);
		ArrayList<Integer> frames =  h_detector.detect(args[1]);
		for (int i = 0; i < frames.size(); ++i) {
			System.out.println(frames.get(i));
		}
	}
}
