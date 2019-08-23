# Common-PrintServer

Common-PrintServer is a mini server allowing you to print your documents directly on the printer via your internet browser.

## Usage

From the command-line :
```
cd out/artifacts/Common-PrintServer
java -jar Common-PrinterServer.jar
```

Else, you can just double-click on the Common-PrinterServer.jar file, most of operating system should be able to open it.  

If your operating system supports Java SystemTray then you should see the icon on your task bar, example on windows :  
![Common-PrintServer icon](https://doc-cloudfront.common-services.com/sonice_etiquetage/wp-content/uploads/2016/08/Common-PrintServer-Barre-des-taches.jpg)

You can now go to : [http://localhost:4567/getPrinters](http://localhost:4567/getPrinters).  
A list of your installed printers should be listed, example :  
`{"response":"Canon MX340 series Printer|Canon MX340 series FAX|Datamax-O\u0027Neil E4204B-Mark-III","error":""}`

_Please not that the printer driver needs to be installed for the printer to be listed._

You can check logs and errors in the Log menu from the TrayIcon :  
![Log](https://doc-cloudfront.common-services.com/sonice_etiquetage/wp-content/uploads/2016/08/log.jpg)

## Ports

Common-PrintServer uses port **4567** for HTTP requests and **4568** for HTTPS requests.  
Make sure they're available.

## API

A simple REST API allows developers to interact with printers.
All response from the API are in JSON format, and contains 2 indexes :
* response
* error

_(See example in **Usage**)_

The **response** index contains the expected response from the API.  
In case of an error, the **error** index is filled with the error message and **response** is empty.

### Base URL

All requests must be done on :
* http::locahost:4567/
* http::locahost:4568/

depending on your website using HTTPS or not (see **Ports** upper).


Therefore, to fetch a list of available printers you can call either `http://localhost:4567/getPrinters` or `https://localhost:4568/getPrinters`

### `GET /getPrinters`

Returns a list of available printers, separated by a vertical bar | :
`{"response":"Canon MX340 series Printer|Canon MX340 series FAX|Datamax-O\u0027Neil E4204B-Mark-III","error":""}`

If no printers are found, returns :
`{"response":"","error":"No printer found..."}`

### `GET /getPrinter`

Returns the currently selected printer, or "N/A" if none selected yet.

### `GET /setPrinter/:name`

Set the selected printer to :name variable.

### `POST /printRaw`

Send the raw code to the printer to print. Simply send the raw code to print in the request body.  
Returns `{"response":"OK","error":""}` on success.

### `POST /printFileByURL`

Send the document to print from URL to the printer to print. Simply send the URL of document to print in the request body.  
Returns `{"response":"OK","error":""}` on success.

## HTTPS

To deal with HTTPS request, made from HTTPS website, Common-PrintServer uses a self signed certificate.  
Therefore, you need to add it to your allowed certificate, or go on this page from your browser :  [https://localhost:4568/getPrinters](https://localhost:4568/getPrinters).  

Your browser will bloc the page for security reasons, add an exception to access the page anyway then you'll be able to deal
with Common-PrintServer from HTTPS website on the client machine.

Example with Firefox :

![Firefox exception](https://doc-cloudfront.common-services.com/sonice_etiquetage/wp-content/uploads/2016/08/Firefox.jpg)

Example with Chrome :

![Chrome exception](https://www.dropbox.com/s/uqimj54z7vy4l1s/connexion_pas_priv%C3%A9e.png?raw=1)

## Contributing

I do not have much time to focus on this anymore, it might not be that necessary anymore with Google Cloud Printer and stuff but who knows,
maybe it can help someone out there.

If you are interested in contributing to Common-PrintServer please fork the project and create a pull request.

Thank you :) !

## License

```
The MIT License (MIT)

Copyright (c) 2016 CommonPrintServer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```