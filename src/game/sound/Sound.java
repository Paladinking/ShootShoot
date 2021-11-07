package game.sound;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sound {

    public static final int MINE_PLACE = 0, SNIPER = 1, EXPLOSION = 2;

    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    private final byte[] bytes;
    private final AudioFormat format;
    private final long frameLength;

    public Sound() {
        this.bytes = new byte[0];
        this.format = null;
        this.frameLength = 0;
    }

    public Sound(InputStream soundData) throws IOException, UnsupportedAudioFileException {
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(soundData);
        format = inputStream.getFormat();
        this.frameLength = inputStream.getFrameLength();
        bytes = inputStream.readAllBytes();
        inputStream.close();
    }

    public void play() {
        if (bytes.length == 0) return;
        playSfx(new ByteArrayInputStream(bytes), format, frameLength);
    }

    private static void playSfx(InputStream soundData, AudioFormat format, long frameLength) {
        executorService.submit(() -> {
            try {
                AudioInputStream audioInputStream = new AudioInputStream(soundData, format, frameLength);
                final int BUFFER_SIZE = 128000;

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

                SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
                sourceLine.open(format);


                int nBytesRead = 0;
                byte[] abData = new byte[BUFFER_SIZE];
                sourceLine.start();
                while (nBytesRead != -1) {
                    nBytesRead = audioInputStream.read(abData, 0, abData.length);
                    if (nBytesRead >= 0) {
                        sourceLine.write(abData, 0, nBytesRead);
                    }
                }

                sourceLine.drain();
                sourceLine.close();
                audioInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("BAD...");
            }

        });
    }

}
