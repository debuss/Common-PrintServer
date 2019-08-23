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

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.SimpleDoc;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

/**
 * Class for all print functions. Any addition print type should be written
 * here.
 *
 * @author Luigi & Alexandre D.
 */
class CommonPrintServerPrintServices {

    /**
     * Send raw code directly to the printer. This method is mostly used for
     * thermal printer like Zebra with ZPL or DPL raw code.
     *
     * @param String raw_code Raw code to send to printer.
     * @return String
     */
    static String printRaw(String raw_code) {
        CommonPrintServerLogger.log(" printRaw");
        CommonPrintServerLogger.log("\n" + raw_code + "\n");
        InputStream psStream;

        psStream = new ByteArrayInputStream(raw_code.getBytes());
        DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc myDoc = new SimpleDoc(psStream, psInFormat, null);

        try {
            DocPrintJob job = CommonPrintServerServer.getInstance().getSelectedPrinter().createPrintJob();
            job.print(myDoc, null);
        } catch (NullPointerException | PrintException e) {
            return "NOK|" + e.getMessage();
        }

        try {
            psStream.close();
        } catch (IOException ex) {
            return "NOK|" + ex.getMessage();
        }

        return "OK";
    }

    /**
     * Print file thanks to its URL. Fetch the file and print it.
     *
     * @param file_url URL of the file to print.
     * @return Response, OK or NOK for error message.
     */
    static String printFileByURL(String file_url) {
        CommonPrintServerLogger.log(" printFileByURL");
        CommonPrintServerLogger.log("    > " + file_url);

        //If no Printer is selected, return immediately.
        if (!CommonPrintServerServer.getInstance().isPrinterSelected()) {
            CommonPrintServerLogger.log("Operation aborted: No printer selected.");
            return "NOK|No printer selected";
        }

        URL url;
        try {
            url = new URL(file_url);
        } catch (MalformedURLException ex) {
            return "NOK|" + ex.getMessage();
        }
        InputStream psStream;
        try {
            psStream = url.openStream();
        } catch (IOException ex) {
            return "NOK|" + ex.getMessage();
        }
        URLConnection urlConn;
        try {
            urlConn = url.openConnection();
        } catch (IOException ex) {
            return "NOK|" + ex.getMessage();
        }

        // If file is a PDF, special treatment, uses PDFBox to print it else
        // not working.
        if (urlConn.getContentType() != null && urlConn.getContentType().equalsIgnoreCase("application/pdf")) {
            CommonPrintServerLogger.log("    > application/pdf [" + file_url + "]");
            String filename = file_url.substring(file_url.lastIndexOf('/') + 1, file_url.length());

            // Create temp PDF file from the one in URL
            File temp;
            try {
                temp = File.createTempFile("Common-PrintServer_", ".pdf");
            } catch (IOException ex) {
                return "NOK|" + ex.getMessage();
            }
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(temp);
            } catch (FileNotFoundException ex) {
                return "NOK|" + ex.getMessage();
            }
            int read;
            byte[] bytes = new byte[1024];
            try {
                while ((read = psStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                    outputStream.flush();
                }
            } catch (IOException ex) {
                return "NOK|" + ex.getMessage();
            }
            try {
                outputStream.close();
            } catch (IOException ex) {
                return "NOK|" + ex.getMessage();
            }

            // PDFBox
            PDDocument document;
            try {
                document = PDDocument.load(temp);
            } catch (IOException ex) {
                return "NOK|" + ex.getMessage();
            }

            if (!CommonPrintServerServer.getInstance().isPrinterSelected()) {
                CommonPrintServerLogger.log("Operation aborted: No printer selected.");
                return "NOK|No printer selected";
            }

            DocPrintJob job;

            // No worry about NullPointerExeption. If no printer is selected
            // the function return.
            job = CommonPrintServerServer.getInstance().getSelectedPrinter().createPrintJob();

            if (job == null) {
                try {
                    psStream.close();
                } catch (IOException ex) {
                    return "NOK|" + ex.getMessage();
                }
                return "NOK|Unable to find the printer...";
            }

            PrinterJob pjob = PrinterJob.getPrinterJob();
            try {
                pjob.setPrintService(job.getPrintService());
            } catch (PrinterException ex) {
                return "NOK|" + ex.getMessage();
            }
            pjob.setJobName(filename);
            pjob.setPageable(new PDFPageable(document));
            try {
                pjob.print();
            } catch (PrinterException ex) {
                return "NOK|" + ex.getMessage();
            }
        } else {
            // If not PDF but a file with raw code then print.
            DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;

            Doc myDoc = new SimpleDoc(psStream, psInFormat, null);

            try {
                DocPrintJob job = CommonPrintServerServer.getInstance().getSelectedPrinter().createPrintJob();
                job.print(myDoc, null);
            } catch (PrintException pe) {
                try {
                    psStream.close();
                } catch (IOException ignored) {
                }
                return "NOK|" + pe.getMessage();
            }

        }

        try {
            psStream.close();
        } catch (IOException ex) {
            return "NOK|" + ex.getMessage();
        }

        return "OK";
    }

}
