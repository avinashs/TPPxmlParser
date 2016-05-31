# tppXMLparser
Fork of the original by Damian Fermin https://sourceforge.net/projects/tppxmlparser/
Parses output from Peptide Prophet or Protein Prophet (from the Trans-proteomic Pipeline analysis suite) into a .tsv file
or directly into a MySQL database table.

Fork has been modified to also parse out the grpWt field from protXML. Necessary for implementing razor peptide scoring scheme.
Further, the dbResourcesProperties file [used if outputting to a MySQL database] is now packaged within the .jar for easy portability.
