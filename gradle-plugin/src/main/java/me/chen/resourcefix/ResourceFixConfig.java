package me.chen.resourcefix;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ResourceFixConfig {
    public ArrayList<String> excludePackage = new ArrayList<>();

    public String insertClass;

    public String insertStaticMethod;

    public String getInsertClass() {
        return insertClass.replace(".", "/");
    }

    public ArrayList<Pattern> exludePattern = new ArrayList<>();

    public void initPattern() {
        exludePattern.clear();
        for (String str : excludePackage
        ) {
            exludePattern.add(Pattern.compile(str));
        }
    }
}
