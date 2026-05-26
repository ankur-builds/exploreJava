import java.util.*;

public class SourceMatcher {

    public static List<String> findMatches(String text, List<String> sources) {
        String lowerText = text.toLowerCase();
        Set<String> set = new HashSet<>();
        int n = lowerText.length();

        for (String source : sources) {
            String target = source.toLowerCase();
            int m = target.length();

            for (int i = 0; i <= n - m; ++i) {
                if (lowerText.startsWith(target, i)) {
                    boolean leftBoundary =
                        (i == 0) ||
                        !Character.isLetterOrDigit(lowerText.charAt(i - 1));
                    boolean rightBoundary =
                        (i + m == n) ||
                        !Character.isLetterOrDigit(lowerText.charAt(i + m));

                    if (leftBoundary && rightBoundary) {
                        set.add(target);
                        break;
                    }
                }
            }
        }

        return new ArrayList<>(set);
    }

    public static void main(String[] args) {
        // Example 1
        String text1 =
            "The company fiercely protects its intellectual property, despite internal disagreement.";
        List<String> sources1 = Arrays.asList(
            "intellectual property",
            "agree",
            "protects"
        );

        System.out.println(findMatches(text1, sources1));
        // Output: [intellectual property, protects]

        // Example 2
        String text2 = "Artificial Intelligence (AI) is evolving fast.";
        List<String> sources2 = Arrays.asList("ai", "artificial intelligence");

        System.out.println(findMatches(text2, sources2));
        // Output: [ai, artificial intelligence]
    }
}

/*
Problem Statement: The Tag/Source Matcher

Description
You are building a content moderation system.
Given a string text and an array of strings sources (which can contain both single words and multi-word phrases),
find all unique strings from sources that appear in text as an exact, case-insensitive, whole-word match.

A whole-word match means the source string must be bounded by word boundaries (e.g., spaces, punctuation, or the start/end of the text).
Partial matches within a larger word must be ignored.

Input
text (string): A block of text containing alphanumeric characters,spaces, and punctuation.
sources (List[string]): A list of target words or phrases to search for.

Output
List[string]: A list of unique tags from sources that were found in the text. The order of the output does not matter.

Constraints
1 <= text.length <= 10^5
1 <= sources.length <= 10^4
1 <= sources[i].length <= 50

Examples
Example 1:
Input:
text = "The company fiercely protects its intellectual property, despite internal disagreement."
sources = ["intellectual property", "agree", "protects"]

Output:
["intellectual property", "protects"]

Explanation:"intellectual property" is a multi-word phrase and matches exactly."protects" matches exactly.
"agree" is a partial match inside "disagreement", so it is ignored.

Example 2:
Input:
text = "Artificial Intelligence (AI) is evolving fast."
sources = ["ai", "artificial intelligence"]

Output: ["ai", "artificial intelligence"]

Explanation: Punctuation like ( and ) are treated as boundaries. Matches are case-insensitive.
*/
