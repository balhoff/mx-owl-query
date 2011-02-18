package org.hymao.mx;

public class Vocab {
    
    public static class MX {
        public static final String PREFIX = "http://purl.oclc.org/NET/mx-database/";
        public static final String HAS_MX_ID = PREFIX + "has_mx_id";
    }
    
    public static class OBO_REL {
        
        public static final String PREFIX = "http://purl.obolibrary.org/obo/";
        public static final String INHERES_IN = "OBO_REL_inheres_in";
        public static final String INHERES_IN_PART_OF = "OBO_REL_inheres_in_part_of";
        public static final String TOWARDS = "OBO_REL_towards";
        public static final String HAS_PART = "OBO_REL_has_part";
        public static final String BEARER_OF = "OBO_REL_bearer_of";
    }

    public static class CDAO {
        
        public static final String PREFIX = "http://www.evolutionaryontology.org/cdao/1.0/cdao.owl#";
        public static final String STANDARD_CHARACTER = PREFIX + "StandardCharacter";
        public static final String STANDARD_STATE = PREFIX + "Standard";
        public static final String DATA_MATRIX = PREFIX + "CharacterStateDataMatrix";
        public static final String MATRIX_CELL = PREFIX + "StandardStateDatum";
        public static final String OTU = PREFIX + "TU";
        
        public static final String HAS_TU = PREFIX + "has_TU";
        public static final String HAS_CHARACTER = PREFIX + "has_Character";
        public static final String BELONGS_TO_CHARACTER = PREFIX + "belongs_to_Character";
        public static final String BELONGS_TO_TU = PREFIX + "belongs_to_TU";
        public static final String HAS_STATE = PREFIX + "has_State";

    }
    
    public static class PHENOSCAPE {
        public static final String PREFIX = "http://vocab.phenoscape.org/";
        public static final String PUBLICATION = PREFIX + "publication";
        public static final String TAXON = PREFIX + "taxon";
        public static final String REPRESENTS_TAXON = PREFIX + "represents_taxon";
        public static final String POSITED_BY = PREFIX + "posited_by";
        public static final String SPECIMEN = PREFIX + "specimen";
    }
    
    public static class DWC {
        public static final String PREFIX = "http://rs.tdwg.org/dwc/terms/";
        public static final String HAS_SPECIMEN = PREFIX + "individualID";
        public static final String SPECIMEN_TO_COLLECTION = PREFIX + "collectionID";
        public static final String SPECIMEN_TO_CATALOG_ID = PREFIX + "catalogNumber";
    }

}
