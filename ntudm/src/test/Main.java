package test;

import java.io.BufferedReader;
import java.io.FileReader;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader("data/测试/成绩.txt"));
            reader.readLine();
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                    break;
                
                String[] words = line.trim().split("\t");
                for  (String word : words)
                {
                    System.out.print(word + "\t");
                }
                System.out.println();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
