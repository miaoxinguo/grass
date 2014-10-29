package org.miaoxg.grass.core.model;

public class ArrayModel extends Model {
	private byte[] byteArray;
//	private Byte[] byteObjectArray;   // 查询对象数组不支持
	
    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] ba) {
        this.byteArray = ba;
    }

//    public Byte[] getByteObjectArray() {
//        return byteObjectArray;
//    }
//
//    public void setByteObjectArray(Byte[] byteObjectArray) {
//        this.byteObjectArray = byteObjectArray;
//    }
}
