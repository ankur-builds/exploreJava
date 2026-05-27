/*
DESCRIPTION
Implement an in-memory file system that supports hierarchical directory structures with operations for creating directories (mkdir),
listing contents (ls), creating/appending to files (addContentToFile), and reading file contents (readContentFromFile). The system
must handle path parsing (e.g., /a/b/c), distinguish between files and directories, and return listings in lexicographical order.
For example, calling mkdir("/a/b/c") should create nested directories, and addContentToFile("/a/b/file.txt", "hello") should create
the file with content.

Input:
mkdir("/a/b/c")
addContentToFile("/a/b/file.txt", "hello")
ls("/a/b")
readContentFromFile("/a/b/file.txt")

Output:

["c", "file.txt"]
"hello"

Explanation: Create nested directories, add a file with content, list directory contents in lexicographical order, and read the file content

Constraints:

All paths are absolute (start with /)
Path components contain only alphanumeric characters, dots, and underscores
File and directory names are case-sensitive
ls on a file path returns a list containing only that filename
ls on a directory returns all direct children in lexicographical order
mkdir creates all intermediate directories if they don't exist
addContentToFile creates the file and parent directories if they don't exist, otherwise appends content

*/
