package com.modnmetl.virtualrealty.model.region;

public interface IVirtualBlock {

    int getX();
    void setX(int x);

    int getY();
    void setY(int y);

    int getZ();
    void setZ(int z);

    int getMaterial();
    void setMaterial(int material);

    byte getData();
    void setData(byte data);

    String getBlockData();
    void setBlockData(String blockData);

}
