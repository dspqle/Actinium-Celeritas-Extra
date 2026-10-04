package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import net.minecraft.client.resources.I18n;
import dhj.embeddedt.embeddium.impl.gui.framework.TextComponent;
import dhj.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import dhj.embeddedt.embeddium.api.OptionGroupConstructionEvent;
import dhj.embeddedt.embeddium.api.options.control.CyclingControl;
import dhj.embeddedt.embeddium.api.options.structure.*;

import java.util.List;

/**
 * Hooks into the Celeritas options GUI so Celeritas Extra can extend it.
 * <p>
 * Two construction events are handled:
 * - {@link OptionGUIConstructionEvent} adds the five Celeritas Extra pages
 * (animation, particle, details, render, extra) built by
 * {@link ActiniumExtraGameOptionPages}.
 * - {@link OptionGroupConstructionEvent} rewrites the vanilla WINDOW group,
 * swapping the plain Fullscreen and VSync boolean toggles for richer
 * three-way cycling controls.
 */
public class ActiniumExtraOptionsListener {

    /**
     * Registers the Actinium option-GUI construction listeners.
     * <p>
     * Called by {@link jp.s12kuma01.celeritasextra.CeleritasExtraMod} during mod construction
     * when the Actinium renderer is loaded.
     */
    public static void register() {
        dhj.embeddedt.embeddium.api.OptionGUIConstructionEvent.BUS
                .addListener(ActiniumExtraOptionsListener::onCeleritasOptionsConstruct);
        dhj.embeddedt.embeddium.api.OptionGroupConstructionEvent.BUS
                .addListener(ActiniumExtraOptionsListener::onOptionGroupConstruct);
    }

    private static final ActiniumExtraOptionsStorage celeritasExtraOpts = new ActiniumExtraOptionsStorage();

    /**
     * Registers every Celeritas Extra option page when the Celeritas options GUI is built.
     *
     * @param event the GUI construction event the pages are added to
     */
    public static void onCeleritasOptionsConstruct(OptionGUIConstructionEvent event) {
        CeleritasExtraMod.LOGGER.info("Registering Celeritas Extra options pages (Actinium backend)");

        // Register all Celeritas Extra option pages
        event.addPage(ActiniumExtraGameOptionPages.animation());
        event.addPage(ActiniumExtraGameOptionPages.particle());
        event.addPage(ActiniumExtraGameOptionPages.details());
        event.addPage(ActiniumExtraGameOptionPages.render());
        event.addPage(ActiniumExtraGameOptionPages.extra());

        CeleritasExtraMod.LOGGER.info("Successfully registered {} Celeritas Extra option pages", 5);
    }

    /**
     * Replaces vanilla boolean toggles in the WINDOW group with enhanced cycling controls:
     * - Fullscreen → 3-way Screen Mode on compatible desktop runtimes
     * - VSync → 3-way VSync (Off / On / Adaptive)
     */
    public static void onOptionGroupConstruct(OptionGroupConstructionEvent event) {
        if (!event.getId().matches(StandardOptions.Group.WINDOW)) {
            return;
        }

        List<Option<?>> options = event.getOptions();
        for (int i = 0; i < options.size(); i++) {
            Option<?> option = options.get(i);
            if (option.getId() == null) continue;

            if (option.getId().matches(StandardOptions.Option.FULLSCREEN)) {
                if (WindowModeSupport.canOfferBorderless()) {
                    options.set(i, createScreenModeOption());
                    CeleritasExtraMod.LOGGER.debug("Replaced Fullscreen option with screen mode control");
                }
            } else if (option.getId().matches(StandardOptions.Option.VSYNC)) {
                options.set(i, createVSyncOption());
                CeleritasExtraMod.LOGGER.debug("Replaced VSync option with adaptive VSync control");
            }
        }
    }

    /**
     * Builds the replacement for the vanilla Fullscreen toggle: a three-way
     * cycling control over {@link CeleritasExtraGameOptions.ScreenMode} offering
     * windowed, borderless, and fullscreen modes.
     *
     * @return the configured screen-mode option
     */
    private static OptionImpl<CeleritasExtraGameOptions, CeleritasExtraGameOptions.ScreenMode> createScreenModeOption() {
        return OptionImpl.createBuilder(CeleritasExtraGameOptions.ScreenMode.class, celeritasExtraOpts)
                .setId(StandardOptions.Option.FULLSCREEN.cast())
                .setName(TextComponent.literal(I18n.format("celeritasextra.option.screen_mode")))
                .setTooltip(TextComponent.literal(
                        I18n.format("celeritasextra.option.screen_mode.tooltip")))
                .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.ScreenMode.class,
                        new TextComponent[]{
                                TextComponent.literal(CeleritasExtraGameOptions.ScreenMode.WINDOWED.getLocalizedName()),
                                TextComponent.literal(CeleritasExtraGameOptions.ScreenMode.BORDERLESS.getLocalizedName()),
                                TextComponent.literal(CeleritasExtraGameOptions.ScreenMode.FULLSCREEN.getLocalizedName())
                        }))
                .setBinding(CeleritasExtraGameOptions.ScreenMode::apply,
                        CeleritasExtraGameOptions.ScreenMode::getCurrent)
                .build();
    }

    /**
     * Builds the replacement for the vanilla VSync toggle: a three-way cycling
     * control over {@link CeleritasExtraGameOptions.VerticalSyncOption} offering
     * off, on, and adaptive modes.
     *
     * @return the configured vertical-sync option
     */
    private static OptionImpl<CeleritasExtraGameOptions, CeleritasExtraGameOptions.VerticalSyncOption> createVSyncOption() {
        return OptionImpl.createBuilder(CeleritasExtraGameOptions.VerticalSyncOption.class, celeritasExtraOpts)
                .setId(StandardOptions.Option.VSYNC.cast())
                .setName(TextComponent.translatable("options.vsync"))
                .setTooltip(TextComponent.literal(
                        I18n.format("celeritasextra.option.extra.vertical_sync.tooltip")))
                .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.VerticalSyncOption.class,
                        new TextComponent[]{
                                TextComponent.literal(CeleritasExtraGameOptions.VerticalSyncOption.OFF.getLocalizedName()),
                                TextComponent.literal(CeleritasExtraGameOptions.VerticalSyncOption.ON.getLocalizedName()),
                                TextComponent.literal(CeleritasExtraGameOptions.VerticalSyncOption.ADAPTIVE.getLocalizedName())
                        }))
                .setBinding(CeleritasExtraGameOptions.VerticalSyncOption::apply,
                        CeleritasExtraGameOptions.VerticalSyncOption::getCurrent)
                .setImpact(OptionImpact.VARIES)
                .build();
    }
}
