/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Common-PrintServer
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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * CommonPrintServer logger class.
 *
 * @author Alexandre D.
 * @version 1.0.0
 * @since 2016-04-06
 */
class CommonPrintServerLogger {

    private static JFrame frame;
    private static JTextArea container;

    /** Log list. */
    private static final ArrayList<String> log = new ArrayList<>();

    /**
     * Log a message.
     *
     * @param String str
     */
    static void log(String str) {
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

        str = "[" + timeStamp + "] " + str;
        log.add(str);

        if (frame != null && frame.isShowing()) {
            container.append("\n" + str);
        }
    }

    static void clearLog() {
        CommonPrintServerLogger.log.clear();
        if (CommonPrintServerLogger.container != null) {
            CommonPrintServerLogger.container.setText("");
        }
    }

    /**
     * Format logs to display in Frame.
     *
     * @return Logs formatted.
     */
    private static String formatLog() {
        String str = "";

        str = log.stream().map((temp) -> temp + "\n").reduce(str, String::concat);

        return str;
    }

    /**
     * Open a Frame with logs.
     * It is possible to copy/save the log and clear it.
     */
    static void displayLog() {
        frame = new JFrame("Common-PrintServer - Logs");
        container = new JTextArea(CommonPrintServerLogger.log.isEmpty() ? "" : CommonPrintServerLogger.formatLog());
        JScrollPane src_pane = new JScrollPane(container);
        JToolBar toolbar = new JToolBar();
        JButton button_clear = new JButton("Clear");
        JButton button_copy = new JButton("Copy");
        JButton button_save = new JButton("Save as");

        container.setEditable(false);

        toolbar.setFloatable(false);
        toolbar.add(button_clear);
        toolbar.addSeparator();
        toolbar.add(button_copy);
        toolbar.addSeparator();
        toolbar.add(button_save);

        button_clear.addActionListener((ActionEvent e) -> {
            container.setText("");
        });

        button_copy.addActionListener((ActionEvent e) -> {
            StringSelection stringSelection = new StringSelection(container.getText());
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        });

        button_save.addActionListener((ActionEvent e) -> {
            File file;

            JFileChooser dialog = new JFileChooser();
            dialog.setSelectedFile(new File("Common-PrintServer_debug.txt"));
            dialog.setFileFilter(new FileNameExtensionFilter("Text file", "txt"));

            // Make sure the user didn't cancel the file chooser
            if (dialog.showSaveDialog(container) == JFileChooser.APPROVE_OPTION) {
                file = dialog.getSelectedFile();

                try {
                    // Now write to the file
                    PrintWriter output = new PrintWriter(new FileWriter(file));
                    output.println(container.getText());
                    output.close();
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(
                            container,
                            "Can't save file " + ioe.getMessage());
                }
            }
        });

        frame.setIconImage(CommonPrintServerTrayIcon.createImage("images/printer.png"));
        frame.setSize(800, 600);
        frame.add(toolbar, BorderLayout.NORTH);
        frame.add(src_pane, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
