package xyz.sanchon.jgamedatabase.controller;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Profile("portable")
public class ShutdownController {

    private final ConfigurableApplicationContext context;

    public ShutdownController(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @PostMapping("/shutdown")
    @ResponseBody
    public String shutdown() {
        new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            context.close();
            System.exit(0);
        }, "shutdown").start();

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <title>jGameDatabase detenida</title>
                  <style>
                    body { font-family: sans-serif; text-align: center; padding: 4rem; background: #f8f9fa; }
                    h2   { color: #343a40; }
                    p    { color: #6c757d; }
                  </style>
                </head>
                <body>
                  <h2>jGameDatabase se ha detenido.</h2>
                  <p>Puedes cerrar esta ventana.</p>
                </body>
                </html>
                """;
    }
}