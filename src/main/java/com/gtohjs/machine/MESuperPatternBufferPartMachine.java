package com.gtohjs.machine;

import com.gtohjs.config.MEPatternBufferConfig;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

/** Configurable high-capacity ME pattern buffer that retains GTO's native AE behavior. */
public final class MESuperPatternBufferPartMachine extends MEOutputCapablePatternBufferPartMachine {
    public MESuperPatternBufferPartMachine(MetaMachineBlockEntity holder) {
        super(holder, MEPatternBufferConfig.patternCount());
    }
}
