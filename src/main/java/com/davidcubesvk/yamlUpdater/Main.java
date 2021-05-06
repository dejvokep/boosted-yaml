package com.davidcubesvk.yamlUpdater;

import com.davidcubesvk.yamlUpdater.block.Block;
import com.davidcubesvk.yamlUpdater.block.Section;
import com.davidcubesvk.yamlUpdater.reactor.Reactor;
import com.davidcubesvk.yamlUpdater.version.Pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().start();
    }

    private void start() throws Exception {
        File file = new java.io.File("src/main/resources");
        YamlUpdater updater = new YamlUpdater(getClass().getClassLoader(), file);
        System.out.println(
                updater.update(
                        updater.createSettings()
                                .setDiskFile("from.yml")
                                .setResourceFile("to.yml")
                                .setDiskFileVersion("1")
                                .setResourceFileVersion("3")
                                .setUpdateDiskFile(false)
                                .setRelocations("2", new HashMap<String, String>() {{
                                    put("b.f", "e.f");
                                }})
                                .setVersionPattern(new Pattern(new Pattern.Part[]{new Pattern.Part(1, 5)}))).getString());
    }

    private static void print(Map<String, Object> map, int depth) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Section) {
                System.out.println("SECTION " + entry.getKey() + " depth=" + depth + "--------------");
                print(((Section) entry.getValue()).getMappings(), depth + 1);
            } else {
                Block b = (Block) entry.getValue();
                System.out.println(b.getKey() + b.getValue().toString());
            }
        }
    }

}