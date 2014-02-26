package net.shadowmage.ancientwarfare.core.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.shadowmage.ancientwarfare.core.config.Statics;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.gui.elements.GuiElement;
import net.shadowmage.ancientwarfare.core.interfaces.IContainerGuiCallback;
import net.shadowmage.ancientwarfare.core.interfaces.ISlotClickCallback;
import net.shadowmage.ancientwarfare.core.interfaces.ITooltipRenderer;
import net.shadowmage.ancientwarfare.core.util.RenderTools;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class GuiContainerBase extends GuiContainer implements IContainerGuiCallback, ITooltipRenderer, ISlotClickCallback
{

private float partialRenderTick = 0.f;
private boolean initDone = false;
private boolean shouldUpdate = false;
private List<GuiElement> elements = new ArrayList<GuiElement>();

private ItemStack tooltipStack;
private int tooltipX;
private int tooltipY;

private String backgroundTextureName;
private ResourceLocation backgroundTexture;

public GuiContainerBase(ContainerBase par1Container, int xSize, int ySize, String backgroundTexture)
  {
  super(par1Container);
  par1Container.setGui(this);
  this.xSize = xSize;
  this.ySize = ySize;
  if(backgroundTexture!=null)
    {
    this.backgroundTextureName = backgroundTexture;
    this.backgroundTexture = new ResourceLocation(Statics.coreModID, "textures/gui/"+backgroundTextureName);    
    }
  }

@Override
public void handleTooltipRender(ItemStack stack)
  {
  int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
  int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
  this.tooltipStack = stack;
  this.tooltipX = x;
  this.tooltipY = y;
  }

protected void addGuiElement(GuiElement element)
  {
  this.elements.add(element);
  }

@Override
public void handleMouseInput()
  {
  super.handleMouseInput();
  
  int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
  int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
  int button = Mouse.getEventButton();
  boolean state = Mouse.getEventButtonState();
  int wheel = Mouse.getEventDWheel();
  
  int type = button >= 0 ? (state ? Listener.MOUSE_DOWN : Listener.MOUSE_UP) : wheel!=0 ? Listener.MOUSE_WHEEL : Listener.MOUSE_MOVED; 
  ActivationEvent evt = new ActivationEvent(type, button, state, x, y, wheel);
  for(GuiElement element : this.elements)
    {
    element.handleMouseInput(evt);
    }
  }

@Override
public void handleKeyboardInput()
  {
  super.handleKeyboardInput();  
  int key = Keyboard.getEventKey();
  boolean state = Keyboard.getEventKeyState();
  ActivationEvent evt = new ActivationEvent(state ? Listener.KEY_DOWN : Listener.KEY_UP, key, Keyboard.getEventCharacter(), state);  
  for(GuiElement element : this.elements)
    {
    element.handleKeyboardInput(evt);
    }
  }

@Override
public void initGui()
  {
  super.initGui();
  if(!initDone)
    {
    initElements();
    initDone = true;
    }
  this.setupElements();
  for(GuiElement element : this.elements)
    {
    element.updateRenderPosition(guiLeft, guiTop);
    }
  }

@Override
public void updateScreen()
  {
  if(this.shouldUpdate)
    {
    this.initGui();
    this.shouldUpdate = false;
    }
  super.updateScreen();
  }

@Override
protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
  {
  if(backgroundTexture!=null)
    {
    Minecraft.getMinecraft().renderEngine.bindTexture(backgroundTexture);
    RenderTools.renderQuarteredTexture(256, 256, 0, 0, 256, 256, width/2 - xSize/2, height/2 - ySize/2, xSize, ySize);
    }
  }

@Override
public void drawScreen(int par1, int par2, float par3)
  {
  this.partialRenderTick = par3;
  super.drawScreen(par1, par2, par3);  
  if(tooltipStack!=null)
    {
    super.renderToolTip(tooltipStack, tooltipX, tooltipY);
    tooltipStack = null;
    }
  }

protected void drawGuiContainerForegroundLayer(int par1, int par2)
  {
  GL11.glDisable(GL11.GL_LIGHTING);
  GL11.glPushMatrix();
  GL11.glTranslatef(-guiLeft, -guiTop, 0);
  for(GuiElement element : elements)
    {
    element.render(par1, par2, partialRenderTick);
    }   
  GL11.glPopMatrix();
  GL11.glEnable(GL11.GL_LIGHTING);
  }

/**
 * call this method to enforce a re-initialization of gui at the start of next game tick (not render tick)<br>
 * setupElements() will then be called<br>
 * and all elements will have their position updated
 */
@Override
public void refreshGui()
  {
  this.shouldUpdate = true;
  }

/**
 * sub-classes should implement this method to setup/change any elements that need adjusting when the gui is initialized<br>
 * any elements that are positioned outside of the gui-window space will need their positions updated by calling element.setPosition(xPos, yPos)
 * as they reference internal position relative to the guiLeft / guiTop values from this gui (which are passed in and updated directly after setupElements() is called)
 */
public abstract void setupElements();

public abstract void initElements();

/**
 * sub-classes should override this method to handle any expected packet data
 */
@Override
public void handlePacketData(Object data)
  {
  
  }

/**
 * TODO implement basic handling of slot-click action for widget-based slots
 */
@Override
public void onSlotClicked(IInventory inventory, int slotIndex, int button)
  {
  ((ContainerBase)inventorySlots).onSlotClicked(inventory, slotIndex, button);
  }

/**
 * deferred to allow proper render-order, and draw the tooltip on top of everything else
 */
@Override
protected void renderToolTip(ItemStack stack, int x, int y)
  {
  tooltipStack = stack;
  tooltipX = x;
  tooltipY = y;  
  }

/**
 * Action event for gui-widgets.  A single event is sent to all element / listeners for each input event (key up/down, mouse up/down/move/wheel)
 * @author Shadowmage
 */
public class ActivationEvent
{
/**
 * the type of event:<br>
 * 0=Key up <br> 
 * 1=Key down <br>
 * 2=Mouse up <br>
 * 4=Mouse Down <br>  
 * 8=Mouse Wheel 
 */
public final int type;
public int key;
public boolean keyEvent;
public int mButton;
public boolean state;
public char ch;
public int mx;
public int my;
public int mw;//mousewheel delta movement
private ActivationEvent(int type, int button, boolean state, int mx, int my, int mw)
  {
  this.type = type;
  this.mButton = button;
  this.state = state;
  this.mx = mx;
  this.my = my;
  this.mw = mw;
  this.keyEvent = key!=0;
  }

private ActivationEvent(int type, int key, char character, boolean state)
  {
  this.type = type;
  this.key = key;
  this.ch = character;
  this.state = state;
  }
}

}