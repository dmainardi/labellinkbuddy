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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.milo.opcua.stack.core.UaException;

/**
 *
 * @author adminavvimpa
 */
public class HeartbeatPlcControl {

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> beeperHandle;

    public HeartbeatPlcControl() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void doHeartbeat() {
        HeartbeatPlc heartbeat = new HeartbeatPlc();
        Runnable task = () -> System.out.println(".");
        Runnable task2 = () -> {
            try {
                heartbeat.sendHeartbeat();
            } catch (InterruptedException | ExecutionException | UaException ex) {
                Logger.getLogger(HeartbeatPlcControl.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("AAHHHHHH");
            }
        };
        beeperHandle = scheduler.scheduleAtFixedRate(task2, 5, 7, SECONDS);
    }
    
    public void cancelHeartbeat() {
        beeperHandle.cancel(true);
        scheduler.shutdownNow();
    }
}
