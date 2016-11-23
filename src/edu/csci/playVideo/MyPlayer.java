package edu.csci.playVideo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.nio.file.*;
import javax.swing.*;
/**
 * Created by rujun on 11/19/2016.
 */
public class MyPlayer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("At least two parameters required: RGB file path and Wav file path");
            return;
        }
        String rgbFilePath = args[0];
        Path p = Paths.get(rgbFilePath);
        String fileName = p.getFileName().toString();
        String wavFilePath = args[1];

        int width = 480;
        int height = 270;
        int fps = 30;
        if (args.length >= 4){
            width = Integer.getInteger(args[2]);
            height = Integer.getInteger(args[3]);
            if (args.length == 5) fps = Integer.getInteger(args[4]);
        }

        //Initialize the player
        JFrame player = new JFrame();
        player.setTitle(fileName);
        player.setVisible(true);
        BorderLayout layout = new BorderLayout();
        player.getContentPane().setLayout(layout);
        player.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //load screen and pause/resume button
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        JLabel screen = new JLabel(new ImageIcon(img));
        JButton stopBtn = new JButton("Pause");
        player.getContentPane().add(screen, BorderLayout.CENTER);
        player.getContentPane().add(stopBtn, BorderLayout.SOUTH);
        player.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        player.setLocation(dim.width/2-player.getSize().width/2, dim.height/2-player.getSize().height/2);

        //load video file and sound file. Start playing!
        VideoLoader videoPlayer = new VideoLoader("video_thread", rgbFilePath, player);
        videoPlayer.setResolution(width, height);
        videoPlayer.setFps(fps);
        WavLoader soundPlayer = new WavLoader("sound_thread", wavFilePath);
        videoPlayer.start();
        soundPlayer.start();
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = stopBtn.getText();
                if (text.equals("Pause")) stopBtn.setText("Resume");
                else stopBtn.setText("Pause");
                videoPlayer.pauseOrResume();
                soundPlayer.pauseOrResume();
            }
        });
    }

}
