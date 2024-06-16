package server;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class ImageFormController {
    //Metoda, która wyświetli plik index.html
    @GetMapping("/html")
    public String showIndexHTML(){
        return "index";
    }
    //Metodę która zostanie wyzwolona przez naciśnięcie przycisku Upload.
    // Metoda wyświetlia plik image.html, wyświetlając w nim przesłany obraz
    @PostMapping("/imageform/upload")
    public String upload(@RequestBody Image image, Model model){
        String img64 = image.getBase64Image();
        model.addAttribute("image", img64);
        return "image";
    }

}
