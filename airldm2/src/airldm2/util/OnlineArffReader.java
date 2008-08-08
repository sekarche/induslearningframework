package airldm2.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Vector;

import weka.core.Instances;
import airldm2.core.LDInstance;
import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.exceptions.ArffReadingException;

public class OnlineArffReader {
   // TODO: Fix Errors . Use SimpleArrfFileReader

   static StreamTokenizer tokenizer;

   /** The keyword used to denote the start of an arff header */
   public final static String ARFF_RELATION = "@relation";

   /** The keyword used to denote the start of the arff data section */
   public final static String ARFF_DATA = "@data";

   /** The keyword used to denote the start of an arff attribute declaration */
   public final static String ARFF_ATTRIBUTE = "@attribute";

   public static LDInstance load(Reader reader) throws ArffReadingException,
         IOException {
      tokenizer = new StreamTokenizer(reader);
      initTokenizer();
      readHeader(reader);

      return null;

   }

   private static void readHeader(Reader inp) throws ArffReadingException,
         IOException {
      tokenizer = new StreamTokenizer(inp);

      int m_Lines = 0;
      Vector<ColumnDescriptor> cols = new Vector<ColumnDescriptor>();
      ColumnDescriptor currColumn;
      double nval;
      String sval;
      String relationName = "";
      int test = 2;
      for (int i = 0; i < 100; i++) {
         int sometype = tokenizer.nextToken();
         int other = tokenizer.ttype;
         System.out.println("tokenizer type=" + tokenizer.ttype);
         switch (tokenizer.ttype) {

         case (StreamTokenizer.TT_EOL): {
            System.out.println("reached end of line");
            break;
         }
         case (StreamTokenizer.TT_NUMBER): {
            nval = tokenizer.nval;
            System.out.println("#" + nval);
            break;

         }
         case (StreamTokenizer.TT_WORD): {

            sval = tokenizer.sval;
            System.out.print("->" + sval);
            break;
         }
         default:

            System.out.println("other char:" + tokenizer.sval);

         }

      }

      // Get name of relation.
      getFirstToken();

      String val = tokenizer.sval;
      if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
         errorMessage(400, "premature end of file");
      }
      if (ARFF_RELATION.equalsIgnoreCase(tokenizer.sval)) {
         getNextToken();
         relationName = tokenizer.sval;
         getLastToken(false);
      } else {
         errorMessage(401, "keyword " + ARFF_RELATION + " expected");
      }

      // Get attribute declarations.
      getFirstToken();
      if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
         errorMessage(402, "premature end of file");
      }

      while (ARFF_ATTRIBUTE.equalsIgnoreCase(tokenizer.sval)) {
         currColumn = parseAttribute();
         cols.add(currColumn);

      }

      // Check if data part follows. We can't easily check for EOL.
      if (ARFF_DATA.equalsIgnoreCase(tokenizer.sval)) {
         errorMessage(403, "keyword " + Instances.ARFF_DATA + " expected");
      }

      // Check if any attributes have been declared.
      if (cols.size() == 0) {
         errorMessage(404, "no attributes declared");
      }

      for (ColumnDescriptor col2 : cols) {
         col2.dump(System.out);
      }
      // create the table in DB
      // read data and store in the DB

   }

   /**
    * Initializes the StreamTokenizer used for reading the ARFF file.
    */
   private static void initTokenizer() {
      int a = '@';
      tokenizer.resetSyntax();
      tokenizer.whitespaceChars(0, ' '); // treat control charcters as
                                          // white
      // spaces
      // space
      tokenizer.wordChars(' ' + 1, '\u00FF');
      tokenizer.wordChars(a - 1, a + 1);

      tokenizer.whitespaceChars(',', ',');
      tokenizer.commentChar('%');
      // tokenizer.quoteChar('"');
      // tokenizer.quoteChar('\'');
      // tokenizer.ordinaryChar('{');
      // tokenizer.ordinaryChar('}');
      tokenizer.eolIsSignificant(true);

   }

   /**
    * Gets next token, skipping empty lines.
    * 
    * @throws ArffReadingException if reading the next token fails
    */
   private static void getFirstToken() throws ArffReadingException, IOException {
      while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
      }

      if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) {
         tokenizer.ttype = StreamTokenizer.TT_WORD;
      } else if ((tokenizer.ttype == StreamTokenizer.TT_WORD)
            && (tokenizer.sval.equals("?"))) {
         tokenizer.ttype = '?';
      }
   }

   /**
    * Gets token and checks if its end of line.
    * 
    * @param endOfFileOk whether EOF is OK
    * @throws ArffReadingException if it doesn't find an end of line
    */
   private static void getLastToken(boolean endOfFileOk)
         throws ArffReadingException, IOException {
      if ((tokenizer.nextToken() != StreamTokenizer.TT_EOL)
            && ((tokenizer.ttype != StreamTokenizer.TT_EOF) || !endOfFileOk)) {
         errorMessage(405, "end of line expected");
      }
   }

   /**
    * Gets next token, checking for a premature and of line.
    * 
    * @throws IOException if it finds a premature end of line
    */
   private static void getNextToken() throws ArffReadingException, IOException {
      if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
         errorMessage(406, "premature end of line");
      }
      if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
         errorMessage(407, "premature end of file");
      } else if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) {
         tokenizer.ttype = StreamTokenizer.TT_WORD;
      } else if ((tokenizer.ttype == StreamTokenizer.TT_WORD)
            && (tokenizer.sval.equals("?"))) {
         tokenizer.ttype = '?';
      }
   }

   /**
    * A utility method to throw exceptions
    * 
    * @param code
    * @param message
    * @throws ArffReadingException
    */
   private static void errorMessage(int code, String message)
         throws ArffReadingException {
      throw new ArffReadingException(code, message);
   }

   private static ColumnDescriptor parseAttribute()
         throws ArffReadingException, IOException {
      ColumnDescriptor col = new ColumnDescriptor();

      Vector<String> values = new Vector<String>();

      // Get attribute name.
      getNextToken();
      String attributeName = tokenizer.sval;
      col.setColumnName(attributeName);

      // Attribute is nominal.

      // Get values for nominal attribute.
      if (tokenizer.nextToken() != '{') {
         errorMessage(408, "{ expected at beginning of enumeration");
      }
      while (tokenizer.nextToken() != '}') {
         if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
            errorMessage(409, "} expected at end of enumeration");
         } else {
            values.addElement(tokenizer.sval);
         }
      }
      if (values.size() == 0) {
         errorMessage(410, "no nominal values found");
      }

      getLastToken(false);
      getFirstToken();
      if (tokenizer.ttype == StreamTokenizer.TT_EOF)
         errorMessage(411, "premature end of file");

      col.setPossibleValues(values);
      return col;
   }

   public static void main(String args[]) throws Exception {
      String base = System.getProperty("user.dir");
      FileReader reader = new FileReader(base + "/sample/HouseVotes.arff");
      OnlineArffReader.load(reader);
   }
}
