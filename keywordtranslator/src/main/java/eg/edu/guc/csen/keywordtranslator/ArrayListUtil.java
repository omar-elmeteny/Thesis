package eg.edu.guc.csen.keywordtranslator;

import java.util.ArrayList;

public class ArrayListUtil {
    public static <T extends Comparable<T>> int binarySearch(ArrayList<T> list, T item) {
        
        int low = 0;
        int high = list.size() - 1;
        int mid = 0;
        while (low <= high) {
            mid = (low + high) / 2;
            int comparison = list.get(mid).compareTo(item);
            if (comparison < 0) {
                low = mid + 1;
            } else if (comparison > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return ~low;
    }

    public static <T extends Comparable<T>> void insertIfNotExists(ArrayList<T> list, T item) {
        int index = binarySearch(list, item);
        if (index < 0) {
            list.add(~index, item);
        }
    }
}
