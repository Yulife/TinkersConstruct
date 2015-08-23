package slimeknights.tconstruct.tools.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.List;

import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.MaterialItem;
import slimeknights.tconstruct.library.tools.ToolPart;

public class Shard extends ToolPart {

  public Shard() {
    super(COST_Shard);
  }

  @Override
  public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
    // this adds a variant of each material to the creative menu
    for(Material mat : TinkerRegistry.getAllMaterials()) {
      if(mat.craftable || mat.castable)
        subItems.add(getItemstackWithMaterial(mat));
    }
  }
}