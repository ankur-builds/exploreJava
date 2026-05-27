public class MinimumCommonValue {

    /*
        INTUITION

        We keep one pointer for each sorted array.

        At every step:
        - look at current values from all arrays
        - find MIN value
        - find MAX value

        CASE 1:
        If MIN == MAX

            then all arrays currently point
            to the same number.

            Since all are equal,
            we found the smallest common number.

        CASE 2:
        If MIN != MAX

            then arrays having MIN value
            are "behind".

            Since arrays are sorted,
            smaller values can NEVER become common
            unless they move forward.

            So:
            advance all pointers whose value == MIN

        Why does this work?

            Sorted arrays only move forward.

            Therefore:
            smaller values must catch up
            to larger values.

        Stop Conditions:

            1. All equal -> answer found
            2. Any pointer reaches end
               -> no common number exists
    */
    public static int minimumCommonValue(int[][] arrays) {
        int len = arrays.length;
        int[] ptr = new int[len];
        while (true) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            for (int i = 0; i < len; ++i) {
                if (ptr[i] == arrays[i].length) return -1;

                int val = arrays[i][ptr[i]];

                min = Math.min(min, val);
                max = Math.max(max, val);
            }

            if (min == max) return min;

            for (int i = 0; i < len; ++i) {
                if (arrays[i][ptr[i]] == min) ptr[i]++;
            }
        }
    }

    public static void main(String[] args) {
        int[][] arrays = {
            { 1, 2, 3, 4, 5, 10, 20 },
            { 6, 7, 10, 12, 13, 14, 20 },
            { 0, 8, 9, 10, 15, 20, 25 },
        };
        System.out.println(MinimumCommonValue.minimumCommonValue(arrays));
    }
}
