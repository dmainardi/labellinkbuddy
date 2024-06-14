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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adminavvimpa
 */
public class BaseDiDati {
    
    private final String DATABASE_URL = "jdbc:postgresql://192.168.1.132:5432/avvimpa";
    private final String DATABASE_USERNAME = "avvimpa";
    private final String DATABASE_PASSWORD = "123Stella!!!";
    
    public EsitoControlloCodiceABarre creaEvento(String nomeOperazione) {
        try (Connection con = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            try (Statement stmt = con.createStatement()) {
                String dataOra = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss"));
                String insertSql = "INSERT INTO evento(dataora, operazione)"
                        + " VALUES('" + dataOra + "',"
                        + " '" + nomeOperazione + "')";
                int righeAggiunte = stmt.executeUpdate(insertSql);
                if (righeAggiunte > 0)
                    return EsitoControlloCodiceABarre.ESITO_POSITIVO;
                else
                    return EsitoControlloCodiceABarre.ERRORE_DATABASE_NESSUNA_RIGA_AGGIUNTA;
            }
        } catch (SQLException ex) {
            Logger.getLogger(BaseDiDati.class.getName()).log(Level.SEVERE, null, ex);
            return EsitoControlloCodiceABarre.ERRORE_DATABASE_CONNESSIONE;
        }
    }
}
