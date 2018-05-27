package com.und.service

import org.springframework.stereotype.Service
import java.awt.image.BufferedImage

@Service
class ImageCreationService {

    fun create1pxImage(): BufferedImage {
        var img = BufferedImage(256, 256,
                BufferedImage.TYPE_INT_RGB)
        return img
    }

}