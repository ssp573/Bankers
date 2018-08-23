# Bankers
Java Implementation of the Banker's algorithm for resource management. Class Banker is the resource manager which has FIFO and Banker's algorithm functions as well as other helper functions. The class Task is for individual tasks which are processed by the resource manager. The third file is SortByID.java. It is implementing Comparator to help compare ArrayLists of Tasks by Task ID to help sort the tasks whenever needed. To run follow the following commands-


compile:

javac Banker.java

run:

java Banker <filename>
  
(Note that the file must be in the current working directory. If not, give full
path.)
