package airldm2.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;

import airldm2.core.LDInstances;
import airldm2.core.LDTestInstances;
import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.RelationalDataSource;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.core.datatypes.relational.TableDescriptor;
import airldm2.database.ConnectionManager;
import airldm2.database.ConnectionManagerFactory;
import airldm2.database.DBHelper;
import airldm2.exceptions.ArffReadingException;
import airldm2.exceptions.DatabaseException;
import airldm2.exceptions.RTConfigException;

/**
 * Reads an ArrFile and returms a sufficent statistic data source If the
 * arff file is read for testInstances, returns an in memory representation
 * of the test instances
 * 
 * @author neeraj
 * 
 */
public class SimpleArffFileReader {
   private static String COMMENT_CHAR = "%";

   public final static String ARFF_RELATION = "@relation";

   /** The keyword used to denote the start of the arff data section */
   public final static String ARFF_DATA = "@data";

   /** The keyword used to denote the start of an arff attribute declaration */
   public final static String ARFF_ATTRIBUTE = "@attribute";

   String file;

   airldm2.core.LDInstances instances = null;

   /* Data Structure to hold test instances. This can be stored in memory */
   airldm2.core.LDTestInstances testInstances = null;

   // Vector<ColumnDescriptor> cols = new Vector<ColumnDescriptor>();
   TableDescriptor desc = new TableDescriptor();

   String relationName;

   Connection con;

   // boolean loaded;

   boolean insertDesc;

   /*
    * if the relation already exists in the database, append it if set to
    * true
    */
   boolean append = false;

   /**
    * 
    * @param insertHeader if the Header from the arff file should also be
    * written in the database
    * @return
    */
   public LDInstances getLDInstances(boolean insertHeader) throws SQLException,
         FileNotFoundException, IOException, ArffReadingException,
         RTConfigException {
      if (instances != null)
         return instances;
      this.insertDesc = insertHeader;
      boolean isTestdataSet = false;
      parse(isTestdataSet);
      return instances;

   }

   public LDTestInstances getTestInstances() throws SQLException,
         FileNotFoundException, IOException, ArffReadingException,
         RTConfigException {

      if (testInstances != null)
         return this.testInstances;
      boolean isTestdataSet = true;
      parse(isTestdataSet);
      return testInstances;

   }

   public SimpleArffFileReader(String arffFile) throws DatabaseException {

      this(arffFile, false);
   }

   /**
    * 
    * @param arffFile
    * @param append append the data if table already exists written in the
    * database
    * @throws DatabaseException
    */
   public SimpleArffFileReader(String arffFile, boolean append)
         throws DatabaseException {
      ConnectionManager manager = ConnectionManagerFactory
            .getConnectionManager();
      /* uncomment line below once fixed errors for pooling */
      try {
         con = manager.createConnection();
      } catch (DatabaseException e) {
         e.printStackTrace();
         throw e;
      }
      // test
      /*
       * try { Class.forName("com.mysql.jdbc.Driver").newInstance(); con =
       * DriverManager.getConnection(
       * "jdbc:mysql://129.186.93.141/db_research", "indus", "indus"); }
       * catch (Exception e) { throw new DatabaseException(
       * DatabaseException.ExceptionType.UNABLE_TO_CONNECT, e); } // test
       * 
       */
      this.file = arffFile;
      this.append = append;
      // loaded = false;

   }

   private boolean parse(boolean testDataSet) throws SQLException,
         FileNotFoundException, IOException, ArffReadingException,
         RTConfigException {

      Vector<ColumnDescriptor> cols = new Vector<ColumnDescriptor>();

      try {
         boolean relationSeen = false;
         boolean attributesSeen = false;
         boolean dataStarts = false;
         int count = 0;

         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line;
         while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith(COMMENT_CHAR) || line.trim().equals("")) {
               continue;
            }
            line = line.trim();
            if (line.startsWith(ARFF_RELATION)) {
               // no error checking that it may occur again after
               // attributes and data
               handleRelation(line);
               relationSeen = true;
            } else if (line.startsWith(ARFF_ATTRIBUTE)) {
               if (!relationSeen) {
                  // should have seen relation
                  throw new ArffReadingException(500,
                        " Attributes Declared before Relation");
               } else {
                  ColumnDescriptor thisCol = handleAttribute(line);
                  cols.add(thisCol);
                  attributesSeen = true;

               }

            } else if (line.startsWith(ARFF_DATA)) {

               if (!(relationSeen && attributesSeen)) {
                  throw new ArffReadingException(500,
                        " Data Declared before attributes/relation");
               } else {
                  dataStarts = true;
                  // after this all lines should be data
                  desc.setTableName(this.relationName);
                  desc.setColumns(cols);

                  // insert intoDB if this is NOT testDataSet
                  if (!testDataSet) {
                     String createTableQuery = DBHelper
                           .getCreateDataTableQuery(desc);
                     try {
                        DBHelper.ExecuteUpdateQuery(createTableQuery, con);
                     } catch (SQLException e) {
                        if (append) {
                           // the exception may have occured because table
                           // exists, so ignore
                           // TODO: Better to check if table exists
                           continue;
                        } else {
                           throw e;
                        }

                     }
                  } else {
                     SingleRelationDataDescriptor singleRelationDesc = new SingleRelationDataDescriptor(
                           desc.getTableName(), desc.getColumns());
                     this.testInstances = new LDTestInstances(
                           singleRelationDesc);
                  }

               }
            } else {
               // everything should be data, must have seen relationName
               // and attributes
               if (!testDataSet) {
                  count++;
                  String insertDataQuery = handleData(line);
                  DBHelper.ExecuteUpdateQuery(insertDataQuery, con);
                  if (count % 100 == 0) {
                     System.out
                           .println("Total rows inserted till now=" + count);
                  }
                  // TODO: Add transactional support
               } else {
                  // this is testDataSet Can be put in memory
                  Vector<String> currInstanceValue = this
                        .getValuesFromthisLine(line);
                  testInstances.addInstance(currInstanceValue);
               }
            }
         }

      } catch (FileNotFoundException e) {
         throw e;

      } catch (IOException e) {
         throw e;

      } catch (ArffReadingException e) {
         throw e;
      }

      RelationalDataSource dataSource = new RelationalDataSource(desc
            .getTableName());
      SingleRelationDataDescriptor singleRelationDesc = new SingleRelationDataDescriptor(
            desc.getTableName(), desc.getColumns());

      // see if required to insert the header of the arffFile in the
      // database
      if (this.insertDesc && !testDataSet) {
         String createDescTable = DBHelper
               .getCreateDataDescQuery(singleRelationDesc.getTableDesc());

         String insertDescTable = DBHelper
               .getInsertDescValuesQuery(singleRelationDesc.getTableDesc());
         try {
            DBHelper.ExecuteUpdateQuery(createDescTable, con);
            DBHelper.ExecuteUpdateQuery(insertDescTable, con);
         } catch (Exception e) {
            e.printStackTrace();
         }

      }

      // Create a Large DataSet Instance and set its
      // descriptor and source
      if (!testDataSet) {
         this.instances = new LDInstances(); // it is currently null
         this.instances.setDesc(singleRelationDesc);
         this.instances.setDataSource(dataSource);
      } else {
         // nothing to do. For testInstances, the values are already stored

      }

      boolean parsed = true;
      return parsed;

   }

   private void handleRelation(String line) {

      StringTokenizer tokenizer = new StringTokenizer(line, "@ ");
      tokenizer.nextToken(); // pass relation
      this.relationName = tokenizer.nextToken();

   }

   private ColumnDescriptor handleAttribute(String line) {
      // @attribute handicapped-infants { 'n', 'y', '?'}
      ColumnDescriptor currCol = new ColumnDescriptor();
      StringTokenizer tokenizer = new StringTokenizer(line, "@ ,{}'");
      tokenizer.nextToken(); // drop key word attribute
      String attributeName = tokenizer.nextToken();
      currCol.setColumnName(attributeName);
      Vector<String> possibleVals = new Vector<String>();
      String currAttribute;
      while (tokenizer.hasMoreTokens()) {
         currAttribute = tokenizer.nextToken();
         possibleVals.add(currAttribute);
      }
      currCol.setPossibleValues(possibleVals);

      return currCol;

   }

   /**
    * 
    * @param line
    * @return an query which can be used to insert the current instance
    * into db
    */
   private String handleData(String line) {
      // 'n','y','n','y','y','y','n','n','n','y','?','y','y','y','n','y','republican'
      /*
       * StringTokenizer tokenizer = new StringTokenizer(line, "', ");
       * Vector<String> vals = new Vector<String>(); String
       * currAttributeValue; while (tokenizer.hasMoreTokens()) {
       * currAttributeValue = tokenizer.nextToken();
       * vals.add(currAttributeValue); }
       */

      Vector<String> vals = getValuesFromthisLine(line);
      return DBHelper.getInsertValuesQuery(this.desc, vals);

      // Insert it into table;

   }

   public Vector<String> getValuesFromthisLine(String line) {
      // 'n','y','n','y','y','y','n','n','n','y','?','y','y','y','n','y','republican'
      StringTokenizer tokenizer = new StringTokenizer(line, "', ");
      Vector<String> vals = new Vector<String>();
      String currAttributeValue;
      while (tokenizer.hasMoreTokens()) {
         currAttributeValue = tokenizer.nextToken();
         vals.add(currAttributeValue);
      }
      return vals;
   }

   public static void main(String[] args) throws Exception {
      System.out.println("Processing.....");
      String base = System.getProperty("user.dir");
      String fileName = base + "/sample/HouseVotesTrain.arff";
      airldm2.util.SimpleArffFileReader read;
      read = new SimpleArffFileReader(fileName);
      LDInstances instances = read.getLDInstances(true);
      // LDTestInstances testInst = read.getTestInstances();
      System.out.println("*******Done***********");
   }
}
