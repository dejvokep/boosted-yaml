package com.davidcubesvk.yamlUpdater.core.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Comments {

    public enum Position {
        BEFORE, INLINE, AFTER
    }

    public enum NodeType {
        KEY, VALUE
    }

    public static final CommentLine BLANK_LINE = new CommentLine(Optional.empty(), Optional.empty(), null, CommentType.BLANK_LINE);

    @Nullable
    public static List<CommentLine> getComments(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position) {
        switch (position) {
            case BEFORE:
                return node == NodeType.KEY ? block.keyBeforeComments : block.valueBeforeComments;
            case INLINE:
                return node == NodeType.KEY ? block.keyInlineComments : block.valueInlineComments;
            case AFTER:
                return node == NodeType.KEY ? block.keyAfterComments : block.valueAfterComments;
            default:
                return null;
        }
    }

    public static void setComments(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position, @Nullable List<CommentLine> comments) {
        switch (position) {
            case BEFORE:
                if (node == NodeType.KEY)
                    block.keyBeforeComments = comments;
                else
                    block.valueBeforeComments = comments;
                break;
            case INLINE:
                if (node == NodeType.KEY)
                    block.keyInlineComments = comments;
                else
                    block.valueInlineComments = comments;
                break;
            case AFTER:
                if (node == NodeType.KEY)
                    block.keyAfterComments = comments;
                else
                    block.valueAfterComments = comments;
                break;
        }
    }

    public static void addComment(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position, @NotNull CommentLine comment) {
        switch (position) {
            case BEFORE:
                if (node == NodeType.KEY)
                    block.keyBeforeComments.add(comment);
                else
                    block.valueBeforeComments.add(comment);
                break;
            case INLINE:
                if (node == NodeType.KEY)
                    block.keyInlineComments.add(comment);
                else
                    block.valueInlineComments.add(comment);
                break;
            case AFTER:
                if (node == NodeType.KEY) {
                    //Might be null
                    if (block.keyAfterComments == null)
                        block.keyAfterComments = new ArrayList<>();
                    //Add
                    block.keyAfterComments.add(comment);
                } else {
                    //Might be null
                    if (block.valueAfterComments == null)
                        block.valueAfterComments = new ArrayList<>();
                    //Add
                    block.valueAfterComments.add(comment);
                }
                break;
        }
    }

    public static CommentLine create(@NotNull String comment, @NotNull Position position) {
        return new CommentLine(Optional.empty(), Optional.empty(), comment, position == Position.INLINE ? CommentType.IN_LINE : CommentType.BLOCK);
    }

}