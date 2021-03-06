package slimeknights.tconstruct.gadgets.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;

import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tileentity.IProgress;

public class TileDryingRack extends TileItemRack implements ITickable, ISidedInventory, IProgress {

  int currentTime;
  int maxTime;

  public TileDryingRack() {
    super("gui.dryingrack.name", 2); // two slots, an input and an output. Should never both have something, output is just to stop item tranfer

    // use a SidedInventory Wrapper to respect the canInsert/Extract calls
    this.itemHandler = new SidedInvWrapper(this, null);
  }

  @Override
  public float getProgress() {
    if(getStackInSlot(0) != null && currentTime < maxTime) {
      return (float) currentTime / (float) maxTime;
    }
    return 0;
  }

  @Override
  public void update() {
    //only run on the server side and if a recipe is available
    if(maxTime > 0 && currentTime < maxTime) {
      currentTime++;
      if(currentTime >= maxTime && !worldObj.isRemote) {
        // add the result to slot 1 and remove the original from slot 0
        setInventorySlotContents(1, TinkerRegistry.getDryingResult(getStackInSlot(0)));
        setInventorySlotContents(0, null);
        //drying time updated in setInventorySlotContents

        // comparator update
        this.worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
      }
    }
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack stack) {
    // if there is no drying recipe, just place the item directly into the output slot for item output and tick efficiency
    if(slot == 0 && !isStackInSlot(1) && stack != null && TinkerRegistry.getDryingResult(stack) == null) {
      slot = 1;
    }

    super.setInventorySlotContents(slot, stack);
    if(slot == 0) {
      updateDryingTime();
    }
    else if(this.worldObj != null) {
      // comparator update
      this.worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
    }
  }

  @Override
  public ItemStack decrStackSize(int slot, int quantity) {
    ItemStack stack = super.decrStackSize(slot, quantity);
    maxTime = 0;
    currentTime = 0;
    return stack;
  }

  public void updateDryingTime() {
    currentTime = 0;
    ItemStack stack = getStackInSlot(0);

    if(stack != null) {
      maxTime = TinkerRegistry.getDryingTime(stack);
    }
    else {
      maxTime = -1;
    }
    //worldObj.scheduleUpdate(pos, blockType, 0);
  }

  @Nonnull
  @Override
  @SideOnly(Side.CLIENT)
  public AxisAlignedBB getRenderBoundingBox() {
    return new AxisAlignedBB(pos.getX(), pos.getY() - 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
  }

  @Nonnull
  @Override
  public int[] getSlotsForFace(@Nonnull EnumFacing side) {
    return new int[]{0, 1};
  }

  @Override
  public boolean canInsertItem(int index, @Nonnull ItemStack itemStackIn, @Nonnull EnumFacing direction) {
    // Only allow inserting if there is no stack in the result slot
    return index == 0 && !isStackInSlot(1);
  }

  @Override
  public boolean canExtractItem(int index, @Nonnull ItemStack stack, @Nonnull EnumFacing direction) {
    return index == 1;
  }

  /**
   * @return The current comparator strength based on if an output exists
   */
  public int comparatorStrength() {
    return isStackInSlot(1) ? 15 : 0;
  }

  /* Saving and Loading */

  @Override
  public void readFromNBT(NBTTagCompound tags) {
    currentTime = tags.getInteger("Time");
    maxTime = tags.getInteger("MaxTime");
    super.readFromNBT(tags);
  }

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound tags) {
    tags.setInteger("Time", currentTime);
    tags.setInteger("MaxTime", maxTime);
    return super.writeToNBT(tags);
  }
}
