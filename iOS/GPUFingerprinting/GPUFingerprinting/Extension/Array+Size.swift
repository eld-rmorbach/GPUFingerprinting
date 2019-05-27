//
//  Array+Size.swift
//  GPUFingerprinting
//
//  Copyright © 2019 Instituto de Pesquisas Eldorado. All rights reserved.
//

import Foundation


extension Array {
    func size() -> Int {
        // stride é a quantidade de memória que um elemento ocupa quando ele está em um array.
        return MemoryLayout<Element>.stride * self.count
    }
}
