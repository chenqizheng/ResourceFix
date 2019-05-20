package me.chen.resourcefix

import org.apache.commons.io.IOUtils
import org.objectweb.asm.*
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class ResourceFixProcessor {
    companion object {
        fun performClassFile(file: File) {
            var byteArray = IOUtils.toByteArray(file.inputStream())
            if (ActivityScanner.shouldProcessClass(byteArray)) {
                modifyClass(byteArray).rewrite(file)
            }
        }

        fun exlude(name: String): Boolean {
            if (ResourceFixPlugin.getResourceFixConfig() == null) {
                return false
            }
            ResourceFixPlugin.getResourceFixConfig().exludePattern.forEach {
                var isExclude = it.matcher(
                    name.replace("/", ".").replace(
                        "\\",
                        "."
                    )
                ).matches()
                if (isExclude) {
                    return true
                }
            }
            return false
        }

        fun performJarFile(file: File) {
            var jarFile = JarFile(file)
            var optJar = File(file.parent, file.name + ".opt")
            if (optJar.exists()) {
                optJar.delete()
            }
            var enumeration = jarFile.entries()
            var jarOutputStream = JarOutputStream(FileOutputStream(optJar))
            while (enumeration.hasMoreElements()) {
                var jarEntry = enumeration.nextElement() as JarEntry
                var entryName = jarEntry.name
                jarOutputStream.putNextEntry(ZipEntry(entryName))
                var inputStream = jarFile.getInputStream(jarEntry)
                var byteArray = IOUtils.toByteArray(inputStream);
                if (entryName.endsWith(".class") && !exlude(
                        entryName
                    ) && ActivityScanner.shouldProcessClass(byteArray)
                ) {
                    byteArray = modifyClass(byteArray)
                }
                jarOutputStream.write(byteArray)
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()
            if (file.exists()) {
                file.delete()
            }
            optJar.renameTo(file)
        }

        private fun modifyClass(byteArray: ByteArray): ByteArray {
            if (ResourceFixPlugin.getResourceFixConfig() == null
                || ResourceFixPlugin.getResourceFixConfig().insertClass == null
                || ResourceFixPlugin.getResourceFixConfig().insertStaticMethod == null
            ) {
                return byteArray
            }
            var result: ByteArray;
            var reader = ClassReader(byteArray)
            var cw = ClassWriter(reader, 0)
            var classVisitor = ModifyGetResourceVisitor(cw)
            reader.accept(classVisitor, Opcodes.ASM5)
            result = cw.toByteArray()
            if (!classVisitor.hasGetResource) {
                result = addGetResourceMethod(byteArray)
            }
            return result;
        }

        private fun addGetResourceMethod(byteArray: ByteArray): ByteArray {
            var reader = ClassReader(byteArray)
            var cw = ClassWriter(reader, 0)
            var classVisitor = AddGetResourceMethodVisitor(cw)
            reader.accept(classVisitor, Opcodes.ASM5)
            var mv =
                cw.visitMethod(Opcodes.ACC_PUBLIC, "getResources", "()Landroid/content/res/Resources;", null, null)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "android/support/v7/app/AppCompatActivity",
                "getResources",
                "()Landroid/content/res/Resources;",
                false
            );
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                ResourceFixPlugin.getResourceFixConfig().getInsertClass(),
                ResourceFixPlugin.getResourceFixConfig().insertStaticMethod,
                "(Landroid/content/res/Resources;)Landroid/content/res/Resources;",
                false
            );
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "android/support/v7/app/AppCompatActivity",
                "getResources",
                "()Landroid/content/res/Resources;",
                false
            )
            mv.visitInsn(Opcodes.ARETURN)
            mv.visitMaxs(1, 1)
            mv.visitEnd()
            return cw.toByteArray()!!
        }

    }

    class ModifyGetResourceVisitor(
        classVisitor: ClassWriter
    ) : ClassVisitor(Opcodes.ASM5, classVisitor) {
        var hasGetResource = false
        override fun visitMethod(
            access: Int,
            name: String?,
            desc: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            if (name == "getResources") {
                //先得到原始的方法
                val mv = cv.visitMethod(access, name, desc, signature, exceptions)
                var newMethod: MethodVisitor? =
                    ModifyGetResourceMethodVisit(mv)
                hasGetResource = true
                return newMethod!!
            }
            return super.visitMethod(access, name, desc, signature, exceptions)
        }
    }

    class ModifyGetResourceMethodVisit(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM5, mv) {
        override fun visitInsn(opcode: Int) {
            if (opcode == Opcodes.ARETURN) {
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "android/support/v7/app/AppCompatActivity",
                    "getResources",
                    "()Landroid/content/res/Resources;",
                    false
                );
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    ResourceFixPlugin.getResourceFixConfig().getInsertClass(),
                    ResourceFixPlugin.getResourceFixConfig().insertStaticMethod,
                    "(Landroid/content/res/Resources;)Landroid/content/res/Resources;",
                    false
                );
                mv.visitInsn(Opcodes.ARETURN);
            }
        }
    }


    class AddGetResourceMethodVisitor(classWriter: ClassWriter) : ClassVisitor(Opcodes.ASM5, classWriter)

}