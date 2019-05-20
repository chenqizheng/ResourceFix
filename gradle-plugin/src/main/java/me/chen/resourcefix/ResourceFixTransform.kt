package me.chen.resourcefix;

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.gradle.api.Project


class ResourceFixTransform(private var project: Project) : Transform() {

    override fun getName(): String {
        return "FontScale"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean = true

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = TransformManager.SCOPE_FULL_PROJECT


    override fun transform(transformInvocation: TransformInvocation?) {
        project.logger.info("ResourceFixTransform transform")
        if (transformInvocation?.inputs?.isNotEmpty()!!) {
            transformInvocation?.inputs?.forEach {
                if (!it.jarInputs.isEmpty()) {
                    it.jarInputs.forEach {
                        var dest = transformInvocation.outputProvider.getContentLocation(
                            it.file.absolutePath,
                            it.contentTypes,
                            it.scopes,
                            Format.JAR
                        )
                        FileUtils.copyFile(it.file, dest)
                        ResourceFixProcessor.performJarFile(dest)
                    }
                }

                if (!it.directoryInputs.isEmpty()) {
                    it.directoryInputs.forEach {
                        project.logger.warn("-- directory: ${it.name} (${it.file.absolutePath})")
                        var dest = transformInvocation.outputProvider.getContentLocation(
                            it.name, it.contentTypes, it.scopes, Format.DIRECTORY
                        )
                        it.file?.walkTopDown()!!.forEach {
                            if (it.isFile && it.extension == "class") {
                                ResourceFixProcessor.performClassFile(it)
                            }
                        }

                        FileUtils.copyDirectory(it.file, dest)
                    }
                }

            }
        }
    }

}


