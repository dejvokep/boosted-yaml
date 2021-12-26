package com.davidcubesvk.yamlUpdater.core.engine;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.Comments;
import com.davidcubesvk.yamlUpdater.core.block.implementation.Section;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.RepresentToNode;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.nodes.*;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A custom representer for the SnakeYAML Engine allowing to represent {@link Section} objects, serializing custom
 * objects, all of which while keeping comments and without any additional time consumption.
 */
public class ExtendedRepresenter extends StandardRepresenter {

    //General settings
    private final GeneralSettings generalSettings;


    /**
     * Creates an instance of the representer.
     * @param generalSettings general settings of the root's file, whose contents are going to be represented
     * @param dumpSettings dumper settings
     */
    public ExtendedRepresenter(@NotNull GeneralSettings generalSettings, @NotNull DumpSettings dumpSettings) {
        //Call the superclass constructor
        super(dumpSettings);
        //Set
        this.generalSettings = generalSettings;

        //Add representers
        super.representers.put(Section.class, new RepresentSection());
        //Serializable
        RepresentToNode representSerializable = new RepresentSerializable();
        //Add all types
        for (Class<?> clazz : generalSettings.getSerializer().getSupportedClasses())
            super.representers.put(clazz, representSerializable);
        for (Class<?> clazz : generalSettings.getSerializer().getSupportedParentClasses())
            super.parentClassRepresenters.put(clazz, representSerializable);
    }

    /**
     * Node representer for serializable objects.
     */
    private class RepresentSerializable implements RepresentToNode {

        @Override
        public Node representData(Object o) {
            //Serialize
            Object serialized = generalSettings.getSerializer().serialize(o, generalSettings.getDefaultMapSupplier());
            //Return
            return ExtendedRepresenter.this.representData(serialized == null ? o : serialized);
        }

    }

    /**
     * Node representer for sections.
     */
    private class RepresentSection implements RepresentToNode {

        @Override
        public Node representData(Object o) {
            //Cast
            Section section = (Section) o;
            //Return
            return applyKeyComments(section, ExtendedRepresenter.this.representData(section.getStoredValue()));
        }

    }

    /**
     * Applies the given block's (if not <code>null</code>) key comments to the given node and returns the given node
     * itself.
     * @param block the block whose key comments to apply
     * @param node node to apply the comments to
     * @return the given node
     */
    @NotNull
    public Node applyKeyComments(@Nullable Block<?> block, @NotNull Node node) {
        //If not null
        if (block != null) {
            //Set
            node.setBlockComments(Comments.get(block, Comments.NodeType.KEY, Comments.Position.BEFORE));
            node.setInLineComments(Comments.get(block, Comments.NodeType.KEY, Comments.Position.INLINE));
            node.setEndComments(Comments.get(block, Comments.NodeType.KEY, Comments.Position.AFTER));
        }
        //Return
        return node;
    }

    /**
     * Applies the given block's (if not <code>null</code>) value comments to the given node and returns the given node
     * itself.
     * @param block the block whose value comments to apply
     * @param node node to apply the comments to
     * @return the given node
     */
    @NotNull
    public Node applyValueComments(@Nullable Block<?> block, @NotNull Node node) {
        //If not null
        if (block != null) {
            //Set
            node.setBlockComments(Comments.get(block, Comments.NodeType.VALUE, Comments.Position.BEFORE));
            node.setInLineComments(Comments.get(block, Comments.NodeType.VALUE, Comments.Position.INLINE));
            node.setEndComments(Comments.get(block, Comments.NodeType.VALUE, Comments.Position.AFTER));
        }
        //Return
        return node;
    }

    @Override
    protected Node representMapping(Tag tag, Map<?, ?> mapping, FlowStyle flowStyle) {
        //Best flow style for this object
        FlowStyle bestStyle = FlowStyle.FLOW;

        //List of mappings
        List<NodeTuple> mappings = new ArrayList<>(mapping.size());
        //Create a node
        MappingNode node = new MappingNode(tag, mappings, flowStyle);
        //Add
        representedObjects.put(objectToRepresent, node);

        //All mappings
        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
            //Block
            Block<?> block = entry.getValue() instanceof Block ? (Block<?>) entry.getValue() : null;
            //Represent nodes
            Node key = applyKeyComments(block, representData(entry.getKey()));
            Node value = applyValueComments(block, representData(block == null ? entry.getValue() : block.getStoredValue()));
            //If a scalar and plain, set to block
            if (!(key instanceof ScalarNode && ((ScalarNode) key).isPlain()) || !(value instanceof ScalarNode && ((ScalarNode) value).isPlain()))
                bestStyle = FlowStyle.BLOCK;

            //Add the value
            mappings.add(new NodeTuple(key, value));
        }

        //If target flow style is automatic
        if (flowStyle == FlowStyle.AUTO)
            //Set to default if not auto, or picked
            node.setFlowStyle(defaultFlowStyle != FlowStyle.AUTO ? defaultFlowStyle : bestStyle);

        //Return
        return node;
    }
}