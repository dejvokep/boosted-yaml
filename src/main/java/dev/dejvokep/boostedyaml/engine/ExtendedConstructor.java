/*
 * Copyright 2022 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.engine;

import dev.dejvokep.boostedyaml.serialization.YamlSerializer;
import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * A custom constructor for the SnakeYAML Engine allowing to deserialize map objects during document construction.
 */
public class ExtendedConstructor extends StandardConstructor {

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
    public ExtendedConstructor(@NotNull LoadSettings settings, @NotNull YamlSerializer serializer) {
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
     * Returns constructed {@link Object Java object} for the given node.
     *
     * @param node the node to get object for
     * @return the constructed object
     */
    @NotNull
    public Object getConstructed(@NotNull Node node) {
        return constructed.get(node);
    }

    /**
     * Clears all the (previously) constructed objects - therefore, freeing up the memory.
     * <p>
     * It is always recommended calling this method after this instance of the constructor is done constructing.
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
        private ConstructMap(@NotNull ConstructYamlMap previous) {
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
