package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.utils.Constants;

public class IndicatorBlock extends Block {

    private final boolean documentStart;
    private final String specification;

    public IndicatorBlock(String comments, int size, String specification) {
        super(Type.INDICATOR, comments, size);
        this.specification = specification;
        this.documentStart = specification.startsWith(Constants.DOCUMENT_START);
    }

    @Override
    public boolean isHeaderContent() {
        return documentStart;
    }

    @Override
    public boolean isFooterContent() {
        return !documentStart;
    }

    public String getSpecification() {
        return specification;
    }
}