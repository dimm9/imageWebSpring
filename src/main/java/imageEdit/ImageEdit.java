package imageEdit;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageEdit {
    BufferedImage image;
    public void read(String path){
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void write(Path path) throws IOException {
        String format = "jpg";
        String[] parts = String.valueOf(path).split("\\.");
        if(parts.length == 2){
            format = parts[1];
        }
        ImageIO.write(image, format, new File(String.valueOf(path)));
    }

    public void increaseBrightness(int scale){
        int width = image.getWidth();
        int height = image.getHeight();
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >>16) & 0xFF;
                int green = (rgb >>8) & 0xFF;
                int blue = rgb & 0xFF;
                alpha = Clamp.clamp(alpha + scale,0,255);
                red = Clamp.clamp(red+scale, 0, 255);
                green = Clamp.clamp(green+scale, 0, 255);
                blue = Clamp.clamp(blue+scale, 0, 255);
                int newRGB = (alpha<<24)|(red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newRGB);
            }
        }
    }
    public void multiThreadBrightness(int scale) throws InterruptedException {
        int processors = Runtime.getRuntime().availableProcessors();
        int height = image.getHeight();
        Thread[] threads = new Thread[processors];
        int fragments = height/processors;
        for(int i=0; i<processors; i++){
            int beginning = i*fragments;
            int end = Math.min(beginning + fragments, height);
            threads[i] = new Thread(new BrightnessWorker(this.image, beginning, end, scale));
            threads[i].start();
        }
        for(Thread thread : threads){
            thread.join();
        }
    }
    public void brightnessThreadPool(int scale) throws InterruptedException {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);
        for (int i = 0; i < image.getHeight(); ++i){
            final int y = i;
            executor.execute(() -> {
                for(int x=0;x<image.getWidth();x++){
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xFF;
                    int red = (rgb >>16) & 0xFF;
                    int green = (rgb >>8) & 0xFF;
                    int blue = rgb & 0xFF;
                    alpha = Clamp.clamp(alpha + scale,0,255);
                    red = Clamp.clamp(red+scale, 0, 255);
                    green = Clamp.clamp(green+scale, 0, 255);
                    blue = Clamp.clamp(blue+scale, 0, 255);
                    int newRGB = (alpha<<24)|(red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, newRGB);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.MINUTES);
    }

    public int[] histogramThreadPool(Chanel chanel) throws InterruptedException {
        int height = image.getHeight();
        int width = image.getWidth();
        int[] histogram = new int[256];
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);
        int fragments = height / processors;

        for (int i = 0; i < processors; i++) {
            int start = i * fragments;
            int end = Math.min(start + fragments, height);
            executor.execute(() -> {
                for (int y = start; y < end; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = image.getRGB(x, y);
                        int value = 0;
                        switch (chanel) {
                            case RED -> value = (rgb >> 16) & 0xFF;
                            case BLUE -> value = rgb & 0xFF;
                            case GREEN -> value = (rgb >> 8) & 0xFF;
                        }
                        synchronized (histogram) {
                            histogram[value]++;
                        }
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.MINUTES);
        return histogram;
    }

    public void generatePNGHistogram(int[][] histogram, String path) throws IOException {
        int width = 256;
        int height = 256;
        BufferedImage histogramIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = histogramIm.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);

        int[] maxRGB = new int[]{0, 0, 0};
        for(int j = 0; j < 3; j++) {
            for(int i = 0; i < histogram[j].length; i++) {
                if(histogram[j][i] > maxRGB[j]) {
                    maxRGB[j] = histogram[j][i];
                }
            }
        }
        for(int j = 0; j < 3; j++) {
            for(int i = 0; i < histogram[j].length; i++) {
                if (maxRGB[j] > 0) {
                    int barHeight = (histogram[j][i] * height) / maxRGB[j];
                    if(j == 0) g.setColor(Color.RED);
                    else if(j == 1) g.setColor(Color.GREEN);
                    else g.setColor(Color.BLUE);
                    g.drawLine(i, height, i, height - barHeight);
                }
            }
        }
        g.dispose();
        ImageIO.write(histogramIm, "png", new File(path));
    }

}
