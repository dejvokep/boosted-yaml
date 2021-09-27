package com.davidcubesvk.yamlUpdater.core.files;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import org.snakeyaml.engine.v2.nodes.Node;

import java.util.Map;

public class YamlDocument extends Section {

    public YamlDocument(Node node, Map<?, ?> mappings) {
        super(node, mappings);
    }
}