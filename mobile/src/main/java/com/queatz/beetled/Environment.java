package com.queatz.beetled;

import java.util.UUID;

/**
 * Created by jacob on 7/23/17.
 */

public class Environment {
    public static final UUID BLE_SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    public static final UUID BLE_SERIAL_PORT_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    public static final UUID BLE_COMMAND_UUID = UUID.fromString("0000dfb2-0000-1000-8000-00805f9b34fb");

    public static final String UNLOCK_ACTION = "com.queatz.beetled.UNLOCK";

    public static final int BLUNO_BAUDRATE = 115200;
    public static final String BLUNO_PASSWORD = "AT+PASSWOR=DFRobot\r\n";
    public static final String BLUNO_BAUDRATE_BUFFER = "AT+CURRUART=" + BLUNO_BAUDRATE + "\r\n";

    public static final String LOCK_STATE_REQUEST = "R";
    public static final String LOCK_STATE_LOCK = "L";
    public static final String LOCK_STATE_UNLOCK = "U";
}
