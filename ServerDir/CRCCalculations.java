package ServerDir;

public class CRCCalculations {
    private static int divisor = 0x8005; // 16 bit
    private static int init = 0xFFFF;

    public static int calculateCRC(byte[] data) {
        int crc = init;
        for (byte b : data) {
            crc ^= (b << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ divisor;
                } else {
                    crc = crc << 1;
                }
            }
        }
        return crc & 0xFFFF; // Return CRC as a 16-bit value
    }

    public static byte[] getCRCBytes(int crc) {
        return new byte[] {(byte) (crc >> 8), (byte) crc}; // High byte first
    }

    public static int extractCRC(byte[] crcBytes) {
        return ((crcBytes[0] & 0xFF) << 8) | (crcBytes[1] & 0xFF);
    }
}
