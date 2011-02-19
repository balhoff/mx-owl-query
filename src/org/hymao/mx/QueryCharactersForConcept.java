package org.hymao.mx;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hymao.mx.Vocab.MX;
import org.hymao.mx.Vocab.OBO_REL;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

public class QueryCharactersForConcept {

    final OWLOntology ontology;
    final OWLOntologyManager manager;
    final OWLDataFactory factory;
    final OWLReasoner reasoner;
    final String conceptURI;

    public QueryCharactersForConcept(OWLOntology ontology, String conceptURI) throws OWLOntologyCreationException {
        this.ontology = ontology;
        this.manager = ontology.getOWLOntologyManager();
        this.factory = this.manager.getOWLDataFactory();
        this.conceptURI = conceptURI;
        this.manager.loadOntology(IRI.create("http://purl.org/obo/owl/HAO"));
        this.manager.loadOntology(IRI.create("http://purl.org/obo/owl/PATO"));
        this.manager.loadOntology(IRI.create("http://purl.org/obo/owl/BSPO"));
        this.manager.applyChange(new AddImport(this.ontology, this.factory.getOWLImportsDeclaration(IRI.create("http://purl.org/obo/owl/HAO"))));
        this.manager.applyChange(new AddImport(this.ontology, this.factory.getOWLImportsDeclaration(IRI.create("http://purl.org/obo/owl/PATO"))));
        this.manager.applyChange(new AddImport(this.ontology, this.factory.getOWLImportsDeclaration(IRI.create("http://purl.org/obo/owl/BSPO"))));
        this.reasoner = new FaCTPlusPlusReasonerFactory().createReasoner(this.ontology);
    }

    public Collection<String> getCharacters() {
        final Set<String> characterIDs = new HashSet<String>();
        final OWLClass concept = this.factory.getOWLClass(IRI.create(this.conceptURI));
        final OWLObjectProperty hasPart = this.factory.getOWLObjectProperty(IRI.create(OBO_REL.HAS_PART));
        final OWLObjectSomeValuesFrom hasPartQueriedConcept = this.factory.getOWLObjectSomeValuesFrom(hasPart, concept);
        final NodeSet<OWLClass> subclasses = this.reasoner.getSubClasses(hasPartQueriedConcept, false);
        final OWLAnnotationProperty hasMXID = this.factory.getOWLAnnotationProperty(IRI.create(MX.HAS_MX_ID));
        final OWLAnnotationProperty describesState = this.factory.getOWLAnnotationProperty(IRI.create(MX.DESCRIBES_STATE));
        final OWLObjectProperty canHaveState = this.factory.getOWLObjectProperty(IRI.create(MX.CAN_HAVE_STATE));
        for (OWLClass phenotype : subclasses.getFlattened()) {
            final Set<OWLAnnotation> stateAnnotations = phenotype.getAnnotations(this.ontology, describesState);
            for (OWLAnnotation stateAnnotation : stateAnnotations) {
                if (stateAnnotation.getValue() instanceof IRI) {
                    final IRI stateIRI = (IRI)(stateAnnotation.getValue());
                    final OWLNamedIndividual state = this.factory.getOWLNamedIndividual(stateIRI);
                    final OWLObjectHasValue hasThisState = this.factory.getOWLObjectHasValue(canHaveState, state);
                    final Set<OWLNamedIndividual> characters = this.reasoner.getInstances(hasThisState, false).getFlattened();
                    for (OWLIndividual character : characters) {
                        if (character instanceof OWLNamedIndividual) {
                            final Set<OWLAnnotation> mxIDAnnotations = ((OWLNamedIndividual)character).getAnnotations(this.ontology, hasMXID);
                            for (OWLAnnotation mxIDAnnotation : mxIDAnnotations) {
                                if (mxIDAnnotation.getValue() instanceof OWLLiteral) {
                                    characterIDs.add(((OWLLiteral)(mxIDAnnotation.getValue())).getLiteral());
                                }
                            }
                        }
                    }
                }
            }
        }
        return characterIDs;
    }

    /**
     * @param args
     * @throws OWLOntologyCreationException 
     */
    public static void main(String[] args) throws OWLOntologyCreationException {
       final String anatomicalCluster = "http://purl.org/obo/owl/HAO#HAO_0000041";
       final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
       final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("/Users/jim/Desktop/mx_characters.owl"));
       final QueryCharactersForConcept query = new QueryCharactersForConcept(ontology, anatomicalCluster);
       System.out.println(query.getCharacters());
    }

}
