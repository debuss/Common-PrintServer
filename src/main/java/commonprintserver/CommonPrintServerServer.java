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

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * This class is responsible for handling server connection. It can
 * start/stop/restart listening for incoming request. It has been built
 * according to singleton pattern.
 *
 * @author Luigi & Alexandre D.
 */
final class CommonPrintServerServer {

    /**
     * Used to handle a single instance of this class.
     */
    private static CommonPrintServerServer instance = null;

    /**
     * Map used to store local printers. Key -> Printer name Value -> The
     * corresponding PrinterService object
     */
    private final Map<String, PrintService> printersMap;

    /**
     * This is the currently selected printer (must be present in printersMap).
     */
    private PrintService selectedPrinter = null;

    /**
     * Private constructor for local initialization. It cannot be used outside
     * of this class.
     */
    private CommonPrintServerServer() {
        this.printersMap = new HashMap<>();
    }

    /**
     * Return the unique class instance.
     *
     * @return The class instance
     */
    static CommonPrintServerServer getInstance() {
        if (instance == null) {
            instance = new CommonPrintServerServer();
        }
        return instance;
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {
        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            response.type("application/json");
        });
    }

    /**
     * Used to start the server, listening to incoming requests.
     */
    void start() {
        CommonPrintServerLogger.log("Common-PrintServer Started");

        /* Thanks to https://github.com/perwendel/spark/commit/e1b6144b63ef5346a35be3cffbf6f96134b4c784. */
        port(4567, 4568);

        /* The classpath is different when running in Intelliji IDEA and from command line. */
        /* To avoid problems with secure method, check both files. */
        File keystore_ide = new File(CommonPrintServer.class.getResource("/keystore/keystore.jks").getPath());
        File keystore_jar = new File("keystore/keystore.jks");

        if (keystore_ide.exists() && keystore_ide.isFile()) {
            secure(CommonPrintServer.class.getResource("/keystore/keystore.jks").getPath(), "printme2016", null, null);
        } else if (keystore_jar.exists() && keystore_jar.isFile()) {
            secure("keystore/keystore.jks", "printme2016", null, null);
        } else {
            CommonPrintServerLogger.log("Unable to load the keystore file. Only HTTP request will be valid.");
        }

        enableCORS("*", "*", "*");

        get("/", (req, res) -> {
            return new CommonPrintServerResponse("Hello World !");
        }, new CommonPrintServerJsonTransformer());

        get("/getPrinters", (req, res) -> {
            String printer_list = this.printersToStringList();

            if ("".equals(printer_list)) {
                return new CommonPrintServerResponse("", "No printer found...");
            }

            return new CommonPrintServerResponse(printer_list.replaceAll("\\|$", ""));
        }, new CommonPrintServerJsonTransformer());

        get("/getPrinter", (req, res) -> {
            return new CommonPrintServerResponse(this.isPrinterSelected() ? getSelectedPrinter().getName() : "N/A");
        }, new CommonPrintServerJsonTransformer());

        get("/setPrinter/:name", (req, res) -> {
            return new CommonPrintServerResponse(this.setSelectedPrinterByName(req.params(":name")));
        }, new CommonPrintServerJsonTransformer());

        post("/printRaw", (req, res) -> {
            String response = CommonPrintServerPrintServices.printRaw(req.body());

            if (response.startsWith("NOK|")) {
                return new CommonPrintServerResponse("", response.substring(4));
            }

            return new CommonPrintServerResponse("OK");
        }, new CommonPrintServerJsonTransformer());

        post("/printFileByURL", (req, res) -> {
            String response = CommonPrintServerPrintServices.printFileByURL(req.body());

            if (response.startsWith("NOK|")) {
                return new CommonPrintServerResponse("", response.substring(4));
            }

            return new CommonPrintServerResponse("OK");
        }, new CommonPrintServerJsonTransformer());

    }

    /**
     * Stop the server and reset logs.
     */
    void stop() {
        spark.Spark.stop();
        CommonPrintServerLogger.clearLog();
        this.selectedPrinter = null;
        this.printersMap.clear();
    }

    /**
     * Simply stop and start again the server.
     */
    void restart() {
        this.stop();
        this.start();
    }

    /**
     * Reset the printer list.
     */
    private void clearPrinterList() {
        this.printersMap.clear();
    }

    /**
     * Search for printers accessible from the server.
     */
    private void loadPrinters() {
        this.clearPrinterList();
        for (PrintService print
                : PrintServiceLookup.lookupPrintServices(null, null)) {
            this.printersMap.put(print.getName(), print);
        }
    }

    /**
     * Return the printers Map.
     *
     * @return The Map containing all the printers.
     */
    private Map<String, PrintService> getPrinters() {
        if (this.printersMap.isEmpty()) {
            this.loadPrinters();
        }

        return printersMap;
    }

    /**
     * Get the list of printers on computer and return it.<br>
     * Format : printer1|printer2|printer3
     *
     * @return List of printer.
     */
    private String printersToStringList() {
        CommonPrintServerLogger.log(" getPrinters");
        String printer_list = "";
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService printer : printServices) {
            CommonPrintServerLogger.log("    > " + printer.getName());
            printer_list += printer.getName() + "|";
        }

        return printer_list;
    }

    /**
     * Return the printer object.
     *
     * @param name The printer name
     * @return The printerService instance if found, otherwise null.
     */
    private PrintService getPrinterByName(String name) {
        return this.getPrinters().get(name);
    }

    /**
     * Get the selected printer.
     *
     * @return Selected printer.
     */
    PrintService getSelectedPrinter() {
        CommonPrintServerLogger.log(" getPrinter > " + (this.isPrinterSelected() ? this.selectedPrinter.getName() : "N/A"));
        return this.selectedPrinter;
    }

    /**
     * Set the selected printer.
     *
     * @param name Printer name.
     * @return Response, OK or NOK for error message.
     */
    private String setSelectedPrinterByName(String name) {
        try {
            this.selectedPrinter = this.getPrinterByName(java.net.URLDecoder.decode(name, "UTF-8"));
            CommonPrintServerLogger.log("Selected printer: " + this.selectedPrinter.getName());
        } catch (UnsupportedEncodingException uee) {
            CommonPrintServerLogger.log(uee.getMessage());
            this.selectedPrinter = null;
            return "NOK|Error with [java.net.URLDecoder.decode].";
        } catch (NullPointerException e) {
            CommonPrintServerLogger.log(e.getMessage());
            this.selectedPrinter = null;
            return "NOK|" + e.getLocalizedMessage();
        }

        CommonPrintServerLogger.log(" setPrinter");
        CommonPrintServerLogger.log("    > " + this.selectedPrinter);
        return "OK";
    }

    /**
     * Check if there is any selected printer.
     *
     * @return True if there is a selected printer, false otherwise.
     */
    boolean isPrinterSelected() {
        return !(this.selectedPrinter == null);
    }

}
