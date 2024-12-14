package manipulations;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FundValueReader {
	 public ArrayList<long[]> FundValue;
	 public int Day;
	 //public boolean RiskFree;
	
	
	public FundValueReader(String string)throws IOException {
      FileReader file = null;
      file = new FileReader(string);
      BufferedReader fileInput = new BufferedReader(file);
      FundValue = new ArrayList();
      String[] stin = null;
      double[] ExpectReturnMatrix;
      Day = 0;
      String line;
      int length;
      long[] price;
      price = null;
      int i;
           

                
            while ((line = fileInput.readLine()) != null) {
                stin=line.split("\t");
                length = stin.length;
                price  = new long[length];
               	 for (i = 0; i < (length); i++)
            		  price[i] = (long)(Double.parseDouble(stin[i])*100);
            	 
               	if(length>0) FundValue.add(price);
               }
                           
            fileInput.close();
 
	}
	                 
          
}