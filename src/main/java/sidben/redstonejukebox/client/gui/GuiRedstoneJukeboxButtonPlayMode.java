package sidben.redstonejukebox.client.gui;

import org.lwjgl.opengl.GL11;
import sidben.redstonejukebox.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;



public class GuiRedstoneJukeboxButtonPlayMode extends GuiButton
{

    protected static int myWidth  = 92;
    protected static int myHeight = 25;

    

    public GuiRedstoneJukeboxButtonPlayMode(int index, int x, int y) {
        super(index, x, y, GuiRedstoneJukeboxButtonPlayMode.myWidth, GuiRedstoneJukeboxButtonPlayMode.myHeight, "");
    }

    
    
    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft par1Minecraft, int mouseX, int mouseY) {
        if (this.visible) {
            boolean isMouseOver = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            this.field_146123_n = isMouseOver;

            if (isMouseOver) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                //par1Minecraft.func_110434_K().func_110577_a(ClientProxy.redstoneJukeboxGui);
                par1Minecraft.getTextureManager().bindTexture(ClientProxy.redstoneJukeboxGui);
                this.drawTexturedModalRect(this.xPosition, this.yPosition, 24, 166, GuiRedstoneJukeboxButtonPlayMode.myWidth, GuiRedstoneJukeboxButtonPlayMode.myHeight);
            }
        }
    }



    /*
    // OBS: Mouseover
    @Override
    public boolean func_82252_a() {     // changed to func_146115_a
        return this.field_82253_i;
    }
    */

    
}