package xyz.sanchon.jgamedatabase;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;

@Component
@Profile("portable")
public class PortableTrayManager implements ApplicationListener<WebServerInitializedEvent> {

    private final ConfigurableApplicationContext context;

    public PortableTrayManager(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        EventQueue.invokeLater(() -> setup(port));
    }

    private void setup(int port) {
        String url = "http://localhost:" + port;

        if (!SystemTray.isSupported()) {
            // Sin bandeja disponible (algunos escritorios Linux): al menos abrir el navegador
            openBrowser(url);
            return;
        }

        TrayIcon trayIcon = new TrayIcon(createIcon(), "jGameDatabase");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("jGameDatabase  –  " + url);

        MenuItem openItem = new MenuItem("Abrir en el navegador");
        openItem.addActionListener(e -> openBrowser(url));

        MenuItem stopItem = new MenuItem("Detener jGameDatabase");
        stopItem.addActionListener(e -> {
            SystemTray.getSystemTray().remove(trayIcon);
            new Thread(() -> {
                context.close();
                System.exit(0);
            }, "shutdown").start();
        });

        PopupMenu menu = new PopupMenu();
        menu.add(openItem);
        menu.addSeparator();
        menu.add(stopItem);

        trayIcon.setPopupMenu(menu);
        // Doble clic en el icono abre el navegador
        trayIcon.addActionListener(e -> openBrowser(url));

        try {
            SystemTray.getSystemTray().add(trayIcon);
            // Notificación balloon para que el usuario encuentre el icono aunque esté oculto
            trayIcon.displayMessage(
                "jGameDatabase lista",
                "Clic derecho en este icono para abrir el navegador o detener la aplicación.",
                TrayIcon.MessageType.INFO
            );
        } catch (AWTException e) {
            System.err.println("[tray] No se pudo añadir el icono a la bandeja: " + e.getMessage());
        }

        // Abrir el navegador automáticamente al arrancar
        openBrowser(url);
    }

    private void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ignored) {}
    }

    private Image createIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(59, 130, 246));
        g.fillOval(1, 1, 30, 30);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.drawString("J", 10, 24);
        g.dispose();
        return img;
    }
}