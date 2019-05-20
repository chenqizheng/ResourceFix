package me.chen.resourcefix

import java.io.File

fun ByteArray.rewrite(file: File) {
    if (file == null) {
        return
    }
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeBytes(this)
}