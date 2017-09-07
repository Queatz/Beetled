package com.queatz.beetleconnect;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.queatz.beetleconnect.util.WriteQueue;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by jacob on 7/23/17.
 *
 * Manages the active bluetooth link to the beetle.
 */
public class BeetleGatt extends BluetoothGattCallback {

    private class BeetledGattWrite {
        final BluetoothGattCharacteristic characteristic;
        final String value;

        public BeetledGattWrite(BluetoothGattCharacteristic characteristic, String value) {
            this.characteristic = characteristic;
            this.value = value;
        }
    }

    private WriteQueue<BeetledGattWrite> queue = new WriteQueue<BeetledGattWrite>() {
        @Override
        public void emit(BeetledGattWrite item) {
            if (item.characteristic == null || gatt == null) {
                queue.next();
                return;
            }

            Log.w("BEETLE", "write: " + item.value);
            item.characteristic.setValue(item.value.getBytes(Charset.forName("ISO-8859-1")));
            gatt.writeCharacteristic(item.characteristic);
        }
    };

    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic serialPortCharacteristic;
    private boolean isValidBeetle = false;

    public void close() {
        if (gatt == null) {
            return;
        }

        if (isValidBeetle) {
            Beetle.getBeetleListener().onDisconnected();
            Beetle.getBeetleManager().unlock(this);
        }

        gatt.close();
        gatt = null;
    }

    public void write(String string) {
        if (gatt == null) {
            return;
        }

        queue.add(new BeetledGattWrite(serialPortCharacteristic, string));
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (isValidBeetle) {
            return;
        }

        this.gatt = gatt;

        switch (newState) {
            case BluetoothProfile.STATE_DISCONNECTED:
                close();
                break;
            case BluetoothProfile.STATE_CONNECTED:
                gatt.discoverServices();
                break;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        this.gatt = gatt;
        BluetoothGattService service = gatt.getService(Config.BLE_SERVICE_UUID);

        if (service == null) {
            close();
            return;
        }

        if (isValidBeetle || Beetle.getBeetleManager().isLocked()) {
            return;
        }

        isValidBeetle = true;
        Beetle.getBeetleManager().lock(this);

        BluetoothGattCharacteristic commandCharacteristic = service.getCharacteristic(Config.BLE_COMMAND_UUID);
        serialPortCharacteristic = service.getCharacteristic(Config.BLE_SERIAL_PORT_UUID);

        if (commandCharacteristic == null || serialPortCharacteristic == null) {
            close();
            return;
        }

        queue.add(new BeetledGattWrite(commandCharacteristic, Config.BLUNO_PASSWORD));
        queue.add(new BeetledGattWrite(commandCharacteristic, Config.BLUNO_BAUDRATE_BUFFER));
        gatt.setCharacteristicNotification(serialPortCharacteristic, true);

        Beetle.getBeetleListener().onConnected();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.w("BEETLE", "read: " + new String(characteristic.getValue()));
        UUID uuid = characteristic.getUuid();
        if (Config.BLE_SERIAL_PORT_UUID.equals(uuid)) {
            Beetle.getBeetleListener().onRead(characteristic.getStringValue(0));
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.w("BEETLE", "<<<<: " + new String(characteristic.getValue()));
        queue.next();
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.w("BEETLE", "got: " + new String(characteristic.getValue()));
        if (!isValidBeetle) {
            return;
        }

        UUID uuid = characteristic.getUuid();
        if (Config.BLE_SERIAL_PORT_UUID.equals(uuid)) {
            Beetle.getBeetleListener().onRead(characteristic.getStringValue(0));
        }
    }
}
