package it.bosler.numeracy.persistence

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

actual class FileStorage actual constructor() : Storage {
    private val dir: String by lazy {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask,
        )
        (paths.firstOrNull()?.path ?: "")
    }

    actual override fun read(fileName: String): String? {
        val path = "$dir/$fileName"
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
    }

    actual override fun write(fileName: String, content: String) {
        val path = "$dir/$fileName"
        (content as NSString).writeToFile(path, true, NSUTF8StringEncoding, null)
    }
}
