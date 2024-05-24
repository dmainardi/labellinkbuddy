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

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    
    public EsitoControlloCodiceABarre stampaEtichettaEControllaCodiceABarre(String identificativo) {
        if (identificativo != null && !identificativo.isBlank()) {
            SerialPort comPortBar = null;
            for (SerialPort commPortTemp : SerialPort.getCommPorts()) {
                String portName = commPortTemp.getDescriptivePortName().toLowerCase();
                if (portName.contains("barcode")) {
                    comPortBar = commPortTemp;
                }
            }

            if (comPortBar != null) {
                EsitoControlloCodiceABarre esito;
                comPortBar.disableExclusiveLock();
                comPortBar.openPort();
                comPortBar.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                try (InputStream inBar = comPortBar.getInputStream()) {
                    BufferedReader readerBar = new BufferedReader(new InputStreamReader(inBar));
                    String lineBar;
                    if (readerBar.ready())
                        readerBar.readLine();   //  utile per 'svuotare' eventuali codici a barre letti prima della stampa dell'etichetta
                    stampaSuEtichettatrice(nomeEtichettatrice, identificativo);
                    if ((lineBar = readerBar.readLine()) != null) {
                        if (lineBar.equals(identificativo)) {
                            System.out.println("TUTTO OK");
                            esito = EsitoControlloCodiceABarre.ESITO_POSITIVO;
                        }
                        else {
                            System.out.println("Nooooo "+ lineBar);
                            esito = EsitoControlloCodiceABarre.ESITO_NEGATIVO;
                        }
                    }
                    else
                        esito = EsitoControlloCodiceABarre.ERRORE_BARCODE_VUOTO;
                } catch (IOException ex) {
                    Logger.getLogger(LabelLinkBuddy.class.getName()).log(Level.SEVERE, null, ex);
                    esito = EsitoControlloCodiceABarre.ERRORE_BARCODE;
                } catch (JRException ex) {
                    Logger.getLogger(LabelLinkBuddy.class.getName()).log(Level.SEVERE, null, ex);
                    esito = EsitoControlloCodiceABarre.ERRORE_CREAZIONE_ETICHETTA;
                } catch (EtichettatriceNonTrovataException ex) {
                    Logger.getLogger(LabelLinkBuddy.class.getName()).log(Level.SEVERE, null, ex);
                    esito = EsitoControlloCodiceABarre.ERRORE_STAMPANTE_NON_TROVATA;
                }
                comPortBar.closePort();
                return esito;
            }
            return EsitoControlloCodiceABarre.ERRORE_BARCODE_NON_COLLEGATO;
        }
        return EsitoControlloCodiceABarre.ERRORE_IDENTIFICATIVO_VUOTO;
    }
    
    private void stampaSuEtichettatrice(String nomeEtichettatrice, String identificativo) throws JRException, EtichettatriceNonTrovataException {
        if (nomeEtichettatrice != null && !nomeEtichettatrice.isBlank()) {
            PrintService printService = PrintUtility.findPrintService(nomeEtichettatrice);
            if (printService != null) {
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
            }
            else
                throw new EtichettatriceNonTrovataException();
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
