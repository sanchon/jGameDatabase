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
            // No system tray available (some Linux desktops): at least open the browser
            openBrowser(url);
            return;
        }

        TrayIcon trayIcon = new TrayIcon(createIcon(), "jGameDatabase");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("jGameDatabase  –  " + url);

        MenuItem openItem = new MenuItem("Open in browser");
        openItem.addActionListener(e -> openBrowser(url));

        MenuItem stopItem = new MenuItem("Stop jGameDatabase");
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
        // Double-clicking the icon opens the browser
        trayIcon.addActionListener(e -> openBrowser(url));

        try {
            SystemTray.getSystemTray().add(trayIcon);
            // Balloon notification so the user can find the icon even if it is hidden
            trayIcon.displayMessage(
                "jGameDatabase ready",
                "Right-click this icon to open the browser or stop the application.",
                TrayIcon.MessageType.INFO
            );
        } catch (AWTException e) {
            System.err.println("[tray] Could not add icon to the system tray: " + e.getMessage());
        }

        // Automatically open the browser on startup
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