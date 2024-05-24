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

/**
 *
 * @author adminavvimpa
 */
public enum EsitoControlloCodiceABarre {
    ANCORA_DA_DEFINIRE((short)0),
    ESITO_POSITIVO((short)1),
    ESITO_NEGATIVO((short)2),
    ERRORE_STAMPANTE_NON_TROVATA((short)3),
    ERRORE_BARCODE((short)4),
    ERRORE_BARCODE_NON_COLLEGATO((short)5),
    ERRORE_CREAZIONE_ETICHETTA((short)6),
    ERRORE_BARCODE_VUOTO((short)7),
    ERRORE_IDENTIFICATIVO_VUOTO((short)8);
    
    private final short value;
    
    private EsitoControlloCodiceABarre(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
    
}
