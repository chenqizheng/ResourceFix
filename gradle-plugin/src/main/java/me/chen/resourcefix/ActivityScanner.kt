package me.chen.resourcefix

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class ActivityScanner {

    companion object {

        fun shouldProcessClass(byteArray: ByteArray): Boolean {
            var reader = ClassReader(byteArray)
            var activityClassVisitor = ActivityClassVisitor()
            reader.accept(activityClassVisitor, 0)
            return activityClassVisitor.isProcess
        }
    }
}

class ActivityClassVisitor :
    ClassVisitor(Opcodes.ASM5) {

    var isProcess: Boolean = false
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        if (!ResourceFixProcessor.exlude(name!!) && superName != null && superName == "android/support/v7/app/AppCompatActivity") {
            isProcess = true
        }
    }
}