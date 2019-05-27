//
//  SendResultService.swift
//  GPUFingerprinting
//
//  Copyright © 2019 Instituto de Pesquisas Eldorado. All rights reserved.
//

import Foundation

struct SendResultService {
    
    typealias SendResultCompletion = (_ success: Bool)->()
    
    static func send(result: Result, to endpoint: String, completion: @escaping SendResultCompletion) {
        
        guard let url = URL(string: endpoint) else { return }
        let session = URLSession.shared
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        let encoder = JSONEncoder()
        
        guard let body = try? encoder.encode(result) else { return }
        request.httpBody = body
        
        let dataTask = session.dataTask(with: request) { data, response, error in
            let r = (error != nil) ? false : true
            completion(r)
        }
        
        dataTask.resume()        
    }
    
    
    
}
