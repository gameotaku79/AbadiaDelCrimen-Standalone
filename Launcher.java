import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    static int BASE_WIDTH = 800;
    static int BASE_HEIGHT = 600;

    static JFrame frame;
    static JPanel container;
    static Applet applet;

    static boolean fullscreen = true;

    // 1=1x, 2=2x, 3=3x, 4=stretch
    static int zoomMode = 4;

    public static void main(String[] args) {

        frame = new JFrame("La Abadia del Crimen");

        try {
            Class<?> clazz = Class.forName("abadiaapplet.Abadia");
            applet = (Applet) clazz.getDeclaredConstructor().newInstance();

            Map<String, String> params = new HashMap<>();
            params.put("width", String.valueOf(BASE_WIDTH));
            params.put("height", String.valueOf(BASE_HEIGHT));

            applet.setStub(new java.applet.AppletStub() {
                public boolean isActive() { return true; }
                public URL getDocumentBase() { try { return new URL("file:./"); } catch (Exception e) { return null; } }
                public URL getCodeBase() { try { return new URL("file:./"); } catch (Exception e) { return null; } }
                public String getParameter(String name) { return params.get(name); }
                public java.applet.AppletContext getAppletContext() { return null; }
                public void appletResize(int width, int height) {}
            });

            applet.init();
            applet.start();

            container = new JPanel(new GridBagLayout());
            container.setBackground(Color.BLACK);
            container.add(applet);

            frame.setContentPane(container);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false); // ventana fija

            setupGlobalKeys();

            enterFullscreen();

            frame.setVisible(true);
            applet.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void applyScale() {

        int width;
        int height;

        // stretch SOLO en fullscreen
        if (fullscreen && zoomMode == 4) {

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

            double scaleX = (double) screen.width / BASE_WIDTH;
            double scaleY = (double) screen.height / BASE_HEIGHT;

            double scale = Math.min(scaleX, scaleY);

            width = (int)(BASE_WIDTH * scale);
            height = (int)(BASE_HEIGHT * scale);

        } else {

            int scale = zoomMode;

            // en ventana, stretch se comporta como 2x
            if (!fullscreen && zoomMode == 4) {
                scale = 2;
            }

            width = BASE_WIDTH * scale;
            height = BASE_HEIGHT * scale;
        }

        applet.setPreferredSize(new Dimension(width, height));
        applet.setSize(width, height);

        if (!fullscreen) {
            frame.pack();
        }

        applet.invalidate();
        applet.validate();
        applet.repaint();

        container.revalidate();
        container.repaint();

        frame.repaint();
    }

    static void enterFullscreen() {

        frame.dispose();

        frame.setUndecorated(true);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screen);
        frame.setLocation(0, 0);

        zoomMode = 4;

        applyScale();
    }

    static void toggleFullscreen() {

        fullscreen = !fullscreen;

        frame.dispose();

        if (fullscreen) {

            frame.setUndecorated(true);

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize(screen);
            frame.setLocation(0, 0);

            zoomMode = 4;

        } else {

            frame.setUndecorated(false);
            frame.setExtendedState(JFrame.NORMAL);

            zoomMode = 2;
        }

        applyScale();

        frame.setVisible(true);
        frame.revalidate();
        frame.repaint();

        applet.requestFocus();
    }

    static void setupGlobalKeys() {

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {

                    public boolean dispatchKeyEvent(KeyEvent e) {

                        if (e.getID() != KeyEvent.KEY_PRESSED) return false;

                        switch (e.getKeyCode()) {

                            case KeyEvent.VK_Z:

                                if (fullscreen) {
                                    zoomMode++;
                                    if (zoomMode > 4) zoomMode = 1;
                                } else {
                                    zoomMode++;
                                    if (zoomMode > 3) zoomMode = 1;
                                }

                                applyScale();
                                return false;

                            case KeyEvent.VK_F11:
                                toggleFullscreen();
                                return false;
                        }

                        return false;
                    }
                });
    }
}