package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class NpcBanditMountedArcher extends NpcFactionMountedArcher
{

public NpcBanditMountedArcher(World par1World)
  {
  super(par1World);
  }

@Override
public void attackEntityWithRangedAttack(EntityLivingBase var1, float var2)
  {
  // TODO Auto-generated method stub
  }

@Override
public String getNpcType()
  {
  return "bandit.mounted_archer";
  }

}