package net.charonus.modellers_dream.screen;

import com.tterrag.registrate.util.entry.MenuEntry;
import net.charonus.modellers_dream.ModellersDream;
import net.charonus.modellers_dream.screen.custom.DispatcherTable.DispatcherMenu;
import net.charonus.modellers_dream.screen.custom.DispatcherTable.DispatcherScreen;

public class ModMenuTypes {

    public static final MenuEntry<DispatcherMenu> DISPATCHER_MENU = ModellersDream.REGISTRATE
            .menu("dispatcher_menu",
                    (type, windowId, inv, buf) -> new DispatcherMenu(windowId, inv, buf),
                    () -> DispatcherScreen::new)
            .register();

    public static void register() {
    }
}