import edu.csci.processVideo.MyProcessor;

public class Processor {
	public static void main(String[] args){
		MyProcessor h_processor = new MyProcessor(args[0], args[1]);
		h_processor.process();
		h_processor.save(args[2], args[3]);
	}
}
