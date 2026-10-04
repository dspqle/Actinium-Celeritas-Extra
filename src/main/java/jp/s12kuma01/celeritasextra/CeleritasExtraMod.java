package jp.s12kuma01.celeritasextra;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Main mod entry point for Celeritas Extra, a client-only companion to the
 * Celeritas / Actinium rendering stack.
 * <p>
 * Celeritas Extra layers additional rendering options on top of the renderer and
 * surfaces them inside its own options GUI. This class drives the Forge lifecycle:
 * during construction it detects which renderer is present (Celeritas or its
 * Actinium fork) and registers the matching option-GUI construction listeners;
 * during initialization it bootstraps the client configuration.
 * <p>
 * The mod is {@code clientSideOnly} and accepts any remote version, so it can join
 * servers that do not have it installed.
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
        clientSideOnly = true, acceptableRemoteVersions = "*",
        dependencies = "required-after:cleanroom@[0.6.10-alpha,);"
                + "after:celeritas;after:actinium;after:assetmover@[2.5,)")
public class CeleritasExtraMod {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    @Mod.Instance
    public static CeleritasExtraMod INSTANCE;

    private File configDirectory;

    /**
     * Wires Celeritas Extra into the renderer's options GUI during mod construction.
     * <p>
     * The mod supports both the original Celeritas renderer and its Actinium fork.
     * Each backend exposes an identically-shaped options API under a different root
     * package, so we register whichever listener set corresponds to the renderer that
     * is actually loaded. If neither is present this is a hard failure because the
     * mod's option GUI has nowhere to attach.
     *
     * @param event the Forge construction event
     */
    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        if (Loader.isModLoaded("assetmover")) {
            try {
                jp.s12kuma01.celeritasextra.compat.assetmover.AssetMoverCompat
                        .registerModernCloudTexture();
                LOGGER.info("Requested the Minecraft 1.21.6 cloud texture through AssetMover");
            } catch (RuntimeException | LinkageError throwable) {
                LOGGER.error("AssetMover integration failed; the modern cloud texture was not requested",
                        throwable);
            }
        } else {
            LOGGER.info("AssetMover is not installed; the modern cloud texture will not be downloaded");
        }

        boolean registered = false;

        if (Loader.isModLoaded("celeritas")) {
            org.taumc.celeritas.api.OptionGUIConstructionEvent.BUS.addListener(
                    jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraOptionsListener::onCeleritasOptionsConstruct);
            org.taumc.celeritas.api.OptionGroupConstructionEvent.BUS.addListener(
                    jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraOptionsListener::onOptionGroupConstruct);
            LOGGER.info("Successfully registered Celeritas Extra with the Celeritas GUI");
            registered = true;
        }

        if (Loader.isModLoaded("actinium")) {
            jp.s12kuma01.celeritasextra.client.gui.ActiniumExtraOptionsListener.register();
            LOGGER.info("Successfully registered Celeritas Extra with the Actinium GUI");
            registered = true;
        }

        if (!registered) {
            LOGGER.error("Neither Celeritas nor Actinium is loaded; Celeritas Extra options will not be available");
        }
    }

    /**
     * Captures Forge's canonical configuration directory before client initialization.
     *
     * @param event the Forge pre-initialization event
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.configDirectory = event.getModConfigurationDirectory();
        LOGGER.info("Celeritas Extra pre-initialization");
    }

    /**
     * Bootstraps the client on the effective client side during Forge initialization.
     * <p>
     * Delegates to {@link jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod#onClientInit(File)}
     * so dedicated-server environments never load client-only classes.
     *
     * @param event the Forge initialization event
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod
                    .onClientInit(this.configDirectory);
        }
        LOGGER.info("Celeritas Extra initialized");
    }
}
