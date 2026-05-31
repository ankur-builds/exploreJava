import java.util.*;

class WordSearch {

    class TrieNode {

        TrieNode[] children = new TrieNode[26];
        String word; // stores complete word at terminal node
    }

    private TrieNode root = new TrieNode();
    private List<String> result = new ArrayList<>();

    private void insert(String word) {
        TrieNode curr = root;

        for (char ch : word.toCharArray()) {
            int idx = ch - 'a';

            if (curr.children[idx] == null) {
                curr.children[idx] = new TrieNode();
            }

            curr = curr.children[idx];
        }

        curr.word = word;
    }

    public List<String> findWords(char[][] board, String[] words) {
        // Build Trie
        for (String word : words) {
            insert(word);
        }

        int rows = board.length;
        int cols = board[0].length;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                dfs(board, r, c, root);
            }
        }

        return result;
    }

    private void dfs(char[][] board, int row, int col, TrieNode node) {
        if (
            row < 0 || row >= board.length || col < 0 || col >= board[0].length
        ) {
            return;
        }

        char ch = board[row][col];

        if (ch == '#') {
            return;
        }

        TrieNode next = node.children[ch - 'a'];

        if (next == null) {
            return; // Trie pruning
        }

        // Found a complete word
        if (next.word != null) {
            result.add(next.word);

            // Prevent duplicates
            next.word = null;
        }

        board[row][col] = '#';

        dfs(board, row + 1, col, next);
        dfs(board, row - 1, col, next);
        dfs(board, row, col + 1, next);
        dfs(board, row, col - 1, next);

        board[row][col] = ch;
    }
}
