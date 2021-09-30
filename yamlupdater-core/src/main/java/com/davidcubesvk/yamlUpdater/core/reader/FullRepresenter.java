package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.Mapping;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.nodes.*;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FullRepresenter extends StandardRepresenter {

    public FullRepresenter(DumpSettings settings) {
        super(settings);
    }

    @Override
    public Node represent(Object data) {
        //If instance of section
        if (data instanceof Section)
            return representSection((Section) data);
        //Return
        return super.represent(data);
    }

    public Node representSection(Section section) {
        return representMapping(getTag(section.getValue().getClass(), Tag.MAP), section.getValue(),
                FlowStyle.AUTO);
    }

    public Node representKeyNode(Block<?> block, Object instance) {
        //Represent
        Node node = representData(instance);
        //If not null
        if (block != null) {
            //Set
            node.setBlockComments(block.getKeyBlockComments());
            node.setInLineComments(block.getKeyInlineComments());
            node.setEndComments(block.getKeyEndComments());
        }
        //Return
        return node;
    }

    public Node representValueNode(Block<?> block, Object instance) {
        //Represent
        Node node = representData(instance);
        //If not null
        if (block != null) {
            //Set
            node.setBlockComments(block.getValueBlockComments());
            node.setInLineComments(block.getValueInlineComments());
            node.setEndComments(block.getValueEndComments());
        }
        //Return
        return node;
    }


    @Override
    protected Node representMapping(Tag tag, Map<?, ?> mapping, FlowStyle flowStyle) {
        List<NodeTuple> value = new ArrayList<>(mapping.size());
        MappingNode node = new MappingNode(tag, value, flowStyle);
        representedObjects.put(objectToRepresent, node);
        FlowStyle bestStyle = FlowStyle.FLOW;
        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
            // ----- YamlLib start -----
            //Block
            Block<?> block = entry.getValue() instanceof Block<?> ? (Block<?>) entry.getValue() : null;
            Node nodeKey = representKeyNode(block, entry.getKey());
            Node nodeValue = representValueNode(block, entry.getValue());
            // ----- YamlLib end -----
            if (!(nodeKey instanceof ScalarNode && ((ScalarNode) nodeKey).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            value.add(new NodeTuple(nodeKey, nodeValue));
        }
        if (flowStyle == FlowStyle.AUTO) {
            if (defaultFlowStyle != FlowStyle.AUTO) {
                node.setFlowStyle(defaultFlowStyle);
            } else {
                node.setFlowStyle(bestStyle);
            }
        }
        return node;
    }
}