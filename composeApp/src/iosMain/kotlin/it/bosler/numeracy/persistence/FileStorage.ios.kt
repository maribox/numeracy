package it.bosler.numeracy.persistence

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

/** Files in the app's Documents directory, written atomically by Foundation. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class FileStorage actual constructor() : Storage {
    private val dir: String by lazy {
        val urls = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        (urls.firstOrNull() as? NSURL)?.path ?: ""
    }

    actual override fun read(fileName: String): String? =
        NSString.stringWithContentsOfFile("$dir/$fileName", NSUTF8StringEncoding, null)

    actual override fun write(fileName: String, content: String) {
        NSString.create(string = content).writeToFile("$dir/$fileName", true, NSUTF8StringEncoding, null)
    }
}
