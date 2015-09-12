package sidben.redstonejukebox.client.gui;

import java.util.ArrayList;
import java.util.List;
import sidben.redstonejukebox.handler.ConfigurationHandler;
import sidben.redstonejukebox.reference.Reference;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;


public class ModGuiConfig extends GuiConfig {


    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, getConfigElements(), Reference.ModID, false, false, GuiConfig.getAbridgedConfigPath(ConfigurationHandler.config.toString()));
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        List<IConfigElement> recordTradingConfigs = new ArrayList<IConfigElement>();
        List<IConfigElement> customRecordConfigs = new ArrayList<IConfigElement>();
        
        
        // Record Trading
        recordTradingConfigs = new ConfigElement(ConfigurationHandler.config.getCategory(ConfigurationHandler.CATEGORY_TRADING)).getChildElements();
        list.add(new DummyConfigElement.DummyCategoryElement(ConfigurationHandler.CATEGORY_TRADING, "sidben.redstonejukebox.config.category.record_trading", recordTradingConfigs));

        // Custom Records
        customRecordConfigs = new ConfigElement(ConfigurationHandler.config.getCategory(ConfigurationHandler.CATEGORY_CUSTOM)).getChildElements();
        list.add(new DummyConfigElement.DummyCategoryElement(ConfigurationHandler.CATEGORY_CUSTOM, "sidben.redstonejukebox.config.category.custom_records", customRecordConfigs));
        
        
        return list;
    }

}