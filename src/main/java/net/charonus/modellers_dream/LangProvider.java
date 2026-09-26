package net.charonus.modellers_dream;

import net.createmod.ponder.Ponder;

import static net.charonus.modellers_dream.ModellersDream.REGISTRATE;

public class LangProvider {
    public LangProvider() {
        // Set the Creative Mode Tab
        REGISTRATE.addRawLang("creativetab.modellers_dream.modellers_dream_tab", "Create: Modellers Dream");

        // Config
        SpecialLang CFG = new SpecialLang(ModellersDream.MOD_ID + ".configuration");
        CFG.addKey("dispatcher", "Dispatcher Behaviour Limits");
        CFG.addKey("max_pending_stations", "Max Pending Stations");

        // Network Setup Ponder
        PonderLang ponderLang = new PonderLang(ModellersDream.MOD_ID + ".ponder.track_network_setup");
        ponderLang.setHeader("Setting up the Track Network");
        ponderLang.addText("Select a Train Track and place the Connector nearby");
        ponderLang.addText("Now we need to add a Dispatcher Table");
        ponderLang.addText("Right-Click the Track Connector with the Dispatcher Table to connect to the network, then place it somewhere");
        ponderLang.addText("Right-Click to open the Dispatcher Table GUI and go dispatch some trains!");
        ponderLang.addText("To dispatch trains you also need to put an empty train schedule to the driver!");

        // Schedule Deployment Ponder
        PonderLang schedulePonderLang = new PonderLang(ModellersDream.MOD_ID + ".ponder.deployer_schedule");
        schedulePonderLang.setHeader("Deploying Schedules to Stations (Create: Modellers Dream)");
        schedulePonderLang.addText("A Deployer holding a Schedule can deliver it straight to a Station");
        schedulePonderLang.addText("With no Train at the Station, the Deployer simply does nothing");
        schedulePonderLang.addText("Now that a Train is present...");
        schedulePonderLang.addText("...the Schedule is handed directly to the Train's Conductor, and consumed");

        // Track Connector Texts
        SpecialLang TC = new SpecialLang(ModellersDream.MOD_ID + ".track_connector");
        TC.addKey("set", "Position set. Right-click a block to place.");
        TC.addKey("clear", "Selection cleared.");
        TC.addKey("missing", "Select a track first.");
        TC.addKey("occupied", "That rail block is already occupied.");

        // Dispatcher Table Texts
        SpecialLang DT = new SpecialLang(ModellersDream.MOD_ID + ".dispatcher_table");
        DT.addKey("linked", "Linked to Track Network.");
        DT.addKey("cleared", "Track Network cleared.");
        DT.addKey("missing_link", "Link a Track Connector first.");

    }

    protected static class PonderLang {
        protected String sceneId;
        protected int text_idx = 1;
        public PonderLang(String sceneId) {
            this.sceneId = sceneId;
        }

        public void setHeader(String header) {
            REGISTRATE.addRawLang(sceneId + ".header", header);
        }

        public void addText(String text) {
            REGISTRATE.addRawLang(sceneId + ".text_" + text_idx, text);
            text_idx++;
        }
    }

    protected record SpecialLang(String defaultNamespace) {

        public void addKey(String Key, String Translation) {
                REGISTRATE.addRawLang(this.defaultNamespace + "." + Key, Translation);
            }
        }
}
