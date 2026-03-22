package xyz.sanchon.jgamedatabase.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    /**
     * Convierte Markdown a HTML y lo sanea para evitar XSS (etiquetas/atributos no permitidos).
     */
    public String toSafeHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Node document = parser.parse(markdown);
        String html = htmlRenderer.render(document);
        return Jsoup.clean(html, Safelist.relaxed());
    }
}
