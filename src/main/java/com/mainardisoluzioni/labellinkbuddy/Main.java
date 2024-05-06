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

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.milo.opcua.stack.core.UaException;

/**
 *
 * @author adminavvimpa
 */
public class Main {
    public static void main(String[] args) {
        final String nomeEtichettatrice;
        if (args.length > 0)
            nomeEtichettatrice = args[0];
        else
            nomeEtichettatrice = "TOSHIBA-001";

        /*LabelLinkBuddy instance = new LabelLinkBuddy(nomeEtichettatrice);
        instance.stampaEtichettaEControllaCodiceABarre();*/
        
        PyPanda instance = new PyPanda(nomeEtichettatrice);
        try {
            instance.createClientAndWaitForPrint();
        } catch (UaException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
