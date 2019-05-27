//
//  ViewController.swift
//  GPUFingerprinting
//
//  Copyright © 2019 Instituto de Pesquisas Eldorado. All rights reserved.
//

import UIKit
import CryptoSwift

class ViewController: UIViewController {

    let endpoint = "http://10.10.6.101:3000"
    let dateFormat = "dd-MM-yyyy HH:mm:ss"
    
    var deviceInfo = ""
    var osVersion = ""
    
    private func send(hash: String, method: String, image: ImageType, base64: String) {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = dateFormat
        let result = Result(device: deviceInfo, osVersion: osVersion, imageHash: hash, method: method, image: image.rawValue, date: dateFormatter.string(from: Date()), base64: base64)
        SendResultService.send(result: result, to: endpoint) {  success in
          print(success)
        }
    }

    private func renderWithGLKit(image: ImageType) {
        let imageRenderer = GLKitRenderer()
        let rect = CGRect(x: 0, y: 0, width: 250, height: 250)
        imageRenderer.renderImage(type: image, in: rect) { base64 in
            if base64 != nil {
                self.send(hash: base64!.md5(), method: "OpenGL", image: image, base64: base64!)
            }
        }
    }

    private func addRenderButton() {
        let button = UIButton(frame: CGRect(x: 0, y: 0, width: 200, height: 100))
        button.backgroundColor = UIColor.blue
        button.addTarget(self, action: #selector(render), for: .touchUpInside)
        button.setTitle("Render", for: .normal)
        button.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(button)
        
        button.centerXAnchor.constraint(equalTo: self.view.centerXAnchor).isActive = true
        button.centerYAnchor.constraint(equalTo: self.view.centerYAnchor).isActive = true
        button.heightAnchor.constraint(equalToConstant: 100).isActive = true
        button.widthAnchor.constraint(equalToConstant: 200).isActive = true
    }
    
    func renderOpenGL() {
        renderWithGLKit(image: ImageType.triangleAndSquareAliasing)
        renderWithGLKit(image: ImageType.triangleAndSquareAntiAliasing)
        renderWithGLKit(image: ImageType.triangleAndSquareAntiAliasingTexture)
        renderWithGLKit(image: ImageType.triangleAndSquareAntiAliasingLight)
        renderWithGLKit(image: ImageType.triangleAndSquareAntiAliasingOverlapRotate)
        renderWithGLKit(image: ImageType.triangleAndSquareAntiAliasingOverlapRotateLight)
    }
    
    @objc func render() {
        renderOpenGL()
    }
    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        
        let systemVersion = UIDevice.current.systemVersion
        let deviceName = UIDevice.modelName
        deviceInfo = "\(deviceName)"
        osVersion = "\(systemVersion)"
        
        addRenderButton()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
    
  
}

