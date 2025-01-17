package net.lecnam.fm;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToFileBidAskSpread {
    BufferedWriter fileInput;

    //public WriteToFileBidAskSpread(double taxe, int cancelTime, int extSimulations) throws IOException{
    public WriteToFileBidAskSpread(String fileName) throws IOException{
        FileWriter file = null;
        //String str = "BidAskSpread" + "_" + taxe + "_" + cancelTime + "_" + extSimulations;
        String str = fileName;
        file = new FileWriter(str, true);

        fileInput = new BufferedWriter(file);
    }

    public void Write(String data) throws IOException{
        //fileInput[OrderBookId].append(Long.toString(Price));
        fileInput.write(data + "\n");
    }

    public void Close() throws IOException{
        fileInput.close();

    }

}

