package edu.grinnell.csc207.compression;

import java.util.Map;
import java.util.*;

/**
 * A HuffmanTree derives a space-efficient coding of a collection of byte
 * values.
 *
 * The huffman tree encodes values in the range 0--255 which would normally take
 * 8 bits. However, we also need to encode a special EOF character to denote the
 * end of a .grin file. Thus, we need 9 bits to store each byte value. This is
 * fine for file writing (modulo the need to write in byte chunks to the file),
 * but Java does not have a 9-bit data type. Instead, we use the next larger
 * primitive integral type, short, to store our byte values.
 */
public class HuffmanTree {

    /**
     * a node of a huffman tree
     */
    public static class Node implements Comparable<Node> {

        private int frequency;
        private short value;
        private Node left;
        private Node right;

        Node(Node left, Node right) {
            this.value = -1;
            this.frequency = left.frequency + right.frequency;
            this.left = left;
            this.right = right;
        }

        Node(short value, int frequency) {
            this.value = value;
            this.frequency = frequency;
            this.left = null;
            this.right = null;
        }

        /**
         * @param other a node
         * @return compare the frequency of the node and the other node
         */
        public int compareTo(Node other) {
            if (this.frequency > other.frequency) {
                return 1;
            } else if (this.frequency == other.frequency && this.value > other.value) {
                return 1;
            } else {
                return -1;
            }
        }
        
        /**
         * @return the value of the node
         */
        public short getVal() {
            return value;
        }

        /**
         * @return the frequency of the node
         */
        public int getFreq() {
            return frequency;
        }
        
        /**
         * @return true if the node is a leaf not an internal node
         */
        public boolean isLeaf() {
            return left == null && right == null;
        }

    }

    /**
     * the root of huffman tree
     */
    public Node root;

    /**
     * Constructs a new HuffmanTree from a frequency map.
     *
     * @param freqs a map from 9-bit values to frequencies.
     */
    public HuffmanTree(Map<Short, Integer> freqs) {
        freqs.put((short) 256, 1);
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (short key : freqs.keySet()) {
            Node n = new Node(key, freqs.get(key));
            pq.add(n);
        }
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            if (left.value == -1 && right.value != -1) {
                Node temp = left;
                left = right;
                right = temp;
            }
            Node parent = new Node(left, right);

            pq.add(parent);
        }
        root = pq.poll();
    }

    /**
     * Constructs a new HuffmanTree from the given file.
     *
     * @param in the input file (as a BitInputStream)
     */
    public HuffmanTree(BitInputStream in) {
        root = readh(in);
    }

    private Node readh(BitInputStream in) {
        int current = in.readBit();
        if (current == -1) {
            return null;
        }
        if (current == 1) {
            Node left = readh(in);
            Node right = readh(in);
            return new Node(left, right);
        } else {
            short value = (short) in.readBits(9);
            return new Node(value, 1);
        }
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     *
     * @param out the output file as a BitOutputStream
     */
    public void serialize(BitOutputStream out) {
        serializeh(root, out);

    }

    private void serializeh(Node cur, BitOutputStream out) {
        if (cur.left == null && cur.right == null) {
            out.writeBit(0);
            out.writeBits(cur.value, 9);
        } else {
            out.writeBit(1);
            serializeh(cur.left, out);
            serializeh(cur.right, out);
        }
    }

    /**
     * Encodes the file given as a stream of bits into a compressed format using
     * this Huffman tree. The encoded values are written, bit-by-bit to the
     * given BitOuputStream.
     *
     * @param in the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode(BitInputStream in, BitOutputStream out) {
        Node current = root;
        Map<Short, String> codes = new HashMap<>();
        encodeh(root, "", codes);
        while (true) {
            int value = in.readBits(8);
            if (value == -1) {
                break;
            }
            short val = (short) value;
            String code = codes.get(val);
            for (int i = 0; i < code.length(); i++) {
                int bit = (int) code.charAt(i) - 48;
                out.writeBit(bit);
            }

        }
        String eof = codes.get((short) 256);
        for (int i = 0; i < eof.length(); i++) {
            int bit = (int) eof.charAt(i) - 48;
            out.writeBit(bit);
        }
    }

    private void encodeh(Node cur, String code, Map<Short, String> codes) {
        if (cur == null) {
            return;
        }
        if (cur.left == null && cur.right == null) {
            codes.put(cur.value, code);
            return;
        }
        encodeh(cur.left, code + "0", codes);
        encodeh(cur.right, code + "1", codes);
    }

    /**
     * Decodes a stream of huffman codes from a file given as a stream of bits
     * into their uncompressed form, saving the results to the given output
     * stream. Note that the EOF character is not written to out because it is
     * not a valid 8-bit chunk (it is 9 bits).
     *
     * @param in the file to decompress.
     * @param out the file to write the decompressed output to.
     */
    public void decode(BitInputStream in, BitOutputStream out) {
        Node current = root;
        while (true) {
            int bit = in.readBit();
            if (bit == -1) {
                break;
            }
            if (bit == 0) {
                current = current.left;
            } else {
                current = current.right;
            }

            if (current.left == null && current.right == null) {
                if (current.value == 256) {
                    out.close();
                    break;
                }
                out.writeBits(current.value, 8);
                current = root;
            }
        }
    }
}
