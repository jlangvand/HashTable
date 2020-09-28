import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

public class Oppg4 {
    public static void main(String... args) {

        SharedUtils.printDebug = false;

        assignment1();

        Assignment2 assignment2 = new Assignment2();
        assignment2.setup((int)1e7, (int)1e9);
        assignment2.run();

    }

    static void assignment1() {
        SharedUtils.printHeader("Assignment 1:\nCreate Hashtable, populate it with names from a file");

        // Create hashtable
        MyHashTable<Person> table = new MyHashTable<>();

        // Read file into array and populate table
        Person[] people = readPeopleFromFile(System.getProperty("user.dir") + "/names");
        for (Person person : people) table.add(person);

        // Tests: Joakim should be present in the table, Svlad is made up and should fail
        Optional<Person> joakim = table.search("Langvand, Joakim Skogø");
        Optional<Person> svlad = table.search("Cjelli, Svlad");
        System.out.println("\nSearch tests " + (joakim.isPresent() && !svlad.isPresent() ? "OK" : "FAILED"));

        // Print the table
        System.out.println("\n" + table);
        System.out.println("\nTable load (entries/size): " + table.getLoad());
    }

    /*
      The following method is written by @martinus
      https://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
    */
    static int countLinesNew(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i=0; i<1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                for (int i=0; i<readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }


    static Person[] readPeopleFromFile(String filename) {
        try {
            Person[] people = new Person[countLinesNew(filename)];
            File names = new File(filename);
            Scanner scanner = new Scanner(names);
            int index = 0;
            while (scanner.hasNextLine()) {
                String data[] = scanner.nextLine().split(",");
                people[index++] = new Person(data[0], data[1]);
            }
            return people;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class Assignment2 {
    private int[] intArray;
    private MyHashTable<Integer> hashtable;
    private boolean ready;
    private SimpleTimer timer;

    Assignment2() {
        timer = new SimpleTimer();
        this.ready = false;
        SharedUtils.printHeader("Assignment 2:\nPopulate hashtable with 10M ints and measure perf.");
    }

    /*
      Initialise fields. Returns false if something went wrong.
    */
    public void setup(int size, int bound) {
        this.timer.start();
        this.intArray = getRandomIntArray(size, bound);
        this.hashtable = new MyHashTable((int)((double)size * 1.35));
        SharedUtils.print("Setup complete in " + this.timer.stop() + "s");
        this.ready = true;
    }

    /*
      Default parameters
    */
    public void setup() {
        this.setup((int)10e8, (int)10e7);
    }

    public boolean run() {
        if (!ready) {
            SharedUtils.print("Run setup first");
            return false;
        }

        boolean status = true;

        this.timer.start();
        for (int i = 0; i < intArray.length; hashtable.add(intArray[i++]));
        double duration = this.timer.stop();

        java.util.HashMap hashmap = new java.util.HashMap();

        this.timer.start();
        for (int i = 0; i < intArray.length; hashmap.put(null, intArray[i++]));
        double duration2 = this.timer.stop();

        SharedUtils.print("Length of array: " + intArray.length, true);

        SharedUtils.print("Custom hashtable: " + duration + "s");
        SharedUtils.print("Java hashtable: " + duration2 + "s");

        int collisions = hashtable.getCollisions();
        double load = ((double)((int)(hashtable.getLoad() * 100)/100.0));
        int overhead = (int)((1 - load) * 100);

        status = overhead > 35 ? false : status;

        //SharedUtils.print(hashtable + "\n", true);
        SharedUtils.print("Collisions: " + hashtable.getCollisions() +
                          " (~" + + (int)(((double)collisions / (double)intArray.length)*100) +
                          "%), Load: ~" + load +
                          " (~" + overhead +
                          "% overhead) Rehashes: " + hashtable.getRehashes(), false);

        return status;
    }

    /*
      Generate table of size 'size' populated by random ints in range [0, bound>
    */
    private static int[] getRandomIntArray(int size, int bound) {
        Random rand = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) array[i] = rand.nextInt(bound);
        return array;
    }
}

class SimpleTimer {
    private Instant instant;

    SimpleTimer() {
        this.instant = Instant.now();
    }

    public void start() {
        this.instant = Instant.now();
    }

    public double stop() {
        return (double)ChronoUnit.MICROS.between(instant, Instant.now()) * 1.0e-6;
    }
}

class SharedUtils {
    public static boolean printDebug = false;
    public static void printHeader(String str) {
        int cols = 80;
        String lines[] = str.split("\n");
        for (int i = 0; i < 80; i++) System.out.print("*");
        for (String line : lines) {
            int len = line.length();
            System.out.print("\n*");
            printNTimes(" ", (cols / 2) - (len / 2));
            System.out.print(line);
            printNTimes(" ", (cols / 2) - (len / 2) - (len % 2) - 2);
            System.out.print("*");
        }
        System.out.println();
        for (int i = 0; i < 80; i++) System.out.print("*");
        System.out.println();
    }

    static void printNTimes(String str, int n) {
        for (int i = 0; i < n; i++) System.out.print(str);
    }

    static void print(String str, boolean debug) {
        if (!debug || printDebug) print(str);
    }

    static void print(String str) {
        System.out.println(str);
    }
}

class Person {
    private String fname;
    private String lname;

    Person(String fname, String lname) {
        this.fname = fname;
        this.lname = lname;
    }

    public String toString() {
        return String.format("%s, %s", lname, fname);
    }
}

class MyHashTable<T> {
    private int size;
    private int entries;
    private int collisions;
    private double loadFactor;
    private LinkedList<T>[] table;
    private int rehashes;

    public static int defaultSize = 51;
    public static double defaultLoadFactor = 0.75;

    MyHashTable() {
        this(defaultSize, defaultLoadFactor);
    }

    MyHashTable(int size) {
        this(size, defaultLoadFactor);
    }

    MyHashTable(int size, double loadFactor) {
        this.size = nextPrime(size);
        this.entries = 0;
        this.loadFactor = loadFactor;
        this.rehashes = 0;
        this.table = (LinkedList<T>[])java.lang.reflect.Array.newInstance(LinkedList.class, this.size);
        for (int i = 0; i < this.size; i++) table[i] = new LinkedList<T>();
    }

    /*
      Get number of collisions
     */
    public int getCollisions() {
        int collisions = 0, size = 0;
        for (int i = 0; i < this.size; size = this.table[i++].getSize())
            collisions += size > 1 ? --size : 0;
        return collisions;
    }

    public int getRehashes() {
        return this.rehashes;
    }

    /*
      Search for an object in the table. Returns the object if present.
    */
    public Optional<T> search(String cmp) {
        int index = genHash(cmp);
        Optional<T> ret = Optional.empty();
        for (int i = 0; i < this.table[index].getSize(); i++) {
            T obj = this.table[index].get(i);
            if (obj.toString().equals(cmp.toString())) return ret.of(obj);
        }
        return ret;
    }

    /*
      Get the current load of the table
    */
    public double getLoad() {
        return (double)this.entries / (double)this.size;
    }

    /*
      Find the next prime number after n (or n if it's prime)
    */
    private static int nextPrime(int n) {
        n = n % 2 > 0 ? n : n + 1;
        while (!isPrime(n)) n += 2;
        return n;
    }

    /*
      Crude primality test, fine for small numbers
    */
    private static boolean isPrime(int n) {
        if (n % 2 == 0) return false;
        for (int i = 3; i < java.lang.Math.sqrt(n); i += 2)
            if (n % i == 0) return false;
        return true;
    }

    /*
      Add an object to the table.

      We'll use toString() to hash the object.

      Before returning, we'll check if the load exceeds the load factor and rehash if necessary.
    */
    public void add(T obj) {
        int index = genHash(obj);
        table[index].push(obj);
        this.entries++;
        if (getLoad() > loadFactor) rehash();
    }

    /*
      Pretty print.
    */
    public String toString() {
        String out = "Contents of hashtable. Asterisk indicates collision.\n Index    Count     Value(s)";
        int count;
        for (int i = 0; i < size; i++) {
            count = table[i].getSize();
            out = String.format("%s\n%s%-7d  %-7d  %s", out, count > 1 ? "*" : " ",
                                i, count, table[i].toString());
        }
        return out;
    }

    /*
      Rehash the table. This one could be nicer.
    */
    private void rehash() {
        int oldSize = this.size;
        // Increase the table by ~35%
        this.size = nextPrime((int)(this.entries * 1.35));
        LinkedList<T> newTable[] =
            (LinkedList<T>[])java.lang.reflect.Array.newInstance(LinkedList.class, this.size);
        for (int i = 0; i < this.size; newTable[i++] = new LinkedList<T>());
        for (int i = 0; i < oldSize; i++)
            for (int j = 0; j < table[i].getSize(); j++) {
                T obj = table[i].get(j);
                newTable[genHash(obj)].push(obj);
            }
        this.table = newTable;
        this.rehashes++;
    }

    private int genHash(Object obj) {
        String str = obj.toString();
        int hash = 0;

        // If the object is a Number, treat it as an int.
        // This would not perform good on floats..
        if (obj instanceof java.lang.Number) {
            // Hashing method by @Thomas Mueller
            // https://stackoverflow.com/questions/664014/what-integer-hash-function-are-good-that-accepts-an-integer-hash-key
            hash = Integer.parseInt(str);
            hash = ((hash >>> 16) ^ hash) * 0x45d9f3b;
            hash = ((hash >>> 16) ^ hash) * 0x45d9f3b;
            hash = (hash >>> 16) ^ hash;
        } else {
            // This should do for strings.
            // Chars are weighted by position, so 'foo' and 'oof' produce unique hashes.
            for (int i = 0; i < str.length(); hash += (int)str.charAt(i++) + i);
        }

        return (hash > 0 ? hash : - hash) % this.size;
    }
}

/*
  How to implement a linked list in five minutes. Let's come back to this to improve it..
*/
class LinkedList<T> {
    private Node<T> first;

    LinkedList() {
        this.first = null;
    }

    public T get(int n) {
        Node<T> node = this.first;
        if (node == null) return null;
        for (int i = 0; i != n && node != null; i++) node = node.getNext();
        return node.getObj();
    }

    public void push(T obj) {
        Node<T> newNode = new Node(obj, this.first);
        this.first = newNode;
    }

    public T pop() {
        T obj = this.first.getObj();
        this.first = this.first.getNext();
        return obj;
    }

    public int getSize() {
        Node<T> tmp = this.first;
        int size = 0;
        while (tmp != null) {
            tmp = tmp.getNext();
            size++;
        }
        return size;
    }

    public String toString() {
        int size = this.getSize();
        String objectNames = " ";
        Node<T> tmp = this.first;
        for (int i = 0; i < size; i++) {
            objectNames = String.format("%s{%s} ", objectNames, tmp.getObj().toString());
            tmp = tmp.getNext();
        }
        return objectNames;
    }

    private class Node<T> {
        private T obj;
        private Node<T> next;

        Node(T obj, Node<T> next) {
            this.obj = obj;
            this.next = next;
        }

        public Node<T> getNext() {
            return this.next;
        }

        public T getObj() {
            return obj;
        }
    }
}
