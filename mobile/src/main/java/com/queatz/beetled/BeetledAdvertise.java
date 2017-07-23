package com.queatz.beetled;

/**
 * Created by jacob on 7/23/17.
 */

public class BeetledAdvertise {
/*
    private static final UUID ADVERTISE_CHARACTERISTIC_UUID = UUID.fromString("2d444574-5fdb-4e49-8921-3823f80b39a6"); // made up
    private static final UUID UUID_SERVICE = UUID.fromString("2d444574-5fdb-4e49-8921-3823f80b39a5"); // made up

    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback advertiseCallback;

    private BluetoothGattServer bluetoothGattServer;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic advertiseCharacteristic;

    public void disableAdvertise() {
        if (advertiseCallback != null) {
            BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

            if (advertiser != null) {
                advertiser.stopAdvertising(advertiseCallback);
                advertiseCallback = null;
            }
        }

        if (bluetoothGattServer != null) {
            bluetoothGattServer.close();
            bluetoothGattServer = null;
        }
    }

    private void doAdvertise() {
        if (bluetoothGattServer != null) {
            return;
        }

        advertiseCharacteristic = new BluetoothGattCharacteristic(
                ADVERTISE_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        advertiseCharacteristic.setValue("my_profile");

        bluetoothGattService = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        bluetoothGattService.addCharacteristic(advertiseCharacteristic);

        bluetoothGattServer = bluetoothManager.openGattServer(app, new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
            }
        });

        bluetoothGattServer.addService(bluetoothGattService);

        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.w(getClass().getSimpleName(), "advertise - advertise start success");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.w(getClass().getSimpleName(), "advertise - advertise start failure: " + errorCode);
            }
        };

        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        if (advertiser != null) {
            AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(true)
                    .build();

            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addServiceUuid(new ParcelUuid(UUID_SERVICE))
                    .build();

            advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
        }
    }*/
}
