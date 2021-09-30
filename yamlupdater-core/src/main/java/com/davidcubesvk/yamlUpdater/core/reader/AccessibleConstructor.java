package com.davidcubesvk.yamlUpdater.core.reader;

import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.BaseConstructor;
import org.snakeyaml.engine.v2.nodes.Node;

import java.lang.reflect.Field;
import java.util.Map;

public class AccessibleConstructor extends BaseConstructor {

    private static Field CONSTRUCTED_MAP;
    private Map<Node, Object> constructed;

    public AccessibleConstructor(LoadSettings settings) {
        super(settings);

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

    static {
        try {
            CONSTRUCTED_MAP = BaseConstructor.class.getDeclaredField("constructedObjects");
            CONSTRUCTED_MAP.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }
}
