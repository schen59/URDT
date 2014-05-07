package common;

import util.CommonUtil;

import java.util.Arrays;

import static common.Messages.EOF;

/**
 * Class which holds the byte stream of the response data.
 * @author Shaofeng Chen
 * @since 2/21/14
 */
public class RawData {
    private byte[] responseData;
    private int cursor;

    public RawData(byte[] responseData) {
        this.responseData = responseData;
        cursor = 0;
    }

    /**
     * Get one byte from the response data stream.
     * @return byte
     */
    public byte getByte() {
        checkEnoughDataLeft(1);
        return responseData[cursor++];
    }

    /**
     * Get unsigned 8 bits from the response data stream.
     * @return int
     */
    public int getU8() {
        checkEnoughDataLeft(1);
        return CommonUtil.getU8From(responseData[cursor++]);
    }

    /**
     * Get unsigned 8 bits from the response data stream without changing the cursor.
     * @return int
     */
    public int peekU8() {
        checkEnoughDataLeft(0);
        return CommonUtil.getU8From(responseData[cursor]);
    }

    /**
     * Get unsigned 16 bits from the response data stream.
     * @return int
     */
    public int getU16() {
        checkEnoughDataLeft(2);
        byte high = responseData[cursor++];
        byte low = responseData[cursor++];
        return CommonUtil.toU16From(high, low);
    }

    /**
     * Get unsigned 32 bits from the resposne data stream.
     * @return long
     */
    public long getU32() {
        checkEnoughDataLeft(4);
        byte first = responseData[cursor++];
        byte second = responseData[cursor++];
        byte third = responseData[cursor++];
        byte fourth = responseData[cursor++];
        return CommonUtil.toU32From(first, second, third, fourth);
    }

    public int get32() {
        return (int)getU32();
    }

    public byte[] getRemainingBytes() {
        return Arrays.copyOfRange(responseData, cursor, responseData.length);
    }

    /**
     * Get pointer from the response data stream.
     * @return int
     */
    public int getPointer() {
        checkEnoughDataLeft(2);
        byte high = (byte)(responseData[cursor++] & 0x3f);
        byte low = responseData[cursor++];
        return CommonUtil.toU16From(high, low);
    }

    /**
     * Seek the cursor to specified location.
     * @param cursor
     */
    public void seek(int cursor) {
        this.cursor = cursor;
    }

    /**
     * Get the location of current cursor.
     * @return
     */
    public int getCursor() {
        return cursor;
    }

    private void checkEnoughDataLeft(int needed) {
        if (cursor+needed > responseData.length) {
            throw new UdtException(EOF);
        }
    }
}
