package TKPIU.core.tree;

import java.util.List;

import TKPIU.algorithms.mining.AbstractMiner;

public interface IConditionalTreeBuilder {
    UPFTree buildConditionalTree(
            List<AbstractMiner.ConditionalPath> cpb
    );
}
