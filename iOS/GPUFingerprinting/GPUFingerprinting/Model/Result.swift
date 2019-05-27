//
//  Result.swift
//  GPUFingerprinting
//
//  Copyright © 2019 Instituto de Pesquisas Eldorado. All rights reserved.
//

import Foundation

struct Result: Codable {
    let device: String
    let osVersion: String
    let imageHash: String
    let method: String
    let image: String
    let date: String
    
    let imageId: String
    let base64: String
    
    init(device: String, osVersion: String, imageHash: String, method: String, image: String, date: String, base64: String) {
        self.device = device
        self.osVersion = osVersion
        self.imageHash = imageHash
        self.method = method
        self.image = image
        self.date = date
        self.base64 = base64
        imageId = UUID().uuidString
    }
    
}
