package com.gallr.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSItemProvider
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): () -> Unit {
    val currentCallback = rememberUpdatedState(onImagePicked)

    return {
        val callback = currentCallback.value
        val config = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = 1
        }
        val picker = PHPickerViewController(configuration = config)
        val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, completion = null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult
                if (result == null) {
                    callback(null)
                    return
                }
                val provider: NSItemProvider = result.itemProvider
                if (provider.hasItemConformingToTypeIdentifier(UTTypeImage.identifier)) {
                    provider.loadDataRepresentationForTypeIdentifier(UTTypeImage.identifier) { data, _ ->
                        if (data != null) {
                            val image = UIImage(data = data)
                            val jpegData = UIImageJPEGRepresentation(image, 0.8)
                            val bytes = jpegData?.toByteArray()
                            callback(bytes)
                        } else {
                            callback(null)
                        }
                    }
                } else {
                    callback(null)
                }
            }
        }
        picker.delegate = delegate

        // Find root view controller using Obj-C compatible iteration
        var rootVC: platform.UIKit.UIViewController? = null
        for (scene in UIApplication.sharedApplication.connectedScenes) {
            val windowScene = scene as? UIWindowScene ?: continue
            for (window in windowScene.windows) {
                val win = window as? UIWindow ?: continue
                if (win.isKeyWindow()) {
                    rootVC = win.rootViewController
                    break
                }
            }
            if (rootVC != null) break
        }

        if (rootVC != null) {
            // Present from the topmost presented controller
            var topVC = rootVC
            while (topVC?.presentedViewController != null) {
                topVC = topVC.presentedViewController
            }
            topVC?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    if (size == 0) return ByteArray(0)
    val bytes = ByteArray(size)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}
