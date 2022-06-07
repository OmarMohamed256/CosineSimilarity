package pkg;

import java.io.*;
import java.lang.System.Logger.Level;
import java.util.*;
import java.util.logging.Logger;
//=====================================================================
class DictEntry3 {

    public int doc_freq = 0; // number of documents that contain the term
    public int term_freq = 0; //number of times the term is mentioned in the collection
    public HashSet<Integer> postingList;

    DictEntry3() {
        postingList = new HashSet<Integer>();
    }
}

//=====================================================================
class Index3 {

    //--------------------------------------------
    Map<Integer, String> sources;  // store the doc_id and the file name
    HashMap<String, DictEntry3> index; // THe inverted index
    //--------------------------------------------

    Index3() {
        sources = new HashMap<Integer, String>();
        index = new HashMap<String, DictEntry3>();
    }

    //---------------------------------------------
    public void printPostingList(HashSet<Integer> hset) {
        Iterator<Integer> it2 = hset.iterator();
        while (it2.hasNext()) {
            System.out.print(it2.next() + ", ");
        }
        System.out.println("");
    }
    
    
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry3 dd = (DictEntry3) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
            //it.remove(); // avoids a ConcurrentModificationException
             printPostingList(dd.postingList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*****    Number of terms = " + index.size());
        System.out.println("------------------------------------------------------");

    }

    //-----------------------------------------------
    public void buildIndex(String[] files) throws IOException {
        int i = 0;
        for (String fileName : files) {
            try ( BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                sources.put(i, fileName);
                String ln;
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+");
                    for (String word : words) {
                        word = word.toLowerCase();
                        // check to see if the word is not in the dictionary to make sure terms are unique
                        if (!index.containsKey(word)) {
                            index.put(word, new DictEntry3());
                        }
                        // add document id to the posting list
                        if (!index.get(word).postingList.contains(i)) {
                            index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term 
                            index.get(word).postingList.add(i); // add the posting to the posting:ist
                        }
                        //set the term_fteq in the collection
                        index.get(word).term_freq += 1;
                    }
                }

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            i++;
        }

        Set<String> wordList0 = getDistinctWords("D:\\\\IR labs\\\\docs\\\\106.txt");
        Set<String> wordList1 = getDistinctWords("D:\\\\IR labs\\\\docs\\\\107.txt");
        Set<String> wordList2 = getDistinctWords("D:\\\\IR labs\\\\docs\\\\108.txt");
        Set<String> wordList3 = getDistinctWords("D:\\\\IR labs\\\\docs\\\\109.txt");
        Set<String> finalList = merge(wordList0,wordList1,wordList2,wordList3);

        
        List <Set<String>> docsList = new ArrayList<Set<String>>();
        docsList.add(wordList0);
        docsList.add(wordList1);
        docsList.add(wordList2);
        docsList.add(wordList3);
        
        HashMap<Double,String> similarityMap=new HashMap<Double,String>();
        
        List <List<Integer>> intList = new ArrayList<List<Integer>>();
        intList = createIndexedVectors(docsList, finalList);
        for ( int i1=0 ; i1<intList.size();i1++) {
        	for ( int i2=0 ; i2<intList.size();i2++) {
            	if(i1 != i2) {
            		similarityMap.put( dotprod(intList.get(i1),intList.get(i2) ) / (magnitude(intList.get(i1)) + magnitude(intList.get(i2))),"similarity(d"+ i1 + ",d" + i2 +") = ");
            	}
            }
        }
        
        Map<Double, String> sortedMap = new TreeMap<>(similarityMap);
        
        sortedMap.entrySet().forEach(entry -> {
            System.out.println(entry.getValue() + " " + entry.getKey());
        });
        
        /*System.out.println("similarity(d1,d2) = " + dotprod(d1_List, d2_List) / (magnitude(d1_List) + magnitude(d2_List)));
        System.out.println("similarity(d2,d3) = " + dotprod(d2_List, d3_List) / (magnitude(d2_List) + magnitude(d3_List)));
        System.out.println("similarity(d1,d3) = " + dotprod(d1_List, d3_List) / (magnitude(d1_List) + magnitude(d3_List)));
        System.out.println("similarity(d0,d3) = " + dotprod(d0_List, d3_List) / (magnitude(d0_List) + magnitude(d3_List)));
        System.out.println("similarity(d0,d2) = " + dotprod(d0_List, d2_List) / (magnitude(d0_List) + magnitude(d2_List)));
        System.out.println("similarity(d0,d1) = " + dotprod(d0_List, d1_List) / (magnitude(d0_List) + magnitude(d1_List)));*/
        
    }
    
    //--------------------------------------------------------------------------
    
    //--------------------------------------------------------------------------
    double magnitude(List<Integer> d0_List) {
    	double mag = 0;
    	
    	for(int i = 0 ; i < d0_List.size();i++) {
    		mag = mag + Math.pow(d0_List.get(i), 2);
    	}
    	return mag;
    }
    
    //--------------------------------------------------------------------------
    
    double dotprod(List<Integer> d0_List, List<Integer> d1_List) {

        double result = 0.0;
        int size = Math.min(d0_List.size(), d1_List.size());

        int n = 0;
        for (int i = 0; i < size; i++) {
            result += d0_List.get(i) * d1_List.get(i);
        }

        return result;
    }
    //--------------------------------------------------------------------------
    
    
    public static <T> Set<T> merge(Collection<? extends T>... collections) {
        Set<T> newSet = new HashSet<T>();
        for (Collection<? extends T> collection : collections)
            newSet.addAll(collection);
        return newSet;
    }
    //--------------------------------------------------------------------------
    public List<List<Integer>> createIndexedVectors(List <Set<String>> docsList,Set<String> finalList) {
    	List<List<Integer>> d1_List = new ArrayList<List<Integer>>();
    	for (Set<String> str : docsList) {
    		d1_List.add(vectorize(finalList,str));
        }
		return d1_List;
    }
    
    //--------------------------------------------------------------------------
    public List<Integer> vectorize(Set<String> finalList, Set<String> wordList) {
    	List<Integer> d1_List = new ArrayList<Integer>();
    	for (String str : finalList) {
        	if(wordList.contains(str)) {
        		
        		d1_List.add(1);
        	}else {
        		d1_List.add(0);
        	}
        }
		return d1_List;
    }
   
    
    //---------------------------------------------------------------------------
    // query inverted index
    // takes a string of terms as an argument
    public String find(String phrase) {
        String result = "";
        String[] words = phrase.split("\\W+");
        HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);

        for (String word : words) {
            res.retainAll(index.get(word).postingList);
        }
        if (res.size() == 0) {
            System.out.println("Not found");
            return "";
        }
       // String result = "Found in: \n";
        for (int num : res) {
            result += "\t" + sources.get(num) + "\n";
        }
        return result;
    }

    //----------------------------------------------------------------------------  
    
    public Set<String> getDistinctWords(String fileName) {
        Set<String> wordSet = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " ,.;:\"");
                while (st.hasMoreTokens()) {
                    wordSet.add(st.nextToken().toLowerCase());
                }
            }
        } catch (IOException e) {
            
        }
        return wordSet;
    }
    

    //----------------------------------------------------------------------------  
    
    

    //---------------------------------------------------------------------------- 


  //--------------------------------------------------------------------------
    // And of set of posting lists

    
}

//=====================================================================
public class cosine {

    public static void main(String args[]) throws IOException {
        Index3 index = new Index3();
        String phrase = "";
 /**/ 
        index.buildIndex(new String[]{
            "D:\\IR labs\\docs\\500.txt", // change it to your path e.g. "c:\\tmp\\100.txt"
            "D:\\IR labs\\docs\\501.txt",
            "D:\\IR labs\\docs\\502.txt",
            "D:\\IR labs\\docs\\503.txt",
            "D:\\IR labs\\docs\\504.txt",            
            "D:\\IR labs\\docs\\100.txt", // change it to your path e.g. "c:\\tmp\\100.txt"
            "D:\\IR labs\\docs\\101.txt",
            "D:\\IR labs\\docs\\102.txt",
            "D:\\IR labs\\docs\\103.txt",
            "D:\\IR labs\\docs\\104.txt",
            "D:\\IR labs\\docs\\105.txt",
            "D:\\IR labs\\docs\\106.txt",
            "D:\\IR labs\\docs\\107.txt",
            "D:\\IR labs\\docs\\108.txt",
            "D:\\IR labs\\docs\\109.txt"              
        });  

    }
}

