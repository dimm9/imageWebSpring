package imageEdit;

import java.awt.image.BufferedImage;

public class BrightnessWorker implements Runnable{
    public BufferedImage image;
    public int start, end;
    public int scale;

    public BrightnessWorker(BufferedImage image, int start, int end, int scale) {
        this.image = image;
        this.start = start;
        this.end = end;
        this.scale = scale;
    }

    @Override
    public void run() {
        int width = image.getWidth();
        for(int x=0; x<width; x++){
            for(int y=start; y<end; y++){
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >>16) & 0xFF;
                int green = (rgb >>8) & 0xFF;
                int blue = rgb & 0xFF;
                alpha = Clamp.clamp(alpha+scale,0,255);
                red = Clamp.clamp(red+scale, 0, 255);
                green = Clamp.clamp(green+scale, 0, 255);
                blue = Clamp.clamp(blue+scale, 0, 255);
                int newRGB = (alpha<<24)|(red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newRGB);
            }
        }
    }
}
