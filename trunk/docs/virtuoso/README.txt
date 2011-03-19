This document describes how to set up a Virtuoso repository (storing RDF graphs) under Windows environment, and then load RDF graphs from a file.
For an up-to-date description please refer to http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSIndex


Setting up Virtuoso
===================

1. Download Virtuoso Open-Source Edition from http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSDownload

2. Extract to a folder (which will be referred to as %VIRTUOSO_ROOT% from now on)

3. Start up server using default configs: %VIRTUOSO_ROOT%\bin\virtuoso-t.exe -f

SPARQL server should now be accessed through http://localhost:8890/, click Conductor, login as dba/dba, and go into RDF tab


Load RDF graphs using script
============================

1. Copy rdfloader.sql into %VIRTUOSO_ROOT%\database
   Copy RDF file (e.g. moviesTrain.xml) into %VIRTUOSO_ROOT%\database

2. Run %VIRTUOSO_ROOT%\bin\isql.exe in %VIRTUOSO_ROOT%\database

3. ld_dir ('./', '*.xml', ':moviesTrain');

   (':moviesTrain' specifies the graph IRI)

4. rdf_loader_run ();


Load RDF graphs using an importer
=================================

1. Run airldm2.util.rdf.RDFImporter <graph IRI> <RDF file name in XML format>

