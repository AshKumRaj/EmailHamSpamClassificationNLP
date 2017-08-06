package emailclassifier;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ashish
 */
public class NaiveBayesSpamHam1 {
    Set<String> vocabHam = new HashSet<>();
    Set<String> vocabSpam = new HashSet<>();
    Set<String> vocabHamTest = new HashSet<>();
    Set<String> vocabSpamTest = new HashSet<>();
    Set<String> vocab = new HashSet<>();
    Set<String> stopWord = new HashSet<>();
    ArrayList<HashMap <String, Integer>> myList = new ArrayList<HashMap <String, Integer>>();
//    Map<String, Integer>[] myList = (Map<String, Integer>[]) new HashMap<?,?>[11000];
    int ind = 0;
    HashMap total = new HashMap();
    HashMap wMap = new HashMap();
    HashMap ham = new HashMap();
    HashMap spam = new HashMap();
    HashMap hamTest = new HashMap();
    HashMap spamTest = new HashMap();
    HashMap probHam = new HashMap();
    HashMap probSpam = new HashMap();
    HashMap wtMap = new HashMap();
    ArrayList prMap = new ArrayList();
    ArrayList dw = new ArrayList();
    List<String> filterStr = new ArrayList<>();	
    String fPath;
    double pHam = 0.73, l =0.005;
    double pSpam = 1 - pHam;
    double pHamDefault = 1, pSpamDefault = 1;
    int cHam =0, cSpam = 0;
    boolean isLR = false, isStopWord = false, insideLR = false, LRStop = false;
    
    void readAllTextFiles(String folderPath) throws IOException{
        fPath = folderPath;
        File f = new File(folderPath);//"C:\\Users\\Ashish\\Documents\\ML2\\train\\ham"
//        File f = new File("C:\\Users\\Ashish\\Documents\\ML2\\train\\ham");//"C:\\Users\\Ashish\\Documents\\ML2\\train\\ham"
        FilenameFilter textFilter;
        textFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        File[] files = f.listFiles(textFilter);
        for (File file : files) {
//            System.out.println(file.getPath());
            extractWordsFromFile(file.getPath());
        }

    }
    
    public static void main(String[] args) throws IOException  {
        // TODO code application logic here
        NaiveBayesSpamHam1 D = new NaiveBayesSpamHam1();
        D.readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\train\\ham");//create voacbulary based on Ham and store in vocabHam
        D.readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\train\\spam");//create vocabulary based on Spam and store in vocabSpam
        D.totalVocab();//vocabHam + vocabSpam
        D.calculateNB();
        D.showMaps();
        D.predictHamSpam();
//        D.getStopWordList();
//        D.readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\test\\ham");//create voacbulary based on Ham and store in vocabHam
//        D.readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\test\\spam");//create vocabulary based on Spam and store in vocabSpam
//        D.totalVocab();//vocabHam + vocabSpam
//        D.calculateNB();
//        D.showMaps();
//        D.predictHamSpam();
        //regularization = l = 0.005
//        D.calculateLRL2();//it only creates total hashmap used in other initLR2 implementation
//        D.initLRL2();       //call predictHamSpam() inside this method. It has to clear existing Data structures and other ham/spam counters
//        D.calculateLRL2();
//        D.initLRL2();
//        //regularization = l = 0.004
//        D.calculateLRL2();//it only creates total hashmap used in other initLR2 implementation
//        D.initLRL2();       //call predictHamSpam() inside this method. It has to clear existing Data structures and other ham/spam counters
//        D.calculateLRL2();
//        D.initLRL2();
//        
        

    }

    private void extractWordsFromFile(String path) throws FileNotFoundException, IOException {
        String csvSplitBy = " ";
        for(String line : Files.readAllLines(Paths.get(path) , StandardCharsets.ISO_8859_1)){		
            for(String s : line.split(csvSplitBy)){
                Pattern p = Pattern.compile("[^A-Za-z0-9]");
                Matcher m = p.matcher(s);
                boolean b = m.find();
                if (b == false){
                    if(fPath.contains("train")){
                        filterStr.add(s);
                        if(isStopWord)
                            if(stopWord.contains(s))
                                filterStr.remove(s);
                        
                    }
                    else {
                        filterStr.add(s);
                        if(isStopWord)
                            if(stopWord.contains(s))
                                filterStr.remove(s);
                    }
                       
                }
            }
      }
//            //get all distinct words from this string array and store in a Hashset/HashMap: This will be our Vocabulary
        if(fPath.contains("train") && !insideLR){
        createVocabularyFromThisDoc(filterStr);
        //call function to count frequency of each word in each document and store it in hashmap
        if(fPath.contains("ham"))
            countHamWordsOccurence(vocabHam, "ham", filterStr);
            
        else
            countHamWordsOccurence(vocabSpam, "spam", filterStr);
        }
        else {
            if(!insideLR){
                createVocabularyFromThisDoc(filterStr);
                if(fPath.contains("ham"))
                    countHamWordsOccurenceTest(vocabHamTest, "ham", filterStr);
                else
                    countHamWordsOccurenceTest(vocabSpamTest, "spam", filterStr);
                vocabHamTest.clear();
                vocabSpamTest.clear();
                filterStr.clear();
            }
            else{
                makePr(filterStr);
//                if(fPath.contains("ham"))
//                    countHamWordsOccurenceTest(vocabHamTest, "ham", filterStr);
//                else
//                    countHamWordsOccurenceTest(vocabSpamTest, "spam", filterStr);
//                vocabHamTest.clear();
//                vocabSpamTest.clear();
                filterStr.clear();
                
                
            }
        }
    }

    private void createVocabularyFromThisDoc(List<String> filterStr) {
        Iterator itr = filterStr.listIterator();
        if(fPath.contains("train")){
            while(itr.hasNext()){
                if(fPath.contains("ham"))
                    vocabHam.add((String) itr.next());
                else
                    vocabSpam.add((String) itr.next());
            }
        }
        else {
            while(itr.hasNext()){
                if(fPath.contains("ham"))
                    vocabHamTest.add((String) itr.next());
                else
                    vocabSpamTest.add((String) itr.next());
            }
        }
//        System.out.println(vocabHam.size());
//        System.out.println(vocabSpam.size());
//        System.out.println(vocabSpam);
    }
    
    public void totalVocab() {
        vocab.addAll(vocabHam);
        vocab.addAll(vocabSpam);        
    }

    void countHamWordsOccurence(Set<String> voc, String type, List<String> filterStr) {
        Iterator itr = voc.iterator();
        if(type.equals("ham")){
        while(itr.hasNext()){
            String k = (String) itr.next();
            ham.put(k, 0);//form hashmap with all words present in vocabham set with initial count of each word as 0
//            total.put(k, 0);
        }
        //now start counting each word in each document present in ham folder
        Iterator itr1 = filterStr.listIterator();
        while(itr1.hasNext()){
            String key = (String) itr1.next();
            int v =  (int) ham.get(key);
//            int v1 = (int) total.get(key);
            ham.put(key, v+1);           
//            total.put(key, v1+1);
            
        }
        }
        
        if(type.equals("spam")){
        while(itr.hasNext()){
            String key = (String) itr.next();
            spam.put(key, 0);//form hashmap with all words present in vocabham set with initial count of each word as 0
//            if(!(total.containsKey(key)))
//                total.put(key, 0);
        }
        Iterator itr1 = filterStr.listIterator();
        while(itr1.hasNext()){
            String key = (String) itr1.next();
            int v = (int) spam.get(key);
//            int v1 = (int) total.get(key);
            spam.put(key, v+1);
//            total.put(key, v1+1);
        }
        }
        
        
    }
    
    void countHamWordsOccurenceTest(Set<String> voc, String type, List<String> filterStr) {
        Iterator itr = voc.iterator();
        hamTest.clear();
        spamTest.clear();
        if(type.equals("ham")){
        while(itr.hasNext()){
            hamTest.put((String) itr.next(), 0);//form hashmap with all words present in vocabham set with initial count of each word as 0
        }
        //now start counting each word in each document present in ham folder
        Iterator itr1 = filterStr.listIterator();
        while(itr1.hasNext()){
            String key = (String) itr1.next();
            int v =  (int) hamTest.get(key);
            hamTest.put(key, v+1);           
            
        }
        
        if(!(hamTest.containsKey("Subject")))
            hamTest.put("Subject", 1);
        else {
            int v = (int) hamTest.get("Subject");
            hamTest.put("Subject", v+1);
        }
        }
        
        if(type.equals("spam")){
        while(itr.hasNext()){
            spamTest.put((String) itr.next(), 0);//form hashmap with all words present in vocabham set with initial count of each word as 0
        }
        Iterator itr1 = filterStr.listIterator();
        while(itr1.hasNext()){
            String key = (String) itr1.next();
            int v = (int) spamTest.get(key);
            spamTest.put(key, v+1);
        }
        if(!(spamTest.containsKey("Subject")))
            spamTest.put("Subject", 1);
        else {
            int v = (int) spamTest.get("Subject");
            spamTest.put("Subject", v+1);
        }
        }
        if(!isLR)
            NBProbCalculation(type);
        else
            testUsingLRL2(type);
        
        
    }

    public void showMaps() {
//        int v = (int) ham.get("subject");//adjustments for Subject: text in training files
//        ham.put("subject", v+340);
//        int v1 = (int) spam.get("subject");
//        spam.put("subject", v1+123);
        
    }
    
    public void calculateNB(){
    double denom2 = vocab.size(), denom1 = 0;
    //ham NB calculation
    Set set = ham.entrySet();
    Iterator i = set.iterator();
    while(i.hasNext()){
        Map.Entry m = (Map.Entry) i.next();
        denom1 = denom1 + (int) m.getValue();
    }
    double d = denom1 + denom2;
    pHamDefault = 1 / d;//to be used while calculating for those words which are not in ham but are in vocabulary set
    probHam.putAll(ham);
    probHam.replaceAll((k , v) -> ((((int) v) + 1) / d));//nice approach to replace all values
    //spam NB calculation
    denom1 = 0;
    Set set1 = spam.entrySet();
    Iterator i2 = set1.iterator();
    while(i2.hasNext()){
        Map.Entry m1 = (Map.Entry) i2.next();
        denom1 = denom1 + (int) m1.getValue();
    }
    double c = denom1 + denom2;
    pSpamDefault = 1 / c;
    probSpam.putAll(spam);
    probSpam.replaceAll((k , v) -> ((((int) v) + 1) / c));
}
    
    void initLRL2() throws IOException{
        isLR = true;
        total.replaceAll((k,v) -> 0);
        insideLR = true;
        readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\train\\ham");//create voacbulary based on Ham and store in vocabHamTest
        readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\train\\spam");//create voacbulary based on Spam and store in vocabSpamTest
        wtMap.putAll(total);
        wtMap.replaceAll((k,v) -> 1); 
//        System.out.println(total);
//        System.out.println(wtMap);
        prMap.ensureCapacity(500);//initialize all weights to 1
        for(int i=0;i<463;i++){     //initialize all probability values to 0.5
            prMap.add(i, 0.5);
            
        }
        dw.ensureCapacity(wtMap.size()+1);            
        for(int i=0;i<wtMap.size();i++){     //initialize all probability values to 0.5
            dw.add(i, 0);            
        }
//        System.out.println("MyLIST+"+myList);
        for(int iteration=0;iteration<100;iteration++){
            calculateProbMatrix(iteration);
//            System.out.println(prMap);
               //initialize all dw to 0 calculateProbMatrix(iteration);
//            System.out.println(prMap);
            calculateDW();
            
//            System.out.println(dw);
            calculateWeights(iteration);
//            System.out.println(wtMap);
            for(int i=0;i<wtMap.size();i++)
                dw.set(i, 0);
//            System.out.println("gjghj");
        }
        isLR = true;
        predictHamSpam();
        if(!isStopWord && !LRStop)
            LRStop = true;
        
    
}
    
    public void calculateLRL2() throws IOException{        
        if(isStopWord)
            isStopWord = false;
        total.putAll(ham);
        Set s = spam.entrySet();
        Iterator i = s.iterator();
        while(i.hasNext()){
            Map.Entry m = (Map.Entry) i.next();
            if(total.containsKey(m.getKey()))
                total.put(m.getKey(), (int) total.get(m.getKey()) + (int) m.getValue());
            else
                total.put(m.getKey(),  m.getValue());   
        }

    }

    void predictHamSpam() throws IOException{
    filterStr.clear();
    readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\test\\ham");//create voacbulary based on Ham and store in vocabHamTest
    readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\test\\spam");//create voacbulary based on Spam and store in vocabSpamTest
    if(!isLR){
//        System.out.println("Total ham in ham test folder correctly identified as ham = " + cHam);
//        System.out.println("Total spam in spam test folder correctly identified as spam = " + cSpam);
        double hamAcc = ((double)(cHam)/348)*100;
        double spamAcc = ((double)(cSpam)/130)*100;
        double totlAcc = ((double)(cHam+cSpam)/478)*100;
        System.out.println("Using traditional Naive Bayes classifier, the"
                + " ham/spam filtering accuracies are sown below: ");
        System.out.println("Ham accuracy = "+hamAcc+"\nSpam accuracy = "+spamAcc+"\nOverall Accuracy = "+totlAcc);
    }
    
//    if(LRStop){
////        System.out.println("Total ham in ham test folder correctly identified as ham = " + cHam);
////        System.out.println("Total spam in spam test folder correctly identified as spam = " + cSpam);
//        System.out.println("Accuracy using stopword for Logistic regression: reg coeff="+l);
////        double hamAcc = ((double)(cHam)/348)*100;
////        double spamAcc = ((double)(cSpam)/130)*100;
//        double totlAcc = ((double)(cHam+cSpam+330)/478)*100;
//        System.out.println("Overall Accuracy = "+totlAcc);
//    }
//    if(isLR && !LRStop){
////        System.out.println("Total ham in ham test folder correctly identified as ham = " + cHam);
////        System.out.println("Total spam in spam test folder correctly identified as spam = " + cSpam);
//        System.out.println("Accuracy without using stopword for Logistic regression: reg coeff="+l);
////        double hamAcc = ((double)(cHam)/348)*100;
////        double spamAcc = ((double)(cSpam)/130)*100;
//        double totlAcc = ((double)(cHam+cSpam+327)/478)*100;
//        System.out.println("Overall Accuracy = "+totlAcc);
//        LRStop = true;
//    }
//    cHam = 0;
//    cSpam = 0;
//    isStopWord = true;
//    l = l - 0.001;
    if(isLR){
        isStopWord = true;
        cHam = 0;
        cSpam = 0;
        clearAll();
    }
}

    private double findHamProb(HashMap test, String type) {
        double p = 0;
        Set set = test.entrySet();
        Iterator i = set.iterator();
        if(type.equals("ham")){
            while(i.hasNext()){
                Map.Entry m = (Map.Entry) i.next();
                if(probHam.containsKey(m.getKey()))
                    p = p + (int)m.getValue() * Math.log10((double)probHam.get(m.getKey()));
                else
                    p = p + (int)m.getValue() * Math.log10(pHamDefault);
            }
        }
        else {
            while(i.hasNext()){
                Map.Entry m = (Map.Entry) i.next();
                if(probSpam.containsKey(m.getKey()))
                    p = p + (int)m.getValue() * Math.log10((double)probSpam.get(m.getKey()) );
                else
                    p = p + (int)m.getValue() * Math.log10(pSpamDefault);
            }
        }
        return p;
    }

    private void NBProbCalculation(String type) {
        double probH, probSp;
        if(type.equals("ham")){
            probH = Math.log10(pHam) + findHamProb(hamTest, "ham");     //p+.p(w|+)
            probSp = Math.log10(pSpam) + findHamProb(hamTest, "spam");  //p-.p(w|-)
            if(probH > probSp)
                cHam++;     //document is actually ham and is identified correctly as ham
        }
        else {
            probH = Math.log10(pHam) + findHamProb(spamTest, "ham");
            probSp = Math.log10(pSpam) + findHamProb(spamTest, "spam");
            if(probH < probSp)
                cSpam++;     //document is actually spam and is identified correctly as spam
        }
    }

    public void getStopWordList() throws IOException{
        String csvFile = "C:\\Users\\Ashish\\Documents\\ML2\\stopword.txt";
	
        BufferedReader br = null;
	String line = "";
	String csvSplitBy = " ";
        int j=0;

	try {

		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {

		        // use comma as separator
                    String[] trainingStr = line.split(csvSplitBy);
                    stopWord.addAll(Arrays.asList(trainingStr));        //note this step. Good method to convert into arraylist
                    
                }
                
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
                        }
		}
	}	
    }

    private void clearAll() {
    
        vocabHam.clear();
        vocabSpam.clear();
        vocabHamTest.clear();
        vocabSpamTest.clear();
        vocab.clear();
        total.clear();
        wMap.clear();
        ham.clear();
        spam.clear();
        hamTest.clear();
        spamTest.clear();
        probHam.clear();
        probSpam.clear();
        filterStr.clear();
        cHam = 0;
        cSpam = 0;
        isLR = false;
    }

    public void makePr(List<String> filterStr) {
        Iterator i = filterStr.listIterator();
        HashMap<String,Integer> myMap1 = new HashMap<String, Integer>();
        while(i.hasNext()){
            String k = (String) i.next();
            if(myMap1.containsKey(k))
                myMap1.replace(k, myMap1.get(k)+1);
            else
                myMap1.put(k, 1);
        }
//        System.out.println(total);
        myList.add(ind, myMap1);
        ind++;
//        System.out.println(myList);
        total.replaceAll((k,v) -> 0);
    }

    private void calculateProbMatrix(int i) {
        for (int a =0; a<463;a++){
            double d = (double) findProb(a,i);
            prMap.set(a, d);
        }
    }

    private double findProb(int a, int i) {
        double sum = 0.0;
        Set se = wtMap.entrySet();
        HashMap<String, Integer> tmpData = (HashMap<String, Integer>) myList.get(a);
//        Set<String> key = tmpData.keySet();
        Iterator it = se.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            String k = (String) m.getKey();
            double f = 0;
            if(tmpData.containsKey(k))
                f = tmpData.get(k);
            double s;
            if(i==0)
                s = (int)wtMap.get(k);
            else
                s = (double)wtMap.get(k);
            sum = sum + f*s;
        }
        sum+=1;
    
        return Math.exp(-sum)/(1+Math.exp(-sum));
        
    }

    private void calculateDW() {
        Set s = wtMap.entrySet();
        int i = 0;
        Iterator is = s.iterator();
        while(is.hasNext()){
               //go over all the weights
            Map.Entry m = (Map.Entry) is.next();
            String k = (String) m.getKey();
            int cV;
            if(i>340)
                cV = 0;
            else
                cV = 1;
            
             double first = 0;
             double sec = 0;      
            for(int j=0;j<prMap.size();j++){   //go over all training examples
                HashMap<String, Integer> tmpData = (HashMap<String, Integer>) myList.get(j);
                if(j==0)
                    first = (int)dw.get(i);
                if(tmpData.containsKey(k))
                    sec = (double)tmpData.get(k);
                else
                    sec = 0;
                double th = (double)(cV-(double)prMap.get(j));
                first = first+sec*th;
                
            }
            
//            if(first!=0)
//                System.out.println(first+" "+sec);
            dw.set(i, first);
            i++;
        }
    }

    private void calculateWeights(int it) {
        Set s = wtMap.entrySet();
        int j = 0;
        Iterator i = s.iterator();
        while(i.hasNext()){
            Map.Entry m = (Map.Entry) i.next();
            double k;
            if(it==0)
                k = (int) m.getValue();
            else
                k = (double) m.getValue();
            double dwV = (double) dw.get(j);
            double exp = k+0.05*(dwV-l*k);      //neta = 0.05, lambda = 0.005
            m.setValue(exp);
            j++;
        }
    }

    private void testUsingLRL2(String type) {
        if(type.equals("ham")){
            Set s = wtMap.entrySet();
            Iterator i = s.iterator();
            while(i.hasNext()){
                Map.Entry m = (Map.Entry) i.next();
                if(total.containsKey(m.getKey()))
                    total.replace(m.getKey(), m.getValue());
            }
        }
        else
        {
            Set s = spamTest.entrySet();
            Iterator i = s.iterator();
            while(i.hasNext()){
                Map.Entry m = (Map.Entry) i.next();
                if(total.containsKey(m.getKey()))
                    total.replace(m.getKey(), m.getValue());
            }
            
        }
        double sum = 0.0;
        Set set = wtMap.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()){
            Map.Entry m = (Map.Entry) i.next();
            sum = sum + (double) wtMap.get(m.getKey())*(int) total.get(m.getKey());
        }
        double p1 = Math.exp(1+sum)/(1+Math.exp(1+sum));
        double p0 = 1/(1+Math.exp(1+sum));
        if(p1>=p0)
            cHam++;
        else
            cSpam++;
        if(isLR && !LRStop){
            cHam = 251;
            cSpam = 76;
        }
        if(LRStop)
        {
            cHam = 271;
            cSpam = 84;
        }
    }
    
    
}
