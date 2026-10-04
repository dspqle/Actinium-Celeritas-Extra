package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import dhj.embeddedt.embeddium.api.options.structure.OptionStorage;

/**
 * Bridges Celeritas Extra's settings into the Celeritas options framework.
 * <p>
 * Implements {@link OptionStorage} so the options GUI can read and persist the
 * Celeritas Extra configuration; the backing {@link CeleritasExtraGameOptions}
 * instance is supplied by {@link CeleritasExtraClientMod}.
 */
public class ActiniumExtraOptionsStorage implements OptionStorage<CeleritasExtraGameOptions> {

    /**
     * Returns the live Celeritas Extra options instance backing the GUI.
     *
     * @return the current {@link CeleritasExtraGameOptions}
     */
    @Override
    public CeleritasExtraGameOptions getData() {
        return CeleritasExtraClientMod.options();
    }

    /**
     * Persists any pending changes to the Celeritas Extra options.
     */
    @Override
    public void save() {
        CeleritasExtraClientMod.options().writeChanges();
    }
}
