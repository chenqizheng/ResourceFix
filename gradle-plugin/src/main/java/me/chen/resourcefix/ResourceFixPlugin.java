package me.chen.resourcefix;

import com.android.build.gradle.AppExtension;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ResourceFixPlugin implements Plugin<Project> {
    private final static String CONFIG = "resourceFix";

    private static Project sProject;

    private static ResourceFixConfig resourceFixConfig;

    @Override
    public void apply(Project project) {
        sProject = project;
        project.getExtensions().create(CONFIG, ResourceFixConfig.class);

        ResourceFixTransform transform = new ResourceFixTransform(project);
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        android.registerTransform(transform);

        project.afterEvaluate(project1 ->  {
            resourceFixConfig = (ResourceFixConfig) project1.getExtensions().findByName(CONFIG);
            resourceFixConfig.initPattern();
        });

        resourceFixConfig = (ResourceFixConfig) project.getExtensions().findByName(CONFIG);
    }

    public static void log(String str) {
        sProject.getLogger().warn(str);
    }

    public static ResourceFixConfig getResourceFixConfig() {
        return resourceFixConfig;
    }
}