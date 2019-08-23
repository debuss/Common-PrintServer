/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 CommonPrintServer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package commonprintserver;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * CommonPrintServer TrayIcon class.
 *
 * @author Alexandre D.
 * @version 1.0.0
 * @since 2016-04-06
 */
class CommonPrintServerTrayIcon {

    private static JFrame frame;
    private static JEditorPane container;
    private static HTMLEditorKit kit;

    CommonPrintServerTrayIcon() {
        // Use an appropriate Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        }

        // Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        // Adding TrayIcon.
        SwingUtilities.invokeLater(CommonPrintServerTrayIcon::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(createImage("images/printer.png"), "Common-PrintServer");
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem currentPrinterItem = new MenuItem("Current Printer");
        MenuItem logItem = new MenuItem("Logs");
        MenuItem startItem = new MenuItem("Start");
        MenuItem stopItem = new MenuItem("Stop");
        MenuItem restartItem = new MenuItem("Restart");
        MenuItem stateItem = new MenuItem("Server : ON");

        // Set state of MenuItem
        // Ex: Server is started so disable start button
        startItem.setEnabled(false);
        stateItem.setEnabled(false);

        // Add components to popup menu
        popup.add(aboutItem);
        popup.add(currentPrinterItem);
        popup.add(logItem);
        popup.addSeparator();
        popup.add(stateItem);
        popup.add(startItem);
        popup.add(stopItem);
        popup.add(restartItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // JOptionPane.showMessageDialog(null, "This dialog box is run from actionPerformed");
            }
        });

        trayIcon.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (CommonPrintServerServer.getInstance().isPrinterSelected()) {
                    currentPrinterItem.setLabel("Current Printer : " + CommonPrintServerServer.getInstance().getSelectedPrinter().getName());
                } else {
                    currentPrinterItem.setLabel("Current Printer : N/A");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        stopItem.addActionListener((ActionEvent e) -> {
            startItem.setEnabled(true);
            stopItem.setEnabled(false);
            restartItem.setEnabled(false);
            stateItem.setLabel("Server: OFF");
            CommonPrintServerServer.getInstance().stop();
        });

        startItem.addActionListener((ActionEvent e) -> {
            startItem.setEnabled(false);
            stopItem.setEnabled(true);
            restartItem.setEnabled(true);
            CommonPrintServerServer.getInstance().start();
            stateItem.setLabel("Server: ON");
        });

        restartItem.addActionListener((ActionEvent e) -> {
            CommonPrintServerServer.getInstance().restart();
        });

        aboutItem.addActionListener((ActionEvent e) -> {
            frame = new JFrame("About");
            container = new JEditorPane();
            kit = new HTMLEditorKit();

            container.setEditable(false);
            container.setEditorKit(kit);
            container.addHyperlinkListener((HyperlinkEvent e1) -> {
                if (e1.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(e1.getURL().toURI());
                        } catch (IOException | URISyntaxException ex) {
                            CommonPrintServerLogger.log(ex.getMessage());
                        }
                    }
                }
            });

            container.setText(
                "<html><body>"
                        + "<center>"
                        + "<img src=\"" + ClassLoader.getSystemResource("images/printer.png").toString() + "\" width=150 height=150></img>"
                        + "<h1 style=\"color: #ff4b28;\">Common-PrintServer</h1>"
                        + "</center>"
                        + "<p>Common-PrintServer is a mini server allowing you to print your documents directly on the printer via your internet browser.</p>"
                        + "<br>"
                        + "<p>Developped by Alexandre D.</p>"
                        + "</body></html>"
            );

            frame.setIconImage(CommonPrintServerTrayIcon.createImage("images/printer.png"));
            frame.setResizable(false);
            frame.setSize(500, 350);
            frame.add(container);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        logItem.addActionListener((ActionEvent e) -> {
            CommonPrintServerLogger.displayLog();
        });

        exitItem.addActionListener((ActionEvent e) -> {
            CommonPrintServerServer.getInstance().stop();
            tray.remove(trayIcon);
            System.exit(0);
        });
    }

    static Image createImage(String path) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return Toolkit.getDefaultToolkit().getImage(url);
    }

}
