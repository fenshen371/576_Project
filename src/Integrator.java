import edu.csci.wav.*;
import edu.csci.processVideo.*;
import edu.csci.ads.*;
import edu.csci.utils.*;

import java.util.*;
import java.io.FileNotFoundException;


public class Integrator {
	public static void main(String[] args) throws Exception {
		String video_file = args[0];
		String wav_file = args[1];

		String save_video_file = args[2];
		String save_wav_file = args[3];

		int logo_number = (args.length - 4 ) / 3;
		if (logo_number * 3 + 4 != args.length) {
			System.out.println("Invalid Arguments");
			return;
		}
		String[] logo = new String[logo_number];
		String[] ad_video = new String[logo_number];
		String[] ad_wav = new String[logo_number];
		int arg_index = 4;
		for (int i = 0; i < logo_number; ++i) {
			logo[i] = args[arg_index];
			ad_video[i] = args[arg_index + 1];
			ad_wav[i] = args[arg_index + 2];
			arg_index += 3;
		}

		System.out.println("Detecting ads...");
		Filter filter = new Filter(video_file, wav_file);
		ArrayList<Interval> deletion = filter.getAdBoundaries();

		System.out.println("Detecting logos...");
		LogoCMP cmp = new LogoCMP();
		
		SortedMap<Integer, Integer> insertion = new TreeMap<Integer, Integer>();
		/*
		for (int i = 0; i < logo_number; ++i) {
			//Vector<Integer> positions  = cmp.findLogoInVideo(logo[i], video_file);
			for (int j = 0; j < positions.size(); ++j) {
				insertion.put(positions.get(j), i);
			}
		}
		*/
		// Test. 0 for starbucks, 1 for subway.
		insertion.put(2070, 1);
		insertion.put(5235, 0);

		System.out.println("Generating rgb and wav file...");
		Writer writer = new Writer(save_video_file, save_wav_file);

		int current_index = 0;
		int insert_index = insertion.firstKey();
		int delete_number = 0;
		Interval delete_interval = deletion.get(delete_number);

		boolean insertion_done = false;
		boolean deletion_done = false;
		while (true) {
			if (insert_index < delete_interval.startFrameIndex) {
				// Insert new ads.
				System.out.println("branch 1, insert");
				System.out.println("from " + current_index + " to " + insert_index);
				writer.load(video_file, wav_file, current_index, insert_index);
				current_index = insert_index;
				int ad_index = insertion.get(insert_index);
				writer.load(ad_video[ad_index], ad_wav[ad_index], 0);
				insertion.remove(insert_index);
				if (insertion.isEmpty()) {
					insertion_done = true;
					break;
				}
				insert_index = insertion.firstKey();
			} else {
				// Delete original ads.
				System.out.println("branch 2, delete");
				System.out.println("from " + current_index + " to " + delete_interval.startFrameIndex);
				writer.load(video_file, wav_file, current_index, delete_interval.startFrameIndex);
				current_index = (int)delete_interval.endFrameIndex;
				++delete_number;
				if (delete_number == deletion.size()) {
					deletion_done = true;
					break;
				}
				delete_interval = deletion.get(delete_number);
			}
		}
		// Insertion done or deletion done.
		if (insertion_done) {
			while (!deletion_done) {
				// Delete original ads.
				System.out.println("branch 3, delete");
				System.out.println(" from " + current_index + " to " + delete_interval.startFrameIndex);
				writer.load(video_file, wav_file, current_index, delete_interval.startFrameIndex);
				current_index = (int)delete_interval.endFrameIndex;
				++delete_number;
				if (delete_number == deletion.size()) {
					deletion_done = true;
					break;
				}
				delete_interval = deletion.get(delete_number);
			}
		} else {
			while (!insertion_done) {
				// Insert new ads.
				System.out.println("branch 4, insert");
				System.out.println(" from " + current_index + " to " + insert_index);
				writer.load(video_file, wav_file, current_index, insert_index);
				current_index = insert_index;
				int ad_index = insertion.get(insert_index);
				writer.load(ad_video[ad_index], ad_wav[ad_index], 0);
				insertion.remove(insert_index);
				if (insertion.isEmpty()) {
					insertion_done = true;
					break;
				}
				insert_index = insertion.firstKey();
			}
		}
		// Fix the end.
		writer.load(video_file, wav_file, current_index);
		writer.writeWavThenCloseOutputStreams();
	}
}
