package regression;

import evaluation.Evaluation;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class RegressionModel
{
    private Classifier classifier;
    
    public RegressionModel()
    {
        classifier = null;
    }
    
    public RegressionModel(Classifier classifier)
    {
        this.classifier = classifier;
    }
    
    public Instances getFileInstances(String fileName)throws Exception
    {
        Instances m_instances = DataSource.read(fileName);
        m_instances.setClassIndex(m_instances.numAttributes() - 1);
        return m_instances;
    }
    
    public void buildClassifier(Instances train) throws Exception
    {
        classifier.buildClassifier(train);
    }
    
    public void loadModel(String modelFileName) throws Exception
    {
        classifier = (Classifier) weka.core.SerializationHelper.read(modelFileName);
        
    }
    
    public void test()
    {
        try
        {
            loadModel("data/model/RF.model");
            
            Instances test = DataSource.read("data/rank_prediction_test.arff");
            test.setClassIndex(test.numAttributes() - 1);
            
            evaluation.Evaluation eval = new Evaluation(classifier);
            eval.generateRank("data/rank_prediction.arff", "data/result.csv");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        RegressionModel rm = new RegressionModel(new RandomForest());
        rm.test();
    }
}
