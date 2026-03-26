package xyz.sanchon.jgamedatabase;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartupListener implements ApplicationListener<WebServerInitializedEvent> {

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        String url = "http://localhost:" + port;
        String line = "=".repeat(45);
        System.out.println("\n" + line);
        System.out.println("  jGameDatabase lista");
        System.out.println("  Abre tu navegador en: " + url);
        System.out.println("  Para detener: Ctrl+C (o bandeja del sistema)");
        System.out.println(line + "\n");
    }
}