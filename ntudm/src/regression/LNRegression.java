package regression;

import evaluation.Evaluation;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class LNRegression
{
    private Classifier linearRegression = new LinearRegression();
    
	/**
	 * 加载数据集
	 * @param fileName 数据集名
	 * @return 样例
	 * @throws Exception 异常
	 */
	public Instances getFileInstances(String fileName)throws Exception
	{
		Instances m_instances = DataSource.read(fileName);
		m_instances.setClassIndex(m_instances.numAttributes() - 1);
		return m_instances;
	}
	
	public void buildClassifier(Instances train) throws Exception
	{
	    linearRegression.buildClassifier(train);
	}
	
	public void test()
	{
	    try
	    {
    	    Instances train = getFileInstances("data/rank_prediction_train.arff");
    	    buildClassifier(train);
    	    
    	    Instances test = DataSource.read("data/rank_prediction_test.arff");
    	    evaluation.Evaluation eval = new Evaluation(linearRegression, test);
//    	    eval.test();
    	    eval.generateRank("data/rank_prediction.arff", "data/result.csv");
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}
	
	public static void main(String[] args)
	{
	    LNRegression r = new LNRegression();
	    r.test();
	}
}
