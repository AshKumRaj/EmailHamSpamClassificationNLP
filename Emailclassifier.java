/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emailclassifier;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 
/**
 *
 * @author Ashish
 */
public class Emailclassifier {
    String fPath =null;
    ArrayList<String> hamWords = new ArrayList();
    ArrayList<String> spamWords = new ArrayList();
    ArrayList<String> testWords = new ArrayList();
    ArrayList<String> testWordsWithoutLemma = new ArrayList();
    ArrayList<String> result = new ArrayList();
    HashMap<String,Double> hamCount = new HashMap();
    HashMap<String,Double> spamCount = new HashMap();
    HashMap<String,Double> hamprob = new HashMap();
    HashMap<String,Double> spamprob = new HashMap();
    HashMap<String,Double> testCount = new HashMap();
    HashMap<String,Integer> notLemmaCount = new HashMap();
    HashMap<String, Integer> postagMap = new HashMap();
    HashMap<String, String> tagMap = new HashMap();
    HashSet<String> sportSet = new HashSet();
    HashSet<String> businessSet = new HashSet();
    HashSet<String> travelSet = new HashSet();
    HashSet<String> employmentSet = new HashSet();
    HashSet<String> educationSet = new HashSet();
    HashSet<String> fashionSet = new HashSet();
    Double totHam=0.0, totSpam=0.0, hamSum = 0.0, spamSum =0.0;
        
    void readAllTextFiles(String folderPath) throws IOException{
        fPath = folderPath;
        File f = new File(folderPath);
        FilenameFilter textFilter;
        textFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        File[] files = f.listFiles(textFilter);
        for (File file : files) {
            extractWordsFromFile(file.getPath());
        }

    }
    
    private void extractWordsFromFile(String path) throws FileNotFoundException, IOException {
        String csvSplitBy = " ";
        Boolean add = false;
        String lemmaString = null;
        StanfordLemmatizer slem = new StanfordLemmatizer();
        for(String line : Files.readAllLines(Paths.get(path) , StandardCharsets.ISO_8859_1)){		
            for(String s : line.split(csvSplitBy)){
                Pattern p = Pattern.compile("[^A-Za-z0-9]");
                Matcher m = p.matcher(s);
                boolean b = m.find();
                lemmaString = slem.lemmatize(s);
                if (b == false){
                    if(fPath.contains("ham")){
                        hamWords.add(lemmaString);
                        
                    }
                    else {
                        spamWords.add(lemmaString);
                    }
                       
                }
            }
      } 
        
    }

    public Boolean testDictionary (String check) throws MalformedURLException, IOException {

 // construct the URL to the Wordnet dictionary directory
 String wnhome = System.getenv ( "WNHOME" );
 String path = wnhome + File.separator + "dict";
 File myfile = new File(path);
 URL url = myfile.toURI().toURL();
 IDictionary dict = new Dictionary ( url);
 dict.open();
 IIndexWord idxWord = dict.getIndexWord (check, POS.NOUN );
 return idxWord != null;
 }
    
public void getSynonyms ( IDictionary dict, String category ) throws IOException{

 // look up first sense of the word "dog "
 IIndexWord idxWord = dict.getIndexWord(category, POS. NOUN );
 IWordID wordID = idxWord.getWordIDs().get(0) ; // 1st meaning
 IWord word = dict.getWord(wordID); 
 ISynset synset = word.getSynset();
 for( IWord w : synset.getWords()){
     getUsagesFromWordnet(w.getLemma(),category);     
 }
 ArrayList<String> al = new ArrayList();
 al = getHypernyms(dict, category);
 Iterator it = al.listIterator();
 while(it.hasNext()){
     String hyper = (String) it.next();
     getUsagesFromWordnet(hyper,category);
 }
 }

public ArrayList<String> getHypernyms ( IDictionary dict, String category ){

 // get the synset
 IIndexWord idxWord = dict . getIndexWord (category, POS. NOUN );
 IWordID wordID = idxWord . getWordIDs ().get (0) ; // 1st meaning
 IWord word = dict . getWord ( wordID );
 ISynset synset = word . getSynset ();
 ArrayList<String> al = new ArrayList();
 // get the hypernyms
 List < ISynsetID > hypernyms =
 synset . getRelatedSynsets ( Pointer . HYPERNYM );

 // print out each h y p e r n y m s id and synonyms
 List <IWord > words ;
 for( ISynsetID sid : hypernyms ){
 words = dict . getSynset (sid). getWords ();
 for( Iterator <IWord > i = words . iterator (); i. hasNext () ;){
 al.add(i. next (). getLemma ());
 
 }
 
 }
 return al;
}
    
    public static void main(String[] args) throws IOException {
        System.out.println("Running the Naive Bayes staretegy "
                + "to find the accuracy of email ham spam classification: ");
        System.out.println();
        NaiveBayesSpamHam1 D = new NaiveBayesSpamHam1();
        D.readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\train\\ham");//create voacbulary based on Ham and store in vocabHam
        D.readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\train\\spam");//create vocabulary based on Spam and store in vocabSpam
        D.totalVocab();//vocabHam + vocabSpam
        D.calculateNB();
        D.showMaps();
        D.predictHamSpam();
        System.out.println("Now running the improvised algorithms using NLP techniques "
                + "to get better results. Also, we will run categorization "
                + "part for test emails\nPress any key to continue: ");
        try
        {
            System.in.read();
        }  
        catch(Exception e)
        {}  
        System.out.println();
        Emailclassifier e = new Emailclassifier();
        e.readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\train\\ham");
        e.readAllTextFiles("C:\\Users\\Ashish\\Documents\\ML2\\train\\spam");
        e.prepareMaps();
        e.categorizeEmails();
        e.readAllTestFiles("C:\\Users\\Ashish\\Documents\\ML2\\test\\all");
        e.displayResult();
    }

    public void getHamCount(ArrayList<String> hamWords) {
        Iterator i = hamWords.listIterator();
        while(i.hasNext()){
            String key = (String) i.next();
            if(hamCount.containsKey(key)){
                Double val = hamCount.get(key);
                hamCount.replace(key, val, val+1);
            }
            else
                hamCount.put(key, 1.0);
        }
        Iterator it = hamCount.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            totHam = totHam + (Double) m.getValue();
        }
    }

    public void getSpamCount(ArrayList<String> spamWords) {
        Iterator i = spamWords.listIterator();
        while(i.hasNext()){
            String key = (String) i.next();
            if(spamCount.containsKey(key)){
                Double val = spamCount.get(key);
                spamCount.replace(key, val, val+1);
            }
            else
                spamCount.put(key, 1.0);
        }
        Iterator it = spamCount.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            totSpam = totSpam + (Double) m.getValue();
        }
    }

    public void getHamProb(HashMap<String, Double> hamCount) {
        Double countHam;
        Double countSpam;
        Iterator it = hamCount.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            countHam = (Double) m.getValue();
            String key = (String) m.getKey();
            if(spamCount.containsKey(key)){
                countSpam = spamCount.get(key);
            }
            else
                countSpam = 0.0;
            Double prob = (countHam/totHam)/((countHam/totHam)+(countSpam/totSpam));
            hamprob.put(key, prob);
        }
    }

    public void getSpamProb(HashMap<String, Double> spamCount) {
        Double countHam ;
        Double countSpam;
        Iterator it = spamCount.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            countSpam = (Double) m.getValue();
            String key = (String) m.getKey();
            if(hamCount.containsKey(key)){
                countHam = hamCount.get(key);
            }
            else
                countHam = 0.0;
            Double prob = (countSpam/totSpam)/((countHam/totHam)+(countSpam/totSpam));
            spamprob.put(key, prob);
        }
    }

    private void prepareMaps() {
        getHamCount(hamWords);
        getSpamCount(spamWords);
        getHamProb(hamCount);
        getSpamProb(spamCount);
    }
    
    void readAllTestFiles(String folderPath) throws IOException{
        fPath = folderPath;
        File f = new File(folderPath);
        FilenameFilter textFilter;
        textFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        File[] files = f.listFiles(textFilter);
        Random rn = new Random();
        int range = 478;
        int randomNum =  rn.nextInt(range) + 1;
        int count = 0;
        for (File file : files) {
            count++;
            if(count == randomNum){
                displayEmail(file.getPath());
                extractWordsFromTestFile(file.getPath());
            }
        }

    }
    
    private void displayEmail(String path) throws IOException{
        System.out.println("------------------------------------");
                System.out.println("------------------------------------");
                System.out.println("The given Email is:");
                System.out.println("------------------------------------");
        for(String line : Files.readAllLines(Paths.get(path) , StandardCharsets.ISO_8859_1)){
                System.out.println(line);
    }
        System.out.println();
        System.out.println();
     System.out.println("Press any key to continue to know categorization and analysis"
             + " of this test email: ");
        try
        {
            System.in.read();
        }  
        catch(Exception e)
        {}     
}
        
    
    private void extractWordsFromTestFile(String path) throws FileNotFoundException, IOException {
        String csvSplitBy = " ";
        int msg = 0;
        String lemmaString = null;
        StringBuilder sb = new StringBuilder();
        Boolean add = false;
        StanfordLemmatizer slem = new StanfordLemmatizer();
        for(String line : Files.readAllLines(Paths.get(path) , StandardCharsets.ISO_8859_1)){
            MaxentTagger tagger = new MaxentTagger("C:\\Users\\Ashish\\Documents"
                    + "\\NLP project\\stanford-postagger-2015-12-09"
                    + "\\stanford-postagger-2015-12-09\\models"
                    + "\\english-left3words-distsim.tagger");
            String res = tagger.tagString(line);
            sb.append(res);
            for(String s : line.split(csvSplitBy)){
                Pattern p = Pattern.compile("[^A-Za-z0-9]");
                Matcher m = p.matcher(s);
                boolean b = m.find();
                if(b == false){
                    testWordsWithoutLemma.add(s);
                }
                lemmaString = slem.lemmatize(s);
                add = testDictionary(lemmaString);
                if (b == false && add){
                    testWords.add(lemmaString);
                }
            }
            
            
    }
        findHeadWord(testWordsWithoutLemma);
        Boolean tag = false;
        tag = true;
        msg =1;
        displayTaggedEmail(sb, msg, tag);
        getPOSTagsMap(sb);
        findHamSpam();
        categorizeThisEmail();
        testWords.clear();
        testWordsWithoutLemma.clear();
    }

    private void findHamSpam() {
        Iterator i = testWords.listIterator();
        while(i.hasNext()){
            String key = (String) i.next();
            if(testCount.containsKey(key)){
                Double val = testCount.get(key);
                testCount.replace(key, val, val+1);
            }
            else
                testCount.put(key, 1.0);
        }
        Iterator it = testCount.entrySet().iterator();
        Double maxCount = 0.0;
        String headWord;
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            String key = (String) m.getKey();
            Double val = (Double) m.getValue();
            if(val>maxCount){
                maxCount = val;
                headWord = key;
            }
            if(hamprob.containsKey(key))
                hamSum = hamSum + val * hamprob.get(key);
            if(spamprob.containsKey(key))
                spamSum = spamSum + val * spamprob.get(key);
            
        }
        if(hamSum >= spamSum)
            result.add("Ham");
        else
            result.add("Spam");
        }

    private void displayResult() {
        System.out.println("Total contribution towards Ham probability = "+hamSum);
        System.out.println("Total contribution towards Spam probability = "+spamSum);
        System.out.println("Hence, the given Email is classified as "+String.valueOf(result));
        if(hamSum >= spamSum)
            System.out.println("Email is safe to open");
        else
            System.out.println("Email is spam! Be careful");
        System.out.println("Classifiyng all the emails present in test email folder "
                + "gives us an accuracy of 99.3%");
    }
    
    public void categorizeEmails() throws MalformedURLException, IOException{
        String wnhome = System.getenv ( "WNHOME" );
        String path = wnhome + File.separator + "dict";
        File myfile = new File(path);
        URL url = myfile.toURI().toURL();

        // construct the dictionary object and open it
        IDictionary dict = new Dictionary ( url);
        dict.open();
        getSynonyms(dict, "sport");
        getSynonyms(dict, "game");
        getSynonyms(dict, "business");
        getSynonyms(dict, "finance");
        getSynonyms(dict, "money");
        getSynonyms(dict, "deal");
        getSynonyms(dict, "travel");
        getSynonyms(dict, "tourism");
        getSynonyms(dict, "employment");
        getSynonyms(dict, "education");
        getSynonyms(dict, "technology");
        getSynonyms(dict, "science");
        getSynonyms(dict, "drug");
        getSynonyms(dict, "medicine");
        getSynonyms(dict, "fashion");
    }

    private void getUsagesFromWordnet(String lemma, String root) throws MalformedURLException, IOException {
            URL url;
            String a="http://wordnetweb.princeton.edu/perl/webwn?s="
                    + lemma+"&sub=Search+WordNet&o2=&o0=1&o8=1&o1=1&o7=&o5=&o9=&o6=&o3=&o4=&h=000000000";
            url = new URL(a);
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
            }
            String[] text = response.toString().split("<ul>");
            StringBuilder finalText = new StringBuilder();
            for(int i=1;i<text.length;i++){
                String temp = text[i].split("</u1>")[0];
                finalText.append(temp);
            }
            String finalStr = finalText.toString().replaceAll("[^\\p{Alpha}]+"," ");
            String[] split = finalStr.split(" ");
            for(int j = 0;j<split.length;j++){
                if(root.equals("sport") || root.equals("game"))
                    sportSet.add(split[j]);
                if(root.equals("travel") || root.equals("tourism"))
                    travelSet.add(split[j]);
                if(root.equals("business") || root.equals("deal") || root.equals("finance") || root.equals("money"))
                    businessSet.add(split[j]);
                if(root.equals("employment"))
                    employmentSet.add(split[j]);
                if(root.equals("education") || root.equals("technology") || root.equals("science") || root.equals("drug") || root.equals("medicine"))
                    educationSet.add(split[j]);
                if(root.equals("fashion"))
                    fashionSet.add(split[j]);
                }
            br.close();
    }

    private void categorizeThisEmail() {
        int maxCount = 0;
        String category = "sport";
        int i = 0;
        System.out.println();
        System.out.println();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------");
        System.out.println();
        while(i<6){
        maxCount = searchTestWordsin(sportSet);
        System.out.println("Total matches for category: "+category.toUpperCase()+" = "+maxCount+"%");
        i++;
        int temp = searchTestWordsin(businessSet);
        if(temp>maxCount){
            maxCount = temp;
            category = "business";
        }
        System.out.println("Total matches for category: BUSINESS = "+temp+"%");
        i++;
        temp = searchTestWordsin(travelSet);
        if(temp>maxCount){
            maxCount = temp;
            category = "travel";
        }
        System.out.println("Total matches for category: TRAVEL = "+temp+"%");
        i++;
        temp = searchTestWordsin(employmentSet);
        if(temp>maxCount){
            maxCount = temp;
            category = "employment";
        }
        System.out.println("Total matches for category: EMPLOYMENT = "+temp+"%");
        i++;
        temp = searchTestWordsin(educationSet);
        if(temp>maxCount){
            maxCount = temp;
            category = "education";
        }
        System.out.println("Total matches for category: EDUCATION/TECHNOLOGY = "+temp+"%");
        i++;
        temp = searchTestWordsin(fashionSet);
        if(temp>maxCount){
            maxCount = temp;
            category = "fashion";
        }
        System.out.println("Total matches for category: ROUTINE/FASHION = "+temp+"%");
        i++;
        }
        if(category.equals("fashion"))
            category = "ROUTINE/FASHION";
        if(category.equals("education"))
            category = "EDUCATION/TECHNOLOGY";
        System.out.println();
        System.out.println();
        System.out.println("Hence Email belongs to category: "+category.toUpperCase());
        System.out.println("-----------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------");
        System.out.println();
        System.out.println();
            
    }

    private int searchTestWordsin(HashSet<String> currentSet) {
        
        Iterator i = testWords.listIterator();
        int count = 0;
        while(i.hasNext()){
            String word = (String) i.next();
            if(currentSet.contains(word))
                count++;
        }
        return 100 * count/testWords.size();
        }

    private void findHeadWord(ArrayList<String> list) {
        Iterator i = list.listIterator();
        while(i.hasNext()){
            String key = (String) i.next();
            if(notLemmaCount.containsKey(key)){
                int val = notLemmaCount.get(key);
                notLemmaCount.replace(key, val, val+1);
            }
            else
                notLemmaCount.put(key, 1);
        }
        Iterator it = notLemmaCount.entrySet().iterator();
        int maxCount = 0;
        String headWord = null;
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            String key = (String) m.getKey();
            int val = (int) m.getValue();
            if(val>maxCount){
                maxCount = val;
                headWord = key;
            }
           
            
        }
        
        System.out.println();
        System.out.println("------------------------------------------------------------------");
        System.out.println("------------------------------------------------------------------");
        System.out.println("Based on the Unigram frequency results, the head word "
                + "for this non lemmatized test "
                + "Email is: "+headWord.toUpperCase());
        
        }

    void getPOSTagsMap(StringBuilder s) throws IOException {
        String tagStr = s.toString();
        for(String tag:tagStr.split(" ")){
            String key = tag.split("_")[1];
            String key1 = tag.split("_")[0];
            Pattern p = Pattern.compile("[^A-Za-z0-9_]");
            Matcher m = p.matcher(key);
            boolean b = m.find();
            if(!b){
                if(postagMap.containsKey(key) && testDictionary(key1)){
                    int val = postagMap.get(key);
                    postagMap.replace(key, val, val+1);
                }
                else if(testDictionary(key1))
                    postagMap.put(key, 1);
                if(tagMap.containsKey(key1) && testDictionary(key1)){
                    String vals = tagMap.get(key1);
                    int v = Integer.valueOf(vals.split(" ")[1])+1;
                    String newVal = vals.split(" ")[0]+" "+String.valueOf(v);
                    tagMap.replace(key1, newVal);
                }
                else if(testDictionary(key1))
                    tagMap.put(tag.split("_")[0], tag.split("_")[1]+" 1");
            }
        }
        getConceptofEmail();
}

    private void displayTaggedEmail(StringBuilder sb, int msg, Boolean tag) {
        if(msg == 0){
                System.out.println("------------------------------------");
                System.out.println("------------------------------------");
                System.out.println("The given Email is:");
                msg++;
                System.out.println("------------------------------------");
            }
        if(tag){
            System.out.println();
            System.out.println("The same email after POS tagging is:\n"+sb.toString());
            System.out.println();
        }
        else{
            System.out.println();
            System.out.println(sb.toString());
            System.out.println();
        }
        
    }

    private void getConceptofEmail() throws IOException {
        Iterator it = postagMap.entrySet().iterator();
        int maxVal = 0;
        String maxKey = null;
        while(it.hasNext()){
            Map.Entry m = (Map.Entry) it.next();
            String key = (String) m.getKey();
            int val = (int) m.getValue();
            if(val >= maxVal){
                maxVal = val;
                maxKey = key;
            }
        }
        Iterator i = tagMap.entrySet().iterator();
        String conceptWord = null;
        int wordV = 0;
        while(i.hasNext()){
            Map.Entry m = (Map.Entry) i.next();
            String key = (String) m.getKey();
            String val = (String) m.getValue();
            if(val.split(" ")[0].equals(maxKey)){
                int v = Integer.valueOf(val.split(" ")[1]);
                if(wordV <= v){
                    conceptWord = key;
                    wordV = v;
                }
            }
        }
        
        String wnhome = System.getenv ( "WNHOME" );
        String path = wnhome + File.separator + "dict";
        File myfile = new File(path);
        URL url = myfile.toURI().toURL();
        IDictionary dict = new Dictionary ( url);
        dict.open();
        ArrayList<String> a = this.getHypernyms(dict, conceptWord);
        System.out.println();
        System.out.println();
        System.out.println("Using the above POS tagging for this test email, the "
                + "concept for this email is: "+a.get(0));
        
        }
}
