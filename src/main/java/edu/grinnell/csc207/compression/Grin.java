package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.*;
import java.io.*;

/**
 * The driver for the Grin compression program.
 */
public class Grin {

    /**
     * Decodes the .grin file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     *
     * @param infile the file to decode
     * @param outfile the file to ouptut to
     */
    public static void decode(String infile, String outfile) throws IOException {
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        if (in.readBits(32) != 0x736) {
            throw new IllegalArgumentException();
        }
        HuffmanTree tree = new HuffmanTree(in);
        tree.decode(in, out);
        in.close();
        out.close();
    }

    /**
     * Creates a mapping from 8-bit sequences to number-of-occurrences of those
     * sequences in the given file. To do this, read the file using a
     * BitInputStream, consuming 8 bits at a time.
     *
     * @param file the file to read
     * @return a freqency map for the given file
     */
    public static Map<Short, Integer> createFrequencyMap(String file) throws IOException {
        BitInputStream in = new BitInputStream(file);
        Map<Short, Integer> freqs = new HashMap<>();
        while (true) {
            int value = in.readBits(8);
            if (value == -1) {
                break;
            }
            short val = (short) value;
            if (freqs.containsKey(val)) {
                freqs.put(val, freqs.get(val) + 1);
            } else {
                freqs.put(val, 1);
            }
        }
        return freqs;
    }

    /**
     * Encodes the given file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     *
     * @param infile the file to encode.
     * @param outfile the file to write the output to.
     */
    public static void encode(String infile, String outfile) throws IOException {
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        HuffmanTree tree = new HuffmanTree(createFrequencyMap(infile));
        out.writeBits(0x736, 32);
        tree.serialize(out);
        tree.encode(in, out);
        in.close();
        out.close();
    }

    /**
     * The entry point to the program.
     *
     * @param args the command-line arguments.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java Grin <encode|decode> <infile> <outfile>");
            return;
        }
        String command = args[0];
        String infile = args[1];
        String outfile = args[2];
        if (command.equals("encode")) {
            encode(infile, outfile);
        } else if (command.equals("decode")) {
            decode(infile, outfile);
        } else {
            System.out.println("First argument must be 'encode' or 'decode'");
            return;
        }
    }
}
