package com.davidcubesvk.yamlUpdater.core.engine;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.Mapping;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.utils.serialization.Serializable;
import com.davidcubesvk.yamlUpdater.core.utils.serialization.YamlSerializer;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.RepresentToNode;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.nodes.*;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FullRepresenter extends StandardRepresenter {

    private YamlSerializer serializer;

    public FullRepresenter(DumpSettings settings, YamlSerializer serializer) {
        //Call the superclass constructor
        super(settings);
        //Set
        this.serializer = serializer;
        //Add representers
        //super.parentClassRepresenters.put(Section.class, new RepresentSection());
        super.parentClassRepresenters.put(Block.class, new RepresentBlock());
        super.parentClassRepresenters.put(Serializable.class, new RepresentSerializable());
    }

    private class RepresentSerializable implements RepresentToNode {

        @Override
        public Node representData(Object o) {
            return FullRepresenter.this.representData(serializer.serialize(o));
        }

    }

    private class RepresentBlock implements RepresentToNode {

        @Override
        public Node representData(Object o) {
            //If a section
            if (o instanceof Section) {
                //Cast
                Section section = (Section) o;
                //Return
                return applyKeyComments(section, FullRepresenter.this.representData(section.getValue()));
            }

            //Mapping
            Mapping mapping = (Mapping) o;
            return null;
        }

    }

    public Node applyKeyComments(Block<?> block, Node node) {
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

    public Node applyValueComments(Block<?> block, Node node) {
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
            System.out.println("Representing entry: " + entry.getKey() + " " + entry.getValue());
            // ----- YamlLib start -----
            //Block
            Block<?> block = entry.getValue() instanceof Block ? (Block<?>) entry.getValue() : null;
            System.out.println("block = " + block + ", representing = " + (block == null ? entry.getValue() : block.getValue()));
            System.out.println((block == null ? "no block provided" : block.getValue()));
            //Represent nodes
            Node nodeKey = applyKeyComments(block, representData(entry.getKey()));
            Node nodeValue = applyValueComments(block, representData(block == null ? entry.getValue() : block.getValue()));
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