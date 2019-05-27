//
//  GLKitRenderer.swift
//  ImageGenerator
//
//  Copyright © 2019 Instituto de Pesquisas Eldorado. All rights reserved.
//

import GLKit
import CryptoSwift

class GLKitRenderer: NSObject, ImageRenderAPI {
    
    var context = EAGLContext(api: EAGLRenderingAPI.openGLES3)
    
    private var effect = GLKBaseEffect()
    
    var completionResult: RenderCompletionBlock?
    
    var renderView: GLKView?
    
    private var imageType: ImageType?
    
    let Cube = [
        //front
        Vertex(x: -1.0, y: -1.0, z: 1.0, r: 0.5, g: 0.5, b: 1, a: 1, tx: 0.0, ty: 0.0),
        Vertex(x: 1.0, y: -1.0, z: 1.0, r: 0.5, g: 0.5, b: 1, a: 1, tx: 1.0, ty: 0.0),
        Vertex(x: 1.0, y: 1.0, z: 1.0, r: 0.5, g: 0.5, b: 1, a: 1, tx: 1.0, ty: 1.0),
        Vertex(x: -1.0, y: 1.0, z: 1.0, r: 0.5, g: 0.5, b: 1, a: 1, tx: 0.0, ty: 1.0),
        //back
        Vertex(x: -1.0, y: -1.0, z: -1.0, r: 0.5, g: 0.5, b: 1, a: 1, tx: 0.0, ty: 0.0),
        Vertex(x: 1.0, y: -1.0, z: -1.0, r: 0.5, g: 0.5, b: 1, a: 1, tx: 1.0, ty: 1.0),
        Vertex(x: 1.0, y: 1.0, z: -1.0, r: 0.5, g: 0.5, b: 1, a: 1, tx: 1.0, ty: 1.0),
        Vertex(x: -1.0, y: 1.0, z: -1.0, r: 0.5, g: 0.5, b: 1, a: 1, tx: 0.0, ty: 1.0)
    ];
    
    let NormalCube: [Float] = [
        0.0, -1.0, 0.0,
        0.0, 1.0, 0.0,
        1.0, 0.0, 0.0,
        0.0, 0.0, 1.0,
        -1.0, 0.0, 0.0,
        0.0, 0.0, -1.0
    ];
    
    
    //EBO
    let CubeIndices: [GLubyte] = [
        //front
        0, 1, 2,
        2, 3, 0,
        //right
        1, 5, 6,
        6, 2, 1,
        //back
        // back
        7, 6, 5,
        5, 4, 7,
        // left
        4, 0, 3,
        3, 7, 4,
        // bottom
        4, 5, 1,
        1, 0, 4,
        // top
        3, 2, 6,
        6, 7, 3
    ]
    
    let Triangle = [
        Vertex(x: 0, y: 1.0, z: 0, r: 1.0, g: 0, b: 0, a: 1.0, tx: 0.0, ty: 0.0),
        Vertex(x: -1.0, y: -1.0, z: 0, r: 0.0, g: 1.0, b: 0.0, a: 1.0, tx: 0.0, ty: 0.0),
        Vertex(x: 1.0, y: -1.0, z: 0, r: 0.0, g: 0.0, b: 1.0, a: 1.0, tx: 0.0, ty: 0.0),
        ]
    
    //EBO
    let TriangleIndices: [GLubyte] = [
        0, 1, 2
    ]
    
    let NormalTriangle: [Float] = [
        0, -1.0, 0.0,
        1.0, 1.0, 0.0,
        1.0, 1.0, 0.0,
        ];
    
    
    // Variáveis para o objeto element buffer object, vértice buffer object e vértice array buffer object.
    private var cubeIndicesBuffer = GLuint()
    private var cubePositionBuffer = GLuint()
    
    private var normalCubePositionBuffer = GLuint()
    private var normalTrianglePositionBuffer = GLuint()
    private var triangleIndicesBuffer = GLuint()
    private var trianglePositionBuffer = GLuint()
    private var vao = GLuint()
    
    private var sVao = GLuint()
    
    
    
    
    deinit {
        debugPrint("\(#function) of \(self.description) called")
        tearDown()
    }
    
    private func tearDown() {
        EAGLContext.setCurrent(context)
        glDeleteBuffers(1, &vao)
        glDeleteBuffers(1, &trianglePositionBuffer)
        glDeleteBuffers(1, &triangleIndicesBuffer)
        glDeleteBuffers(1, &cubeIndicesBuffer)
        glDeleteBuffers(1, &cubePositionBuffer)
        glDeleteBuffers(1, &sVao)
        if self.imageType == ImageType.triangleAndSquareAntiAliasingOverlapRotateLight {
            glDeleteBuffers(1, &normalCubePositionBuffer)
            glDeleteBuffers(1, &normalTrianglePositionBuffer)
        }
        
        
        EAGLContext.setCurrent(nil)
        context = nil
    }
    
    private func prepareTriangle() {
        // Quando gera-se buffers, você precisa especificar informação sobre como ler cores e posições da sua estrutura de dados. OpenGL espera um GLuint para o atributo vértice de cor.
        let vertexAttribColor = GLuint(GLKVertexAttrib.color.rawValue)
        // Atributo vértice de posição
        let vertexAttribPosition = GLuint(GLKVertexAttrib.position.rawValue)
        // Tamanho, em bytes, de um elemento Vertex quando este está em um array
        let vertexSize = MemoryLayout<Vertex>.stride
        // A múltiplicação corresponde aos elementos x, y e z de Vertex
        let colorOffset = MemoryLayout<GLfloat>.stride * 3
        // Faz a conversão para UnsafeRawPointer
        let colorOffsetPointer = UnsafeRawPointer(bitPattern: colorOffset)
        
        // Criar VAO
        
        // Gera, ou cria, um novo VAO. O primeiro parâmetro é a quantidade de VAOs para se gerar, e o segundo parâmetro é um ponteiro para um `GLuint` onde ele irá armazenar o ID do objeto gerado
        glGenVertexArraysOES(1, &sVao)
        // Informa ao OpenGL que, com esse bind, todas as chamadas para configurar ponteiros para atributos vertex devem ser armazenadas no VAO fornecido. OpenGL irá utilizar o VAO fornecido até que você faça `unbind` do objeto
        glBindVertexArrayOES(sVao)
        
        // Criar trianglePositionBuffer
        glGenBuffers(1, &trianglePositionBuffer)
        glBindBuffer(GLenum(GL_ARRAY_BUFFER), trianglePositionBuffer)
        glBufferData(GLenum(GL_ARRAY_BUFFER), // Indica para qual buffer você está passando os dados
            Triangle.size(),// especifica o tamanho, em bytes, de dados.
            Triangle, // Os dados a serem utilizados
            GLenum(GL_STATIC_DRAW)) // Como a GPU deve gerenciar os dados. Como os dados passados para a unidade de processamento gráfico raramente irá mudar, essa flag otimiza o OpenGL.
        
        
        
        // Prepara o OpenGL para interpretação dos dados
        
        glEnableVertexAttribArray(vertexAttribPosition) // Diz ao OpenGL que este array corresponde à posição da sua geometria
        glVertexAttribPointer(vertexAttribPosition, //
            3, // Quantos valores são representados para cada vértice. Na estrutura Vertex, há 3 atributos para posição (x,y,z)
            GLenum(GL_FLOAT), // O tipo de cada valor (ou atributo)
            GLboolean(UInt8(GL_FALSE)), // se os dados devem ser normalizados ou não (posição W. Se true = 1.0, se false = 0.0)
            GLsizei(vertexSize), // O tamanho do passo (stride)
            nil) // Offset da posição dos dados.
        
        if (self.imageType == ImageType.triangleAndSquareAntiAliasingOverlapRotateLight) {
            // Normal
            glGenBuffers(1, &normalTrianglePositionBuffer)
            glBindBuffer(GLenum(GL_ELEMENT_ARRAY_BUFFER), normalTrianglePositionBuffer)
            glBufferData(GLenum(GL_ELEMENT_ARRAY_BUFFER),
                         NormalTriangle.size(),
                         NormalTriangle,
                         GLenum(GL_STATIC_DRAW));
            
            let vertexAttribNormal = GLuint(GLKVertexAttrib.normal.rawValue)
            let normalSize = MemoryLayout<Float>.stride * 3
            let normalOffset = MemoryLayout<Float>.stride
            
            let normalOffsetPointer = UnsafeRawPointer(bitPattern: normalOffset)
            glVertexAttribPointer(vertexAttribNormal,
                                  3,
                                  GLenum(GL_FLOAT),
                                  GLboolean(UInt8(GL_FALSE)),
                                  GLsizei(normalSize),
                                  normalOffsetPointer)
            
            glEnableVertexAttribArray(vertexAttribNormal)
            // Normal End
        }
        
        glEnableVertexAttribArray(vertexAttribColor)
        glVertexAttribPointer(vertexAttribColor,
                              4,
                              GLenum(GL_FLOAT),
                              GLboolean(UInt8(GL_FALSE)),
                              GLsizei(vertexSize),
                              colorOffsetPointer)
        
        // Criar triangleIndicesBuffer
        glGenBuffers(1, &triangleIndicesBuffer)
        glBindBuffer(GLenum(GL_ELEMENT_ARRAY_BUFFER), triangleIndicesBuffer)
        glBufferData(GLenum(GL_ELEMENT_ARRAY_BUFFER),
                     TriangleIndices.size(),
                     TriangleIndices,
                     GLenum(GL_STATIC_DRAW))
        
        //
        //         Unbind do VAO
        glBindVertexArrayOES(0)
        glBindBuffer(GLenum(GL_ARRAY_BUFFER), 0)
        glBindBuffer(GLenum(GL_ELEMENT_ARRAY_BUFFER), 0)
    }
    
    private func prepareCube() {
        // Quando gera-se buffers, você precisa especificar informação sobre como ler cores e posições da sua estrutura de dados. OpenGL espera um GLuint para o atributo vértice de cor.
        let vertexAttribColor = GLuint(GLKVertexAttrib.color.rawValue)
        // Atributo vértice de posição
        let vertexAttribPosition = GLuint(GLKVertexAttrib.position.rawValue)
        // Tamanho, em bytes, de um elemento Vertex quando este está em um array
        let vertexSize = MemoryLayout<Vertex>.stride
        // A múltiplicação corresponde aos elementos x, y e z de Vertex
        let colorOffset = MemoryLayout<GLfloat>.stride * 3
        // Faz a conversão para UnsafeRawPointer
        let colorOffsetPointer = UnsafeRawPointer(bitPattern: colorOffset)
        
        // Criar VAO
        
        // Gera, ou cria, um novo VAO. O primeiro parâmetro é a quantidade de VAOs para se gerar, e o segundo parâmetro é um ponteiro para um `GLuint` onde ele irá armazenar o ID do objeto gerado
        glGenVertexArraysOES(1, &vao)
        // Informa ao OpenGL que, com esse bind, todas as chamadas para configurar ponteiros para atributos vertex devem ser armazenadas no VAO fornecido. OpenGL irá utilizar o VAO fornecido até que você faça `unbind` do objeto
        glBindVertexArrayOES(vao)
        
        // Criar cubePositionBuffer
        
        glGenBuffers(1, &cubePositionBuffer)
        glBindBuffer(GLenum(GL_ARRAY_BUFFER), cubePositionBuffer)
        glBufferData(GLenum(GL_ARRAY_BUFFER), // Indica para qual buffer você está passando os dados
            Cube.size(),// especifica o tamanho, em bytes, de dados.
            Cube, // Os dados a serem utilizados
            GLenum(GL_STATIC_DRAW)) // Como a GPU deve gerenciar os dados. Como os dados passados para a unidade de processamento gráfico raramente irá mudar, essa flag otimiza o OpenGL.
        
        // Prepara o OpenGL para interpretação dos dados
        glEnableVertexAttribArray(vertexAttribPosition) // Diz ao OpenGL que este array corresponde à posição da sua geometria
        glVertexAttribPointer(vertexAttribPosition, //
            3, // Quantos valores são representados para cada vértice. Na estrutura Vertex, há 3 atributos para posição (x,y,z)
            GLenum(GL_FLOAT), // O tipo de cada valor (ou atributo)
            GLboolean(UInt8(GL_FALSE)), // se os dados devem ser normalizados ou não. (posição W. Se true = 1.0, se false = 0.0)
            GLsizei(vertexSize), // O tamanho do passo (stride)
            nil) // Offset da posição dos dados.
        
        if (self.imageType == ImageType.triangleAndSquareAntiAliasingOverlapRotateLight) {
            
            // Normal
            glGenBuffers(1, &normalCubePositionBuffer)
            glBindBuffer(GLenum(GL_ELEMENT_ARRAY_BUFFER), normalCubePositionBuffer)
            glBufferData(GLenum(GL_ELEMENT_ARRAY_BUFFER),
                         NormalCube.size(),
                         NormalCube,
                         GLenum(GL_STATIC_DRAW));
            
            let vertexAttribNormal = GLuint(GLKVertexAttrib.normal.rawValue)
            let normalSize = MemoryLayout<Float>.stride * 3
            let normalOffset = MemoryLayout<Float>.stride
            
            let normalOffsetPointer = UnsafeRawPointer(bitPattern: normalOffset)
            glVertexAttribPointer(vertexAttribNormal,
                                  3,
                                  GLenum(GL_FLOAT),
                                  GLboolean(UInt8(GL_FALSE)),
                                  GLsizei(normalSize),
                                  normalOffsetPointer)
            glEnableVertexAttribArray(vertexAttribNormal)
            // Normal End
            
        }
        glEnableVertexAttribArray(vertexAttribColor)
        glVertexAttribPointer(vertexAttribColor,
                              4,
                              GLenum(GL_FLOAT),
                              GLboolean(UInt8(GL_FALSE)),
                              GLsizei(vertexSize),
                              colorOffsetPointer)
        
        
        // Habilita textura
        if (self.imageType == ImageType.triangleAndSquareAntiAliasingTexture) {
            let vertexAttribTexture = GLuint(GLKVertexAttrib.texCoord0.rawValue)
            let textureSize = MemoryLayout<Vertex>.stride
            let textureOffset = MemoryLayout<GLfloat>.stride * 7
            
            let textureOffsetPointer = UnsafeRawPointer(bitPattern: textureOffset)
            glEnableVertexAttribArray(vertexAttribTexture)
            glVertexAttribPointer(vertexAttribTexture,
                                  2,
                                  GLenum(GL_FLOAT),
                                  GLboolean(UInt8(GL_FALSE)),
                                  GLsizei(textureSize),
                                  textureOffsetPointer)
        }
        
        // Criar cubeIndicesBuffer
        glGenBuffers(1, &cubeIndicesBuffer)
        glBindBuffer(GLenum(GL_ELEMENT_ARRAY_BUFFER), cubeIndicesBuffer)
        glBufferData(GLenum(GL_ELEMENT_ARRAY_BUFFER),
                     CubeIndices.size(),
                     CubeIndices,
                     GLenum(GL_STATIC_DRAW))
        
        //
        //         Unbind do VAO
        glBindVertexArrayOES(0)
        glBindBuffer(GLenum(GL_ARRAY_BUFFER), 0)
        glBindBuffer(GLenum(GL_ELEMENT_ARRAY_BUFFER), 0)
        
        
    }
    
    func renderImage(type: ImageType, in renderRect: CGRect, completion: @escaping RenderCompletionBlock) {        
        self.completionResult = completion
        self.imageType = type
        guard let context = context else {
            debugPrint("Not supported openGL version")
            self.completionResult?(nil)
            return
        }
        
        EAGLContext.setCurrent(context)
        
        renderView = GLKView(frame: renderRect, context: context)        
        renderView?.drawableColorFormat = .RGBA8888
        renderView?.drawableDepthFormat = .format24
        renderView?.drawableStencilFormat = .format8
        renderView?.drawableMultisample = (type == .triangleAndSquareAliasing) ? .multisampleNone : .multisample4X // É uma forma de antialising que suavisa jagged edges, melhorando a qualidade da imagem em apps 3D, porém, utilizando mais memória e tempo de processamento.
        renderView?.delegate = self
        
        
        prepareTriangle()
        prepareCube()
        switch type {
        case .triangleAndSquareAntiAliasingLight:
            changeAmbientLight()
        case .triangleAndSquareAntiAliasingOverlapRotateLight:
            changeDiffuseLight()
            print("changeDiffuseLight")
        default:
            print("Default")
        }
        getBase64()
    }
    
    func renderImage(type: ImageType, in renderRect: CGRect) -> String? {
        return nil
    }
    
    private func getBase64() {
        
        guard let rv = renderView else {
            self.completionResult?(nil)
            return
        }
        rv.bindDrawable()
        rv.contentScaleFactor = 1.0
        let snapshot = rv.snapshot
        
        guard let imageData = snapshot.pngData() else {
            self.completionResult?(nil)
            return
        }
        
        self.completionResult?(imageData.base64EncodedString())
    }
    
    func changeDiffuseLight() {
        let ambientIntensity: Float = 1.0
        effect.lightingType = .perPixel
        effect.colorMaterialEnabled = GLboolean(GL_TRUE)
        effect.light0.diffuseColor = GLKVector4Make(1 * ambientIntensity, 1 * ambientIntensity, 1 * ambientIntensity, 1.0)        
        effect.light0.enabled = GLboolean(GL_TRUE)
        effect.light0.position = GLKVector4Make(2.0, -0.5, -5, 1.0)
        effect.material.shininess = 100.0
    }
    
    func changeAmbientLight() {
        effect.colorMaterialEnabled = GLboolean(GL_TRUE)
        effect.lightModelTwoSided = GLboolean(GL_FALSE)
        effect.light0.enabled = GLboolean(GL_TRUE)
        effect.light0.ambientColor = GLKVector4Make(1.0, 0.0, 0.0, 1.0)
        //effect.light0.diffuseColor = GLKVector4Make(0.1, 0.1, 0.1, 1.0)
        effect.light0.position = GLKVector4Make(4, -2.0, -6, 1.0)
        //effect.material.specularColor = GLKVector4Make(0.0, 0.0, 0.0, 1.0)
        //effect.material.shininess = 4.0
        effect.lightingType = .perPixel
    }
    
    func changeTexture() {
        
        guard let woodImage = UIImage(named: "wood2") else { return }
        
        guard let textureInfo = try? GLKTextureLoader.texture(with: woodImage.cgImage!, options: nil) else { return }
        
        effect.texture2d0.enabled = GLboolean(GL_TRUE)
        effect.texture2d0.target = .target2D
        effect.texture2d0.name = textureInfo.name
        effect.texture2d0.envMode = .decal
        
    }
    
}

extension GLKitRenderer: GLKViewDelegate {
    
    func glkView(_ view: GLKView, drawIn rect: CGRect) {
        // Define os valores RGB e alfa a serem utilizados quando a tela for limpa. Nesse caso, lightgray
        glClearColor(0.0, 0.0, 0.0, 1.0)
        
        // Realiza a limpeza, de fato. GL_COLOR_BUFFER_BIT define que o buffer de cor/renderização atual deve ser limpo. Outras opções são: GL_DEPTH_BUFFER_BIT e GL_STENCIL_BUFFER_BIT
        glClear(GLbitfield(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT))
        glEnable(GLenum(GL_DEPTH_TEST))
        
        glBindVertexArrayOES(sVao)
        
        let pMatrix = GLKMatrix4.init(m: (2.41421365737915039063, 0, 0, 0, 0,
                                          2.41421365737915039063, 0, 0, 0, 0,
                                          -1.00200200080871582031, -1, 0, 0,
                                          -0.200200200080871582031, 0))
        
        var mvMatrix = GLKMatrix4.init(m: (1, 0, 0, 0,
                                           0, 1, 0, 0,
                                           0, 0, 1, 0,
                                           -1.5, 0, -7, 1))
        
        effect.transform.projectionMatrix = pMatrix
        
        effect.transform.modelviewMatrix = mvMatrix
        
        effect.prepareToDraw()
        
        glDrawElements(GLenum(GL_TRIANGLES), // diz ao OpenGL o que desenhar.
            GLsizei(Triangle.count), // Quantos vértices desenhar.
            GLenum(GL_UNSIGNED_BYTE), // Especifica os valores contidos em cada índice. `SquareIndices` é um array de bytes GLbyte
            nil)
        
        glBindVertexArrayOES(0)
        
        glBindVertexArrayOES(vao)
        
        if (self.imageType == ImageType.triangleAndSquareAntiAliasingOverlapRotate) {
            mvMatrix = GLKMatrix4.init(m: (1, 0, 0, 0,
                                           0, 1, 0, 0,
                                           0, 0, 1, 0,
                                           0, 0, -9, 1 ))
            mvMatrix = GLKMatrix4Rotate(mvMatrix, GLKMathDegreesToRadians(60), 0, 0, 1)
            mvMatrix = GLKMatrix4Rotate(mvMatrix, GLKMathDegreesToRadians(10), 0, 1, 0)
        } else if (self.imageType == ImageType.triangleAndSquareAntiAliasingOverlapRotateLight) {
            mvMatrix = GLKMatrix4.init(m: (1, 0, 0, 0,
                                           0, 1, 0, 0,
                                           0, 0, 1, 0,
                                           -0.5, 0, -9, 1 ))
            mvMatrix = GLKMatrix4Rotate(mvMatrix, GLKMathDegreesToRadians(30), 0, 0, 1)
            mvMatrix = GLKMatrix4Rotate(mvMatrix, GLKMathDegreesToRadians(-30), 0, 1, 0)
        } else {
            mvMatrix = GLKMatrix4.init(m: (1, 0, 0, 0,
                                           0, 1, 0, 0,
                                           0, 0, 1, 0,
                                           1.0, 0, -7, 1))
            mvMatrix = GLKMatrix4Rotate(mvMatrix, GLKMathDegreesToRadians(10), 0, 1, 0)
        }
        
        effect.transform.modelviewMatrix = mvMatrix
        
        if self.imageType == ImageType.triangleAndSquareAntiAliasingTexture {
            changeTexture()
        }
        
        effect.prepareToDraw()
        glDrawElements(GLenum(GL_TRIANGLES), // diz ao OpenGL o que desenhar.
            GLsizei(CubeIndices.count), // Quantos vértices desenhar.
            GLenum(GL_UNSIGNED_BYTE), // Especifica os valores contidos em cada índice. `SquareIndices` é um array de bytes GLbyte
            nil) // Offset
        
        glBindVertexArrayOES(0)
    }
}
