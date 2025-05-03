package edu.grinnell.csc207.compression;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;

public class Tests {

    @Test
    public void testHuffmanTreein() throws IOException {
        BitInputStream in = new BitInputStream("files/huffman-example.grin");
        if (in.readBits(32) != 0x736) {
            throw new IllegalArgumentException();
        }
        HuffmanTree tree = new HuffmanTree(in);
        assertTrue(!tree.root.isLeaf());
        assertEquals(-1, tree.root.getVal());
    }

    @Test
    public void testHuffmanTreemap() throws IOException {
        Map<Short, Integer> map = Grin.createFrequencyMap("files/huffman-example.txt");
        HuffmanTree tree = new HuffmanTree(map);
        assertTrue(!tree.root.isLeaf());
        assertEquals(-1, tree.root.getVal());
    }

    @Test
    public void testDecode() throws IOException {
        Grin.decode("files/huffman-example.grin", "files/test.txt");
        File file = new File("files/test.txt");
        Scanner scanner = new Scanner(file);
        assertEquals("a ab bza", scanner.nextLine());
    }

    @Test
    public void testEncode() throws IOException {
        Grin.encode("files/huffman-example.txt", "files/test.grin");
        Grin.decode("files/test.grin", "files/test.txt");
        File file = new File("files/test.txt");
        Scanner scanner = new Scanner(file);
        assertEquals("a ab bza", scanner.nextLine());
    }

    @Test
    public void testLongTextEncode() throws IOException {
        Grin.encode("files/pg2600.txt", "files/test.grin");
        Grin.decode("files/test.grin", "files/test.txt");
        File file = new File("files/test.txt");
        Scanner scanner = new Scanner(file);
        File pg = new File("files/pg2600.txt");
        Scanner pgs = new Scanner(pg);
        String line = "";
        while (scanner.hasNextLine() && pgs.hasNextLine()) {
            assertEquals(pgs.nextLine(), scanner.nextLine());
        }
    }
}
