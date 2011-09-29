package org.hymao.mx;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class ApplyPhenotypeAnnotationsToSpecimens {
    
    private static final IRI OCCURRENCE = IRI.create("http://rs.tdwg.org/dwc/terms/Occurrence");
    private static final IRI IDENTIFICATION_ID = IRI.create("http://rs.tdwg.org/dwc/terms/identificationID");
    private static final IRI TAXON_ID = IRI.create("http://rs.tdwg.org/dwc/terms/taxonID");
    private static final IRI CHARACTER_STATE_DATUM = IRI.create("http://www.evolutionaryontology.org/cdao/1.0/cdao.owl#CharacterStateDatum");
    private static final IRI HAS_STATE = IRI.create("http://www.evolutionaryontology.org/cdao/1.0/cdao.owl#has_State");
    private static final IRI BELONGS_TO_CHARACTER = IRI.create("http://www.evolutionaryontology.org/cdao/1.0/cdao.owl#belongs_to_Character");
    private static final IRI BELONGS_TO_TU = IRI.create("http://www.evolutionaryontology.org/cdao/1.0/cdao.owl#belongs_to_TU");
    private static final IRI HAS_EXTERNAL_REFERENCE = IRI.create("http://www.evolutionaryontology.org/cdao/1.0/cdao.owl#has_External_Reference");
    private static final IRI FEMALE_ORGANISM = IRI.create("http://purl.obolibrary.org/obo/HAO_0000028");
    private static final IRI CAN_HAVE_STATE = IRI.create("http://purl.oclc.org/NET/mx-database/can_have_state");
    private static final IRI DENOTES_PHENOTYPE_OF = IRI.create("http://purl.oclc.org/NET/mx-database/denotes_phenotype_of");
    private static final IRI POSITED_BY = IRI.create("http://vocab.phenoscape.org/posited_by");
    
    public ApplyPhenotypeAnnotationsToSpecimens(OWLOntology ontology) {
        final OWLOntologyManager manager = ontology.getOWLOntologyManager();
        final OWLDataFactory factory = manager.getOWLDataFactory();
        final OWLClass classOccurrence = factory.getOWLClass(OCCURRENCE);
        final OWLClass classFemaleOrganism = factory.getOWLClass(FEMALE_ORGANISM);
        final OWLClass classCharacterStateDatum = factory.getOWLClass(CHARACTER_STATE_DATUM);
        final OWLObjectProperty hasState = factory.getOWLObjectProperty(HAS_STATE);
        final OWLObjectProperty identificationID = factory.getOWLObjectProperty(IDENTIFICATION_ID);
        final OWLObjectProperty taxonID = factory.getOWLObjectProperty(TAXON_ID);
        final OWLObjectProperty belongsToCharacter = factory.getOWLObjectProperty(BELONGS_TO_CHARACTER);
        final OWLObjectProperty belongsToTU = factory.getOWLObjectProperty(BELONGS_TO_TU);
        final OWLObjectProperty hasExternalReference = factory.getOWLObjectProperty(HAS_EXTERNAL_REFERENCE);
        final OWLObjectProperty canHaveState = factory.getOWLObjectProperty(CAN_HAVE_STATE);
        final OWLObjectProperty denotesPhenotypeOf = factory.getOWLObjectProperty(DENOTES_PHENOTYPE_OF);
        final OWLAnnotationProperty positedBy = factory.getOWLAnnotationProperty(POSITED_BY);
        final OWLClassExpression classFemaleCharacter = factory.getOWLObjectAllValuesFrom(canHaveState, factory.getOWLObjectAllValuesFrom(denotesPhenotypeOf, classFemaleOrganism));
        final Map<OWLIndividual, Set<OWLIndividual>> taxonToSpecimens = new HashMap<OWLIndividual, Set<OWLIndividual>>();
        for (OWLIndividual occurrence : classOccurrence.getIndividuals(ontology)) {
            final OWLIndividual determination = occurrence.getObjectPropertyValues(identificationID, ontology).iterator().next();
            final OWLIndividual taxon = determination.getObjectPropertyValues(taxonID, ontology).iterator().next();
            if (!taxonToSpecimens.containsKey(taxon)) {
                taxonToSpecimens.put(taxon, new HashSet<OWLIndividual>());
            }
            taxonToSpecimens.get(taxon).add(occurrence);
        }
        for (OWLIndividual datum : classCharacterStateDatum.getIndividuals(ontology)) {
            final OWLIndividual otu = datum.getObjectPropertyValues(belongsToTU, ontology).iterator().next();
            final OWLIndividual taxon = otu.getObjectPropertyValues(hasExternalReference, ontology).iterator().next();
            final OWLIndividual state = datum.getObjectPropertyValues(hasState, ontology).iterator().next();
            final OWLIndividual character = datum.getObjectPropertyValues(belongsToCharacter, ontology).iterator().next();
            final boolean femaleCharacter = character.getTypes(ontology).contains(classFemaleCharacter);
            final OWLAnnotation positedByAnnotation = factory.getOWLAnnotation(positedBy, datum.asOWLNamedIndividual().getIRI());
            for (OWLClassExpression stateDesc : state.getTypes(ontology)) {
                if (stateDesc instanceof OWLObjectAllValuesFrom) {
                    final OWLObjectAllValuesFrom denotesPhenotype = (OWLObjectAllValuesFrom)stateDesc;
                    final OWLClassExpression phenotype = denotesPhenotype.getFiller();
                    for (OWLIndividual specimen : taxonToSpecimens.get(taxon)) {
                        boolean femaleSpecimen = specimen.getTypes(ontology).contains(classFemaleOrganism);
                        if (femaleCharacter) {
                            if (femaleSpecimen) {
                                manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(phenotype, specimen, Collections.singleton(positedByAnnotation)));
                            }
                        } else {
                            manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(phenotype, specimen, Collections.singleton(positedByAnnotation)));
                        }
                    }
                }
            }
        }
    }

    /**
     * @param args
     * @throws OWLOntologyCreationException 
     * @throws OWLOntologyStorageException 
     */
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("/Users/jim/Dropbox/New Caledonia phenotypes/new_caledonia.owl"));
        new ApplyPhenotypeAnnotationsToSpecimens(ontology);
        manager.saveOntology(ontology, IRI.create(new File("/Users/jim/Dropbox/New Caledonia phenotypes/new_caledonia_processed.owl")));
     }

}
