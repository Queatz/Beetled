package com.queatz.bettleconnect;

import java.util.UUID;

/**
 * Created by jacob on 9/1/17.
 */

public class Config {
    public static final UUID BLE_SERVICE_UUID = UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");
    public static final UUID BLE_SERIAL_PORT_UUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    public static final UUID BLE_COMMAND_UUID = UUID.fromString("0000dfb2-0000-1000-8000-00805f9b34fb");

    public static final int BLUNO_BAUDRATE = 115200;
    public static final String BLUNO_PASSWORD = "AT+PASSWOR=DFRobot\r\n";
    public static final String BLUNO_BAUDRATE_BUFFER = "AT+CURRUART=" + BLUNO_BAUDRATE + "\r\n";
}
