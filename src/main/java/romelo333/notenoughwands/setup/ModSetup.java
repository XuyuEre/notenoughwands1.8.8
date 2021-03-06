package romelo333.notenoughwands.setup;

import mcjty.lib.setup.DefaultModSetup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import romelo333.notenoughwands.ConfigSetup;
import romelo333.notenoughwands.ForgeEventHandlers;
import romelo333.notenoughwands.Items.GenericWand;
import romelo333.notenoughwands.ModItems;
import romelo333.notenoughwands.network.NEWPacketHandler;
import romelo333.notenoughwands.varia.WrenchChecker;

public class ModSetup extends DefaultModSetup {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());

        NEWPacketHandler.registerMessages("notenoughwands");

        ModItems.init();

        GenericWand.setupConfig(ConfigSetup.getMainConfig());
    }

    @Override
    protected void setupModCompat() {

    }

    @Override
    protected void setupConfig() {
        ConfigSetup.init();
    }

    @Override
    public void createTabs() {
        createTab("NotEnoughWands", () -> new ItemStack(ModItems.teleportationWand));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        ConfigSetup.postInit();
        WrenchChecker.init();
    }

}
