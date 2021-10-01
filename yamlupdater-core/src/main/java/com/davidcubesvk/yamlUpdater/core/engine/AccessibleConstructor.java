package com.davidcubesvk.yamlUpdater.core.engine;

import com.davidcubesvk.yamlUpdater.core.utils.serialization.YamlSerializer;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.BaseConstructor;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.lang.reflect.Field;
import java.util.Map;

public class AccessibleConstructor extends StandardConstructor {

    private static Field CONSTRUCTED_MAP;
    private YamlSerializer serializer;
    private Map<Node, Object> constructed;

    public AccessibleConstructor(LoadSettings settings, YamlSerializer serializer) {
        //Call the superclass constructor
        super(settings);
        //Set
        this.serializer = serializer;
        //Add constructors
        tagConstructors.put(Tag.MAP, new ConstructMap((ConstructYamlMap) tagConstructors.get(Tag.MAP)));

        //Try to get
        try {
            constructed = (Map<Node, Object>) CONSTRUCTED_MAP.get(this);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public Map<Node, Object> getConstructed() {
        return constructed;
    }

    public Object getConstructed(Node node) {
        return constructed.get(node);
    }

    private class ConstructMap extends ConstructYamlMap {

        private ConstructYamlMap previous;

        private ConstructMap(ConstructYamlMap previous) {
            this.previous = previous;
        }

        @Override
        public Object construct(Node node) {
            //Construct the map
            Map<Object, Object> map = (Map<Object, Object>) previous.construct(node);
            //Deserialize
            Object deserialized = serializer.deserialize(map);

            //Return
            return deserialized == null ? map : deserialized;
        }

    }

    static {
        try {
            CONSTRUCTED_MAP = BaseConstructor.class.getDeclaredField("constructedObjects");
            CONSTRUCTED_MAP.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }
}
