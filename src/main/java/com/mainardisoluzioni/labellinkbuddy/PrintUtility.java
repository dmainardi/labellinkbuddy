/*
 * Copyright (C) 2024 adminavvimpa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mainardisoluzioni.labellinkbuddy;

import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import javax.print.PrintService;

/**
 *
 * @author adminavvimpa
 */
public final class PrintUtility {

    /**
     * Retrieve a Print Service with a name containing the specified
     * PrinterName; will return null if not found.
     *
     * @param printerName
     * @return
     */
    public static PrintService findPrintService(String printerName) {

        printerName = printerName.toLowerCase();

        PrintService service = null;

        // Get array of all print services
        PrintService[] services = PrinterJob.lookupPrintServices();

        // Retrieve a print service from the array
        for (int index = 0; service == null && index < services.length; index++)
            if (services[index].getName().toLowerCase().contains(printerName))
                service = services[index];

        // Return the print service
        return service;
    }

    /**
     * Retrieves a List of Printer Service Names.
     *
     * @return List
     */
    public static List<String> getPrinterServiceNameList() {

        // get list of all print services
        PrintService[] services = PrinterJob.lookupPrintServices();
        List<String> list = new ArrayList<>();

        for (int i = 0; i < services.length; i++)
            list.add(services[i].getName());

        return list;
    }

    /**
     * Utility class; no construction!
     */
    private PrintUtility() {
    }
}

