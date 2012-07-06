package airldm2.util.rdf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;

public class ResultFormatter {
   public static void main(String[] args) throws IOException {
      BufferedWriter out = new BufferedWriter(new FileWriter("result.csv"));
      BufferedReader in = new BufferedReader(new FileReader(args[0]));
      out.write("cut,accuracy,precision,recall");
      out.newLine();
      
      String line;
      while ((line=in.readLine()) != null) {
         line = line.trim();
         if (line.isEmpty()) continue;
         
         int cutIndex = line.indexOf("=");
         int cut = Integer.parseInt(line.substring(cutIndex + 2));
         line = in.readLine().trim();
         Scanner s = new Scanner(line);
         int tp = Integer.parseInt(s.next());
         int fn = Integer.parseInt(s.next());
         line = in.readLine().trim();
         s = new Scanner(line);
         int fp = Integer.parseInt(s.next());
         int tn = Integer.parseInt(s.next());
         
         write(out, cut, tp, fn, fp, tn);
      }
      in.close();
      out.close();
   }

   private static void write(BufferedWriter out, int cut, int tp, int fn, int fp, int tn) throws IOException {
      DecimalFormat f = new DecimalFormat("0.000");
      int N = tp + fn + fp + tn;
      double acc = (double) (tp + tn) / N;
      double prec = (double) tp / (tp + fp);
      double reca = (double) tp / (tp + fn);
      out.write(cut + "," + f.format(acc) + "," + f.format(prec) + "," + f.format(reca));
      out.newLine();
   }
}
