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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterName;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;

/**
 *
 * @author adminavvimpa
 */
public class LabelLinkBuddy {
    private final String nomeEtichettatrice;

    public LabelLinkBuddy(String nomeEtichettatrice) {
        this.nomeEtichettatrice = nomeEtichettatrice;
    }
    
    public void provaAVedereSeFunziona() {
        stampaSuEtichettatrice(nomeEtichettatrice, "aaahhhh");
    }
    
    private void stampaSuEtichettatrice(String nomeEtichettatrice, String identificativo) {
        if (nomeEtichettatrice != null && !nomeEtichettatrice.isBlank()) {
            PrintService printService = PrintUtility.findPrintService(nomeEtichettatrice);
            if (printService != null) {
                try {
                    JasperPrint jasperPrint = creaReport(identificativo);

                    PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();

                    PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
                    printServiceAttributeSet.add(printService.getAttribute(PrinterName.class));

                    JRPrintServiceExporter exporter = new JRPrintServiceExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    SimplePrintServiceExporterConfiguration configuration = new SimplePrintServiceExporterConfiguration();

                    configuration.setPrintServiceAttributeSet(printServiceAttributeSet);
                    configuration.setPrintRequestAttributeSet(printRequestAttributeSet);
                    configuration.setDisplayPageDialog(false);
                    configuration.setDisplayPrintDialog(false);
                    exporter.setConfiguration(configuration);
                    exporter.exportReport();

                    //JasperExportManager.exportReportToPdfFile(jasperPrint, "maina.pdf");
                } catch (JRException ex) {
                    Logger.getLogger(LabelLinkBuddy.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getLogger(LabelLinkBuddy.class.getName()).log(Level.SEVERE, null, "vendita.etichetta.stampa.errore.etichettatriceMancante");
            }
        }
    }

    private JasperPrint creaReport(String identificativo) throws JRException {
        List<Object> tempBean = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("ReportTitle", "Etichetta");
        params.put("identificativo", identificativo);

        tempBean.add(new Object());

        ResourceFileHelper resourceFileHelper = new ResourceFileHelper();
        InputStream inputStream = resourceFileHelper.getFileAsIOStream("documents/etichetta.jasper");
        return JasperFillManager.fillReport(inputStream, params, new JRBeanCollectionDataSource(tempBean));
    }
}
