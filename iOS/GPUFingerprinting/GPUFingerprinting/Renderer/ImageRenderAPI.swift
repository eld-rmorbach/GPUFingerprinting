//
//  ImageRenderAPI.swift
//  GPUFingerprinting
//
//  Copyright © 2019 Instituto de Pesquisas Eldorado. All rights reserved.
//
import Foundation
import UIKit

typealias RenderCompletionBlock = (_ base64: String?) -> Void

enum ImageType: String {
    case overlaidText, overlaidTextWithCurve, triangleAndSquareAntiAliasing, triangleAndSquareAliasing, triangleAndSquareAntiAliasingLight, triangleAndSquareAntiAliasingTexture, triangleAndSquareAntiAliasingOverlapRotate, triangleAndSquareAntiAliasingOverlapRotateLight, triangleAntialiasingMetal
}

protocol ImageRenderAPI {
    @discardableResult
    func renderImage(type: ImageType, in renderRect: CGRect) -> String?
    func renderImage(type: ImageType, in renderRect: CGRect, completion: @escaping RenderCompletionBlock)
}
extension ImageRenderAPI {
    func renderImage(type: ImageType, in renderRect: CGRect) -> String? {
        fatalError("Not implemented")
    }
    func renderImage(type: ImageType, in renderRect: CGRect, completion: @escaping RenderCompletionBlock) {
        completion(nil)
    }
}
