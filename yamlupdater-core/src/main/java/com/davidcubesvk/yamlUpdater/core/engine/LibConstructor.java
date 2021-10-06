package com.davidcubesvk.yamlUpdater.core.engine;

import com.davidcubesvk.yamlUpdater.core.serialization.YamlSerializer;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * A custom constructor for the SnakeYAML Engine allowing to deserialize map objects during document construction.
 */
public class LibConstructor extends StandardConstructor {

    //Serializer
    private final YamlSerializer serializer;
    //Constructed Java objects by nodes
    private final Map<Node, Object> constructed = new HashMap<>();

    /**
     * Creates an instance of the constructor.
     *
     * @param settings   the engine's load settings
     * @param serializer serializer
     */
    public LibConstructor(LoadSettings settings, YamlSerializer serializer) {
        //Call the superclass constructor
        super(settings);
        //Set
        this.serializer = serializer;
        //Add constructors
        tagConstructors.put(Tag.MAP, new ConstructMap((ConstructYamlMap) tagConstructors.get(Tag.MAP)));
    }

    @Override
    protected Object construct(Node node) {
        //Construct the object
        Object o = super.construct(node);
        //Add
        constructed.put(node, o);
        //Return
        return o;
    }

    @Override
    protected Object constructObjectNoCheck(Node node) {
        //Construct the object
        Object o = super.constructObjectNoCheck(node);
        //Add
        constructed.put(node, o);
        //Return
        return o;
    }

    /**
     * Returns constructed Java object for the given node.
     *
     * @param node the node to get object for
     * @return the constructed Java object
     */
    public Object getConstructed(Node node) {
        return constructed.get(node);
    }

    /**
     * Clears all the (previously) constructed objects - therefore, freeing up the memory. It is always recommended to
     * call this method after this instance of the constructor is done constructing.
     */
    public void clear() {
        constructed.clear();
    }

    /**
     * Custom object constructor for YAML maps, with functionality to deserialize them during construction.
     */
    private class ConstructMap extends ConstructYamlMap {

        //Previous constructor
        private final ConstructYamlMap previous;

        /**
         * Creates an instance of custom map constructor.
         *
         * @param previous the previous constructor, used to construct the map itself
         */
        private ConstructMap(ConstructYamlMap previous) {
            this.previous = previous;
        }

        @Override
        public Object construct(Node node) {
            //Construct the map (safe to suppress because StandardConstructor always returns Map of objects)
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) previous.construct(node);
            //Deserialize
            Object deserialized = serializer.deserialize(map);

            //Return
            return deserialized == null ? map : deserialized;
        }

    }
}
