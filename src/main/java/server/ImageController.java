package server;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
public class ImageController {
    //Metoda typu GET powinna przyjąć obraz w formacie
    // base64 oraz liczbę całkowitą określającą jasność. Metoda powinna rozjaśnić obraz o podaną wartość i zwrócić go w formacie base64
    @PostMapping("/brightness")
    public ResponseEntity<String> changeBrightness(@RequestBody Image image){
        int brightness = image.getBrightness();
        String imgEncrypted = image.getBase64Image();

        String[] parts = imgEncrypted.split(",", 2);
        String imageCode = parts[1];
        String format = parts[0].substring(parts[0].indexOf("/")+1, parts[0].indexOf(";"));

        byte[] imgDecoded = Base64.getDecoder().decode(imageCode);
        String result = "";
        try {
            ByteArrayInputStream byteInput = new ByteArrayInputStream(imgDecoded);
            BufferedImage imgResult = ImageIO.read(byteInput);
            changeBrightness(imgResult, brightness);
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            ImageIO.write(imgResult, format, byteOutput);
            byteOutput.close();
            result =  format + "," + Base64.getEncoder().encodeToString(byteOutput.toByteArray());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PostMapping("/brightnessBytes")
    public ResponseEntity<byte[]> increaseBrightnessBytes (@RequestBody Image image) {
        String base64 = image.getBase64Image();
        int brightness = image.getBrightness();

        String[] parts = base64.split(",", 2);
        String format = parts[0].substring(parts[0].indexOf("/") + 1,parts[0].indexOf(";"));
        String imageCode = parts[1];

        byte[] imageBytes = new byte[0];
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(imageCode);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            changeBrightness(bufferedImage, brightness);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, format, byteArrayOutputStream);

            imageBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "image/" + format);
            headers.add("Content-Length", String.valueOf(imageBytes.length));
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return new ResponseEntity<>(imageBytes, HttpStatus.BAD_REQUEST);
    }

    private BufferedImage changeBrightness(BufferedImage originalImage, int brightness) {
        //BufferedImage result = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                Color color = new Color(originalImage.getRGB(x, y));
                int r = clamp(color.getRed() + brightness);
                int g = clamp(color.getGreen() + brightness);
                int b = clamp(color.getBlue() + brightness);
                Color newColor = new Color(r, g, b);
                originalImage.setRGB(x, y, newColor.getRGB());
            }
        }
        return originalImage;
    }
    private int clamp(int value) {
        return Math.max(0, Math.min(value, 255));
    }
}
