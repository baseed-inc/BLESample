//
//  PeripheralManager.swift
//  virtual-peripheral
//
//  Created by Hiroshi TANAKA on 2020/06/23.
//  Copyright © 2020 Baseed. All rights reserved.
//

import Foundation
import CoreBluetooth

class PeripheralManagerDelegate : NSObject, CBPeripheralManagerDelegate {
    // テスト用のサービス
    let BLE_SERVICE_UUID = CBUUID(string:"BFFD2CDD-C6D4-4F53-AE05-F2A0BE431EDE")
    // テスト用のキャラクタリスティック
    let BLE_CHARA_UUID = CBUUID(string:"E919A93F-EEBF-45BE-A148-2A256559EAB2")
    var myCharacteristic:CBMutableCharacteristic!
    var powerOn = false

    override init() {
        super.init()
        myCharacteristic = CBMutableCharacteristic(
               type: self.BLE_CHARA_UUID
            , properties: CBCharacteristicProperties.notify
            , value: nil
            , permissions: CBAttributePermissions.readable)
    }
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        if peripheral.state == .poweredOn {
            powerOn = true
            print("powerOn")
            
            addService(peripheral: peripheral)
        } else if peripheral.state == .poweredOff {
            peripheral.stopAdvertising()
            powerOn = false
            print("powerOff")
        }
    }
    func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
        if error == nil {
            print("success start advertising")
        } else {
            print("failed start advertising")
        }
    }
    func peripheralManager(_ peripheral: CBPeripheralManager, didAdd service: CBService, error: Error?) {
        if error == nil {
            print("success add service")
            peripheral.startAdvertising([CBAdvertisementDataServiceUUIDsKey:[BLE_SERVICE_UUID]])
        } else {
            print("failed add service")
        }
    }
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveRead request: CBATTRequest) {
        print("didReceiveRead")
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didSubscribeTo characteristic: CBCharacteristic) {
        print("subscribe")
        // TODO: 一度に送信できないことを考慮
        peripheral.updateValue("SampleData".data(using: .utf8)!, for: self.myCharacteristic, onSubscribedCentrals: nil)
    }
    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didUnsubscribeFrom characteristic: CBCharacteristic) {
        print("unsubscribe")
    }
    func peripheralManagerIsReady(toUpdateSubscribers peripheral: CBPeripheralManager) {
        print("Ready");
    }
    func addService(peripheral: CBPeripheralManager!) {
        let service = CBMutableService(type: BLE_SERVICE_UUID, primary: true)
        service.characteristics = [self.myCharacteristic]
        peripheral.add(service)
    }
    func sendData() {
        
    }
}
