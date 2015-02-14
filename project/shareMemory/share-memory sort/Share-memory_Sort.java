import java.io.BufferedReader;
import java.util.Iterator;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

final public class Sort {

    private static Sort instance = null;
    private int numberOfWords = 0;
    private String[] words;

    public static void main(String[] args) {
        
        if (args.length != 3) {
            System.out.println("Wrong number of arguments!");
            return;
        }

        int threadCount;
        try {
            threadCount = Integer.parseInt(args[0]);

            if (threadCount < 1) {
                System.out.println("You must specify minimum 1 thread for sorting!");
                return;
            }
        }
        catch (NumberFormatException e) {
            System.out.println(args[0] + " should be a number representing "
                    + "amount of threads to be used for sorting!");
            return;
        }

        Sort sort = Sort.getInstance();
        Date start = new Date(); // For å beregne tidsforbruk

        if (!sort.getWordsFromInput(args[1])) { // Les inn fra fil
            System.out.println("Could not retrieve words from " + args[1] + "!");
            return;
        }

        if (!sort.sortWords(threadCount)) { // Sorter med threadCount antall tråder
            System.out.println("Something went wrong when sorting the words!");
            return;
        }

        if (!sort.writeWordsToOutput(args[2])) { // Skriv resultat til fil
            System.out.println("Could not write sorted words to " + args[2]);
            return;
        }

        Date end = new Date(); // Beregn tidsforbruk
        long timeToComplete = end.getTime() - start.getTime();

        System.out.println();
        System.out.println("Using " + threadCount + " threads, " + sort.getWords().length + " words was sorted in "
                + timeToComplete + " milliseconds.");
    }

    private Sort() {
    } // Gjør det umulig å opprette ett nyt Sort objekt utenfor denne klassen

    private static Sort getInstance() {
        if (instance == null) {
            instance = new Sort();
        }

        return instance;
    } // Returner singleton objekt

    public boolean getWordsFromInput(String inputFile) {
        System.out.print("Loading contents of " + inputFile + "... ");
        Date start = new Date();

        StringBuilder firstLine = new StringBuilder(); // Første linja, som inneholder antall ord
        StringBuilder lines = new StringBuilder(); // Ordene for sortering

        try {
            BufferedReader input = new BufferedReader(new FileReader(inputFile));
            boolean readFirstLine = false; // Har vi lest første linje?

            for (int charByte = input.read(); charByte >= 0; charByte = input.read()) {
                char readChar = (char) charByte;

                if (readChar == '\r') { // ignorer \r tegn
                }
                else if (readFirstLine) { // Vi har lest første linje
                    lines.append(readChar);
                }
                else { // Vi har ikke lest første linje, les frem til linjeskift
                    if (readChar == '\n') {
                        readFirstLine = true;
                        continue;
                    }

                    firstLine.append(readChar);
                }
            }

            input.close();
        }
        catch (IOException e) {
            return false;
        }

        words = lines.toString().split("\n");

        try {
            numberOfWords = Integer.parseInt(firstLine.toString());
        }
        catch (NumberFormatException e) {
            return false;
        }

        Date end = new Date();
        long timeDiff = end.getTime() - start.getTime();

        System.out.println(timeDiff + "ms");

        return true;
    }

    public boolean writeWordsToOutput(String outputFile) {
        System.out.print("Writing results to " + outputFile + "... ");
        Date start = new Date();

        if (words.length != numberOfWords) { // Sjekker om vi har sortert riktig antall ord
            System.out.println("Sorted list does not contain expected number of words!");
            return false;
        }

        try {
            FileWriter output = new FileWriter(outputFile);

            for (int i = 0; i < words.length; i++) {
                String outputWord = (i == words.length - 1) ? words[i] : words[i] + "\n";
                output.write(outputWord);
            }

            output.close();
        }
        catch (IOException e) {
            return false;
        }

        Date end = new Date();
        long timeDiff = end.getTime() - start.getTime();
        System.out.println(timeDiff + "ms");

        return true;
    }

    public boolean sortWords(int threadCount) {
        System.out.print("Sorting... ");
        Date start = new Date();

        LinkedList<WordHandler> wordHandlers = new LinkedList<WordHandler>();

        initSortThreads(threadCount, wordHandlers); // Start sortering
        boolean sortResult = interleaveThreads(wordHandlers); // Flett sammen resultat

        Date end = new Date();
        long timeDiff = end.getTime() - start.getTime();
        System.out.println(timeDiff + "ms");

        return sortResult;
    }

    private void initSortThreads(int threadCount, LinkedList<WordHandler> wordHandlers) {
        int wordsPerThread = words.length / threadCount;
        int additionalWordsPerThread = words.length % threadCount;

        int currentOffset = 0;

        for (int i = 0; i < threadCount; i++) {
            int wordsForThread = wordsPerThread;

            if (additionalWordsPerThread > 0) {
                wordsForThread++;
                additionalWordsPerThread--;
            }

            WordSorter sorter = new WordSorter(words, currentOffset, currentOffset + wordsForThread);
            wordHandlers.add(sorter);

            currentOffset += wordsForThread;
        }
    }

    private boolean interleaveThreads(LinkedList<WordHandler> wordHandlers) {
        WordHandler buffer = null;

        while (wordHandlers.size() > 0) {
            try {
                wordHandlers.peek().join();

                if (buffer == null && wordHandlers.size() == 1) {
                    words = wordHandlers.poll().getWords();
                }
                else if (buffer == null) {
                    buffer = wordHandlers.poll();
                }
                else {
                    Interleaver merge = new Interleaver(buffer.getWords(), wordHandlers.poll().getWords());
                    wordHandlers.add(merge);
                    buffer = null;
                }
            }
            catch (InterruptedException e) {
                System.out.println("Main sort thread was interupted!");
                return false;
            }
        }

        return true;
    }

    public String[] getWords() {
        return words;
    }
}

public class Interleaver extends Thread implements WordHandler {

    private String[] list1 = null;
    private String[] list2 = null;
    private String[] sorted = null;


    public Interleaver(String[] list1, String[] list2) {
        this.list1 = list1;
        this.list2 = list2;

        start();
    }


    @Override
    public void run() {
        sorted = new String[list1.length + list2.length];
        int list1Pos = 0;
        int list2Pos = 0;

        for (int i = 0; i < sorted.length; i++) {
            if (list1Pos >= list1.length) {
                sorted[i] = list2[list2Pos++];
            } else if (list2Pos >= list2.length) {
                sorted[i] = list1[list1Pos++];
            } else if (list1[list1Pos].compareTo(list2[list2Pos]) < 0) {
                sorted[i] = list1[list1Pos++];
            } else {
                sorted[i] = list2[list2Pos++];
            }
        }
    }


    public String[] getWords() {
        return sorted;
    }
}


public class SortedStringsList implements Iterable<String> {

    private Node head = new Node(null);
    private Node middle = head;
    private int size = 0;

    public void add(String string) {
        if (middle.value != null && middle.value.compareTo(string) < 0) {
            addToList(middle, string); // Start insetting fra midten
        }
        else {
            addToList(head, string); // Start innsetting fra start
        }
    }

    private void addToList(Node start, String string) {
        Node current = start;
        Node newEntry = new Node(string);

        int middleIndex = size / 2; // Indexen til den midterse verdien i lista

        for (int i = 0;; i++, current = current.next) {
            if (current.next == null) {
                current.next = newEntry;
                break;
            }
            else if (current.next.value.compareTo(string) > 0) {
                newEntry.next = current.next;
                current.next = newEntry;
                break;
            }

            if (i == middleIndex) { // Oppdater midterste verdi, hvis vi er i midten av lista
                this.middle = current;
            }
        }

        size++;
    }

    public String[] toArray() {
        int i = 0;
        String[] array = new String[size];

        for (String string : this) {
            array[i++] = string;
        }

        return array;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iter();
    }

    private class Node {

        public Node next = null;
        public String value = null;

        public Node(String value) {
            this.value = value;
        }
    }

    private class Iter implements Iterator<String> {

        private Node current = head;

        @Override
        public boolean hasNext() {
            return current.next != null;
        }

        @Override
        public String next() {
            current = current.next;
            return current.value;
        }

        @Override
        public void remove() {
            // throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
            throw new RuntimeException(“does not implement”);
        }
    }
}

public interface WordHandler {
    public void join() throws InterruptedException;
    public String[] getWords();
}

public class WordSorter extends Thread implements WordHandler {

    private String[] sorted = null; // Den etterhvert sorterte arrayen
    private String[] sourceStrings = null; // Kildearrayet som vi henter ordene våre fra
    private int start = 0; // Settes i konstruktør, startindex i kildearrayet
    private int end = 0; // Siste index i kildearrayet


    public WordSorter(String[] sourceList, int start, int end) {
        this.sourceStrings = sourceList;
        this.start = start;
        this.end = end;

        start();
    }


    @Override
    public void run() {
        SortedStringsList sorter = new SortedStringsList();

        for (int i = start; i < end; i++) {
            sorter.add(sourceStrings[i]);
        }

        sorted = sorter.toArray();
    }


    public String[] getWords() {
        return sorted;
    }
}

