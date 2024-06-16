package server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
public class RectangleController {
    private List<Rectangle> rectangles = new ArrayList<>();

    @GetMapping("/simpleRect")
    public Rectangle getRect() {
        return new Rectangle(Rectangle.defaultX, Rectangle.defaultY, Rectangle.defaultWidth, Rectangle.defaultHeight, Rectangle.defaultColor);
    }

    //dodaje prostokąt o określonych w kodzie parametrach
    @GetMapping("addDefault")
    public int addDefault() {
        rectangles.add(new Rectangle(Rectangle.defaultX, Rectangle.defaultY, Rectangle.defaultWidth, Rectangle.defaultHeight, Rectangle.defaultColor));
        return rectangles.size();
    }

    //odpowiadała na żądanie HTTP POST. Prostokąt zostanie zdefiniowany w ciele żądania HTTP
    @PostMapping("/addRect")
    public int addRectangle(@RequestBody Rectangle rectangle) {
        rectangles.add(rectangle);
        return rectangles.size();
    }

    //zwraca listę prostokątów zmapowaną na JSON.
    @GetMapping("/rectangles")
    public List<Rectangle> getRectangles() {
        return this.rectangles;
    }

    //wygeneruje napis zawierający kod SVG z prostokątami znajdującymi się na liście
    @GetMapping("/rectsSVG")
    public String generateSVG() {
        StringBuilder svgRects = new StringBuilder("<svg width=\"400\" height=\"200\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        for (Rectangle r : rectangles) {
            svgRects.append(String.format(Locale.ENGLISH, "<rect width=\"%d\" height=\"%d\" x=\"%d\" y=\"%d\" rx=\"0\" ry=\"0\" fill=\"%s\" />\n",
                    r.getWidth(), r.getWidth(), r.getX(), r.getY(), r.getColor()));
        }
        svgRects.append("</svg>");
        return svgRects.toString();
    }

    //GET z argumentem typu int,  zwracającą prostokąt w liście o podanym indeksie
    @GetMapping("/GET/{index}")
    public Rectangle GET(@PathVariable int index) {
        if(index < 0 || index > rectangles.size()){
            throw new IndexOutOfBoundsException();
        }
        return rectangles.get(index);
    }

    //PUT z argumentem typu int i argumentem typu Rectangle, modyfikującą istniejący na liście pod tym
    // indeksem prostokąt na prostokąt przekazany argumentem
    @PutMapping("/PUT/{idx}")
    public int PUT(@PathVariable int idx, @RequestBody Rectangle rect) {
        rectangles.add(idx, rect);
        return rectangles.size();
    }

    //usuwającą prostokąt z listy z miejsca o podanym indeksie
    @DeleteMapping("/DELETE/{idx}")
    public int DELETE(@PathVariable int idx) {
        rectangles.remove(idx);
        return rectangles.size();
    }
}