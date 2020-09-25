import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

public class Oppg4 {
    public static void main(String... args) {

        // Read file into array
        Person[] people = readPeopleFromFile(System.getProperty("user.dir") + "/names");

        // Create
        MyHashTable<Person> table = new MyHashTable<>(people.length);
        for (Person person : people) table.add(person);
        Optional<Person> joakim = table.search("Langvand, Joakim Skogø");
        Optional<Person> stine = table.search("Reppe, Stine");
        joakim.isPresent();
        stine.toString();
        System.out.println("Joakim is " + (joakim.isPresent() ? "present" : "not found"));
        System.out.println("Stine is " + (stine.isPresent() ? "present" : "not found"));
        System.out.println("HashTable load: " + table.getLoad());
        System.out.println(table.toString());
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
                String data = scanner.nextLine();
                people[index++] = new Person(data.split(",")[0], data.split(",")[1]);
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
    private float loadFactor;
    private LinkedList<T>[] table;

    MyHashTable() {
        this.size = 13;
        this.entries = 0;
        this.loadFactor = 0.8f;
        this.table = (LinkedList<T>[])java.lang.reflect.Array.newInstance(LinkedList.class, this.size);
        for (int i = 0; i < this.size; i++) table[i] = new LinkedList<T>();
    }

    /*
      Search for an object in the table. Returns the object if present.
     */
    public Optional<T> search(Object cmp) {
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
    public float getLoad() {
        return (float)this.entries / (float)this.size;
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
        if (getLoad() > loadFactor) {
            System.out.println("Entries: " + this.entries + ", Size: " + this.size + ", Load: " + getLoad());
            rehash();
            System.out.println("Rehashed");
        }
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
        this.size = nextPrime((int)(this.entries * 1.4));
        LinkedList<T> newTable[] =
            (LinkedList<T>[])java.lang.reflect.Array.newInstance(LinkedList.class, this.size);
        for (int i = 0; i < this.size; i++) newTable[i] = new LinkedList<T>();
        for (int i = 0; i < oldSize; i++)
            for (int j = 0; j < table[i].getSize(); j++) {
                T obj = table[i].get(j);
                newTable[genHash(obj)].push(obj);
            }
        this.table = newTable;
    }

    private int genHash(Object obj) {
        String str = obj.toString();
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash += (i + 1) * (int)str.charAt(i);
        }
        return hash % this.size;
    }
}

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
