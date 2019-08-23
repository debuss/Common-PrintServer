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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * CommonPrintServer Properties class.
 *
 * @author Alexandre D.
 * @version 1.0.0
 * @since 2016-04-06
 * @deprecated
 */
public class CommonPrintServerProperties {

    /** Properties instance to read the properties */
    private static Properties propertiesRead = null;

    /** Properties instance to write (save) the properties */
    private static Properties propertiesWrite = null;

    /**
     * Get a property value.
     *
     * @param property Name of the property.
     * @return Property value
     */
    public static String get(String property) {
        if (propertiesRead == null) {
            propertiesRead = new Properties();
        }

        InputStream input = null;

        try {
            input = new FileInputStream("config.properties");

            propertiesRead.load(input);
        } catch (IOException io) {
            CommonPrintServerLogger.log("Unable to load properties file.");
            CommonPrintServerLogger.log(io.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    CommonPrintServerLogger.log(e.getMessage());
                }
            }
        }

        return propertiesRead.getProperty(property);
    }

    /**
     * Set/Save a property.
     *
     * @param property Name of the property.
     * @param value Value of the property.
     */
    public static void set(String property, String value) {
        if (propertiesWrite == null) {
            propertiesWrite = new Properties();
        }

        OutputStream output = null;

        try {
            output = new FileOutputStream("config.properties");

            propertiesWrite.setProperty(property, value);
            propertiesWrite.store(output, null);
        } catch (IOException io) {
            CommonPrintServerLogger.log("Unable to load properties file.");
            CommonPrintServerLogger.log(io.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    CommonPrintServerLogger.log(e.getMessage());
                }
            }
        }
    }

}
