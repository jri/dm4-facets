package de.deepamehta.plugins.facets;

import de.deepamehta.plugins.facets.service.FacetsService;

import de.deepamehta.core.AssociationDefinition;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.CompositeValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.Directives;
import de.deepamehta.core.service.Plugin;

import java.util.logging.Logger;



public class FacetsPlugin extends Plugin implements FacetsService {

    // ------------------------------------------------------------------------------------------------------- Constants

    private static final String REF_PREFIX = "ref_id:";     // ### Double definition. See AttachedDeepaMehtaObject.

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods



    // ************************************
    // *** FacetsService Implementation ***
    // ************************************



    @Override
    public void associateWithFacetType(long topicId, String facetTypeUri) {
        dms.createAssociation(new AssociationModel("dm4.core.instantiation", 
            new TopicRoleModel(topicId,      "dm4.core.instance"),
            new TopicRoleModel(facetTypeUri, "dm4.facets.facet")), null);   // clientState=null
    }

    // ### TODO: unify structurally equal code.
    // ### See AttachedDeepaMehtaObject#updateCompositeValue()
    @Override
    public Topic setFacet(Topic topic, String facetTypeUri, TopicModel facet, ClientState clientState,
                                                                              Directives directives) {
        AssociationDefinition assocDef = getAssocDef(facetTypeUri);
        String assocTypeUri = assocDef.getTypeUri();
        String childTopicTypeUri = assocDef.getPartTopicTypeUri();
        TopicType childTopicType = dms.getTopicType(childTopicTypeUri, null);
        if (assocTypeUri.equals("dm4.core.composition_def")) {
            Topic childTopic = fetchChildTopic(topic, assocDef, true);      // fetchComposite=true
            if (childTopic != null) {
                // update existing facet
                childTopic.update(facet, clientState, directives);
            } else {
                // Note: the type URI of a simplified topic model (as constructed
                // from update requests) is not initialzed.
                facet.setTypeUri(childTopicTypeUri);
                // create and associate new facet
                childTopic = dms.createTopic(facet, null);
                associateChildTopic(topic, assocDef, childTopic.getId());
            }
            return childTopic;
        } else if (assocTypeUri.equals("dm4.core.aggregation_def")) {
            if (childTopicType.getDataTypeUri().equals("dm4.core.composite")) {
                throw new RuntimeException("Aggregation of composite topic types not yet supported");
            } else {
                // remove current assignment
                RelatedTopic childTopic = fetchChildTopic(topic, assocDef, false);     // fetchComposite=false
                if (childTopic != null) {
                    long assocId = childTopic.getAssociation().getId();
                    dms.deleteAssociation(assocId, null);  // clientState=null
                }
                //
                String value = facet.getSimpleValue().toString();
                boolean assignExistingTopic = value.startsWith(REF_PREFIX);
                if (assignExistingTopic) {
                    // update DB
                    long childTopicId = Long.parseLong(value.substring(REF_PREFIX.length()));
                    associateChildTopic(topic, assocDef, childTopicId);
                } else {
                    throw new RuntimeException("Creating new instances of aggregated facets on-the-fly " +
                        "not yet supported");
                    // create new child topic
                    // ### setChildTopicValue(assocDefUri, valueTopic.getSimpleValue());
                }
            }
            return null;    // ### FIXME
        } else {
            throw new RuntimeException("Association type \"" + assocTypeUri + "\" not supported");
        }
    }

    @Override
    public Topic getFacet(Topic topic, String facetTypeUri) {
        // ### TODO: integrity check: is the topic an instance of that facet type?
        // ### TODO: many cardinality
        return fetchChildTopic(topic, getAssocDef(facetTypeUri), true);     // fetchComposite=true
    }



    // ------------------------------------------------------------------------------------------------- Private Methods

    private AssociationDefinition getAssocDef(String facetTypeUri) {
        // Note: a facet type has exactly *one* association definition
        return dms.getTopicType(facetTypeUri, null).getAssocDefs().values().iterator().next();
    }

    /**
     * Fetches and returns a child topic or <code>null</code> if no such topic extists.
     * <p>
     * ### Note: There is a principal copy in AttachedDeepaMehtaObject but here the precondition is different:
     * The given AssociationDefinition is not necessarily part of the given topic's type.
     */
    private RelatedTopic fetchChildTopic(Topic topic, AssociationDefinition assocDef, boolean fetchComposite) {
        String assocTypeUri       = assocDef.getInstanceLevelAssocTypeUri();
        String myRoleTypeUri      = assocDef.getWholeRoleTypeUri();
        String othersRoleTypeUri  = assocDef.getPartRoleTypeUri();
        String othersTopicTypeUri = assocDef.getPartTopicTypeUri();
        //
        return topic.getRelatedTopic(assocTypeUri, myRoleTypeUri, othersRoleTypeUri, othersTopicTypeUri,
            fetchComposite, false, null);
    }

    /**
     * Note: There is a principal copy in AttachedDeepaMehtaObject but here the precondition is different:
     * The given AssociationDefinition is not necessarily part of the given topic's type.
     */
    private void associateChildTopic(Topic topic, AssociationDefinition assocDef, long childTopicId) {
        dms.createAssociation(new AssociationModel(assocDef.getInstanceLevelAssocTypeUri(),
            new TopicRoleModel(topic.getId(), assocDef.getWholeRoleTypeUri()),
            new TopicRoleModel(childTopicId,  assocDef.getPartRoleTypeUri())), null);   // clientState=null
    }
}
