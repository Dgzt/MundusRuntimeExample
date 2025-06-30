package com.mygdx.game.teavm;

import com.github.xpenatan.gdx.backends.teavm.config.AssetFilter;
import com.github.xpenatan.gdx.backends.teavm.config.AssetFilterOption;

/**
 * Coped from DefaultAssetFilter implementation
 */
public class MundusAssetFilter implements AssetFilter {
    @Override
    public boolean accept(String file, boolean isDirectory, AssetFilterOption op) {
        if(isDirectory && file.endsWith(".svn")) return false;
        if(file.endsWith(".jar")) return false;
        // if(file.endsWith("assets.txt")) return false;
        return true;
    }
}
