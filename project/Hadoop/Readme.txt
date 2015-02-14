Hadoop_Configuration_File

Contains:
16Nodes -- Master configuration file and Slave1 configuration file
           (Use Slave1 as an example for Slaves').
SingleNode -- Master configuration file and Slave configuration file.
===================================================================================
Hadoop_Jar_File

Contains:
sort.jar -- Hadoop sort code.

Input:  input folder which contains the input file.
Output: output folder that the output file will output in.

1. In the first, you should use teragen.jar to generate input file
   and put it into the input folder.
2. In the second, use sort.jar to sort the input file.
3. Do not create input and output folder in advance.

Generate test file: hadoop jar hadoop/hadoop-examples-*.jar teragen 100000000 input

Command: hadoop jar sort.jar input output

-----------------------------------------------------------------------------------

wordcountMR.jar -- Hadoop wordcount code.

Input:  input folder which contains the input file.
Output: output folder that the output file will output in.

Command: hadoop jar wordcount.jar input output

===================================================================================
Hadoop_Source_Code

sort -- Hadoop sort source code. 
wordcountMR -- Hadoop wordcount source code.


