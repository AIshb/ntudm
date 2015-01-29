package evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Evaluation
{
    private Classifier classifier = null;
    private Instances test;
//    private double spearman = 0.0;
    
    private ArrayList<Rank> predictedRank1 = null;
    private ArrayList<Rank> predictedRank2 = null;
    private ArrayList<Rank> actualRank1 = null;
    private ArrayList<Rank> actualRank2 = null;
    
    public Evaluation(Classifier classifier)
    {
        this.classifier = classifier;
    }
    
    /**
     * 返回两学期的spearman
     * @return
     * @throws Exception
     */
    public double[] spearman() throws Exception
    {
        if (predictedRank1 == null || actualRank1 == null
            || predictedRank2 == null || actualRank2 == null)
            throw new Exception("无法计算，需要先调用evaluateModel");
        
        double[] spearman = new double[2];
        spearman[0] = spearman(predictedRank1, actualRank1);
        spearman[1] = spearman(predictedRank2, actualRank2);
        return spearman;
    }
    
    /**
     * 返回指定学期的spearman
     * @param term
     * @return
     * @throws Exception
     */
    public double spearman(int term) throws Exception
    {
        if ((term == 1 && (predictedRank1 == null || actualRank1 == null))
            || (term == 2 && (predictedRank2 == null || actualRank2 == null)))
            throw new Exception("无法计算，需要先调用evaluateModel");
        
        double spearman;
        if (term == 1)
            spearman = spearman(predictedRank1, actualRank1);
        else
            spearman = spearman(predictedRank2, actualRank2);
        return spearman;
    }
    
    private double spearman(ArrayList<Rank> predictedRank, ArrayList<Rank> actualRank) throws Exception
    {
        if (predictedRank.size() != actualRank.size())
            throw new Exception("排名人数错误");
        
        double spearman = 0.0;
        int n = predictedRank.size();
        for (int i = 0; i < n; ++i)
        {
            int pr_rank = predictedRank.get(i).rank;
            int ac_rank = actualRank.get(i).rank;
            spearman += ((pr_rank - ac_rank) * (pr_rank - ac_rank));
        }
        
        spearman = 1 - 6.0 * spearman / (n * (n * n - 1));
        return spearman;
    }
    
    /**
     * 根据提供的测试集计算预测排名和实际排名
     * @param data
     * @throws Exception
     */
    public void evaluateModel(Instances data) throws Exception
    {
        test = data;
        getPredictedRank();
        getActualRank();
    }
    
    /**
     * 生成预测的排名
     * @param dataName: 第三学期排名,arff文件
     * @param outputName: 输出文件的名字
     * @throws Exception
     */
    public void generateRank(String dataName, String outputName) throws Exception
    {
        Instances data = DataSource.read(dataName);
        ArrayList<Rank> list = new ArrayList<>();
        HashMap<Integer, Integer> rank = new HashMap<>();
        HashMap<Integer, Integer> count = new HashMap<>();

        for (int i = 0; i < data.numInstances(); ++i)
        {
            int stu_no = (int)test.instance(i).value(1);
            int t_rank = (int)classifier.classifyInstance(data.instance(i));
            
            if (rank.containsKey(stu_no))
                rank.put(stu_no, t_rank + rank.get(stu_no));
            else
                rank.put(stu_no, t_rank);
            if (count.containsKey(stu_no))
                count.put(stu_no, 1 + count.get(stu_no));
            else
                count.put(stu_no, 1);
        }
        
        Iterator<Entry<Integer, Integer>> iter = rank.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<Integer, Integer> entry = (Entry<Integer, Integer>)iter.next();
            int stu_no = entry.getKey();
            list.add(new Rank(stu_no, entry.getValue() / count.get(stu_no)));
        }
        
        reRank(list);
        
        FileWriter fw = new FileWriter(outputName);
        fw.write("id,rank\r\n");
        for (int i = 0; i < list.size(); ++i)
        {
            Rank r = list.get(i);
            fw.write(r.stu_no + "," + r.rank + "\r\n");
        }
        fw.close();
    }
    
    /**
     * 根据训练好的分类器和测试集得到预测的排名
     * @throws Exception
     */
    private void getPredictedRank() throws Exception
    {
        predictedRank1 = new ArrayList<>();
        predictedRank2 = new ArrayList<>();
        HashMap<Integer, Integer> rank1 = new HashMap<>();
        HashMap<Integer, Integer> rank2 = new HashMap<>();
        HashMap<Integer, Integer> count1 = new HashMap<>();
        HashMap<Integer, Integer> count2 = new HashMap<>();

        for (int i = 0; i < test.numInstances(); ++i)
        {
            int t_term = (int)test.instance(i).value(0);
            int stu_no = (int)test.instance(i).value(1);
            int rank = (int)classifier.classifyInstance(test.instance(i));
            
            if (t_term == 1)
            {
                if (rank1.containsKey(stu_no))
                    rank1.put(stu_no, rank + rank1.get(stu_no));
                else
                    rank1.put(stu_no, rank);
                if (count1.containsKey(stu_no))
                    count1.put(stu_no, 1 + count1.get(stu_no));
                else
                    count1.put(stu_no, 1);
            }
            else
            {
                if (rank2.containsKey(stu_no))
                    rank2.put(stu_no, rank + rank2.get(stu_no));
                else
                    rank2.put(stu_no, rank);
                if (count2.containsKey(stu_no))
                    count2.put(stu_no, 1 + count2.get(stu_no));
                else
                    count2.put(stu_no, 1);
            }
        }
        
        Iterator<Entry<Integer, Integer>> iter = rank1.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<Integer, Integer> entry = (Entry<Integer, Integer>)iter.next();
            int stu_no = entry.getKey();
            predictedRank1.add(new Rank(stu_no, entry.getValue() / count1.get(stu_no)));
        }
        iter = rank2.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<Integer, Integer> entry = (Entry<Integer, Integer>)iter.next();
            int stu_no = entry.getKey();
            predictedRank2.add(new Rank(stu_no, entry.getValue() / count2.get(stu_no)));
        }
        
        reRank(predictedRank1);
        reRank(predictedRank2);
    }

    /**
     * 根据训练好的分类器和测试集得到预测的排名
     * @param term: 获取指定学期的排名
     * @throws Exception
     */
    private void getPredictedRank(int term) throws Exception
    {
        if (term < 1 || term > 2)
            return;
        
        if (term == 1)
            predictedRank1 = new ArrayList<>();
        else
            predictedRank2 = new ArrayList<>();
        HashMap<Integer, Integer> rank = new HashMap<>();
        HashMap<Integer, Integer> count = new HashMap<>();
        
        for (int i = 0; i < test.numInstances(); ++i)
        {
            int t_term = (int)test.instance(i).value(0);
            int stu_no = (int)test.instance(i).value(1);
            int t_rank = (int)classifier.classifyInstance(test.instance(i));
            
            if (t_term == term)
            {
                if (rank.containsKey(stu_no))
                    rank.put(stu_no, t_rank + rank.get(stu_no));
                else
                    rank.put(stu_no, t_rank);
                if (count.containsKey(stu_no))
                    count.put(stu_no, 1 + count.get(stu_no));
                else
                    count.put(stu_no, 1);
            }
        }
        
        Iterator<Entry<Integer, Integer>> iter = rank.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<Integer, Integer> entry = (Entry<Integer, Integer>)iter.next();
            int stu_no = entry.getKey();
            if (term == 1)
                predictedRank1.add(new Rank(stu_no, entry.getValue() / count.get(stu_no)));
            else
                predictedRank2.add(new Rank(stu_no, entry.getValue() / count.get(stu_no)));
        }
        
        if (term == 1)
            reRank(predictedRank1);
        else
            reRank(predictedRank2);
    }
    
    private void reRank(ArrayList<Rank> rank)
    {
        sortByRank(rank);
        for (int i = 0; i < rank.size(); ++i)
            rank.get(i).rank = i + 1;
        sortByStu_no(rank);
    }
    
    /**
     * 获取测试集实际排名，需要保证有data/测试/成绩.txt文件
     * @throws Exception
     */
    private void getActualRank() throws Exception
    {
        actualRank1 = new ArrayList<>();
        actualRank2 = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader("data/测试/成绩.txt"));
        reader.readLine();
        
        String line;
        while ((line = reader.readLine()) != null)
        {
            String[] words = line.trim().split("\t");
            int t = Integer.parseInt(words[0]);
            if (t == 1)
                actualRank1.add(new Rank(Integer.parseInt(words[1]), Integer.parseInt(words[2])));
            else if (t == 2)
                actualRank2.add(new Rank(Integer.parseInt(words[1]), Integer.parseInt(words[2])));
        }
        reader.close();
        
        sortByStu_no(actualRank1);
        sortByStu_no(actualRank2);
    }
 
    /**
     * 获取测试集实际排名，需要保证有data/测试/成绩.txt文件
     * @param term: 获取指定学期的排名
     * @throws Exception
     */
    private void getActualRank(int term) throws Exception
    {
        if (term < 1 || term > 2)
            throw new Exception("term error");
        
        if (term == 1)
            actualRank1 = new ArrayList<>();
        else
            actualRank2 = new ArrayList<>();
        
        BufferedReader reader = new BufferedReader(new FileReader("data/测试/成绩.txt"));
        reader.readLine();
        
        String line;
        while ((line = reader.readLine()) != null)
        {
            String[] words = line.trim().split("\t");
            int t = Integer.parseInt(words[0]);
            if (t == term && t == 1)
                actualRank1.add(new Rank(Integer.parseInt(words[1]), Integer.parseInt(words[2])));
            else if (t == term && t == 2)
                actualRank2.add(new Rank(Integer.parseInt(words[1]), Integer.parseInt(words[2])));
        }
        reader.close();
        
        if (term == 1)
            sortByStu_no(actualRank1);
        else
            sortByStu_no(actualRank2);
    }
    
    private void sortByStu_no(ArrayList<Rank> list)
    {
        Comparator<Rank> comp = new Comparator<Rank>() {
            
            @Override
            public int compare(Rank o1, Rank o2)
            {
                return o1.stu_no - o2.stu_no;
            }
        };
        
        Collections.sort(list, comp);
    }
    
    private void sortByRank(ArrayList<Rank> list)
    {
        Comparator<Rank> comp = new Comparator<Rank>() {
            
            @Override
            public int compare(Rank o1, Rank o2)
            {
                return o1.rank - o2.rank;
            }
        };
        
        Collections.sort(list, comp);
    }
    
    // 测试用，忽略
    public void test() throws Exception
    {
        try
        {
            getPredictedRank();
            for (Rank rank : predictedRank1)
                System.out.print("{" + rank.stu_no + ", " + rank.rank + "} ");
            System.out.println();
            
            getActualRank();
            for (Rank rank : actualRank1)
                System.out.print("{" + rank.stu_no + ", " + rank.rank + "} ");
            System.out.println();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        try
        {
//            ArrayList<Rank> rank = Evaluation.getActualRank();
//            System.out.println(rank.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
