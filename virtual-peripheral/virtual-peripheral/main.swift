//
//  main.swift
//  virtual-peripheral
//
//  Created by Hiroshi TANAKA on 2020/06/22.
//  Copyright © 2020 Baseed. All rights reserved.
//

import Foundation
import CoreBluetooth

let peripheralManagerDelegate = PeripheralManagerDelegate()
let peripheralManager = CBPeripheralManager(delegate: peripheralManagerDelegate, queue: nil)

let runLoop = RunLoop.current
let distantFuture = Date.distantFuture
while runLoop.run(mode: RunLoop.Mode.default, before: distantFuture) {
    sleep(1)
}
