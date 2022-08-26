# Overview
The goal of the project was to learn about the use and function of different data structures, as well as the process of software development as a whole. 

**Stage 1**: HashTable. Basic storage of documents in memory using arrays and hashing.

**Stage 2**: Stack. Undo functionality, i.e. a command such as “create” or “delete” can be undone and popped from the undo stack. Also introduced Lambda expressions.

**Stage 3**: Trie. Basic search using the trie to store the words of all the documents and the set of documents in which those words appear.

**Stage 4**: Heap. Memory management, where the heap was designed to have available the least-used document (i.e. a min-heap) and if a threshold of bytes or documents was reached, the least used document would be wiped.

**Stage 5**: BTree. Persistence using the BTree to manage the location of the documents on disk being stored in JSON format, which were retrieved and brought into memory when called.

All of these 5 stages can be seen independently, as they were handed in in subsequent stages.

NOTE: The original repo used for this project was private. Therefore, when this code was copied over, none of the edit history persisted. 
